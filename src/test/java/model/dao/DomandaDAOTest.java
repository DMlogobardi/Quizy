package model.dao;

import jakarta.persistence.*;
import model.entity.Domanda;
import model.entity.Quiz;
import model.entity.Risposta;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;
import model.exception.UserNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DomandaDAOTest {

    EntityManager em;
    DomandaDAO dao;

    // =========================
    // ===== UNIT TESTS ========
    // =========================
    @Nested
    @Tag("unit")
    class UnitTests {

        @Mock
        EntityManager mockEm;

        @BeforeEach
        void setup() throws Exception {
            dao = new DomandaDAO();
            em = mockEm;
            injectEm(dao, em);
        }

        //test findById
        @Test
        void findById_ShouldReturnAppException_WhenIdIsZeroOrNegative() {
            assertThrows(AppException.class, () -> dao.findById(0));

            verifyNoInteractions(em);
        }

        @Test
        void findById_ShouldReturnUtente_WhenIdIsInvalid() {
            int id = 1;
            Domanda fintaDomanda = new Domanda();
            fintaDomanda.setId(id);

            when(em.find(Domanda.class, id)).thenReturn(fintaDomanda);

            Domanda result = dao.findById(id);

            assertNotNull(result);
            assertEquals(result, fintaDomanda);
            verify(em).find(Domanda.class, id);
        }

        //test findAll

        @Test
        void findAll_ShouldReturnAppException_WhenPageSizeIsZeroOrNegativeAndPageNumberIsValid() {
            assertThrows(AppException.class, () -> dao.findAll(1, 0));

            verifyNoInteractions(em);
        }

        @Test
        void findAll_ShouldReturnAppException_WhenPageNumberIsZeroOrNegativeAndPageSizeIsValid() {
            assertThrows(AppException.class, () -> dao.findAll(0, 1));

            verifyNoInteractions(em);
        }

        @Test
        void findAll_ShouldReturnList_WhenParametersAreValid() throws AppException {
            int pageNumber = 2;
            int pageSize = 10;
            int expectedFirstResult = 10;

            List<Domanda> fintaLista = Arrays.asList(new Domanda(), new Domanda());

            TypedQuery<Domanda> mockedQuery = mock(TypedQuery.class);

            when(em.createNamedQuery("Domanda.findAll", Domanda.class)).thenReturn(mockedQuery);
            when(mockedQuery.setFirstResult(anyInt())).thenReturn(mockedQuery);
            when(mockedQuery.setMaxResults(anyInt())).thenReturn(mockedQuery);
            when(mockedQuery.getResultList()).thenReturn(fintaLista);

            List<Domanda> result = dao.findAll(pageNumber, pageSize);

            assertNotNull(result);
            assertEquals(2, result.size());

            verify(em).createNamedQuery("Domanda.findAll", Domanda.class);
            verify(mockedQuery).setFirstResult(expectedFirstResult);
            verify(mockedQuery).setMaxResults(pageSize);
            verify(mockedQuery).getResultList();
        }

        //test findAllByQuiz

        @Test
        void findAllByQuiz_ShouldReturnEmptyFild_WhenQuizIsNull() {
            assertThrows(EmptyFild.class, () -> dao.findAllByQuiz(null));

            verifyNoInteractions(em);
        }

        @Test
        void findAllByQuiz_ShouldReturnEmptyFild_WhenQuizIdIsNull() {
            Quiz fakeQuiz = new Quiz();
            fakeQuiz.setId(null);

            assertThrows(EmptyFild.class, () -> dao.findAllByQuiz(fakeQuiz));

            verifyNoInteractions(em);
        }

        @Test
        void findAllByQuiz_ShouldReturnEmptyFild_WhenQuestionExist() {
            Quiz fakeQuiz = new Quiz();
            fakeQuiz.setId(0);

            assertThrows(EmptyFild.class, () -> dao.findAllByQuiz(fakeQuiz));

            verifyNoInteractions(em);
        }

        @Test
        void findAllByQuiz_ShouldReturnList_WhenQuizAndIdIsValid() {
            Quiz q = new Quiz();
            q.setId(1);

            List<Domanda> fintaLista = Arrays.asList(new Domanda(), new Domanda());
            TypedQuery<Domanda> mockedQuery = mock(TypedQuery.class);

            when(em.createNamedQuery("Domanda.findAllByQuiz", Domanda.class)).thenReturn(mockedQuery);
            when(mockedQuery.setParameter("quiz", q)).thenReturn(mockedQuery);
            when(mockedQuery.getResultList()).thenReturn(fintaLista);

            List<Domanda> result = dao.findAllByQuiz(q);

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(mockedQuery).getResultList();
        }

        @Test
        void findAllByQuiz_ShouldReturnEmptyList_WhenNoQuestionsExist() throws Exception {
            Quiz q = new Quiz();
            q.setId(1);

            TypedQuery<Domanda> mockedQuery = mock(TypedQuery.class);

            when(em.createNamedQuery("Domanda.findAllByQuiz", Domanda.class)).thenReturn(mockedQuery);
            when(mockedQuery.setParameter("quiz", q)).thenReturn(mockedQuery);
            when(mockedQuery.getResultList()).thenReturn(Collections.emptyList());

            List<Domanda> result = dao.findAllByQuiz(q);

            assertNotNull(result);
            assertTrue(result.isEmpty(), "La lista dovrebbe essere vuota");
            assertEquals(0, result.size());
        }

        //test delete

        @Test
        void delete_ShouldReturnEmptyFild_WhenDomandaIsNull() {
            assertThrows(EmptyFild.class, () -> dao.delete(null));

            verifyNoInteractions(em);
        }

        @Test
        void delete_ShouldReturnEmptyFild_WhenDomandaIdIsNull() {
            Domanda fakeDomanda = new Domanda();
            fakeDomanda.setId(null);

            assertThrows(EmptyFild.class, () -> dao.delete(fakeDomanda));

            verifyNoInteractions(em);
        }

        @Test
        void delete_ShouldReturnEmptyFild_WhenDomandaIdIsZeroOrNegative() {
            Domanda fakeDomanda = new Domanda();
            fakeDomanda.setId(0);

            assertThrows(EmptyFild.class, () -> dao.delete(fakeDomanda));

            verifyNoInteractions(em);
        }

        @Test
        void delete_shouldRemoveDomanda_whenDomandaIsValid() throws Exception {
            Domanda d = new Domanda();
            d.setId(1);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);
            when(em.merge(d)).thenReturn(d);

            dao.delete(d);

            verify(tx).begin();
            verify(em).merge(d);
            verify(em).remove(d);
            verify(tx).commit();
        }

        @Test
        void delete_shouldRollback_whenExceptionOccurs() {
            Domanda d = new Domanda();
            d.setId(1);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);
            when(tx.isActive()).thenReturn(true);

            // Simuliamo un errore durante il merge o la remove
            when(em.merge(any())).thenThrow(new RuntimeException("Database error"));

            assertThrows(UserNotFoundException.class, () -> dao.delete(d));

            verify(tx).begin();
            verify(tx).rollback();
            verify(tx, never()).commit();
        }

        // insert test

        @Test
        void insert_ShouldDomandaReturnEmptyFild_WhenDomadaIsNull() {
            assertThrows(EmptyFild.class, () -> dao.insert(null));

            verifyNoInteractions(em);
        }

        @Test
        void insert_ShouldDomandaReturnEmptyFild_WhenDomadaQuizIsNull() {
            Domanda domandaTest = new Domanda();
            domandaTest.setQuiz(null);

            assertThrows(EmptyFild.class, () -> dao.insert(domandaTest));

            verifyNoInteractions(em);
        }

        @Test
        void insert_ShouldDomandaReturnEmptyFild_WhenDomadaQuizIdisNull() {
            Quiz quizTest = new Quiz();
            Domanda domandaTest = new Domanda();
            quizTest.setId(null);
            domandaTest.setQuiz(quizTest);

            assertThrows(EmptyFild.class, () -> dao.insert(domandaTest));

            verifyNoInteractions(em);
        }

        @Test
        void insert_ShouldDomandaReturnEmptyFild_WhenDomadaQuizIdisZeroOrNegative() {
            Quiz quizTest = new Quiz();
            Domanda domandaTest = new Domanda();
            quizTest.setId(0);
            domandaTest.setQuiz(quizTest);

            assertThrows(EmptyFild.class, () -> dao.insert(domandaTest));

            verifyNoInteractions(em);
        }

        @Test
        void insert_ShouldPersistDomanda_WhenIsValid() {
            Quiz quizTest = new Quiz();
            Domanda domandaTest = new Domanda();
            quizTest.setId(1);
            domandaTest.setQuiz(quizTest);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);

            dao.insert(domandaTest);

            verify(tx).begin();
            verify(em).persist(domandaTest);
            verify(tx).commit();
        }

        @Test
        void insert_ShouldRollBack_WhenExceptionOccurs() {
            Quiz quizTest = new Quiz();
            Domanda domandaTest = new Domanda();
            quizTest.setId(1);
            domandaTest.setQuiz(quizTest);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);
            when(tx.isActive()).thenReturn(true);

            doThrow(new RuntimeException()).when(em).persist(domandaTest);

            assertThrows(AppException.class, () -> dao.insert(domandaTest));

            verify(tx).rollback();
            verify(tx, never()).commit();
        }

        //test upadate

        @Test
        void upadate_ShouldReturnEmptyFild_WhenDomandaIsNull() {
            assertThrows(EmptyFild.class, () -> dao.update(null));

            verifyNoInteractions(em);
        }

        @Test
        void upadate_ShouldReturnEmptyFild_WhenDomandaIdIsNull() {
            Domanda domandaTest = new Domanda();
            domandaTest.setId(null);

            assertThrows(EmptyFild.class, () -> dao.update(domandaTest));

            verifyNoInteractions(em);
        }

        @Test
        void upadate_ShouldReturnEmptyFild_WhenDomandaIdIsZeroOrNegative() {
            Domanda domandaTest = new Domanda();
            domandaTest.setId(0);

            assertThrows(EmptyFild.class, () -> dao.update(domandaTest));

            verifyNoInteractions(em);
        }

        @Test
        void update_ShouldThrowEntityNotFoundException_WhenDomandaDoesNotExistInDB() {
            Domanda domandaTest = new Domanda();
            domandaTest.setId(999);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);
            when(tx.isActive()).thenReturn(true);

            when(em.find(Domanda.class, 999)).thenReturn(null);

            assertThrows(EntityNotFoundException.class, () -> dao.update(domandaTest));

            verify(tx).rollback();
            verify(em, never()).merge(any());
        }

        @Test
        void update_ShouldRollback_WhenDatabaseErrorsOrEntity() {
            Domanda domandaTest = new Domanda();
            domandaTest.setId(1);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);
            when(tx.isActive()).thenReturn(true);

            when(em.find(Domanda.class, 1)).thenReturn(domandaTest);

            doThrow(new RuntimeException("DB Crash")).when(em).merge(any());

            assertThrows(AppException.class, () -> dao.update(domandaTest));

            verify(tx).rollback();
            verify(tx, never()).commit();
        }

        @Test
        void upadate_ShouldMergAndCommit_WhenDomandaIsValid() {
            Domanda domandaTest = new Domanda();
            domandaTest.setId(1);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);
            when(em.find(Domanda.class, 1)).thenReturn(domandaTest);

            dao.update(domandaTest);

            verify(tx).begin();
            verify(em).merge(domandaTest);
            verify(tx).commit();
            verify(tx, never()).rollback();
        }
    }

    // =========================
    // === INTEGRATION TESTS ===
    // =========================
    @Nested
    @Tag("integration")
    class IntegrationTests {
        EntityManagerFactory emf;
        Domanda testDomanda1, testDomanda2, testDomanda3;
        Risposta testRisposta1;
        Quiz quizTest, quizTest2;
        Utente testUser;

        @BeforeEach
        void setup() throws Exception {
            emf = Persistence.createEntityManagerFactory("testPU");
            em = emf.createEntityManager();

            dao = new DomandaDAO();
            injectEm(dao, em);

            ;

            em.getTransaction().begin();
            em.createQuery("DELETE FROM Risposta").executeUpdate();
            em.createQuery("DELETE FROM Domanda").executeUpdate();
            em.createQuery("DELETE FROM Quiz").executeUpdate();
            em.createQuery("DELETE FROM Utente").executeUpdate();

            testUser = creaUtenteDiTest("Mario", "Rossi", "mariorossi", "hash123");
            quizTest = creaQuizDiTest(testUser, "test", "test");
            quizTest2 = creaQuizDiTest(testUser, "test1", "test2");


            testDomanda1 = creaDomandaDiTest(quizTest, "come mi chiamo?");
            testRisposta1 = creaRispostaDiTest(testDomanda1, "Davide", false);

            testDomanda2 = creaDomandaDiTest(quizTest, "come ti chiami?");
            testDomanda3 = creaDomandaDiTest(quizTest2, "come si chiama?");

            em.persist(testUser);
            em.persist(quizTest);
            em.persist(quizTest2);
            em.persist(testDomanda1);
            em.persist(testDomanda2);
            em.persist(testDomanda3);

            em.getTransaction().commit();
            em.clear();
        }

        @AfterEach
        void tearDown() {
            if (em.isOpen()) em.close();
            if (emf.isOpen()) emf.close();
        }

        @Test
        @DisplayName("findById deve recuperare la Domanda tramite l'id")
        void findById_Integration() throws Exception {
            Integer id = testDomanda1.getId();

            Domanda result = dao.findById(id);

            assertNotNull(result);
            assertEquals("come mi chiamo?", testDomanda1.getQuesito());
        }



        @Test
        @DisplayName("findAll deve ritornare una lista paginata di Domande")
        void findAll_Integration() throws Exception {
            int pageNumber = 1, pageSize = 2;
            List<Domanda> page1 = dao.findAll(pageNumber, pageSize);

            assertNotNull(page1);
            assertEquals(pageSize, page1.size());

            List<Domanda> page2 = dao.findAll(pageNumber + 1, pageSize);

            assertNotNull(page2);
            assertEquals(1, page2.size());

            assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
        }

        @Test
        @DisplayName("findAllByQuiz deve ritornare una lista di Domande")
        void findAllByQuiz_Integration() throws Exception {
            List<Domanda> result = dao.findAllByQuiz(quizTest);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(result.get(0).getQuiz().getId(), quizTest.getId());
            assertEquals(result.get(1).getQuiz().getId(), quizTest.getId());
        }

        @Test
        @DisplayName("delete deve rimuovere tutti le domande e le risposte correlate")
        void delete_Integration() throws Exception {
            Integer idDomanda = testDomanda1.getId();
            Integer idRisposta = testRisposta1.getId();

            dao.delete(testDomanda1);

            em.clear();

            assertAll("Verifica cancellazione completa",
                    () -> assertNull(em.find(Domanda.class, idDomanda)),
                    () -> assertNull(em.find(Risposta.class, idRisposta))
            );
        }

        @Test
        @DisplayName("insert deve persistere una NUOVA domanda con risposte")
        void insert_Integration() throws Exception {
            Domanda newDomanda = creaDomandaDiTest(quizTest2, "voglio i biscotti?");
            Risposta newRisposta1 = creaRispostaDiTest(newDomanda, "si", false);
            Risposta newRisposta2 = creaRispostaDiTest(newDomanda, "no", true);

            dao.insert(newDomanda);

            Domanda recuperata = em.find(Domanda.class, newDomanda.getId());
            assertNotNull(recuperata);
            assertEquals(2, recuperata.getRisposte().size());
            assertNotNull(recuperata.getRisposte().get(0).getId());
            assertNotNull(recuperata.getRisposte().get(1).getId());
        }

        @Test
        @DisplayName("update deve aggiornare i dati della Domanda")
        void update_Integration() throws Exception {
            Domanda datiAggiornati = creaDomandaDiTest(quizTest, "come mi chiamavo?");
            Risposta rispostatest = creaRispostaDiTest(datiAggiornati, "Lucia", false);
            datiAggiornati.setId(testDomanda1.getId());

            dao.update(datiAggiornati);

            Domanda verificata = em.find(Domanda.class, testDomanda1.getId());

            assertAll("Verifica Aggiornamento",
                    () -> assertNotNull(verificata),
                    () -> assertEquals("come mi chiamavo?", verificata.getQuesito()),
                    () -> assertEquals(1, verificata.getRisposte().size())
            );
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
        private Utente creaUtenteDiTest(String nome, String cognome, String username, String pwd) {
            Utente u = new Utente();
            u.setNome(nome);
            u.setCognome(cognome);
            u.setUsername(username);
            u.setPasswordHash(pwd);

            u.setIsCreatore(true);
            u.setIsCompilatore(true);
            u.setIsManager(false);
            return u;
        }
    }

    // =========================
    // ===== UTIL METHOD =======
    // =========================
    private void injectEm(Object dao, EntityManager em) throws Exception {
        Class<?> clazz = dao.getClass();

        Field f = clazz.getDeclaredField("em");

        f.setAccessible(true);
        f.set(dao, em);
    }
}
