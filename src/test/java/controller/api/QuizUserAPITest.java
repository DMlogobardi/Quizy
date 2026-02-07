package controller.api;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.core.Application;
import model.dao.FaDAO;
import model.dao.QuizDAO;
import model.dao.RispondeDAO;
import model.entity.Domanda;
import model.entity.Quiz;
import model.entity.Risposta;
import model.entity.Utente;
import model.mapper.EntityRefresher;
import model.menager.QuizUserMenager;
import model.utility.*;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class QuizUserAPITest {

    // ===========================
    // === UNIT TESTS (Mockito) ===
    // ===========================
    @Nested
    class UnitTests {
        private QuizUserAPI api;
        private QuizUserMenager menagerMock;

        @BeforeEach
        void setUp() throws Exception {
            api = new QuizUserAPI();
            menagerMock = mock(QuizUserMenager.class);

            injectMethod(api, menagerMock, "useMenager");
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

            return config;
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

            em.getTransaction().commit();

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

            if (em != null && em.isOpen()) em.close();
            if (emf != null && emf.isOpen()) emf.close();
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
