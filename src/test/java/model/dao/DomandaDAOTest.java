package model.dao;

import jakarta.persistence.*;
import model.entity.Domanda;
import model.entity.Quiz;
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

        @BeforeEach
        void setup() throws Exception {
            emf = Persistence.createEntityManagerFactory("testPU");
            em = emf.createEntityManager();

            dao = new DomandaDAO();
            injectEm(dao, em);

            em.getTransaction().begin();


            em.getTransaction().commit();
            em.clear();
        }

        @AfterEach
        void tearDown() {
            if (em.isOpen()) em.close();
            if (emf.isOpen()) emf.close();
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
