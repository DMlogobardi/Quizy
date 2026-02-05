package model.dao;

import jakarta.persistence.*;
import model.entity.Domanda;
import model.entity.Quiz;
import model.entity.Risposta;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuizDAOTest {

    EntityManager em;
    QuizDAO quizDAO;

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
            quizDAO = new QuizDAO();
            em = mockEm;
            injectEm(quizDAO, em);
        }

        /**
         * Category Partition per findById:
         * 1. Input Valido (ID esistente) -> Restituisce l'oggetto Quiz.
         * 2. Input Valido (ID inesistente) -> Restituisce null.
         * 3. Input Invalido (ID <= 0) -> Lancia eccezione.
         */

        @Test
        void findById_shouldReturnQuiz_whenIdExists() throws AppException {
            int id = 1;
            Quiz fintoQuiz = new Quiz();
            when(em.find(Quiz.class, id)).thenReturn(fintoQuiz);


            Quiz result = quizDAO.findById(id);

            // Assert
            assertNotNull(result);
            assertEquals(fintoQuiz, result);
            verify(em).find(Quiz.class, id);
        }

        @Test
        void findById_shouldReturnNull_whenIdDoesNotExist() throws AppException {
            int id = 999;
            when(em.find(Quiz.class, id)).thenReturn(null);

            Quiz result = quizDAO.findById(id);

            assertNull(result);
        }

        @Test
        void findById_shouldThrowException_whenIdIsInvalid() {
            assertThrows(AppException.class, () -> quizDAO.findById(0));

            verifyNoInteractions(em);
        }

        /**
         * Category Partition per findAll:
         * 1. Input Valido (pageNumber > 0, pageSize > 0) -> Restituisce lista di Quiz.
         * 2. Input Invalido (pageNumber <= 0) -> Lancia AppException.
         * 3. Input Invalido (pageSize <= 0) -> Lancia AppException.
         */

        @Test
        void findAll_shouldReturnList_whenInputsAreValid() throws AppException {
            // Arrange
            int pageNumber = 1;
            int pageSize = 10;
            List<Quiz> expectedList = new ArrayList<>();

            // Mock della catena di chiamate della TypedQuery
            TypedQuery<Quiz> mockedQuery = mock(TypedQuery.class);
            when(em.createNamedQuery("Quiz.findAll", Quiz.class)).thenReturn(mockedQuery);
            when(mockedQuery.setFirstResult(anyInt())).thenReturn(mockedQuery);
            when(mockedQuery.setMaxResults(anyInt())).thenReturn(mockedQuery);
            when(mockedQuery.getResultList()).thenReturn(expectedList);

            // Act
            List<Quiz> result = quizDAO.findAll(pageNumber, pageSize);

            // Assert
            assertNotNull(result);
            assertEquals(expectedList, result);
            verify(mockedQuery).setFirstResult(0); // (1 - 1) * 10 = 0
            verify(mockedQuery).setMaxResults(pageSize);
        }

        @Test
        void findAll_shouldThrowException_whenInputsAreInvalid() {
            assertThrows(AppException.class, () -> quizDAO.findAll(0, 10));
            assertThrows(AppException.class, () -> quizDAO.findAll(1, 0));
        }

        /**
         * Category Partition per findAllByUtente:
         * 1. Input Valido (pageNumber > 0 , Utente.id >0)
         * 2. Input invalido (pageNumber <=0)
         * 3. Input invalido (Utente.id <=0)
         * 4. Input invalido (Utente == null)
         */

        @Test
        void findAllByUtente_shouldReturnList_whenInputsAreValid() throws EntityNotFoundException, EmptyFild {
            int pageNumber = 1;
            Utente utenteFinto = new Utente();
            utenteFinto.setId(1);
            List<Quiz> expectedList = new ArrayList<>();
            TypedQuery<Quiz> mockedQuery = mock(TypedQuery.class);

            when(em.createNamedQuery("Quiz.findAllByUtente", Quiz.class)).thenReturn(mockedQuery);
            when(mockedQuery.setParameter(anyString(), any())).thenReturn(mockedQuery);
            when(mockedQuery.setFirstResult(anyInt())).thenReturn(mockedQuery);
            when(mockedQuery.setMaxResults(anyInt())).thenReturn(mockedQuery);
            when(mockedQuery.getResultList()).thenReturn(expectedList);

            List<Quiz> result = quizDAO.findAllByUtente(pageNumber, utenteFinto);

            assertNotNull(result);
            assertEquals(expectedList, result);
            verify(mockedQuery).setFirstResult(0);
        }

        //test failde per mancato controllo, inserito successivamente
        @Test
        void findAllByUtente_shouldThrowException_whenInputsAreInvalid() {
            TypedQuery<Quiz> mockedQuery = mock(TypedQuery.class);

            Utente utenteValido = new Utente();
            utenteValido.setId(1);

            Utente utenteIdZero = new Utente();
            utenteIdZero.setId(0);

            assertThrows(AppException.class, () -> quizDAO.findAllByUtente(0, utenteValido));
            assertThrows(EmptyFild.class, () -> quizDAO.findAllByUtente(1, utenteIdZero));
            assertThrows(EmptyFild.class, () -> quizDAO.findAllByUtente(1, null));
        }

        /**
         * Category Partition per insert:
         * 1. Input Valido (Quiz != null) -> Persist su DB.
         * 2. Input Invalido (Quiz == null) -> Lancia EmptyFild.
         * 3. Errore durante la transazione -> Lancia EntityNotFoundException e fa rollback.
         */

        @Test
        void insert_shouldPersistQuiz_whenQuizIsValid() throws EntityNotFoundException, EmptyFild {
            Quiz quiz = new Quiz();
            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);

            quizDAO.insert(quiz);

            verify(tx).begin();
            verify(em).persist(quiz);
            verify(tx).commit();
        }

        @Test
        void insert_shouldThrowEmptyFild_whenQuizIsNull() {
            assertThrows(EmptyFild.class, () -> quizDAO.insert(null));
        }

        @Test
        void insert_shouldThrowEntityNotFoundException_andRollback_whenExceptionOccurs() {
            Quiz quiz = new Quiz();
            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);
            doThrow(new RuntimeException("DB Error")).when(em).persist(quiz);
            when(tx.isActive()).thenReturn(true);

            assertThrows(EntityNotFoundException.class, () -> quizDAO.insert(quiz));

            verify(tx).begin();
            verify(em).persist(quiz);
            verify(tx).rollback();
        }

        /**
         * Category Partition per update:
         * 1. Input validi (quiz nuovo != null, quiz vecchio null, utente valido con id>0)
         * 2. Input validi (quiz nuovo != null, quiz vecchio != null, utente valido con id>0)
         * 3. Input invalido (quiz nuovo null) -> Non gestito esplicitamente nel codice ma implicito se quiz nuovo è usato
         * 4. Input invalido (quiz nuovo != null, quiz vecchio qualsiasi, utente null)
         * 5. Input invalido (quiz nuovo != null, quiz vecchio qualsiasi, utente.id null)
         * 6. Input invalido (quiz nuovo != null, quiz vecchio qualsiasi, utente.id =0) -> Non gestito esplicitamente nel codice
         */

        @Test
        void update_shouldUpdateQuiz_whenInputsAreValid_andOldQuizIsNull() throws EntityNotFoundException, EmptyFild {
            // Arrange
            Quiz newQuiz = new Quiz();
            newQuiz.setId(1);
            newQuiz.setTitolo("Nuovo Titolo");
            newQuiz.setDomande(new ArrayList<>());

            Utente utente = new Utente();
            utente.setId(1);

            Quiz oldQuizFromDb = new Quiz();
            oldQuizFromDb.setId(2);
            oldQuizFromDb.setDomande(new ArrayList<>());

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);

            TypedQuery<Quiz> mockedQuery = mock(TypedQuery.class);
            when(em.createQuery(anyString(), eq(Quiz.class))).thenReturn(mockedQuery);
            when(mockedQuery.setParameter(anyString(), any())).thenReturn(mockedQuery);
            when(mockedQuery.getResultStream()).thenReturn(Stream.of(oldQuizFromDb));

            // Act
            quizDAO.update(newQuiz, null, utente);

            // Assert
            verify(tx).begin();
            verify(em).merge(oldQuizFromDb);
            verify(tx).commit();
            assertEquals("Nuovo Titolo", oldQuizFromDb.getTitolo());
        }

        @Test
        void update_shouldUpdateQuiz_whenInputsAreValid_andOldQuizIsNotNull() throws EntityNotFoundException, EmptyFild {
            // Arrange
            Quiz newQuiz = new Quiz();
            newQuiz.setTitolo("Nuovo Titolo");
            newQuiz.setDomande(new ArrayList<>());

            Quiz oldQuiz = new Quiz();
            oldQuiz.setDomande(new ArrayList<>());

            Utente utente = new Utente();
            utente.setId(1);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);

            // Act
            quizDAO.update(newQuiz, oldQuiz, utente);

            // Assert
            verify(tx).begin();
            verify(em).merge(oldQuiz);
            verify(tx).commit();
            assertEquals("Nuovo Titolo", oldQuiz.getTitolo());
        }

        @Test
        void update_shouldThrowEmptyFild_whenUtenteIsNull() {
            Quiz newQuiz = new Quiz();
            assertThrows(EmptyFild.class, () -> quizDAO.update(newQuiz, null, null));
        }

        @Test
        void update_shouldThrowEmptyFild_whenUtenteIdIsNull() {
            Quiz newQuiz = new Quiz();
            Utente utente = new Utente();
            utente.setId(null);
            assertThrows(EmptyFild.class, () -> quizDAO.update(newQuiz, null, utente));
        }

        @Test
        void update_shouldThrowEntityNotFoundException_whenOldQuizNotFound() {
            Quiz newQuiz = new Quiz();
            newQuiz.setId(1);
            Utente utente = new Utente();
            utente.setId(1);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);

            TypedQuery<Quiz> mockedQuery = mock(TypedQuery.class);
            when(em.createQuery(anyString(), eq(Quiz.class))).thenReturn(mockedQuery);
            when(mockedQuery.setParameter(anyString(), any())).thenReturn(mockedQuery);
            when(mockedQuery.getResultStream()).thenReturn(Stream.empty());

            assertThrows(EntityNotFoundException.class, () -> quizDAO.update(newQuiz, null, utente));
            verify(tx).begin();
            if (tx != null && tx.isActive()) verify(tx).rollback();
        }

        /**
         * Category partition per delete:
         * 1. Input validi (idQuiz >0, quiz null, utente.id >0)
         * 2. Input validi (idQuiz >0, quiz != null,  utente.id >0)
         * 3. Input invalidi (idQuiz null)
         * 4. Input invalidi (utente null)
         * 5. Input invalidi (utente.id null)
         */

        @Test
        void delete_shouldRemoveQuiz_whenInputsAreValid() throws EntityNotFoundException, EmptyFild {
            int quizId = 1;
            Utente utente = new Utente();
            utente.setId(1);
            Quiz quizFromDb = new Quiz();
            quizFromDb.setId(quizId);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);

            TypedQuery<Quiz> mockedQuery = mock(TypedQuery.class);
            when(em.createQuery(anyString(), eq(Quiz.class))).thenReturn(mockedQuery);
            when(mockedQuery.setParameter(anyString(), any())).thenReturn(mockedQuery);
            when(mockedQuery.getResultStream()).thenReturn(Stream.of(quizFromDb));

            when(em.merge(quizFromDb)).thenReturn(quizFromDb);

            quizDAO.delete(quizId, utente);

            verify(tx).begin();
            verify(em).merge(quizFromDb);
            verify(em).remove(quizFromDb);
            verify(tx).commit();
        }

        @Test
        void delete_shouldThrowEntityNotFoundException_whenQuizNotFound() {
            int quizId = 1;
            Utente utente = new Utente();
            utente.setId(1);

            TypedQuery<Quiz> mockedQuery = mock(TypedQuery.class);
            when(em.createQuery(anyString(), eq(Quiz.class))).thenReturn(mockedQuery);
            when(mockedQuery.setParameter(anyString(), any())).thenReturn(mockedQuery);
            when(mockedQuery.getResultStream()).thenReturn(Stream.empty());

            assertThrows(EntityNotFoundException.class, () -> quizDAO.delete(quizId, utente));

            verify(em, never()).getTransaction();
        }

        @Test
        void delete_shouldThrowEmptyFild_whenUtenteIdIsInvalid() {
            Utente utente = new Utente();
            utente.setId(0); // Caso ID <= 0 che hai aggiunto nel metodo

            assertThrows(EmptyFild.class, () -> quizDAO.delete(1, utente));
        }

        @Test
        void delete_shouldThrowEmptyFild_whenUtenteIsNull() {
            assertThrows(EmptyFild.class, () -> quizDAO.delete(1, null));
        }

        @Test
        void delete_shouldThrowEmptyFild_whenUtenteIdIsNull() {
            Utente utente = new Utente();
            utente.setId(null);
            assertThrows(EmptyFild.class, () -> quizDAO.delete(1, utente));
        }

        @Test
        void delete_ShouldRollback_WhenDatabaseErrorOccurs() {
            Integer quizId = 5;
            Utente u = new Utente();
            u.setId(1);

            Quiz q = new Quiz();
            q.setId(quizId);
            q.setUtente(u);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);
            when(tx.isActive()).thenReturn(true);

            TypedQuery<Quiz> mockedQuery = mock(TypedQuery.class);
            when(em.createQuery(anyString(), eq(Quiz.class))).thenReturn(mockedQuery);
            when(mockedQuery.setParameter(anyString(), any())).thenReturn(mockedQuery);
            when(mockedQuery.getResultStream()).thenReturn(Stream.of(q));

            when(em.merge(q)).thenReturn(q);
            doThrow(new RuntimeException("Errore di vincolo DB")).when(em).remove(any());

            assertThrows(RuntimeException.class, () -> quizDAO.delete(quizId, u));

            verify(tx).begin();
            verify(tx).rollback();
            verify(tx, never()).commit();
        }
    }

    // =========================
    // === INTEGRATION TESTS ===
    // =========================
    @Nested
    @Tag("integration")
    class IntegrationTests {
        EntityManagerFactory emf;
        Quiz quizTest1, quizTest2, quizTest3;
        Utente testUser, testUser2;

        @BeforeEach
        void setup() throws Exception {
            emf = Persistence.createEntityManagerFactory("testPU");
            em = emf.createEntityManager();

            quizDAO = new QuizDAO();
            injectEm(quizDAO, em);

            // 1. Ordine di cancellazione corretto per evitare vincoli FK
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Risposta").executeUpdate();
            em.createQuery("DELETE FROM Domanda").executeUpdate();
            em.createQuery("DELETE FROM Quiz").executeUpdate();
            em.createQuery("DELETE FROM Utente").executeUpdate();

            testUser = creaUtenteDiTest("Mario", "Rossi", "mariorossi", "hash123");
            testUser2 = creaUtenteDiTest("Pippo", "Alberti", "pippo12", "sc2435");
            em.persist(testUser);
            em.persist(testUser2);

            // 2. Creazione Quiz con grafo completo
            quizTest1 = creaQuizDiTest(testUser, "matematica", "prima prova");
            Domanda d1 = creaDomandaDiTest(quizTest1, "2+2?");
            creaRispostaDiTest(d1, "4", true);

            quizTest2 = creaQuizDiTest(testUser, "geometria", "prova geometria");
            quizTest3 = creaQuizDiTest(testUser2, "funzioni", "prova funzioni");

            em.persist(quizTest1);
            em.persist(quizTest2);
            em.persist(quizTest3);

            em.getTransaction().commit();
            em.clear();
        }

        @AfterEach
        void tearDown() {
            if (em.isOpen()) em.close();
            if (emf.isOpen()) emf.close();
        }

        @Test
        @DisplayName("findById deve recuperare l'utente tramite l'id")
        void findById_Integration() throws Exception {
            Integer id = quizTest1.getId();

            Quiz result = quizDAO.findById(id);

            assertNotNull(result);
            assertEquals("matematica", result.getTitolo());
            assertFalse(result.getDomande().isEmpty());
        }

        @Test
        @DisplayName("findAll deve ritornare una lista paginata di quiz")
        void findAll_Integration() throws Exception {
            int pageNumber = 1, pageSize = 2;
            List<Quiz> page1 = quizDAO.findAll(pageNumber, pageSize);

            assertNotNull(page1);
            assertEquals(pageSize, page1.size());

            List<Quiz> page2 = quizDAO.findAll(pageNumber + 1, pageSize);

            assertNotNull(page2);
            assertEquals(1, page2.size());

            assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
        }

        @Test
        @DisplayName("findAllByUtente deve ritornare una lista paginata di quiz di uno specifico utente")
        void findAllByUtente_Integration() throws Exception {
            int pageNumber = 1;
            List<Quiz> page1 = quizDAO.findAllByUtente(pageNumber, testUser);

            assertNotNull(page1);
            assertEquals(2, page1.size());//perchè ne ho messi 2 in setup

            List<Quiz> page2 = quizDAO.findAllByUtente(pageNumber + 1, testUser);

            assertNotNull(page2);
            assertTrue(page2.isEmpty());
        }

        @Test
        @DisplayName("insert deve persistere un NUOVO quiz con domande")
        void insert_Integration() throws Exception {
            Quiz nuovoQuiz = creaQuizDiTest(testUser2, "Storia", "Quiz su Roma");
            // Aggancio bidirezionale
            Domanda d = creaDomandaDiTest(nuovoQuiz, "Anno fondazione?");
            creaRispostaDiTest(d, "753 AC", true);

            quizDAO.insert(nuovoQuiz);

            em.clear();

            Quiz recuperato = em.find(Quiz.class, nuovoQuiz.getId());
            assertNotNull(recuperato);
            // Verifico che la cascata abbia salvato anche i figli
            assertEquals(1, recuperato.getDomande().size());
            assertNotNull(recuperato.getDomande().get(0).getId());
        }

        @Test
        @DisplayName("update deve aggiornare i dati del Quiz")
        void update_Integration() throws Exception {
            Quiz datiAggiornati = creaQuizDiTest(testUser, "matematica1", "prima prova di matematica1");

            Domanda d = creaDomandaDiTest(datiAggiornati, "Quanto fa 3+3?");
            creaRispostaDiTest(d, "6", true);
            datiAggiornati.setId(quizTest1.getId());

            quizDAO.update(datiAggiornati, null, testUser);

            em.clear();

            Quiz verificato = em.find(Quiz.class, quizTest1.getId());

            assertAll("Verifica Aggiornamento",
                    () -> assertEquals("matematica1", verificato.getTitolo()),
                    () -> assertEquals("Quanto fa 3+3?", verificato.getDomande().get(0).getQuesito()),
                    () -> assertEquals(1, verificato.getDomande().size())
            );
        }

        @Test
        @DisplayName("delete deve rimuovere il quiz e tutte le domande/risposte collegate")
        void delete_Success_Integration() throws Exception {
            Integer idQuiz = quizTest1.getId();
            Integer idDomanda = quizTest1.getDomande().get(0).getId();
            Integer idRisposta = quizTest1.getDomande().get(0).getRisposte().get(0).getId();

            quizDAO.delete(idQuiz, testUser);

            em.clear();

            assertAll("Verifica cancellazione completa",
                    () -> assertNull(em.find(Quiz.class, idQuiz)),
                    () -> assertNull(em.find(Domanda.class, idDomanda)),
                    () -> assertNull(em.find(Risposta.class, idRisposta))
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
