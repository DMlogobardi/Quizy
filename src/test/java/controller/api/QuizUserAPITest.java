package controller.api;

import io.jsonwebtoken.MalformedJwtException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import model.dao.FaDAO;
import model.dao.QuizDAO;
import model.dao.RispondeDAO;
import model.dto.CompletaQuizDTO;
import model.dto.QuizDTO;
import model.entity.Domanda;
import model.entity.Quiz;
import model.entity.Risposta;
import model.entity.Utente;
import model.exception.AppException;
import model.mapper.EntityRefresher;
import model.menager.QuizUserMenager;
import model.utility.*;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QuizUserAPITest {

    // ===========================
    // === UNIT TESTS (Mockito) ===
    // ===========================
    @Nested
    class UnitTests {
        private QuizUserAPI api;
        private QuizUserMenager useMenagerMock;

        @BeforeEach
        void setUp() throws Exception {
            api = new QuizUserAPI();
            useMenagerMock = mock(QuizUserMenager.class);

            injectMethod(api, useMenagerMock, "useMenager");
        }

        //test downRole
        @Test
        void downRole_Success() throws Exception {
            String oldToken = "valid.token.here";
            String newToken = "new.token.here";
            when(useMenagerMock.downUserRole(oldToken)).thenReturn(newToken);

            Response response = api.downRole("Bearer " + oldToken);

            assertEquals(200, response.getStatus());
            Map<String, String> entity = (Map<String, String>) response.getEntity();
            assertEquals(newToken, entity.get("token"));
        }

        @Test
        void downRole_Failure_NoHeader() {
            Response response = api.downRole(null);
            assertEquals(400, response.getStatus());
        }

        @Test
        void downRole_Failure_WrongPrefix() {
            Response response = api.downRole("Basic dXNlcjpwYXNz");
            assertEquals(400, response.getStatus());
        }

        @Test
        void downRole_Failure_MalformedToken() throws Exception {
            String token = "garbage";
            when(useMenagerMock.downUserRole(token)).thenThrow(new MalformedJwtException("Invalid JWT"));

            Response response = api.downRole("Bearer " + token);
            assertEquals(400, response.getStatus());
        }

        @Test
        void downRole_Failure_AppException() throws Exception {
            String token = "valid.but.unauthorized";
            when(useMenagerMock.downUserRole(token)).thenThrow(new AppException("Role downgrade not allowed"));

            Response response = api.downRole("Bearer " + token);
            assertEquals(401, response.getStatus());
        }

        //test getQuiz
        @Test
        void getQuiz_Success() throws Exception {
            String token = "valid.token";
            Map<String, String> body = new HashMap<>();
            body.put("page", "1");

            Quiz mockQuiz = new Quiz();
            mockQuiz.setId(1);
            mockQuiz.setTitolo("Test Quiz");
            mockQuiz.setNumeroDomande(12);
            mockQuiz.setDifficolta("media");
            mockQuiz.setDescrizione("bello");
            mockQuiz.setUtente(new Utente());
            // QuizDTO si aspetta campi base popolati

            when(useMenagerMock.getQuizzes(1, token)).thenReturn(List.of(mockQuiz));

            Response response = api.getQuiz("Bearer " + token, body);

            assertEquals(200, response.getStatus());
            List<QuizDTO> result = (List<QuizDTO>) response.getEntity();
            assertEquals(1, result.size());
            assertEquals("Test Quiz", result.get(0).getTitolo());
        }

        @Test
        void getQuiz_Failure_NoHeader() {
            Response response = api.getQuiz(null, Map.of("page", "1"));
            assertEquals(400, response.getStatus());
        }

        @Test
        void getQuiz_Failure_EmptyBody() {
            Response response = api.getQuiz("Bearer token", new HashMap<>());
            assertEquals(400, response.getStatus());
        }

        @Test
        void getQuiz_Failure_InvalidPageFormat() {
            Map<String, String> body = Map.of("page", "not-a-number");

            assertThrows(NumberFormatException.class, () -> {
                api.getQuiz("Bearer token", body);
            });
        }

        @Test
        void getQuiz_Failure_MalformedToken() throws Exception {
            String token = "bad-token";
            when(useMenagerMock.getQuizzes(anyInt(), eq(token)))
                    .thenThrow(new MalformedJwtException("Invalid"));

            Response response = api.getQuiz("Bearer " + token, Map.of("page", "1"));
            assertEquals(400, response.getStatus());
        }

        @Test
        void getQuiz_Failure_AppException() throws Exception {
            String token = "valid-token";
            when(useMenagerMock.getQuizzes(anyInt(), eq(token)))
                    .thenThrow(new AppException("Generic Error"));

            Response response = api.getQuiz("Bearer " + token, Map.of("page", "1"));
            assertEquals(401, response.getStatus());
        }

        //test startQuiz
        @Test
        void startQuiz_Success() throws Exception {
            String token = "valid.token";
            Quiz quizReq = new Quiz();
            quizReq.setId(1);

            List<Domanda> mockDomande = List.of(new Domanda(), new Domanda());
            when(useMenagerMock.startQuiz(any(Quiz.class), eq(token))).thenReturn(mockDomande);

            Response response = api.startQuiz("Bearer " + token, quizReq);

            assertEquals(200, response.getStatus());
            List<Domanda> result = (List<Domanda>) response.getEntity();
            assertEquals(2, result.size());
        }

        @Test
        void startQuiz_Failure_NoHeader() {
            Response response = api.startQuiz(null, new Quiz());
            assertEquals(400, response.getStatus());
        }

        @Test
        void startQuiz_Failure_NullQuiz() {
            Response response = api.startQuiz("Bearer token", null);
            assertEquals(400, response.getStatus());
        }

        @Test
        void startQuiz_Failure_Unavailable() throws Exception {
            String token = "valid.token";
            Quiz quizReq = new Quiz();

            // Il manager restituisce null (es. quiz non disponibile o bloccato)
            when(useMenagerMock.startQuiz(any(Quiz.class), eq(token))).thenReturn(null);

            Response response = api.startQuiz("Bearer " + token, quizReq);

            assertEquals(451, response.getStatus()); // UNAVAILABLE_FOR_LEGAL_REASONS
        }

        @Test
        void startQuiz_Failure_MalformedToken() throws Exception {
            String token = "bad.token";
            when(useMenagerMock.startQuiz(any(Quiz.class), eq(token)))
                    .thenThrow(new MalformedJwtException("Invalid"));

            Response response = api.startQuiz("Bearer " + token, new Quiz());
            assertEquals(400, response.getStatus());
        }

        @Test
        void startQuiz_Failure_AppException() throws Exception {
            String token = "valid.token";
            when(useMenagerMock.startQuiz(any(Quiz.class), eq(token)))
                    .thenThrow(new AppException("Unauthorized access to quiz"));

            Response response = api.startQuiz("Bearer " + token, new Quiz());
            assertEquals(401, response.getStatus());
        }

        //test startQuizByPass
        @Test
        void startQuizByPass_Success() throws Exception {
            String token = "valid.token";
            Quiz quizReq = new Quiz();
            quizReq.setId(1);
            quizReq.setPasswordQuiz("secret123");

            List<Domanda> mockDomande = List.of(new Domanda());
            when(useMenagerMock.startQuiz(any(Quiz.class), eq("secret123"), eq(token))).thenReturn(mockDomande);

            Response response = api.startQuizByPass("Bearer " + token, quizReq);

            assertEquals(200, response.getStatus());
            List<Domanda> result = (List<Domanda>) response.getEntity();
            assertEquals(1, result.size());
        }

        @Test
        void startQuizByPass_Failure_NoHeader() {
            Response response = api.startQuizByPass(null, new Quiz());
            assertEquals(400, response.getStatus());
        }

        @Test
        void startQuizByPass_Failure_NullQuiz() {
            Response response = api.startQuizByPass("Bearer token", null);
            assertEquals(400, response.getStatus());
        }

        @Test
        void startQuizByPass_Failure_WrongPasswordOrUnavailable() throws Exception {
            String token = "valid.token";
            Quiz quizReq = new Quiz();
            quizReq.setPasswordQuiz("wrong_pass");

            when(useMenagerMock.startQuiz(any(Quiz.class), eq("wrong_pass"), eq(token))).thenReturn(null);

            Response response = api.startQuizByPass("Bearer " + token, quizReq);

            assertEquals(451, response.getStatus());
        }

        @Test
        void startQuizByPass_Failure_MalformedToken() throws Exception {
            String token = "bad.token";
            when(useMenagerMock.startQuiz(any(Quiz.class), anyString(), eq(token)))
                    .thenThrow(new MalformedJwtException("Invalid"));

            Response response = api.startQuizByPass("B" + token, new Quiz());
            assertEquals(400, response.getStatus());
        }

        @Test
        void startQuizByPass_Failure_AppException() throws Exception {
            String token = "valid.token";
            Quiz quizReq = new Quiz();
            quizReq.setPasswordQuiz("pass");

            when(useMenagerMock.startQuiz(any(Quiz.class), eq("pass"), eq(token)))
                    .thenThrow(new AppException("Unauthorized"));

            Response response = api.startQuizByPass("Bearer " + token, quizReq);
            assertEquals(401, response.getStatus());
        }

        //test completaQuiz
        @Test
        void completaQuiz_Success() throws Exception {
            String token = "valid.token";
            CompletaQuizDTO dto = new CompletaQuizDTO();
            dto.setQuiz(new Quiz());
            dto.setRisposteClient(List.of(new Risposta()));

            when(useMenagerMock.completaQuiz(any(Quiz.class), anyList(), eq(token))).thenReturn(85);

            Response response = api.completaQuiz("Bearer " + token, dto);

            assertEquals(200, response.getStatus());
            Map<String, Integer> result = (Map<String, Integer>) response.getEntity();
            assertEquals(85, result.get("punteggio"));
        }

        @Test
        void completaQuiz_Failure_NoHeader() {
            Response response = api.completaQuiz(null, new CompletaQuizDTO());
            assertEquals(400, response.getStatus());
        }

        @Test
        void completaQuiz_Failure_EmptyRisposte() {
            CompletaQuizDTO dto = new CompletaQuizDTO();
            dto.setQuiz(new Quiz());
            dto.setRisposteClient(new ArrayList<>());

            Response response = api.completaQuiz("Bearer token", dto);
            assertEquals(400, response.getStatus());
        }

        @Test
        void completaQuiz_Failure_NullQuiz() {
            CompletaQuizDTO dto = new CompletaQuizDTO();
            dto.setQuiz(null);
            dto.setRisposteClient(List.of(new Risposta()));

            Response response = api.completaQuiz("Bearer token", dto);
            assertEquals(400, response.getStatus());
        }

        @Test
        void completaQuiz_Failure_MalformedToken_FromManager() throws Exception {
            String token = "";
            CompletaQuizDTO dto = new CompletaQuizDTO();
            dto.setQuiz(new Quiz());
            dto.setRisposteClient(List.of(new Risposta()));

            when(useMenagerMock.completaQuiz(any(Quiz.class), anyList(), eq(token)))
                    .thenThrow(new MalformedJwtException("Token invalid or empty"));

            Response response = api.completaQuiz("Bearer " + token, dto);
            assertEquals(400, response.getStatus());
        }

        @Test
        void completaQuiz_Failure_AppException() throws Exception {
            String token = "valid.token";
            CompletaQuizDTO dto = new CompletaQuizDTO();
            dto.setQuiz(new Quiz());
            dto.setRisposteClient(List.of(new Risposta()));

            when(useMenagerMock.completaQuiz(any(Quiz.class), anyList(), eq(token)))
                    .thenThrow(new AppException("Unauthorized complete"));

            Response response = api.completaQuiz("Bearer " + token, dto);
            assertEquals(401, response.getStatus());
        }
    }

    // =========================
    // === INTEGRATION TESTS ===
    // =========================
    @Nested
    class IntegrationTests extends JerseyTest {
        private EntityManagerFactory emf;
        private EntityManager em;
        private JWT_Provider jwtProvider;
        private EntityRefresher refresher;

        private PassCrypt crypt;
        private SessionLog log;
        private QuizLog quizLog;
        private AccessControlService service;
        private QuizDAO dao;
        private FaDAO daoFa;
        private RispondeDAO daoRisponde;

        private QuizUserMenager menager;

        private Utente alreadyLogTest, alreadyLogTestUnauthorized;
        private  String realToken, realTokenUnauthorized;
        private int idForStartQuiz;

        @Override
        protected Application configure() {
            try {
                initFullStack();
            } catch (Exception e) {
                throw new RuntimeException("Errore setup QuizUserAPI", e);
            }

            // Registriamo la API corretta
            ResourceConfig config = new ResourceConfig(QuizUserAPI.class);

            config.register(new AbstractBinder() {
                @Override
                protected void configure() {
                    // Manager e DAO specifici per l'utente che svolge i quiz
                    bind(menager).to(QuizUserMenager.class);
                    bind(daoFa).to(FaDAO.class);
                    bind(daoRisponde).to(RispondeDAO.class);

                    // Dipendenze comuni
                    bind(log).to(SessionLog.class);
                    bind(quizLog).to(QuizLog.class);
                    bind(dao).to(QuizDAO.class);
                    bind(service).to(AccessControlService.class);
                    bind(jwtProvider).to(JWT_Provider.class);
                    bind(crypt).to(PassCrypt.class);
                    bind(refresher).to(EntityRefresher.class);
                }
            });

            return config.packages("controller.api") // Il tuo pacchetto
                    // QUESTA È LA CHIAVE: forza Jackson e disabilita Yasson
                    .register(org.glassfish.jersey.jackson.JacksonFeature.class)
                    .property("jersey.config.server.disableJsonReport", true);
        }

        private void initFullStack() throws Exception {
            // 1. Database H2 in memoria
            emf = Persistence.createEntityManagerFactory("testPU");
            em = emf.createEntityManager();

            // 2. DAO e Utility di persistenza
            dao = new QuizDAO();
            daoFa = new FaDAO();
            daoRisponde = new RispondeDAO();
            refresher = new EntityRefresher();

            injectMethod(dao, em, "em");
            injectMethod(daoFa, em, "em");
            injectMethod(daoRisponde, em, "em");
            injectMethod(refresher, em, "em");

            // 3. Servizi Core (JWT, Sessioni, Sicurezza)
            jwtProvider = new JWT_Provider();
            crypt = new PassCrypt();
            log = new SessionLog();
            service = new AccessControlService();

            injectMethod(log, jwtProvider, "jwtProvider");
            injectMethod(service, jwtProvider, "jwtProvider");

            // 4. Logica stato Quiz
            quizLog = new QuizLog();
            injectMethod(quizLog, refresher, "refresher");

            // 5. Manager Reale (QuizUserMenager)
            menager = new QuizUserMenager();
            injectMethod(menager, crypt, "crypt");
            injectMethod(menager, log, "logBeble");
            injectMethod(menager, service, "accessControl");
            injectMethod(menager, dao, "dao");
            injectMethod(menager, quizLog, "quizLog");
            injectMethod(menager, daoFa, "daoFa");
            injectMethod(menager, daoRisponde, "daoRisponde");

            // 6. Dati Iniziali
            em.getTransaction().begin();

            // Utenti
            alreadyLogTestUnauthorized = creaUtenteDiTest("Mario", "Rossi", "mariorossi", "hash123");
            alreadyLogTest = creaUtenteDiTest("Pippo", "Alberti", "pippo12", "sc2435");
            alreadyLogTest.setIsCreatore(true);

            em.persist(alreadyLogTestUnauthorized);
            em.persist(alreadyLogTest);

            // Generazione Quiz per i test di visualizzazione/risposta
            for (int i = 1; i <= 15; i++) {
                Quiz q = creaQuizDiTest(alreadyLogTest, "Quiz " + i, "Descrizione " + i);
                Domanda d = creaDomandaDiTest(q, "Domanda per quiz " + i);
                creaRispostaDiTest(d, "Risposta corretta", true);
                creaRispostaDiTest(d, "Risposta errata", false);
                em.persist(q);
            }

            Quiz quizConPassword = new Quiz();
            quizConPassword.setTitolo("Quiz Protetto");
            quizConPassword.setDescrizione("Test password");
            quizConPassword.setDifficolta("Difficile");
            quizConPassword.setTempo("20:00");
            quizConPassword.setPasswordQuiz(crypt.hashPassword("testPass123"));
            quizConPassword.setUtente(alreadyLogTest);
            quizConPassword.setDomande(new ArrayList<>());

            Domanda d = new Domanda();
            d.setQuesito("Qual è la capitale d'Italia?");
            d.setPuntiRispostaCorretta(1);
            d.setPuntiRispostaSbagliata(0);
            d.setQuiz(quizConPassword);
            d.setRisposte(new ArrayList<>());
            quizConPassword.getDomande().add(d);

            Risposta r1 = new Risposta();
            r1.setAffermazione("Roma");
            r1.setFlagRispostaCorretta(true);
            r1.setDomanda(d);
            d.getRisposte().add(r1);

            Risposta r2 = new Risposta();
            r2.setAffermazione("Milano");
            r2.setFlagRispostaCorretta(false);
            r2.setDomanda(d);
            d.getRisposte().add(r2);

            em.persist(quizConPassword);
            em.getTransaction().commit();

            idForStartQuiz = quizConPassword.getId();

            // 7. Sessioni attive
            realToken = jwtProvider.generateToken(alreadyLogTest, "creatore");
            realTokenUnauthorized = jwtProvider.generateToken(alreadyLogTestUnauthorized, "compilatore");
            log.aggiungi(realToken, alreadyLogTest);
            log.aggiungi(realTokenUnauthorized, alreadyLogTestUnauthorized);
        }

        @AfterEach
        public void cleanUp() throws NoSuchFieldException, IllegalAccessException {
            Field bibleField = SessionLog.class.getDeclaredField("logBible");
            Field userToTokenField = SessionLog.class.getDeclaredField("userIdToToken");

            bibleField.setAccessible(true);
            userToTokenField.setAccessible(true);

            ((Map<?, ?>) bibleField.get(log)).clear();
            ((Map<?, ?>) userToTokenField.get(log)).clear();

            if (em != null && em.isOpen()) {
                em.getTransaction().begin();
                // Disabilitiamo i vincoli per svuotare tutto senza errori di Foreign Key
                em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

                em.createNativeQuery("TRUNCATE TABLE risponde").executeUpdate();
                em.createNativeQuery("TRUNCATE TABLE risposta").executeUpdate();
                em.createNativeQuery("TRUNCATE TABLE domanda").executeUpdate();
                em.createNativeQuery("TRUNCATE TABLE fa").executeUpdate();
                em.createNativeQuery("TRUNCATE TABLE ticket").executeUpdate();
                em.createNativeQuery("TRUNCATE TABLE quiz").executeUpdate();
                em.createNativeQuery("TRUNCATE TABLE utente").executeUpdate();

                em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
                em.getTransaction().commit();

                em.close();
            }

            if (em != null && em.isOpen()) em.close();
            if (emf != null && emf.isOpen()) emf.close();
        }

        @Test
        void downRole_Integration_Success() {
            Response response = target("/quiz-use/downRole")
                    .request()
                    .header("Authorization", "Bearer " + realTokenUnauthorized)
                    .post(Entity.json(new HashMap<>()));

            assertEquals(200, response.getStatus());

            Map<String, String> resBody = response.readEntity(new GenericType<Map<String, String>>() {});
            assertNotNull(resBody.get("token"));
            assertFalse(resBody.get("token").isEmpty());
            assertNotEquals(realTokenUnauthorized, resBody.get("token"));
        }

        @Test
        void getQuiz_Integration_Success() {
            Map<String, String> body = new HashMap<>();
            body.put("page", "1");

            Response response = target("/quiz-use/getQuiz")
                    .request()
                    .header("Authorization", "Bearer " + realTokenUnauthorized)
                    .post(Entity.json(body));

            assertEquals(200, response.getStatus());

            List<Map<String, Object>> resBody = response.readEntity(new GenericType<List<Map<String, Object>>>() {});
            assertNotNull(resBody);
            assertFalse(resBody.isEmpty());
        }

        @Test
        void startQuiz_SenzaPassword_Integration_Success() {
            Quiz quizPubblico = (Quiz) em.createNativeQuery(
                            "SELECT * FROM quiz WHERE password IS NULL OR password = '' LIMIT 1", Quiz.class)
                    .getSingleResult();

            Map<String, Object> quizSemplice = new HashMap<>();
            quizSemplice.put("id", quizPubblico.getId());
            quizSemplice.put("titolo", quizPubblico.getTitolo());

            em.clear();

            Response response = target("/quiz-use/startQuiz")
                    .request()
                    .header("Authorization", "Bearer " + realTokenUnauthorized)
                    .post(Entity.json(quizSemplice));

            assertEquals(200, response.getStatus());

            String jsonResponse = response.readEntity(String.class);
            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
        }

        @Test
        void startQuiz_ConPassword_Integration_Success() {
            Map<String, Object> quizPayload = new HashMap<>();
            quizPayload.put("id", idForStartQuiz);
            quizPayload.put("passwordQuiz", "testPass123");

            Response response = target("/quiz-use/startQuiz_password")
                    .request()
                    .header("Authorization", "Bearer " + realTokenUnauthorized)
                    .post(Entity.json(quizPayload));

            assertEquals(200, response.getStatus());
            String jsonResponse = response.readEntity(String.class);
            assertNotNull(jsonResponse);
            assertTrue(jsonResponse.contains("risposte"));
        }

        @Test
        void startQuiz_ConPassword_Integration_WrongPassword() {
            Map<String, Object> quizPayload = new HashMap<>();
            quizPayload.put("id", idForStartQuiz);
            quizPayload.put("passwordQuiz", "testPas");

            em.clear();

            Response response = target("/quiz-use/startQuiz_password")
                    .request()
                    .header("Authorization", "Bearer " + realTokenUnauthorized)
                    .post(Entity.json(quizPayload));

            assertEquals(401, response.getStatus());
        }

        @Test
        void completaQuiz_Integration_Success() {
            Quiz quizDb = em.find(Quiz.class, idForStartQuiz);
            Domanda domandaDb = quizDb.getDomande().get(0);

            Risposta rispostaCorretta = domandaDb.getRisposte().stream()
                    .filter(Risposta::getFlagRispostaCorretta)
                    .findFirst()
                    .orElseThrow();

            Map<String, Object> quizMap = new HashMap<>();
            quizMap.put("id", quizDb.getId());

            Map<String, Object> domandaMap = new HashMap<>();
            domandaMap.put("id", domandaDb.getId());

            Map<String, Object> rispostaMap = new HashMap<>();
            rispostaMap.put("id", rispostaCorretta.getId());
            rispostaMap.put("domanda", domandaMap);
            rispostaMap.put("affermazione", rispostaCorretta.getAffermazione());
            rispostaMap.put("flagRispostaCorretta", true);

            List<Map<String, Object>> risposteClient = new ArrayList<>();
            risposteClient.add(rispostaMap);

            Map<String, Object> payload = new HashMap<>();
            payload.put("quiz", quizMap);
            payload.put("risposteClient", risposteClient);

            Response response = target("/quiz-use/completa")
                    .request()
                    .header("Authorization", "Bearer " + realTokenUnauthorized)
                    .post(Entity.json(payload));

            assertEquals(200, response.getStatus());

            Map<String, Integer> result = response.readEntity(new GenericType<Map<String, Integer>>() {});
            assertNotNull(result);
            assertTrue(result.containsKey("punteggio"));
            assertEquals(1, result.get("punteggio").intValue());
        }

        @Test
        void completaQuiz_Integration_EmptyAnswers() {
            Map<String, Object> payload = new HashMap<>();
            payload.put("quiz", new HashMap<String, Object>() {{ put("id", idForStartQuiz); }});
            payload.put("risposteClient", new ArrayList<>()); // Lista vuota

            Response response = target("/quiz-use/completa")
                    .request()
                    .header("Authorization", "Bearer " + realTokenUnauthorized)
                    .post(Entity.json(payload));

            // Il tuo codice restituisce BAD_REQUEST se le risposte sono nulle o vuote
            assertEquals(400, response.getStatus());
        }

        // --- HELPER METHOD PER CREARE QUIZ VALIDI ---
        private Quiz creaQuizDiTest(Utente autore, String titolo, String descrizione) {
            Quiz q = new Quiz();
            q.setUtente(autore);
            q.setTitolo(titolo);
            q.setDescrizione(descrizione);

            // Campi obbligatori (@NotNull) con valori di default coerenti
            q.setTempo("60 minuti");
            q.setDifficolta("Media");
            q.setNumeroDomande(10);

            // Password opzionale, la lasciamo null di default o stringa vuota
            q.setPasswordQuiz(null);

            // Inizializziamo la lista domande per evitare NullPointerException
            q.setDomande(new ArrayList<>());

            return q;
        }

        // --- HELPER METHOD PER CREARE DOMANDE VALIDE ---
        private Domanda creaDomandaDiTest(Quiz quiz, String quesito) {
            Domanda d = new Domanda();
            d.setQuesito(quesito);
            d.setQuiz(quiz); // Legame Domanda -> Quiz
            d.setPuntiRispostaCorretta(1);
            d.setPuntiRispostaSbagliata(0);
            d.setRisposte(new ArrayList<>());

            if (quiz.getDomande() == null) quiz.setDomande(new ArrayList<>());
            quiz.getDomande().add(d); // Legame Quiz -> Domanda (Bidirezionale)
            return d;
        }

        private Risposta creaRispostaDiTest(Domanda domanda, String testo, boolean corretta) {
            Risposta r = new Risposta();
            r.setAffermazione(testo);
            r.setFlagRispostaCorretta(corretta);
            r.setDomanda(domanda); // Legame Risposta -> Domanda

            if (domanda.getRisposte() == null) domanda.setRisposte(new ArrayList<>());
            domanda.getRisposte().add(r); // Legame Domanda -> Risposta (Bidirezionale)
            return r;
        }

        // --- HELPER METHOD PER CREARE UTENTI VALIDI ---
        private Utente creaUtenteDiTest(String nome, String cognome, String username, String pwd) {
            Utente u = new Utente();
            u.setNome(nome);
            u.setCognome(cognome);
            u.setUsername(username);
            u.setPasswordHash(pwd);

            u.setIsCreatore(false);
            u.setIsCompilatore(true);
            u.setIsManager(false);
            return u;
        }
    }

    // =========================
    // ===== UTIL METHOD =======
    // =========================
    private void injectMethod(Object component, Object injectComponent, String nameFild) throws Exception {
        Class<?> clazz = component.getClass();

        Field f = clazz.getDeclaredField(nameFild);

        f.setAccessible(true);
        f.set(component, injectComponent);
    }
}
