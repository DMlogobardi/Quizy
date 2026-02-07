package controller.api;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import model.dao.QuizDAO;
import model.dto.GetQuizDTO;
import model.entity.Domanda;
import model.entity.Quiz;
import model.entity.Risposta;
import model.entity.Utente;
import model.exception.AppException;
import model.mapper.EntityRefresher;
import model.menager.QuizCreatorMenager;
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
import static org.mockito.Mockito.*;

public class QuizCreatorAPITest {

    // ===========================
    // === UNIT TESTS (Mockito) ===
    // ===========================
    @Nested
    class UnitTests  {
        private QuizCreatorAPI api;
        private QuizCreatorMenager menagerMock;

        @BeforeEach
        void setUp() throws Exception {
            api = new QuizCreatorAPI();
            menagerMock = mock(QuizCreatorMenager.class);

            injectMethod(api, menagerMock, "menager");
        }

        //test upRole
        @Test
        void upRole_Success() throws AppException {
            String oldToken = "Bearer valid.token.here";
            String newToken = "new.token.with.upgraded.role";
            when(menagerMock.upUserRole("valid.token.here")).thenReturn(newToken);

            Response response = api.upRole(oldToken);

            assertEquals(200, response.getStatus());
            Map<String, String> body = (Map<String, String>) response.getEntity();
            assertEquals(newToken, body.get("token"));
        }

        @Test
        void upRole_HeaderMissing() {
            Response response = api.upRole(null);
            assertEquals(400, response.getStatus());
        }

        @Test
        void upRole_HeaderWrongFormat() {
            Response response = api.upRole("WrongFormat token");
            assertEquals(400, response.getStatus());
        }

        @Test
        void upRole_MalformedJwt() throws AppException {
            String token = "Bearer malformed.token";
            when(menagerMock.upUserRole("malformed.token")).thenThrow(new MalformedJwtException("invalid"));

            Response response = api.upRole(token);
            assertEquals(400, response.getStatus());
        }

        @Test
        void upRole_AppException_Unauthorized() throws AppException {
            String token = "Bearer token.expired";
            when(menagerMock.upUserRole("token.expired")).thenThrow(new AppException("Unauthorized"));

            Response response = api.upRole(token);
            assertEquals(401, response.getStatus());
        }

        @Test
        void upRole_JwtException_InternalError() throws AppException {
            String token = "Bearer token.signature.error";
            when(menagerMock.upUserRole("token.signature.error")).thenThrow(new JwtException("Signature failed") {});

            Response response = api.upRole(token);
            assertEquals(500, response.getStatus());
        }

        //test createQuiz
        @Test
        void createQuiz_Success() throws AppException {
            String token = "Bearer valid_token";
            Quiz quiz = new Quiz();
            quiz.setTitolo("Test Quiz");

            Response response = api.createQuiz(quiz, token);

            assertEquals(200, response.getStatus());
            verify(menagerMock).createQuiz(quiz, "valid_token");
        }

        @Test
        void createQuiz_HeaderMissing() {
            Quiz quiz = new Quiz();
            Response response = api.createQuiz(quiz, null);
            assertEquals(400, response.getStatus());
        }

        @Test
        void createQuiz_HeaderWrongFormat() {
            Quiz quiz = new Quiz();
            Response response = api.createQuiz(quiz, "Basic dXNlcjpwYXNz");
            assertEquals(400, response.getStatus());
        }

        @Test
        void createQuiz_AppException_BadRequest() throws AppException {
            String token = "Bearer valid_token";
            Quiz quiz = new Quiz();

            doThrow(new AppException("User not creator")).when(menagerMock)
                    .createQuiz(any(Quiz.class), eq("valid_token"));

            Response response = api.createQuiz(quiz, token);

            assertEquals(400, response.getStatus());
        }

        @Test
        void createQuiz_NullQuiz_SuccessLogic() throws AppException {
            // Il controller non valida il corpo del quiz (se null lo passa al manager)
            String token = "Bearer valid_token";

            Response response = api.createQuiz(null, token);

            assertEquals(200, response.getStatus());
            verify(menagerMock).createQuiz(null, "valid_token");
        }

        //test deleteQuiz
        @Test
        void deleteQuiz_Success() throws AppException {
            String token = "Bearer valid_token";
            Quiz quiz = new Quiz();
            quiz.setId(1);

            Response response = api.deleteQuiz(quiz, token);

            assertEquals(200, response.getStatus());
            verify(menagerMock).deleteQuiz(quiz, "valid_token");
        }

        @Test
        void deleteQuiz_HeaderMissing() {
            Quiz quiz = new Quiz();
            Response response = api.deleteQuiz(quiz, null);
            assertEquals(400, response.getStatus());
        }

        @Test
        void deleteQuiz_HeaderWrongFormat() {
            Quiz quiz = new Quiz();
            Response response = api.deleteQuiz(quiz, "Token valid_but_wrong_prefix");
            assertEquals(400, response.getStatus());
        }

        @Test
        void deleteQuiz_MalformedJwt() throws AppException {
            String token = "Bearer malformed_jwt";
            Quiz quiz = new Quiz();

            doThrow(new MalformedJwtException("Invalid JWT structure")).when(menagerMock)
                    .deleteQuiz(any(Quiz.class), eq("malformed_jwt"));

            Response response = api.deleteQuiz(quiz, token);

            assertEquals(400, response.getStatus());
        }

        @Test
        void deleteQuiz_AppException_BadRequest() throws AppException {
            String token = "Bearer valid_token";
            Quiz quiz = new Quiz();

            doThrow(new AppException("Quiz not found or user not authorized")).when(menagerMock)
                    .deleteQuiz(any(Quiz.class), eq("valid_token"));

            Response response = api.deleteQuiz(quiz, token);

            assertEquals(400, response.getStatus());
        }

        //test updateQuiz
        @Test
        void updateQuiz_Success() throws AppException {
            String token = "Bearer valid_token";
            Quiz quiz = new Quiz();
            quiz.setId(1);
            quiz.setTitolo("Quiz Aggiornato");

            Response response = api.updateQuiz(quiz, token);

            assertEquals(200, response.getStatus());
            verify(menagerMock).aggiornaQuiz(quiz, "valid_token");
        }

        @Test
        void updateQuiz_HeaderMissing() {
            Quiz quiz = new Quiz();
            Response response = api.updateQuiz(quiz, null);
            assertEquals(400, response.getStatus());
        }

        @Test
        void updateQuiz_HeaderWrongFormat() {
            Quiz quiz = new Quiz();
            Response response = api.updateQuiz(quiz, "InvalidPrefix 12345");
            assertEquals(400, response.getStatus());
        }

        @Test
        void updateQuiz_MalformedJwt() throws AppException {
            String token = "Bearer token_corrotto";
            Quiz quiz = new Quiz();

            doThrow(new MalformedJwtException("JWT structure invalid"))
                    .when(menagerMock).aggiornaQuiz(any(Quiz.class), eq("token_corrotto"));

            Response response = api.updateQuiz(quiz, token);

            assertEquals(400, response.getStatus());
        }

        @Test
        void updateQuiz_AppException_ForbiddenOrNotFound() throws AppException {
            String token = "Bearer valid_token";
            Quiz quiz = new Quiz();

            doThrow(new AppException("Non autorizzato all'aggiornamento"))
                    .when(menagerMock).aggiornaQuiz(any(Quiz.class), eq("valid_token"));

            Response response = api.updateQuiz(quiz, token);

            assertEquals(400, response.getStatus());
        }

        @Test
        void updateQuiz_NullQuiz_PassedToManager() throws AppException {
            String token = "Bearer valid_token";

            Response response = api.updateQuiz(null, token);

            assertEquals(200, response.getStatus());
            verify(menagerMock).aggiornaQuiz(null, "valid_token");
        }

        //test getQuiz
        @Test
        void getQuiz_Success() throws AppException {
            String token = "Bearer valid_token";
            GetQuizDTO pageDto = new GetQuizDTO();
            pageDto.setPage(1);
            List<Quiz> mockList = List.of(new Quiz(), new Quiz());

            when(menagerMock.getQuizzes(1, "valid_token")).thenReturn(mockList);

            Response response = api.getQuiz(token, pageDto);

            assertEquals(200, response.getStatus());
            assertEquals(mockList, response.getEntity());
        }

        @Test
        void getQuiz_HeaderMissing() {
            GetQuizDTO pageDto = new GetQuizDTO();
            Response response = api.getQuiz(null, pageDto);
            assertEquals(400, response.getStatus());
        }

        @Test
        void getQuiz_HeaderWrongFormat() {
            GetQuizDTO pageDto = new GetQuizDTO();
            Response response = api.getQuiz("Invalid format", pageDto);
            assertEquals(400, response.getStatus());
        }

        @Test
        void getQuiz_PageDtoNull() {
            String token = "Bearer valid_token";
            Response response = api.getQuiz(token, null);
            assertEquals(400, response.getStatus());
        }

        @Test
        void getQuiz_MalformedJwt() throws AppException {
            String token = "Bearer corrupted_jwt";
            GetQuizDTO pageDto = new GetQuizDTO();
            pageDto.setPage(0);

            when(menagerMock.getQuizzes(anyInt(), eq("corrupted_jwt")))
                    .thenThrow(new MalformedJwtException("Invalid token structure"));

            Response response = api.getQuiz(token, pageDto);
            assertEquals(400, response.getStatus());
        }

        @Test
        void getQuiz_AppException_Unauthorized() throws AppException {
            String token = "Bearer valid_token";
            GetQuizDTO pageDto = new GetQuizDTO();

            when(menagerMock.getQuizzes(anyInt(), eq("valid_token")))
                    .thenThrow(new AppException("Session expired or invalid"));

            Response response = api.getQuiz(token, pageDto);
            assertEquals(400, response.getStatus());
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
        private AccessControlService service;
        private QuizDAO dao;
        private QuizLog quizLog;

        private QuizCreatorMenager menager;

        private Utente alreadyLogTest, alreadyLogTestUnauthorized;
        private String realToken, realTokenUnauthorized;

        @Override
        protected Application configure() {
            try {
                initFullStack();
            } catch (Exception e) {
                throw new RuntimeException("Errore nel setup dello stack reale per QuizCreatorAPI", e);
            }


            ResourceConfig config = new ResourceConfig(QuizCreatorAPI.class);

            config.register(new AbstractBinder() {
                @Override
                protected void configure() {

                    bind(menager).to(QuizCreatorMenager.class);

                    bind(log).to(SessionLog.class);
                    bind(quizLog).to(QuizLog.class);
                    bind(dao).to(QuizDAO.class);
                    bind(service).to(AccessControlService.class);
                    bind(jwtProvider).to(JWT_Provider.class);
                    bind(crypt).to(PassCrypt.class);
                    bind(refresher).to(EntityRefresher.class);
                }
            });

            return config;
        }

        private void initFullStack() throws Exception {
            // 1. Database e Persistence
            emf = Persistence.createEntityManagerFactory("testPU");
            em = emf.createEntityManager();

            // 2. DAO e Refresher
            dao = new QuizDAO();
            refresher = new EntityRefresher();
            injectMethod(dao, em, "em");
            injectMethod(refresher, em, "em");

            // 3. Sicurezza e Sessioni
            jwtProvider = new JWT_Provider();
            crypt = new PassCrypt();
            log = new SessionLog();
            service = new AccessControlService();

            injectMethod(log, jwtProvider, "jwtProvider");
            injectMethod(service, jwtProvider, "jwtProvider");

            // 4. Logica Specifica Quiz
            quizLog = new QuizLog();
            injectMethod(quizLog, refresher, "refresher");

            // 5. Manager Reale
            menager = new QuizCreatorMenager();
            injectMethod(menager, crypt, "crypt");
            injectMethod(menager, log, "logBeble"); // Nome variabile dal tuo setup
            injectMethod(menager, service, "accessControl");
            injectMethod(menager, dao, "dao");
            injectMethod(menager, quizLog, "quizLog");

            // 6. Popolamento DB (Transazionale)
            em.getTransaction().begin();

            // Utente Creatore (Autorizzato)
            alreadyLogTest = new Utente();
            alreadyLogTest.setNome("Pippo");
            alreadyLogTest.setCognome("Alberti");
            alreadyLogTest.setUsername("pippo12");
            alreadyLogTest.setPasswordHash(crypt.hashPassword("sc2435"));
            alreadyLogTest.setIsCreatore(true);
            em.persist(alreadyLogTest);

            // Utente Compilatore (Non autorizzato a creare/modificare quiz)
            alreadyLogTestUnauthorized = new Utente();
            alreadyLogTestUnauthorized.setNome("Mario");
            alreadyLogTestUnauthorized.setCognome("Rossi");
            alreadyLogTestUnauthorized.setUsername("mariorossi");
            alreadyLogTestUnauthorized.setPasswordHash(crypt.hashPassword("hash123"));
            alreadyLogTestUnauthorized.setIsCreatore(false);
            em.persist(alreadyLogTestUnauthorized);

            // Generazione 15 Quiz per testare la paginazione (getQuizzes)
            for (int i = 1; i <= 15; i++) {
                Quiz q = new Quiz();
                q.setTitolo("Quiz " + i);
                q.setDescrizione("Descrizione " + i);
                q.setDifficolta("Media");
                q.setTempo("10:00");
                q.setUtente(alreadyLogTest);

                Domanda d = new Domanda();
                d.setQuesito("Domanda per quiz " + i);
                d.setQuiz(q);
                d.setPuntiRispostaCorretta(1);
                d.setPuntiRispostaSbagliata(0);

                Risposta r1 = new Risposta();
                r1.setAffermazione("Corretta");
                r1.setFlagRispostaCorretta(true);
                r1.setDomanda(d);

                em.persist(q); // Persiste a cascata se configurato, altrimenti persisti d e r manualmente
            }

            em.getTransaction().commit();

            // 7. Session Log Setup
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

            if (em != null && em.isOpen()) em.close();
            if (emf != null && emf.isOpen()) emf.close();
        }

        @Test
        void upRole_Integration_Success() {
            Utente creatoreDaPromuovere = new Utente();
            creatoreDaPromuovere.setNome("Test");
            creatoreDaPromuovere.setCognome("User");
            creatoreDaPromuovere.setUsername("test.promozione");
            creatoreDaPromuovere.setPasswordHash(crypt.hashPassword("pass"));
            creatoreDaPromuovere.setIsCreatore(true);
            creatoreDaPromuovere.setIsCompilatore(true);

            em.getTransaction().begin();
            em.persist(creatoreDaPromuovere);
            em.getTransaction().commit();

            String tokenCompilatore = jwtProvider.generateToken(creatoreDaPromuovere, "compilatore");
            log.aggiungi(tokenCompilatore, creatoreDaPromuovere);

            Response response = target("/quiz-manage/upRole")
                    .request()
                    .header("Authorization", "Bearer " + tokenCompilatore)
                    .post(Entity.json(new HashMap<>()));

            assertEquals(200, response.getStatus());

            Map<String, String> body = response.readEntity(new GenericType<Map<String, String>>() {});
            String newToken = body.get("token");

            assertNull(log.getUtente(tokenCompilatore));
            assertNotNull(log.getUtente(newToken));
            assertEquals("creatore", jwtProvider.getRoleFromToken(newToken));
            assertEquals(creatoreDaPromuovere.getId(), log.getUtente(newToken).getId());
        }

        @Test
        void upRole_Integration_Failure_UserCannotBePromoted() {
            Utente soloCompilatore = new Utente();
            soloCompilatore.setNome("Mario");
            soloCompilatore.setCognome("Semplice");
            soloCompilatore.setUsername("mario.semplice");
            soloCompilatore.setPasswordHash(crypt.hashPassword("pass"));
            soloCompilatore.setIsCreatore(false);
            soloCompilatore.setIsCompilatore(true);

            em.getTransaction().begin();
            em.persist(soloCompilatore);
            em.getTransaction().commit();

            String token = jwtProvider.generateToken(soloCompilatore, "compilatore");
            log.aggiungi(token, soloCompilatore);

            Response response = target("/quiz-manage/upRole")
                    .request()
                    .header("Authorization", "Bearer " + token)
                    .post(Entity.json(new HashMap<>()));

            assertEquals(401, response.getStatus());
        }

        @Test
        void upRole_Integration_Failure_InvalidRole() {
            Response response = target("/quiz-manage/upRole")
                    .request()
                    .header("Authorization", "Bearer " + realTokenUnauthorized)
                    .post(Entity.json(new HashMap<>()));

            assertEquals(401, response.getStatus());
        }

        @Test
        void upRole_Integration_Failure_TokenNotAlive() {
            String tokenValidoMaNonLoggato = jwtProvider.generateToken(alreadyLogTest, "compilatore");

            Response response = target("/quiz-manage/upRole")
                    .request()
                    .header("Authorization", "Bearer " + tokenValidoMaNonLoggato)
                    .post(Entity.json(new HashMap<>()));

            assertEquals(401, response.getStatus());
        }

        @Test
        void upRole_Integration_Failure_MalformedToken() {
            String tokenCorrotto = realToken + "manipulated";

            Response response = target("/quiz-manage/upRole")
                    .request()
                    .header("Authorization", "Bearer " + tokenCorrotto)
                    .post(Entity.json(new HashMap<>()));

            assertEquals(401, response.getStatus());
        }

        @Test
        void createQuiz_Integration_Success_PersistenceAndHash() {
            Quiz quiz = new Quiz();
            quiz.setTitolo("Quiz Integrazione");
            quiz.setDescrizione("Descrizione");
            quiz.setTempo("30:00");
            quiz.setDifficolta("Media");
            quiz.setPasswordQuiz("pwd123");

            Domanda d = new Domanda();
            d.setQuesito("Domanda 1");
            d.setPuntiRispostaCorretta(1);
            d.setPuntiRispostaSbagliata(0);

            Risposta r = new Risposta();
            r.setAffermazione("Vera");
            r.setFlagRispostaCorretta(true);

            d.setRisposte(List.of(r));
            quiz.setDomande(List.of(d));

            Response response = target("/quiz-manage/create")
                    .request()
                    .header("Authorization", "Bearer " + realToken)
                    .post(Entity.json(quiz));

            assertEquals(200, response.getStatus());

            // Verifica persistenza reale e hashing
            em.clear();
            List<Quiz> quizzes = em.createQuery("SELECT q FROM Quiz q WHERE q.titolo = 'Quiz Integrazione'", Quiz.class).getResultList();
            assertFalse(quizzes.isEmpty());

            Quiz salvato = quizzes.get(0);
            assertTrue(crypt.verificaPassword("pwd123", salvato.getPasswordQuiz()));
            assertEquals(alreadyLogTest.getId(), salvato.getUtente().getId());
            assertEquals(1, salvato.getDomande().size());
        }

        @Test
        void createQuiz_Integration_Failure_InvalidRole() {
            Quiz quiz = new Quiz();
            quiz.setTitolo("Quiz Fail");

            // Già loggato ma non ha il ruolo 'creatore'
            Response response = target("/quiz-manage/create")
                    .request()
                    .header("Authorization", "Bearer " + realTokenUnauthorized)
                    .post(Entity.json(quiz));

            assertEquals(400, response.getStatus());
        }

        @Test
        void createQuiz_Integration_Failure_MalformedJwt() {
            Quiz quiz = new Quiz();
            String tokenManomesso = realToken + "bad";

            Response response = target("/quiz-manage/create")
                    .request()
                    .header("Authorization", "Bearer " + tokenManomesso)
                    .post(Entity.json(quiz));

            assertEquals(400, response.getStatus());
        }

        @Test
        void deleteQuiz_Integration_Success() {
            Quiz quizDaEliminare = em.createQuery("SELECT q FROM Quiz q WHERE q.utente = :u", Quiz.class)
                    .setParameter("u", alreadyLogTest)
                    .setMaxResults(1)
                    .getSingleResult();

            int id = quizDaEliminare.getId();
            quizLog.aggiungi(alreadyLogTest, quizDaEliminare);

            Response response = target("/quiz-manage/delete")
                    .request()
                    .header("Authorization", "Bearer " + realToken)
                    .post(Entity.json(quizDaEliminare));

            assertEquals(200, response.getStatus());

            em.clear();
            assertNull(em.find(Quiz.class, id));
            assertNull(quizLog.getQuiz(alreadyLogTest, id));
        }

        @Test
        void deleteQuiz_Integration_Failure_NotOwner() {
            Quiz quizDiPippo = em.createQuery("SELECT q FROM Quiz q WHERE q.utente = :u", Quiz.class)
                    .setParameter("u", alreadyLogTest)
                    .setMaxResults(1)
                    .getSingleResult();

            Response response = target("/quiz-manage/delete")
                    .request()
                    .header("Authorization", "Bearer " + realTokenUnauthorized)
                    .post(Entity.json(quizDiPippo));

            assertEquals(400, response.getStatus());

            em.clear();
            assertNotNull(em.find(Quiz.class, quizDiPippo.getId()));
        }

        @Test
        void deleteQuiz_Integration_Failure_TokenNotAlive() {
            Quiz quiz = new Quiz();
            quiz.setId(1);
            String tokenFuoriSessione = jwtProvider.generateToken(alreadyLogTest, "creatore");

            Response response = target("/quiz-manage/delete")
                    .request()
                    .header("Authorization", "Bearer " + tokenFuoriSessione)
                    .post(Entity.json(quiz));

            assertEquals(400, response.getStatus());
        }

        @Test
        void updateQuiz_Integration_Success_SyncLogAndDB() {
            Quiz quizEsistente = em.createQuery("SELECT q FROM Quiz q WHERE q.utente = :u", Quiz.class)
                    .setParameter("u", alreadyLogTest)
                    .setMaxResults(1)
                    .getSingleResult();

            if (quizEsistente.getDomande() == null) {
                quizEsistente.setDomande(new ArrayList<>());
            }

            quizLog.aggiungi(alreadyLogTest, quizEsistente);

            Quiz quizUpdate = new Quiz();
            quizUpdate.setId(quizEsistente.getId());
            quizUpdate.setTitolo("Titolo Modificato Integrazione");
            quizUpdate.setDescrizione("Nuova Descrizione");
            quizUpdate.setTempo(quizEsistente.getTempo());
            quizUpdate.setDifficolta(quizEsistente.getDifficolta());

            Domanda d = new Domanda();
            d.setQuesito("Domanda Aggiornata");
            d.setPuntiRispostaCorretta(1);
            d.setPuntiRispostaSbagliata(0);

            Risposta r = new Risposta();
            r.setAffermazione("Risposta Aggiornata");
            r.setFlagRispostaCorretta(true);

            d.setRisposte(new ArrayList<>(List.of(r)));
            quizUpdate.setDomande(new ArrayList<>(List.of(d)));

            Response response = target("/quiz-manage/update")
                    .request()
                    .header("Authorization", "Bearer " + realToken)
                    .post(Entity.json(quizUpdate));

            assertEquals(200, response.getStatus());

            em.clear();
            Quiz aggiornatoDb = em.find(Quiz.class, quizEsistente.getId());
            assertEquals("Titolo Modificato Integrazione", aggiornatoDb.getTitolo());
        }

        @Test
        void updateQuiz_Integration_Failure_ForbiddenAccess() {
            Quiz quizDiPippo = em.createQuery("SELECT q FROM Quiz q WHERE q.utente = :u", Quiz.class)
                    .setParameter("u", alreadyLogTest)
                    .setMaxResults(1)
                    .getSingleResult();

            // Mario prova ad aggiornare un quiz di Pippo
            Response response = target("/quiz-manage/update")
                    .request()
                    .header("Authorization", "Bearer " + realTokenUnauthorized)
                    .post(Entity.json(quizDiPippo));

            assertEquals(400, response.getStatus());

            em.clear();
            Quiz nonModificato = em.find(Quiz.class, quizDiPippo.getId());
            assertNotEquals("Titolo Modificato Integrazione", nonModificato.getTitolo());
        }

        @Test
        void updateQuiz_Integration_Failure_EntityNotFoundInLog() {
            Quiz quizInesistente = new Quiz();
            quizInesistente.setId(9999);
            quizInesistente.setTitolo("Fake");

            Response response = target("/quiz-manage/update")
                    .request()
                    .header("Authorization", "Bearer " + realToken)
                    .post(Entity.json(quizInesistente));

            assertEquals(400, response.getStatus());
        }

        @Test
        void getQuiz_Integration_Success_LoadFromDBToLog() {
            GetQuizDTO dto = new GetQuizDTO();
            dto.setPage(1);

            Response response = target("/quiz-manage/getQuiz")
                    .request()
                    .header("Authorization", "Bearer " + realToken)
                    .post(Entity.json(dto));

            assertEquals(200, response.getStatus());

            List<Quiz> quizzes = response.readEntity(new GenericType<List<Quiz>>() {});

            assertFalse(quizzes.isEmpty());
            assertEquals(10, quizzes.size());

            Quiz inLog = quizLog.getQuiz(alreadyLogTest, quizzes.get(0).getId());
            assertNotNull(inLog);
            assertEquals(quizzes.get(0).getTitolo(), inLog.getTitolo());
        }

        @Test
        void getQuiz_Integration_Success_Pagination_SecondPage() {
            GetQuizDTO dto = new GetQuizDTO();
            dto.setPage(2);

            Response response = target("/quiz-manage/getQuiz")
                    .request()
                    .header("Authorization", "Bearer " + realToken)
                    .post(Entity.json(dto));

            assertEquals(200, response.getStatus());

            List<Quiz> quizzes = response.readEntity(new GenericType<List<Quiz>>() {});

            assertEquals(5, quizzes.size());
        }

        @Test
        void getQuiz_Integration_Success_FromExistingData() {
            GetQuizDTO dto = new GetQuizDTO();
            dto.setPage(1);

            Response response = target("/quiz-manage/getQuiz")
                    .request()
                    .header("Authorization", "Bearer " + realToken)
                    .post(Entity.json(dto));

            assertEquals(200, response.getStatus());

            List<Quiz> quizzes = response.readEntity(new GenericType<List<Quiz>>() {});

            assertFalse(quizzes.isEmpty());
            assertEquals(10, quizzes.size());
        }

        @Test
        void getQuiz_Integration_Failure_InvalidPage() {
            GetQuizDTO dto = new GetQuizDTO();
            dto.setPage(-1);

            Response response = target("/quiz-manage/getQuiz")
                    .request()
                    .header("Authorization", "Bearer " + realToken)
                    .post(Entity.json(dto));

            assertEquals(400, response.getStatus());
        }

        @Test
        void getQuiz_Integration_Failure_TokenNotAlive() {
            GetQuizDTO dto = new GetQuizDTO();
            dto.setPage(0);
            String tokenFuoriSessione = jwtProvider.generateToken(alreadyLogTest, "creatore");

            Response response = target("/quiz-manage/getQuiz")
                    .request()
                    .header("Authorization", "Bearer " + tokenFuoriSessione)
                    .post(Entity.json(dto));

            assertEquals(400, response.getStatus());
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
