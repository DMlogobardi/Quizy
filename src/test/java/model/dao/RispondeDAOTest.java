package model.dao;

import jakarta.persistence.*;
import model.entity.Domanda;
import model.entity.Risponde;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;
import model.exception.UserNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.when;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RispondeDAOTest {

    EntityManager em;
    RispondeDAO dao;

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
            dao = new RispondeDAO();
            em = mockEm;
            injectEm(dao, em);
        }

        //teset findById

        @Test
        void findById_ShouldReturnAppaException_WhenIdIsZeroOrNegative () {
            assertThrows(AppException.class, () -> dao.findById(0));

            verifyNoInteractions(em);
        }

        @Test
        void findById_ShouldReturnEntity_WhenIdExist() {
            int id = 1;
            Risponde risp = new Risponde();
            Mockito.when(em.find(Risponde.class, id)).thenReturn(risp);

            Risponde risposta = dao.findById(id);

            assertNotNull(risposta);
            assertEquals(risposta, risp);
            verify(em).find(Risponde.class, id);

        }

        @Test
        void findById_ShouldReturnNull_WhenIdNotExist() {
            int id = 999;
            Mockito.when(em.find(Risponde.class, id)).thenReturn(null);

            Risponde risposta = dao.findById(id);

            assertNull(risposta);
        }

        //test findAll

        @Test
        void findAll_ShouldReturnAppException_WhenPageSizeIsNegativeOrZero() {
            assertThrows(AppException.class, () -> dao.findAll(1, 0));

            verifyNoInteractions(em);
        }

        @Test
        void findAll_ShouldReturnAppException_WhenPageNumberIsNegativeOrZero() {
            assertThrows(AppException.class, () -> dao.findAll(0, 1));

            verifyNoInteractions(em);
        }

        @Test
        void findAll_ShouldReturnlist_WhenParameterAreValid() {
            int pageNumber = 2;
            int pageSize = 10;
            int expectedFirstResult = 10;

            List<Risponde> fintaLista = Arrays.asList(new Risponde(), new Risponde());

            TypedQuery<Risponde> mockedQuery = mock(TypedQuery.class);

            Mockito.when(em.createNamedQuery("Risponde.faindAll", Risponde.class)).thenReturn(mockedQuery);
            Mockito.when(mockedQuery.setFirstResult(anyInt())).thenReturn(mockedQuery);
            Mockito.when(mockedQuery.setMaxResults(anyInt())).thenReturn(mockedQuery);
            Mockito.when(mockedQuery.getResultList()).thenReturn(fintaLista);

            List<Risponde> result = dao.findAll(pageNumber, pageSize);

            verify(em).createNamedQuery("Risponde.faindAll", Risponde.class);
            verify(mockedQuery).setFirstResult(expectedFirstResult);
            verify(mockedQuery).setMaxResults(pageSize);
            verify(mockedQuery).getResultList();
        }

        //test findAllByUtente

        @Test
        void findAllByUtente_ShouldReturnEmptyFild_WhenUtenteIsNull() {
            assertThrows(EmptyFild.class, () -> dao.findAllByUtente(null,1,1));

            verifyNoInteractions(em);
        }

        @Test
        void findAllByUtente_ShouldReturnEmptyFild_WhenUtenteIdIsNull() {
            Utente test = new Utente();
            test.setId(null);

            assertThrows(EmptyFild.class, () -> dao.findAllByUtente(test,1,1));

            verifyNoInteractions(em);
        }

        @Test
        void findAllByUtente_ShouldReturnEmptyFild_WhenUtenteIdIsZeroOrNegative() {
            Utente test = new Utente();
            test.setId(0);

            assertThrows(EmptyFild.class, () -> dao.findAllByUtente(test,1,1));

            verifyNoInteractions(em);
        }

        @Test
        void findAllByUtente_ShouldReturnAppException_WhenPageNumerIsZeroOrNegative() {
            Utente test = new Utente();
            test.setId(1);

            assertThrows(AppException.class, () -> dao.findAllByUtente(test,0,1));

            verifyNoInteractions(em);
        }

        @Test
        void findAllByUtente_ShouldReturnAppException_WhenPageSizeIsZeroOrNegative() {
            Utente test = new Utente();
            test.setId(1);

            assertThrows(AppException.class, () -> dao.findAllByUtente(test,1,0));

            verifyNoInteractions(em);
        }

        @Test
        void findAllByUtente_ShouldReturnlist_WhenParameterAreValid() {
            int pageNumber = 2;
            int pageSize = 10;
            int expectedFirstResult = 10;
            Utente test = new Utente();
            test.setId(1);

            List<Risponde> fintaLista = Arrays.asList(new Risponde(), new Risponde());

            TypedQuery<Risponde> mockedQuery = mock(TypedQuery.class);

            Mockito.when(em.createNamedQuery("Risponde.faindAllByUtente", Risponde.class)).thenReturn(mockedQuery);
            Mockito.when(mockedQuery.setParameter("utente", test)).thenReturn(mockedQuery);
            Mockito.when(mockedQuery.setFirstResult(anyInt())).thenReturn(mockedQuery);
            Mockito.when(mockedQuery.setMaxResults(anyInt())).thenReturn(mockedQuery);
            Mockito.when(mockedQuery.getResultList()).thenReturn(fintaLista);

            List<Risponde> result = dao.findAllByUtente(test, pageNumber, pageSize);

            verify(em).createNamedQuery("Risponde.faindAllByUtente", Risponde.class);
            verify(mockedQuery).setParameter("utente", test);
            verify(mockedQuery).setFirstResult(expectedFirstResult);
            verify(mockedQuery).setMaxResults(pageSize);
            verify(mockedQuery).getResultList();
        }

        //test insert

        @Test
        void insert_ShouldreturnEmptyFild_WhenRispondeIsNull() {
            assertThrows(EmptyFild.class, () -> dao.insert(null));

            verifyNoInteractions(em);
        }

        @Test
        void insert_ShouldreturnEmptyFild_WhenRispondeUtenteIsNull() {
            Risponde ris = new Risponde();
            ris.setUtente(null);

            assertThrows(EmptyFild.class, () -> dao.insert(ris));

            verifyNoInteractions(em);
        }

        @Test
        void insert_ShouldreturnEmptyFild_WhenRispondeUtenteIdIsNull() {
            Risponde ris = new Risponde();
            Utente test = new Utente();
            test.setId(null);
            ris.setUtente(test);

            assertThrows(EmptyFild.class, () -> dao.insert(ris));

            verifyNoInteractions(em);
        }

        @Test
        void insert_ShouldreturnEmptyFild_WhenRispondeIsNegativeOrZero() {
            Risponde ris = new Risponde();
            Utente test = new Utente();
            test.setId(0);
            ris.setUtente(test);

            assertThrows(EmptyFild.class, () -> dao.insert(ris));

            verifyNoInteractions(em);
        }

        @Test
        void insert_ShuldPerisentity_WhenisValid(){
            Risponde ris = new Risponde();
            Utente test = new Utente();
            test.setId(1);
            ris.setUtente(test);

            EntityTransaction tx = mock(EntityTransaction.class);
            Mockito.when(em.getTransaction()).thenReturn(tx);

            dao.insert(ris);

            verify(tx).begin();
            verify(em).persist(ris);
            verify(tx).commit();
        }

        @Test
        void insert_ShouldRollBack_WhenExceptionOccurs() {
            Risponde ris = new Risponde();
            Utente test = new Utente();
            test.setId(1);
            ris.setUtente(test);

            EntityTransaction tx = mock(EntityTransaction.class);
            Mockito.when(em.getTransaction()).thenReturn(tx);
            Mockito.when(tx.isActive()).thenReturn(true);

            doThrow(new RuntimeException()).when(em).persist(ris);

            assertThrows(AppException.class, () -> dao.insert(ris));

            verify(tx).rollback();
            verify(tx, never()).commit();
        }

        //test insertAll

        @Test
        void insertAll_ShouldReturnEmptyFild_WhenListIsNull() {
            assertThrows(EmptyFild.class, ()-> dao.insertAll(null));

            verifyNoInteractions(em);
        }

        @Test
        void insertAll_ShouldReturnEmptyFild_WhenListIsEmpty() {
            List<Risponde> test = new ArrayList<>();

            assertThrows(EmptyFild.class, ()-> dao.insertAll(test));

            verifyNoInteractions(em);
        }

        @Test
        void insertAll_ShouldReturnAppException_WhenRispondeIsNull() {
            List<Risponde> test = new ArrayList<>();
            Risponde ris = null;
            test.add(ris);

            EntityTransaction tx = mock(EntityTransaction.class);
            Mockito.when(em.getTransaction()).thenReturn(tx);
            Mockito.when(tx.isActive()).thenReturn(true);

            assertThrows(AppException.class, ()-> dao.insertAll(test));
        }

        @Test
        void insertAll_ShouldReturnAppException_WhenRispondeUtenteIsNull() {
            List<Risponde> test = new ArrayList<>();
            Risponde ris = new Risponde();
            ris.setUtente(null);
            test.add(ris);

            EntityTransaction tx = mock(EntityTransaction.class);
            Mockito.when(em.getTransaction()).thenReturn(tx);
            Mockito.when(tx.isActive()).thenReturn(true);

            assertThrows(AppException.class, ()-> dao.insertAll(test));

        }

        @Test
        void insertAll_ShouldReturnAppException_WhenRispondeUtenteIdIsNull() {
            List<Risponde> test = new ArrayList<>();
            Risponde ris = new Risponde();
            Utente userTest = new Utente();
            userTest.setId(null);
            ris.setUtente(userTest);
            test.add(ris);

            EntityTransaction tx = mock(EntityTransaction.class);
            Mockito.when(em.getTransaction()).thenReturn(tx);
            Mockito.when(tx.isActive()).thenReturn(true);

            assertThrows(AppException.class, ()-> dao.insertAll(test));

        }

        @Test
        void insertAll_ShouldReturnAppException_WhenRispondeUtenteIdIsZeroOrNegative() {
            List<Risponde> test = new ArrayList<>();
            Risponde ris = new Risponde();
            Utente userTest = new Utente();
            userTest.setId(0);
            ris.setUtente(userTest);
            test.add(ris);

            EntityTransaction tx = mock(EntityTransaction.class);
            Mockito.when(em.getTransaction()).thenReturn(tx);
            Mockito.when(tx.isActive()).thenReturn(true);

            assertThrows(AppException.class, ()-> dao.insertAll(test));
        }

        @Test
        void insertAllShouldRollBack_WhenExceptionOccurs() {
            List<Risponde> test = new ArrayList<>();
            Risponde ris = new Risponde();
            Utente userTest = new Utente();
            userTest.setId(1);
            ris.setUtente(userTest);
            test.add(ris);

            EntityTransaction tx = mock(EntityTransaction.class);
            Mockito.when(em.getTransaction()).thenReturn(tx);
            Mockito.when(tx.isActive()).thenReturn(true);

            doThrow(new RuntimeException()).when(em).persist(test);

            assertThrows(AppException.class, () -> dao.insertAll(test));

            verify(tx).rollback();
            verify(tx, never()).commit();
        }

        @Test
        void insertAll_ShouldPersistAllAndCommit_WhenListIsValid() throws AppException, EmptyFild {
            Utente utenteValido = new Utente();
            utenteValido.setId(1);

            Risponde r1 = new Risponde();
            r1.setUtente(utenteValido);

            Risponde r2 = new Risponde();
            r2.setUtente(utenteValido);

            List<Risponde> listaRisposte = Arrays.asList(r1, r2);

            EntityTransaction tx = mock(EntityTransaction.class);
            Mockito.when(em.getTransaction()).thenReturn(tx);

            dao.insertAll(listaRisposte);

            verify(tx).begin();
            verify(em).persist(r1);
            verify(em).persist(r2);
            verify(tx).commit();
            verify(tx, never()).rollback();
        }

        //test update

        @Test
        void update_ShouldreturnEmptyFild_WhenEntityIsNuLL () {
            assertThrows(EmptyFild.class, () -> dao.update(null));

            verifyNoInteractions(em);
        }

        @Test
        void update_ShouldreturnEmptyFild_WhenEntityIdIsNuLL () {
            Risponde test = new Risponde();
            test.setId(null);

            assertThrows(EmptyFild.class, () -> dao.update(test));

            verifyNoInteractions(em);
        }

        @Test
        void update_ShouldreturnEmptyFild_WhenEntityIdIsZeroOrNegative () {
            Risponde test = new Risponde();
            test.setId(0);

            assertThrows(EmptyFild.class, () -> dao.update(null));

            verifyNoInteractions(em);
        }

        @Test
        void update_ShouldThrowEntityNotFoundException_WhenEntityDoesNotExistInDB() {
            Risponde test = new Risponde();
            test.setId(999);

            EntityTransaction tx = mock(EntityTransaction.class);
            Mockito.when(em.getTransaction()).thenReturn(tx);
            Mockito.when(tx.isActive()).thenReturn(true);

            Mockito.when(em.find(Risponde.class, 999)).thenReturn(null);

            assertThrows(EntityNotFoundException.class, () -> dao.update(test));

            verify(tx).rollback();
            verify(em, never()).merge(any());
        }

        @Test
        void upadate_ShouldMergAndCommit_WhenDEntityIsValid() {
            Risponde test = new Risponde();
            test.setId(1);

            EntityTransaction tx = mock(EntityTransaction.class);
            Mockito.when(em.getTransaction()).thenReturn(tx);
            Mockito.when(em.find(Risponde.class, 1)).thenReturn(test);

            dao.update(test);

            verify(tx).begin();
            verify(em).merge(test);
            verify(tx).commit();
            verify(tx, never()).rollback();
        }

        //test delete

        @Test
        void delete_ShouldReturnEmptyFild_WhenEntityIsNull(){
            assertThrows(EmptyFild.class, () -> dao.delete(null));

            verifyNoInteractions(em);
        }

        @Test
        void delete_ShouldReturnEmptyFild_WhenEntityIdIsNull(){
            Risponde test = new Risponde();
            test.setId(null);

            assertThrows(EmptyFild.class, () -> dao.delete(test));

            verifyNoInteractions(em);
        }

        @Test
        void delete_ShouldReturnEmptyFild_WhenEntityIdIsNZeroOrNegative(){
            Risponde test = new Risponde();
            test.setId(0);

            assertThrows(EmptyFild.class, () -> dao.delete(test));

            verifyNoInteractions(em);
        }

        @Test
        void delete_ShouldThrowUserNotFoundException_WhenExceptionOccurs() {
            Risponde test = new Risponde();
            test.setId(1);

            EntityTransaction tx = mock(EntityTransaction.class);
            Mockito.when(em.getTransaction()).thenReturn(tx);
            Mockito.when(tx.isActive()).thenReturn(true);

            Mockito.when(em.merge(any())).thenThrow(new RuntimeException("Database error"));

            assertThrows(UserNotFoundException.class, () -> dao.delete(test));

            verify(tx).begin();
            verify(tx).rollback();
            verify(tx, never()).commit();
        }

        @Test
        void delete_shouldRemoveEntity_whenEntityIsValid() {
            Risponde test = new Risponde();
            test.setId(1);

            EntityTransaction tx = mock(EntityTransaction.class);
            Mockito.when(em.getTransaction()).thenReturn(tx);
            Mockito.when(em.merge(test)).thenReturn(test);

            dao.delete(test);

            verify(tx).begin();
            verify(em).merge(test);
            verify(em).remove(test);
            verify(tx).commit();
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

            dao = new RispondeDAO();
            injectEm(dao, em);

            em.getTransaction().begin();
            em.createQuery("DELETE FROM Risponde").executeUpdate();

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
