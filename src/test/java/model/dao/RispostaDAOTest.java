package model.dao;

import jakarta.persistence.*;
import model.entity.Domanda;
import model.entity.Quiz;
import model.entity.Risposta;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;
import model.exception.RegisterFailed;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RispostaDAOTest {

    EntityManager em;
    RispostaDAO dao;

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
            dao = new RispostaDAO();
            em = mockEm;
            injectEm(dao, em);
        }

        //test findAll
        @Test
        void findAll_ShouldReturnAppException_WhenPageSizeIsZeroOrNegative() {
            assertThrows(AppException.class, () -> dao.findAll(1, 0));

            verifyNoInteractions(em);
        }

        @Test
        void findAll_ShouldReturnAppException_WhenPageNumberIsZeroOrNegative() {
            assertThrows(AppException.class, () -> dao.findAll(0, 1));

            verifyNoInteractions(em);
        }

        @Test
        void findAll_ShouldReturnList_WhenParametersAreValid() throws AppException {
            int pageNumber = 2;
            int pageSize = 10;
            int expectedFirstResult = 10;

            List<Risposta> fintaLista = Arrays.asList(new Risposta(), new Risposta());

            TypedQuery<Risposta> mockedQuery = mock(TypedQuery.class);

            when(em.createNamedQuery("Risposta.faindAll", Risposta.class)).thenReturn(mockedQuery);
            when(mockedQuery.setFirstResult(anyInt())).thenReturn(mockedQuery);
            when(mockedQuery.setMaxResults(anyInt())).thenReturn(mockedQuery);
            when(mockedQuery.getResultList()).thenReturn(fintaLista);

            List<Risposta> result = dao.findAll(pageNumber, pageSize);

            assertNotNull(result);
            assertEquals(2, result.size());

            verify(em).createNamedQuery("Risposta.faindAll", Risposta.class);
            verify(mockedQuery).setFirstResult(expectedFirstResult);
            verify(mockedQuery).setMaxResults(pageSize);
            verify(mockedQuery).getResultList();
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
            Risposta fintaRisposta = new Risposta();
            fintaRisposta.setId(id);

            when(em.find(Risposta.class, id)).thenReturn(fintaRisposta);

            Risposta result = dao.findById(id);

            assertNotNull(result);
            assertEquals(result, fintaRisposta);
            verify(em).find(Risposta.class, id);
        }

        //test findByDomanda
        @Test
        void findByDomanda_ShouldReturnAppException_WhenPageSizeIsZeroOrNegative() {
            Domanda test = new Domanda();
            test.setId(1);

            assertThrows(AppException.class, () -> dao.findByDomanda(test, 1, 0));

            verifyNoInteractions(em);
        }

        @Test
        void findByDomanda_ShouldReturnAppException_WhenPageNumberIsZeroOrNegative() {
            Domanda test = new Domanda();
            test.setId(1);

            assertThrows(AppException.class, () -> dao.findByDomanda(test, 0, 1));

            verifyNoInteractions(em);
        }

        @Test
        void findByDomanda_ShouldReturnAppException_WhenUtenteIdIsZeroOrNegative() {
            Domanda test = new Domanda();
            test.setId(0);

            assertThrows(AppException.class, () -> dao.findByDomanda(test, 1, 1));

            verifyNoInteractions(em);
        }

        @Test
        void findByDomanda_ShouldReturnAppException_WhenUtenteIdIsNull() {
            Domanda test = new Domanda();
            test.setId(null);

            assertThrows(AppException.class, () -> dao.findByDomanda(test, 1, 1));

            verifyNoInteractions(em);
        }

        @Test
        void findByDomanda_ShouldReturnAppException_WhenUtenteIsNull() {
            assertThrows(AppException.class, () -> dao.findByDomanda(null, 1, 1));

            verifyNoInteractions(em);
        }

        @Test
        void findByDomanda_ShouldReturnList_WhenParameterIsValid() {
            Domanda domanda = new Domanda();
            domanda.setId(1);
            int pageNumber = 2;
            int pageSize = 5;

            List<Risposta> fintaLista = Arrays.asList(new Risposta(), new Risposta());
            TypedQuery<Risposta> mockedQuery = mock(TypedQuery.class);

            when(em.createNamedQuery("Risposta.faindAllByDomanda", Risposta.class)).thenReturn(mockedQuery);
            when(mockedQuery.setParameter("domanda", domanda)).thenReturn(mockedQuery);
            when(mockedQuery.setFirstResult(5)).thenReturn(mockedQuery);
            when(mockedQuery.setMaxResults(pageSize)).thenReturn(mockedQuery);
            when(mockedQuery.getResultList()).thenReturn(fintaLista);

            List<Risposta> result = dao.findByDomanda(domanda, pageNumber, pageSize);

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(mockedQuery).setParameter("domanda", domanda);
            verify(mockedQuery).setFirstResult(5);
            verify(mockedQuery).setMaxResults(5);
            verify(mockedQuery).getResultList();
        }

        //test insert
        @Test
        void insert_ShouldReturnEmptyFild_WhenRispostaIsNull() {
            assertThrows(EmptyFild.class, () -> dao.insert(null));

            verifyNoInteractions(em);
        }

        @Test
        void insert_ShouldReturnEmptyFild_WhenRispostaDomandaIsNull() {
            Risposta test = new Risposta();
            test.setDomanda(null);

            assertThrows(EmptyFild.class, () -> dao.insert(test));

            verifyNoInteractions(em);
        }

        @Test
        void insert_ShouldReturnEmptyFild_WhenRispostaDomandaIdIsNull() {
            Domanda domandaTest = new Domanda();
            Risposta test = new Risposta();
            domandaTest.setId(null);
            test.setDomanda(domandaTest);

            assertThrows(EmptyFild.class, () -> dao.insert(test));

            verifyNoInteractions(em);
        }

        @Test
        void insert_ShouldReturnEmptyFild_WhenRispostaDomandaIdIsZeroOrNegative() {
            Domanda domandaTest = new Domanda();
            Risposta test = new Risposta();
            domandaTest.setId(0);
            test.setDomanda(domandaTest);

            assertThrows(EmptyFild.class, () -> dao.insert(test));

            verifyNoInteractions(em);
        }

        @Test
        void insert_ShouldRollback_WhenExceptionOccurs() {
            Risposta r = new Risposta();
            Domanda d = new Domanda();
            d.setId(1);
            r.setDomanda(d);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);
            when(tx.isActive()).thenReturn(true);

            // Simuliamo un errore generico durante la persistenza
            doThrow(new RuntimeException("Database Error")).when(em).persist(r);

            assertThrows(AppException.class, () -> dao.insert(r));

            verify(tx).begin();
            verify(tx).rollback();
            verify(tx, never()).commit();
        }

        @Test
        void insert_ShouldPersist_WhenParametersAreValid() throws Exception {
            Risposta r = new Risposta();
            Domanda d = new Domanda();
            d.setId(1);
            r.setDomanda(d);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);

            dao.insert(r);

            verify(tx).begin();
            verify(em).persist(r);
            verify(tx).commit();
            verify(tx, never()).rollback();
        }

        //test update
        @Test
        void upadate_ShouldReturnEmptyFild_WhenRispostaIsNull() {
            assertThrows(EmptyFild.class, () -> dao.update(null));

            verifyNoInteractions(em);
        }

        @Test
        void upadate_ShouldReturnEmptyFild_WhenRispostaIdIsNull() {
            Risposta rispostaTest = new Risposta();
            rispostaTest.setId(null);

            assertThrows(EmptyFild.class, () -> dao.update(rispostaTest));

            verifyNoInteractions(em);
        }

        @Test
        void upadate_ShouldReturnEmptyFild_WhenRispostaIdIsZeroOrNegative() {
            Risposta rispostaTest = new Risposta();
            rispostaTest.setId(0);

            assertThrows(EmptyFild.class, () -> dao.update(rispostaTest));

            verifyNoInteractions(em);
        }

        @Test
        void update_ShouldRollback_WhenExceptionOccurs() {
            Risposta r = new Risposta();
            r.setId(1);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);
            when(tx.isActive()).thenReturn(true);

            doThrow(new RuntimeException("Database Error")).when(em).merge(r);

            assertThrows(AppException.class, () -> dao.update(r));

            verify(tx).begin();
            verify(tx).rollback();
            verify(tx, never()).commit();
        }

        @Test
        void update_ShouldMerge_WhenParametersAreValid() throws Exception {
            Risposta r = new Risposta();
            r.setId(1);
            r.setAffermazione("Risposta aggiornata");

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);

            dao.update(r);

            verify(tx).begin();
            verify(em).merge(r);
            verify(tx).commit();
            verify(tx, never()).rollback();
        }

        //test delete
        @Test
        void delete_ShouldReturnEmptyFild_WhenRispostaIsNull() {
            assertThrows(EmptyFild.class, () -> dao.delete(null));

            verifyNoInteractions(em);
        }

        @Test
        void delete_ShouldReturnEmptyFild_WhenRispostaIdIsNull() {
            Risposta rispostaTest = new Risposta();
            rispostaTest.setId(null);

            assertThrows(EmptyFild.class, () -> dao.delete(rispostaTest));

            verifyNoInteractions(em);
        }

        @Test
        void delete_ShouldReturnEmptyFild_WhenRispostaIdIsZeroOrNegative() {
            Risposta rispostaTest = new Risposta();
            rispostaTest.setId(0);

            assertThrows(EmptyFild.class, () -> dao.delete(rispostaTest));

            verifyNoInteractions(em);
        }

        @Test
        void delete_ShouldRollback_WhenExceptionOccurs() {
            Risposta r = new Risposta();
            r.setId(1);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);
            when(tx.isActive()).thenReturn(true);

            when(em.merge(r)).thenThrow(new RuntimeException("Database Error"));

            assertThrows(Exception.class, () -> dao.delete(r));

            verify(tx).begin();
            verify(tx).rollback();
            verify(tx, never()).commit();
        }

        @Test
        void delete_ShouldRemove_WhenParametersAreValid() throws Exception {
            Risposta r = new Risposta();
            r.setId(1);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);
            when(em.merge(r)).thenReturn(r);

            dao.delete(r);

            verify(tx).begin();
            verify(em).merge(r);
            verify(em).remove(r);
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
        Risposta risp1, risp2, risp3;

        @BeforeEach
        void setup() throws Exception {
            emf = Persistence.createEntityManagerFactory("testPU");
            em = emf.createEntityManager();

            dao = new RispostaDAO();
            injectEm(dao, em);

            em.getTransaction().begin();

            risp1 = creaRispostaCompleta("pippo", "matt1", "1+1=?", "2", true);
            risp1 = creaRispostaCompleta("pippo", "matt1", "1+1=?", "3", false);
            risp1 = creaRispostaCompleta("pippo", "matt2", "2x+5=?", "15", true);

            em.getTransaction().commit();
            em.clear();
        }

        @AfterEach
        void tearDown() {
            if (em.isOpen()) em.close();
            if (emf.isOpen()) emf.close();
        }

        private Risposta creaRispostaCompleta(String username, String titoloQuiz, String quesitoDomanda, String testoRisposta, boolean isCorretta) {
            Utente u = new Utente();
            u.setNome("Mario");
            u.setCognome("Rossi");
            u.setUsername(username);
            u.setPasswordHash("hash_pwd");
            u.setIsCreatore(true);
            u.setIsCompilatore(true);
            u.setIsManager(false);
            em.persist(u);

            Quiz q = new Quiz();
            q.setUtente(u);
            q.setTitolo(titoloQuiz);
            q.setDescrizione("Descrizione");
            q.setTempo("30 minuti");
            q.setDifficolta("Media");
            q.setNumeroDomande(1);
            q.setDomande(new ArrayList<>());
            em.persist(q);

            Domanda d = new Domanda();
            d.setQuesito(quesitoDomanda);
            d.setQuiz(q);
            d.setPuntiRispostaCorretta(1);
            d.setPuntiRispostaSbagliata(0);
            d.setRisposte(new ArrayList<>());
            q.getDomande().add(d);
            em.persist(d);

            Risposta r = new Risposta();
            r.setAffermazione(testoRisposta);
            r.setFlagRispostaCorretta(isCorretta);
            r.setDomanda(d);
            d.getRisposte().add(r);
            em.persist(r);

            return r;
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
