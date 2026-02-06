package model.menager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import model.dao.FaDAO;
import model.dao.QuizDAO;
import model.dao.RispondeDAO;
import model.entity.*;
import model.exception.AppException;
import model.exception.InvalidRole;
import model.exception.QuizServiceException;
import model.exception.QuizUseException;
import model.mapper.EntityRefresher;
import model.utility.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuizUserMenagerTest {

    QuizUserMenager menager;

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
        QuizLog quizLogMock;

        @Mock
        AccessControlService serviceMock;

        @Mock
        QuizDAO quizDAOMock;

        @Mock
        FaDAO faDAOMock;

        @Mock
        RispondeDAO rispondeDAOMock;

        @BeforeEach
        void setup() throws Exception {
            menager = new QuizUserMenager();

            injectMethod(menager, cryptMock, "crypt");
            injectMethod(menager, logMock, "logBeble");
            injectMethod(menager, quizLogMock, "quizLog");
            injectMethod(menager, serviceMock, "accessControl");
            injectMethod(menager, quizDAOMock, "dao");
            injectMethod(menager, faDAOMock, "daoFa");
            injectMethod(menager, rispondeDAOMock, "daoRisponde");
        }

        //test downUserRole
        @Test
        void downUserRole_ShouldReturnAppException_WhenTokenIsNotAlive() {
            String tokenDead = "deadToken";

            when(logMock.isAlive(tokenDead)).thenReturn(false);

            assertThrows(AppException.class, () -> menager.downUserRole(tokenDead));
        }

        @Test
        void downUserRole_ShouldReturnAppException_WhenUserIsNotUnauthorized() {
            String token = "token";
            Utente test = new Utente();
            test.setId(1);
            test.setUsername("luigi.verdi");
            test.setIsCompilatore(false); // Non è un compilatore
            test.setIsCreatore(true);

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(test);

            assertThrows(AppException.class, () -> menager.downUserRole(token));
        }

        @Test
        void downUserRole_ShouldReturnNewToken_WhenTokenIsRightAndUserHaveRole() throws Exception {
            String token = "token";
            String newToken = "downToken";
            Utente test = new Utente();
            test.setId(1);
            test.setUsername("luigi.verdi");
            test.setIsCompilatore(true);
            test.setIsCreatore(true);

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(test);
            when(serviceMock.newTokenByRole("compilatore", test)).thenReturn(newToken);

            String result = menager.downUserRole(token);

            assertNotNull(result);
            assertEquals(newToken, result);

            verify(logMock).rimuovi(token);
            verify(quizLogMock).clearQuiz(test);
            verify(logMock).aggiungi(eq(newToken), any(Utente.class));
        }

        //test getQuizzes
        @Test
        void getQuizzes_ShouldThrowQuizServiceException_WhenTokenIsNotAlive() {
            String token = "deadToken";
            when(logMock.isAlive(token)).thenReturn(false);

            assertThrows(QuizUseException.class, () -> menager.getQuizzes(0, token));
        }

        @Test
        void getQuizzes_ShouldThrowInvalidRole_WhenRoleIsNotCompilatore() throws Exception {
            String token = "wrongRoleToken";
            when(logMock.isAlive(token)).thenReturn(true);
            doThrow(new InvalidRole("Unauthorized")).when(serviceMock).checkCompilatore(token);

            assertThrows(QuizUseException.class, () -> menager.getQuizzes(0, token));
        }

        @Test
        void getQuizzes_ShouldThrowQuizUseException_WhenPageNumberIsNegative() throws Exception {
            String token = "validToken";
            when(logMock.isAlive(token)).thenReturn(true);

            assertThrows(QuizUseException.class, () -> menager.getQuizzes(-1, token));
        }

        @Test
        void getQuizzes_ShouldReturnQuizzesFromLog_WhenLogIsNotEmpty() throws Exception {
            String token = "validToken";
            Utente u = new Utente();
            List<Quiz> logQuizzes = List.of(new Quiz(), new Quiz());

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuizPaginati(u, 0, 10)).thenReturn(logQuizzes);

            List<Quiz> result = menager.getQuizzes(0, token);

            assertEquals(2, result.size());
            verifyNoInteractions(quizDAOMock);
        }

        @Test
        void getQuizzes_ShouldFetchFromDbAndPopulateLog_WhenLogIsEmpty() throws Exception {
            String token = "validToken";
            Utente u = new Utente();
            Quiz dbQuiz = new Quiz();
            dbQuiz.setId(1);
            List<Quiz> dbQuizzes = List.of(dbQuiz);

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuizPaginati(u, 0, 10)).thenReturn(null);
            when(quizDAOMock.findAll(0, 10)).thenReturn(dbQuizzes);

            List<Quiz> result = menager.getQuizzes(0, token);

            assertEquals(1, result.size());
            verify(quizLogMock).aggiungi(u, dbQuiz);
        }

        @Test
        void getQuizzes_ShouldThrowQuizServiceException_WhenAppExceptionIsThrownInTokenCheck() throws Exception {
            String token = "token";
            when(logMock.isAlive(token)).thenReturn(true);
            doThrow(new AppException("Internal Error")).when(serviceMock).checkCompilatore(token);

            assertThrows(QuizUseException.class, () -> menager.getQuizzes(0, token));
        }

        //test startQuizWhitPassword
        @Test
        void startQuizWhitPassword_ShouldThrowQuizServiceException_WhenTokenIsNotAlive() {
            String token = "deadToken";
            when(logMock.isAlive(token)).thenReturn(false);

            assertThrows(QuizUseException.class, () -> menager.startQuiz(new Quiz(), "dfsfw", token));
        }

        @Test
        void startQuizWhitPassword_ShouldThrowInvalidRole_WhenRoleIsNotCompilatore() throws Exception {
            String token = "wrongRoleToken";
            when(logMock.isAlive(token)).thenReturn(true);
            doThrow(new InvalidRole("Unauthorized")).when(serviceMock).checkCompilatore(token);

            assertThrows(QuizUseException.class, () -> menager.startQuiz(new Quiz(), "dfsfw", token));
        }

        @Test
        void startQuizWhitPassword_ShouldThrowQuizUseException_WhenQuizDoesNotExist() throws Exception {
            String token = "validToken";
            String password = "password";
            Integer quizId = 1;
            Quiz inputQuiz = new Quiz();
            inputQuiz.setId(quizId);
            Utente u = new Utente();

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuiz(u, quizId)).thenReturn(null);
            when(quizDAOMock.findById(quizId)).thenReturn(null);

            assertThrows(QuizUseException.class, () -> menager.startQuiz(inputQuiz, password, token));
        }

        @Test
        void startQuizWhitPassword_ShouldThrowQuizUseException_WhenPasswordIsWrong() throws Exception {
            String token = "validToken";
            String wrongPassword = "wrong";
            String correctPassword = "right";
            Integer quizId = 1;

            Quiz inputQuiz = new Quiz();
            inputQuiz.setId(quizId);

            Utente u = new Utente();

            Quiz foundQuiz = new Quiz();
            foundQuiz.setId(quizId);
            foundQuiz.setPasswordQuiz(correctPassword);

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuiz(u, quizId)).thenReturn(null);
            when(quizDAOMock.findById(quizId)).thenReturn(foundQuiz);
            when(cryptMock.verificaPassword(wrongPassword, correctPassword)).thenReturn(false);

            assertThrows(QuizUseException.class, () -> menager.startQuiz(inputQuiz, wrongPassword, token));
        }

        @Test
        void startQuizWhitPassword_ShouldReturnQuestions_WhenHappyPath() throws Exception {
            String token = "validToken";
            String password = "right";
            Integer quizId = 1;

            Quiz inputQuiz = new Quiz();
            inputQuiz.setId(quizId);

            Utente u = new Utente();
            u.setId(100);

            Quiz foundQuiz = new Quiz();
            foundQuiz.setId(quizId);
            foundQuiz.setPasswordQuiz(password);
            List<Domanda> expectedDomande = List.of(new Domanda());
            foundQuiz.setDomande(expectedDomande);

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuiz(u, quizId)).thenReturn(foundQuiz);
            when(cryptMock.verificaPassword(password, password)).thenReturn(true);
            when(faDAOMock.findByUtenteQuiz(any(Quiz.class), any(Utente.class)))
                    .thenThrow(new NoResultException());

            List<Domanda> result = menager.startQuiz(inputQuiz, password, token);

            assertNotNull(result);
            assertEquals(expectedDomande.size(), result.size());
        }

        //test startQuiz
        @Test
        void startQuiz_ShouldThrowQuizUseException_WhenTokenIsNotAlive() {
            String token = "deadToken";
            when(logMock.isAlive(token)).thenReturn(false);

            assertThrows(QuizUseException.class, () -> menager.startQuiz(new Quiz(), token));
        }

        @Test
        void startQuiz_ShouldThrowQuizUseException_WhenRoleIsNotCompilatore() throws Exception {
            String token = "wrongRoleToken";
            when(logMock.isAlive(token)).thenReturn(true);
            doThrow(new InvalidRole("Unauthorized")).when(serviceMock).checkCompilatore(token);

            assertThrows(QuizUseException.class, () -> menager.startQuiz(new Quiz(), token));
        }

        @Test
        void startQuiz_ShouldThrowQuizUseException_WhenQuizDoesNotExist() throws Exception {
            String token = "validToken";
            Integer quizId = 1;
            Quiz inputQuiz = new Quiz();
            inputQuiz.setId(quizId);
            Utente u = new Utente();

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuiz(any(Utente.class), eq(quizId))).thenReturn(null);
            when(quizDAOMock.findById(quizId)).thenReturn(null);

            assertThrows(QuizUseException.class, () -> menager.startQuiz(inputQuiz, token));
        }

        @Test
        void startQuiz_ShouldThrowQuizUseException_WhenQuizHasPassword() throws Exception {
            String token = "validToken";
            Integer quizId = 1;
            Quiz inputQuiz = new Quiz();
            inputQuiz.setId(quizId);
            Utente u = new Utente();
            Quiz foundQuiz = new Quiz();
            foundQuiz.setId(quizId);
            foundQuiz.setPasswordQuiz("securePassword123");

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuiz(any(Utente.class), eq(quizId))).thenReturn(foundQuiz);

            assertThrows(QuizUseException.class, () -> menager.startQuiz(inputQuiz, token));
        }

        @Test
        void startQuiz_ShouldReturnQuestions_WhenHappyPath() throws Exception {
            String token = "validToken";
            Integer quizId = 1;
            Quiz inputQuiz = new Quiz();
            inputQuiz.setId(quizId);
            Utente u = new Utente();
            u.setId(100);

            Quiz foundQuiz = new Quiz();
            foundQuiz.setId(quizId);
            foundQuiz.setPasswordQuiz(null);

            Domanda d1 = new Domanda();
            d1.setRisposte(new ArrayList<>());
            List<Domanda> expectedDomande = List.of(d1);
            foundQuiz.setDomande(expectedDomande);

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuiz(any(Utente.class), eq(quizId))).thenReturn(foundQuiz);
            when(faDAOMock.findByUtenteQuiz(any(Quiz.class), any(Utente.class)))
                    .thenThrow(new NoResultException());

            List<Domanda> result = menager.startQuiz(inputQuiz, token);

            assertNotNull(result);
            assertEquals(expectedDomande.size(), result.size());
        }

        //test completaQuiz
        @Test
        void completaQuiz_ShouldThrowQuizUseException_WhenQuizDoesNotExist() throws Exception {
            String token = "validToken";
            Integer quizId = 1;
            Quiz inputQuiz = new Quiz();
            inputQuiz.setId(quizId);
            Utente u = new Utente();

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuiz(any(Utente.class), eq(quizId))).thenReturn(null);
            when(quizDAOMock.findById(quizId)).thenReturn(null);

            assertThrows(QuizUseException.class, () -> menager.completaQuiz(inputQuiz, new ArrayList<>(), token));
        }

        @Test
        void completaQuiz_ShouldThrowQuizUseException_WhenListsAreMissing() throws Exception {
            String token = "validToken";
            Integer quizId = 1;
            Quiz inputQuiz = new Quiz();
            inputQuiz.setId(quizId);
            Utente u = new Utente();
            Quiz foundQuiz = new Quiz();
            foundQuiz.setId(quizId);
            foundQuiz.setDomande(null);

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuiz(any(Utente.class), eq(quizId))).thenReturn(foundQuiz);

            assertThrows(QuizUseException.class, () -> menager.completaQuiz(inputQuiz, null, token));
        }

        @Test
        void completaQuiz_ShouldCalculateScoreAndSave_WhenHappyPath() throws Exception {
            String token = "validToken";
            Integer quizId = 1;

            Utente u = new Utente();
            u.setId(100);

            Domanda d1 = new Domanda();
            d1.setId(50);
            d1.setPuntiRispostaCorretta(2);
            d1.setPuntiRispostaSbagliata(-1);

            Quiz foundQuiz = new Quiz();
            foundQuiz.setId(quizId);
            foundQuiz.setTitolo("Quiz Test");
            foundQuiz.setDomande(List.of(d1));

            Risposta rGiusta = new Risposta();
            rGiusta.setId(500);
            rGiusta.setFlagRispostaCorretta(true);
            rGiusta.setDomanda(d1); // Necessario per punteggioRisposta

            List<Risposta> risposteClient = List.of(rGiusta);

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuiz(any(Utente.class), eq(quizId))).thenReturn(foundQuiz);

            int result = menager.completaQuiz(foundQuiz, risposteClient, token);

            assertEquals(2, result);
            verify(faDAOMock).insert(any(Fa.class));
            verify(rispondeDAOMock).insertAll(anyList());
        }

        @Test
        void completaQuiz_ShouldThrowQuizUseException_WhenDomandaNonAppartieneAlQuiz() throws Exception {
            String token = "validToken";
            Integer quizId = 1;
            Utente u = new Utente();

            Domanda dAppartenente = new Domanda();
            dAppartenente.setId(1);

            Quiz foundQuiz = new Quiz();
            foundQuiz.setId(quizId);
            foundQuiz.setDomande(List.of(dAppartenente));

            Domanda dEstranea = new Domanda();
            dEstranea.setId(999); // ID non presente nel quiz

            Risposta rClient = new Risposta();
            rClient.setDomanda(dEstranea);

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuiz(any(Utente.class), eq(quizId))).thenReturn(foundQuiz);

            assertThrows(QuizUseException.class, () -> menager.completaQuiz(foundQuiz, List.of(rClient), token));
        }

        @Test
        void completaQuiz_ShouldThrowQuizUseException_WhenDomandaRispostaPiuVolte() throws Exception {
            String token = "validToken";
            Integer quizId = 1;
            Utente u = new Utente();

            Domanda d1 = new Domanda();
            d1.setId(50);

            Quiz foundQuiz = new Quiz();
            foundQuiz.setId(quizId);
            foundQuiz.setDomande(List.of(d1));

            Risposta r1 = new Risposta();
            r1.setDomanda(d1);
            r1.setFlagRispostaCorretta(true);

            Risposta r2 = new Risposta();
            r2.setDomanda(d1); // Stessa domanda
            r2.setFlagRispostaCorretta(false);

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuiz(any(Utente.class), eq(quizId))).thenReturn(foundQuiz);

            assertThrows(QuizUseException.class, () -> menager.completaQuiz(foundQuiz, List.of(r1, r2), token));
        }

        @Test
        void completaQuiz_ShouldReturnNegativeScore_WhenAnswersAreWrong() throws Exception {
            String token = "validToken";
            Integer quizId = 1;
            Utente u = new Utente();

            Domanda d1 = new Domanda();
            d1.setId(50);
            d1.setPuntiRispostaCorretta(3);
            d1.setPuntiRispostaSbagliata(-2);

            Quiz foundQuiz = new Quiz();
            foundQuiz.setId(quizId);
            foundQuiz.setTitolo("Quiz");
            foundQuiz.setDomande(List.of(d1));

            Risposta rSbagliata = new Risposta();
            rSbagliata.setDomanda(d1);
            rSbagliata.setFlagRispostaCorretta(false);

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(quizLogMock.getQuiz(any(Utente.class), eq(quizId))).thenReturn(foundQuiz);

            int result = menager.completaQuiz(foundQuiz, List.of(rSbagliata), token);

            assertEquals(-2, result);
            verify(faDAOMock).insert(argThat(fa -> fa.getPunteggio() == -2));
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
        QuizLog quizLog;
        AccessControlService service;
        QuizDAO dao;
        FaDAO daoFa;
        RispondeDAO daoRisponde;

        Utente alreadyLogTest, alreadyLogTestUnauthorized;
        String realToken, realTokenUnauthorized;

        @BeforeEach
        void setup() throws Exception {
            emf = Persistence.createEntityManagerFactory("testPU");
            em = emf.createEntityManager();

            dao = new QuizDAO();
            daoFa = new FaDAO();
            daoRisponde = new RispondeDAO();
            refresher = new EntityRefresher();
            injectMethod(dao, em, "em");
            injectMethod(refresher, em, "em");
            injectMethod(daoFa, em, "em");
            injectMethod(daoRisponde, em, "em");

            jwtProvider = new JWT_Provider();
            crypt = new PassCrypt();

            log = new SessionLog();
            service = new AccessControlService();
            injectMethod(log, jwtProvider, "jwtProvider");
            injectMethod(service, jwtProvider, "jwtProvider");

            quizLog = new QuizLog();
            injectMethod(quizLog, refresher, "refresher");

            menager = new QuizUserMenager();
            injectMethod(menager, crypt, "crypt");
            injectMethod(menager, log, "logBeble");
            injectMethod(menager, service, "accessControl");
            injectMethod(menager, dao, "dao");
            injectMethod(menager, quizLog, "quizLog");
            injectMethod(menager, daoFa, "daoFa");
            injectMethod(menager, daoRisponde, "daoRisponde");

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
        void downUserRole_Integration_ShouldUpdateSessionAndQuizLog() throws Exception {
            String newToken = menager.downUserRole(realToken);

            assertNotNull(newToken);
            assertNotEquals(realToken, newToken);
            assertFalse(log.isAlive(realToken));
            assertTrue(log.isAlive(newToken));
            assertEquals(alreadyLogTest.getId(), log.getUtente(newToken).getId());
        }

        @Test
        void getQuizzes_Integration_ShouldFetchFromDbAndSyncWithQuizLog() throws Exception {
            List<Quiz> quizzes = menager.getQuizzes(1, realTokenUnauthorized);

            assertNotNull(quizzes);
            assertFalse(quizzes.isEmpty());
            assertTrue(quizzes.size() <= 10);

            Quiz firstQuiz = quizzes.get(0);
            assertNotNull(quizLog.getQuiz(alreadyLogTestUnauthorized, firstQuiz.getId()));
        }

        @Test
        void startQuiz_NoPassword_Integration_ShouldInitializeLazyCollections() throws Exception {
            Quiz dbQuiz = em.createQuery("SELECT q FROM Quiz q", Quiz.class).setMaxResults(1).getSingleResult();

            List<Domanda> domande = menager.startQuiz(dbQuiz, realTokenUnauthorized);

            assertNotNull(domande);
            assertFalse(domande.isEmpty());

            // Verifica inizializzazione Hibernate (Hibernate.isInitialized)
            assertTrue(Persistence.getPersistenceUtil().isLoaded(domande));
            assertTrue(Persistence.getPersistenceUtil().isLoaded(domande.get(0).getRisposte()));
        }

        @Test
        void startQuiz_WithPassword_Integration_ShouldVerifyAgainstHashedPasswordInDb() throws Exception {
            String rawPassword = "secretPassword";
            String hashedPassword = crypt.hashPassword(rawPassword);

            em.getTransaction().begin();
            Quiz q = creaQuizDiTest(alreadyLogTest, "Quiz Protetto", "Desc");
            q.setPasswordQuiz(hashedPassword);
            em.persist(q);
            em.getTransaction().commit();

            List<Domanda> domande = menager.startQuiz(q, rawPassword, realTokenUnauthorized);

            assertNotNull(domande);
            assertThrows(QuizUseException.class, () -> menager.startQuiz(q, "wrongPassword", realTokenUnauthorized));
        }

        @Test
        void completaQuiz_Integration_ShouldPersistResultsAndBlockReentry() throws Exception {
            // Recupero il quiz senza fetch multipli
            Quiz q = em.createQuery("SELECT q FROM Quiz q WHERE q.passwordQuiz IS NULL", Quiz.class)
                    .setMaxResults(1)
                    .getSingleResult();

            // Inizializzo manualmente per il test
            Domanda d = q.getDomande().get(0);
            Risposta r = d.getRisposte().stream()
                    .filter(Risposta::getFlagRispostaCorretta)
                    .findFirst()
                    .get();

            int punteggio = menager.completaQuiz(q, List.of(r), realTokenUnauthorized);

            assertTrue(punteggio > 0);

            // Verifico persistenza su DB tramite query nativa o pulizia cache
            em.clear();
            Fa fa = em.createQuery("SELECT f FROM Fa f WHERE f.utente.id = :uId AND f.quiz.id = :qId", Fa.class)
                    .setParameter("uId", alreadyLogTestUnauthorized.getId())
                    .setParameter("qId", q.getId())
                    .getSingleResult();

            assertNotNull(fa);
            assertEquals(punteggio, fa.getPunteggio());

            // Integrazione: startQuiz deve tornare null perché isComplete() ora è true
            List<Domanda> resultDopoCompletamento = menager.startQuiz(q, realTokenUnauthorized);
            assertNull(resultDopoCompletamento);
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
