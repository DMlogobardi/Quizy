package model.menager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Persistence;
import model.dao.QuizDAO;
import model.dao.UtenteDAO;
import model.entity.Domanda;
import model.entity.Quiz;
import model.entity.Risposta;
import model.entity.Utente;
import model.exception.*;
import model.mapper.EntityRefresher;
import model.utility.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class QuizCreatorMenagerTest {

    QuizCreatorMenager menager;

    // =========================
    // ===== UNIT TESTS ========
    // =========================
    @Nested
    @Tag("unit")
    class UnitTests {
        @Mock
        PassCrypt cryptMock;

        @Mock
        SessionLog logMock;

        @Mock
        AccessControlService serviceMock;

        @Mock
        QuizDAO daoMock;

        @Mock
        QuizLog quizLogMock;

        @BeforeEach
        void setup() throws Exception {
            menager = new QuizCreatorMenager();

            injectMethod(menager, cryptMock, "crypt");
            injectMethod(menager, logMock, "logBeble");
            injectMethod(menager, serviceMock, "accessControl");
            injectMethod(menager, daoMock, "dao");
            injectMethod(menager, quizLogMock, "quizLog");
        }

        //test upUserRole
        @Test
        void upUserRole_ShouldReturnAppException_WhenTokenIsNotAlive() {
            String tokenDead = "deadToken";

            when(logMock.isAlive(tokenDead)).thenReturn(false);

            assertThrows(AppException.class, () -> menager.upUserRole(tokenDead));
        }

        @Test
        void upUserRole_ShouldReturnAppException_WhenUserIsNotUnauthorized() {
            String token = "token";
            Utente test = new Utente();
            test.setId(1);
            test.setUsername("mario.rossi");
            test.setPasswordHash("paswordDiprova123");
            test.setIsCompilatore(true);
            test.setIsCreatore(false);

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(test);

            assertThrows(AppException.class, () -> menager.upUserRole(token));
        }

        @Test
        void upUserRole_ShouldReturnNewToken_WhenTokenIsRightAndUserHaveRole() {
            String token = "token";
            String newToken = "upToken";
            Utente test = new Utente();
            test.setId(1);
            test.setUsername("mario.rossi");
            test.setPasswordHash("paswordDiprova123");
            test.setIsCompilatore(true);
            test.setIsCreatore(true);

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(test);
            when(serviceMock.newTokenByRole("creatore", test)).thenReturn(newToken);

            String result = menager.upUserRole(token);

            assertNotNull(result);

            verify(logMock).rimuovi(token);
            verify(logMock).aggiungi(eq(newToken), any(Utente.class));
            verify(quizLogMock).clearQuiz(any(Utente.class));

            assertEquals(newToken, result);
        }

        //test createQuiz
        @Test
        void createQuiz_ShouldReturnQuizServiceException_WhenTokenIsNotAlive() {
            String tokenDead = "tokenMorto";

            when(logMock.isAlive(tokenDead)).thenReturn(false);

            assertThrows(QuizServiceException.class, () -> menager.createQuiz(new Quiz(), tokenDead));
        }

        @Test
        void createQuiz_ShouldReturnQuizServiceException_WhenTokenIsNotUnauthorized() {
            String tokenUnauthorized = "tokenUnauthorized";

            when(logMock.isAlive(tokenUnauthorized)).thenReturn(true);
            doThrow(InvalidRole.class).when(serviceMock).checkCreatore(tokenUnauthorized);

            assertThrows(QuizServiceException.class, () -> menager.createQuiz(new Quiz(), tokenUnauthorized));
        }

        @Test
        void createQuiz_ShouldReturnQuizServiceException_WhenQuizIsNullOrExceptionOccours() {
            String tokenUnauthorized = "tokenUnauthorized";

            when(logMock.isAlive(tokenUnauthorized)).thenReturn(true);

            assertThrows(QuizServiceException.class, () -> menager.createQuiz(null, tokenUnauthorized));
        }

        @Test
        void createQuiz_ShouldHashPassword_WhenPasswordIsProvided() throws Exception {
            String token = "validToken";
            String passwordChiaro = "secret123";
            String passwordHashata = "hashedSecret123";

            Quiz quiz = new Quiz();
            quiz.setPasswordQuiz(passwordChiaro);

            Utente testUser = new Utente();

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(testUser);
            when(cryptMock.hashPassword(passwordChiaro)).thenReturn(passwordHashata);

            menager.createQuiz(quiz, token);

            assertEquals(passwordHashata, quiz.getPasswordQuiz());
            assertEquals(testUser, quiz.getUtente());
            verify(cryptMock).hashPassword(passwordChiaro);
            verify(daoMock).insert(quiz);
        }

        @Test
        void createQuiz_ShouldNotHashPassword_WhenPasswordIsNull() throws Exception {
            String token = "validToken";

            Quiz quiz = new Quiz();
            quiz.setPasswordQuiz(null);

            Utente testUser = new Utente();

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(testUser);

            menager.createQuiz(quiz, token);

            assertNull(quiz.getPasswordQuiz());
            verify(cryptMock, never()).hashPassword(anyString());
            verify(daoMock).insert(quiz);
        }

        @Test
        void createQuiz_ShouldSetBidirectionalReferencesAndRegisterQuiz() throws Exception {
            String token = "validToken";
            Quiz quiz = new Quiz();

            Domanda d1 = new Domanda();
            Risposta r1 = new Risposta();
            d1.setRisposte(List.of(r1));
            quiz.setDomande(List.of(d1));

            Utente testUser = new Utente();

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(testUser);

            menager.createQuiz(quiz, token);

            assertEquals(quiz, d1.getQuiz());
            assertEquals(d1, r1.getDomanda());
            assertNull(d1.getId());
            assertNull(r1.getId());
            verify(daoMock).insert(quiz);
        }

        //test delete
        @Test
        void deleteQuiz_ShouldThrowException_WhenTokenNotAlive() {
            String token = "deadToken";
            when(logMock.isAlive(token)).thenReturn(false);

            assertThrows(QuizServiceException.class, () -> menager.deleteQuiz(new Quiz(), token));
        }

        @Test
        void deleteQuiz_ShouldThrowException_WhenRoleIsInvalid() throws Exception {
            String token = "invalidRoleToken";
            when(logMock.isAlive(token)).thenReturn(true);
            doThrow(InvalidRole.class).when(serviceMock).checkCreatore(token);

            assertThrows(QuizServiceException.class, () -> menager.deleteQuiz(new Quiz(), token));
        }

        @Test
        void deleteQuiz_ShouldThrowSpecificException_WhenTokenIsExpired() throws Exception {
            String token = "expiredToken";
            when(logMock.isAlive(token)).thenReturn(true);
            doThrow(TokenExpiredException.class).when(serviceMock).checkCreatore(token);

            QuizServiceException ex = assertThrows(QuizServiceException.class, () -> menager.deleteQuiz(new Quiz(), token));
            assertEquals("token expired, logout forzato", ex.getMessage());
        }

        @Test
        void deleteQuiz_ShouldThrowException_WhenDaoFails() throws Exception {
            String token = "validToken";
            Quiz quiz = new Quiz();
            quiz.setId(1);
            Utente u = new Utente();

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            doThrow(new RuntimeException()).when(daoMock).delete(anyInt(), any());

            assertThrows(QuizServiceException.class, () -> menager.deleteQuiz(quiz, token));
        }

        @Test
        void deleteQuiz_Success_HappyPath() throws Exception {
            String token = "validToken";
            Quiz quiz = new Quiz();
            quiz.setId(100);
            Utente u = new Utente();

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);

            menager.deleteQuiz(quiz, token);

            verify(quizLogMock).rimuoviSingoloQuiz(u, 100);
            verify(daoMock).delete(100, u);
        }

        //test aggiornaQuiz
        @Test
        void aggiornaQuiz_ShouldThrowException_WhenTokenNotAlive() {
            String token = "deadToken";
            when(logMock.isAlive(token)).thenReturn(false);

            assertThrows(QuizServiceException.class, () -> menager.aggiornaQuiz(new Quiz(), token));
        }

        @Test
        void aggiornaQuiz_ShouldThrowException_WhenRoleIsInvalid() throws Exception {
            String token = "invalidRoleToken";
            when(logMock.isAlive(token)).thenReturn(true);
            doThrow(InvalidRole.class).when(serviceMock).checkCreatore(token);

            assertThrows(QuizServiceException.class, () -> menager.aggiornaQuiz(new Quiz(), token));
        }

        @Test
        void aggiornaQuiz_ShouldThrowSpecificException_WhenTokenIsExpired() throws Exception {
            String token = "expiredToken";
            when(logMock.isAlive(token)).thenReturn(true);
            doThrow(TokenExpiredException.class).when(serviceMock).checkCreatore(token);

            QuizServiceException ex = assertThrows(QuizServiceException.class, () -> menager.aggiornaQuiz(new Quiz(), token));
            assertEquals("token expired, logout forzato", ex.getMessage());
        }

        @Test
        void aggiornaQuiz_ShouldThrowException_WhenEntityNotFound() throws Exception {
            String token = "validToken";
            Quiz quiz = new Quiz();
            quiz.setId(1);
            Utente u = new Utente();

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuiz(u, quiz.getId())).thenThrow(EntityNotFoundException.class);

            assertThrows(QuizServiceException.class, () -> menager.aggiornaQuiz(quiz, token));
        }

        @Test
        void aggiornaQuiz_Success_HappyPath() throws Exception {
            String token = "validToken";
            Quiz newQuiz = new Quiz();
            newQuiz.setId(50);
            Quiz oldQuiz = new Quiz();
            oldQuiz.setId(50);
            Utente u = new Utente();

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuiz(u, 50)).thenReturn(oldQuiz);

            menager.aggiornaQuiz(newQuiz, token);

            verify(daoMock).update(newQuiz, oldQuiz, u);
            verify(quizLogMock).aggiornaSingoloQuiz(u, newQuiz);
        }

        //test getQuizzes
        @Test
        void getQuizzes_ShouldThrowException_WhenTokenNotAlive() {
            String token = "deadToken";
            when(logMock.isAlive(token)).thenReturn(false);

            assertThrows(QuizServiceException.class, () -> menager.getQuizzes(0, token));
        }

        @Test
        void getQuizzes_ShouldThrowException_WhenPageNumberIsInvalid() {
            String token = "validToken";
            when(logMock.isAlive(token)).thenReturn(true);

            assertThrows(QuizServiceException.class, () -> menager.getQuizzes(-1, token));
        }

        @Test
        void getQuizzes_ShouldReturnFromLog_WhenDataIsPresentInLog() throws Exception {
            String token = "validToken";
            Utente u = new Utente();
            List<Quiz> logList = List.of(new Quiz(), new Quiz());

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuizPaginati(u, 0, 10)).thenReturn(logList);

            List<Quiz> result = menager.getQuizzes(0, token);

            assertEquals(2, result.size());
            verifyNoInteractions(daoMock);
        }

        @Test
        void getQuizzes_ShouldReturnFromDaoAndPopulateLog_WhenLogIsEmpty() throws Exception {
            String token = "validToken";
            Utente u = new Utente();
            Quiz q1 = new Quiz();
            List<Quiz> daoList = List.of(q1);

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuizPaginati(u, 0, 10)).thenReturn(Collections.emptyList());
            when(daoMock.findAllByUtente(0, u)).thenReturn(daoList);

            List<Quiz> result = menager.getQuizzes(0, token);

            assertEquals(1, result.size());
            verify(daoMock).findAllByUtente(0, u);
            verify(quizLogMock).aggiungi(u, q1);
        }

        @Test
        void getQuizzes_ShouldThrowQuizServiceException_WhenExceptionOccurs() throws Exception {
            String token = "validToken";
            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenThrow(new RuntimeException());

            assertThrows(QuizServiceException.class, () -> menager.getQuizzes(0, token));
        }

        @Test
        void getQuizzes_ShouldReturnEmptyList_WhenBothLogAndDaoAreEmpty() throws Exception {
            String token = "validToken";
            Utente u = new Utente();

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuizPaginati(u, 0, 10)).thenReturn(Collections.emptyList());
            when(daoMock.findAllByUtente(0, u)).thenReturn(Collections.emptyList());

            List<Quiz> result = menager.getQuizzes(0, token);

            assertTrue(result.isEmpty());
        }
    }

    // =========================
    // === INTEGRATION TESTS ===
    // =========================
    @Nested
    @Tag("integration")
    class IntegrationTests {
        EntityManagerFactory emf;
        EntityManager em;
        JWT_Provider jwtProvider;
        EntityRefresher refresher;

        PassCrypt crypt;
        SessionLog log;
        AccessControlService service;
        QuizDAO dao;
        QuizLog quizLog;

        Utente alreadyLogTest, alreadyLogTestUnauthorized;
        String realToken, realTokenUnauthorized;

        @BeforeEach
        void setup() throws Exception {
            emf = Persistence.createEntityManagerFactory("testPU");
            em = emf.createEntityManager();

            dao = new QuizDAO();
            refresher = new EntityRefresher();
            injectMethod(dao, em, "em");
            injectMethod(refresher, em, "em");

            jwtProvider = new JWT_Provider();
            crypt = new PassCrypt();

            log = new SessionLog();
            service = new AccessControlService();
            injectMethod(log, jwtProvider, "jwtProvider");
            injectMethod(service, jwtProvider, "jwtProvider");

            quizLog = new QuizLog();
            injectMethod(quizLog, refresher, "refresher");

            menager = new QuizCreatorMenager();
            injectMethod(menager, crypt, "crypt");
            injectMethod(menager, log, "logBeble");
            injectMethod(menager, service, "accessControl");
            injectMethod(menager, dao, "dao");
            injectMethod(menager, quizLog, "quizLog");

            alreadyLogTestUnauthorized = creaUtenteDiTest("Mario", "Rossi", "mariorossi", "hash123");
            alreadyLogTest = creaUtenteDiTest("Pippo", "Alberti", "pippo12", "sc2435");
            alreadyLogTest.setIsCreatore(true);
            em.getTransaction().begin();
            em.persist(alreadyLogTestUnauthorized);
            em.persist(alreadyLogTest);
            em.getTransaction().commit();

            realToken = jwtProvider.generateToken(alreadyLogTest, "creatore");
            realTokenUnauthorized = jwtProvider.generateToken(alreadyLogTestUnauthorized, "compilatore");
            log.aggiungi(realToken, alreadyLogTest);
            log.aggiungi(realTokenUnauthorized, alreadyLogTestUnauthorized);

            em.getTransaction().begin();
            for (int i = 1; i <= 15; i++) {
                Quiz q = creaQuizDiTest(alreadyLogTest, "Quiz " + i, "Descrizione " + i);
                Domanda d = creaDomandaDiTest(q, "Domanda per quiz " + i);
                creaRispostaDiTest(d, "Risposta corretta", true);
                creaRispostaDiTest(d, "Risposta errata", false);
                em.persist(q);
            }
            em.getTransaction().commit();
        }

        @AfterEach
        void tearDown() {
            if (em.isOpen()) em.close();
            if (emf.isOpen()) emf.close();
        }

        @Test
        @Tag("integration")
        void upUserRole_Integrazione_Successo() throws Exception {
            Utente utenteEvolutivo = creaUtenteDiTest("Test", "User", "test.user", "pass");
            utenteEvolutivo.setIsCreatore(true);

            em.getTransaction().begin();
            em.persist(utenteEvolutivo);
            em.getTransaction().commit();

            String tokenCompilatore = jwtProvider.generateToken(utenteEvolutivo, "compilatore");
            log.aggiungi(tokenCompilatore, utenteEvolutivo);

            String tokenNuovo = menager.upUserRole(tokenCompilatore);

            assertNotNull(tokenNuovo);
            assertNull(log.getUtente(tokenCompilatore));
            assertNotNull(log.getUtente(tokenNuovo));

            String ruoloEffettivo = jwtProvider.getRoleFromToken(tokenNuovo);
            assertEquals("creatore", ruoloEffettivo);

            String ruoloVecchio = jwtProvider.getRoleFromToken(tokenCompilatore);
            assertEquals("compilatore", ruoloVecchio);
        }

        @Test
        @Tag("integration")
        void createQuiz_Integrazione_SalvataggioCompletoConHash() throws Exception {
            Quiz quiz = creaQuizDiTest(alreadyLogTest, "Quiz Integrazione", "Desc");
            quiz.setPasswordQuiz("pwd123");
            Domanda d = creaDomandaDiTest(quiz, "Domanda Test");
            creaRispostaDiTest(d, "R1", true);

            menager.createQuiz(quiz, realToken);

            Quiz salvato = em.find(Quiz.class, quiz.getId());
            assertNotNull(salvato);
            assertTrue(crypt.verificaPassword("pwd123", salvato.getPasswordQuiz()));
            assertEquals(alreadyLogTest.getId(), salvato.getUtente().getId());
            assertEquals(1, salvato.getDomande().size());
            assertEquals(quiz.getId(), salvato.getDomande().get(0).getQuiz().getId());
        }

        @Test
        @Tag("integration")
        void deleteQuiz_Integrazione_Successo() throws Exception {
            Quiz quiz = dao.findAllByUtente(1, alreadyLogTest).get(0);
            int id = quiz.getId();
            quizLog.aggiungi(alreadyLogTest, quiz);

            menager.deleteQuiz(quiz, realToken);

            em.clear();
            assertNull(em.find(Quiz.class, id));
            assertNull(quizLog.getQuiz(alreadyLogTest, id));
        }

        @Test
        @Tag("integration")
        void aggiornaQuiz_Integrazione_ModificaDatiESincronizzazioneLog() throws Exception {
            Quiz quiz = creaQuizDiTest(alreadyLogTest, "Titolo Originale", "Desc");
            em.getTransaction().begin();
            em.persist(quiz);
            em.getTransaction().commit();

            quizLog.aggiungi(alreadyLogTest, quiz);

            quiz.setTitolo("Titolo Modificato");
            menager.aggiornaQuiz(quiz, realToken);

            em.clear();
            Quiz aggiornato = em.find(Quiz.class, quiz.getId());
            assertEquals("Titolo Modificato", aggiornato.getTitolo());
            assertEquals("Titolo Modificato", quizLog.getQuiz(alreadyLogTest, quiz.getId()).getTitolo());
        }

        @Test
        @Tag("integration")
        void getQuizzes_Integrazione_Successo() throws Exception {
            List<Quiz> result = menager.getQuizzes(1, realToken);

            assertEquals(10, result.size());
            assertFalse(quizLog.getQuizPaginati(alreadyLogTest, 1, 10).isEmpty());
        }

        @Test
        @Tag("integration")
        void getQuizzes_Integrazione_PaginaVuota() throws Exception {
            List<Quiz> result = menager.getQuizzes(10, realToken);
            assertTrue(result.isEmpty());
        }

        @Test
        @Tag("integration")
        void createQuiz_Integrazione_ErrorePermessi() {
            Quiz quiz = creaQuizDiTest(alreadyLogTestUnauthorized, "Fail", "Desc");

            assertThrows(QuizServiceException.class, () ->
                    menager.createQuiz(quiz, realTokenUnauthorized)
            );
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
