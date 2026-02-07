package model.dao;

import jakarta.persistence.*;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;
import model.exception.RegisterFailed;
import model.exception.UserNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UtenteDAOTest {

    EntityManager em;
    UtenteDAO dao;

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
            dao = new UtenteDAO();
            em = mockEm;
            injectEm(dao, em);
        }

        //test findForLogin
        @Test
        void findForLogin_ShouldReturnUser_WhenUsernameExists() throws UserNotFoundException, EmptyFild {
            //creazione finto Utente
            String username = "pippo1234";
            Utente fintoUtente = new Utente();
            fintoUtente.setUsername(username);

            //mock query(non possiamo verificare la query senza db)
            TypedQuery<Utente> mockedQuery = mock(TypedQuery.class);
            when(em.createNamedQuery("Utente.login", Utente.class)).thenReturn(mockedQuery);
            when(mockedQuery.setParameter("username", username)).thenReturn(mockedQuery);
            when(mockedQuery.getSingleResult()).thenReturn(fintoUtente);

            Utente result = dao.findForLogin(username);

            assertNotNull(result);
            assertEquals(username, result.getUsername());
            verify(em).createNamedQuery("Utente.login", Utente.class);
        }

        @Test
        void findForLogin_ShouldReturnEmptyFild_WhenUsernameIsEmpty() {
            assertThrows(EmptyFild.class, () -> {
                dao.findForLogin("");
            });

            verifyNoInteractions(em);
        }

        @Test
        void findForLogin_ShouldReturnEmptyFild_WhenUsernameIsNull() {
            assertThrows(EmptyFild.class, () -> {
                dao.findForLogin(null);
            });

            verifyNoInteractions(em);
        }

        @Test
        void findForLogin_ShouldReturnUser_WhenUsernameNotExists() {
            String username = "utenteInesistente";
            TypedQuery<Utente> mockledQuary = mock(TypedQuery.class);

            when(em.createNamedQuery("Utente.login", Utente.class)).thenReturn(mockledQuary);
            when(mockledQuary.setParameter("username", username)).thenReturn(mockledQuary);

            when(mockledQuary.getSingleResult()).thenThrow(new NoResultException());

            UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
                dao.findForLogin(username);
            });
        }

        //test findAllPaginated
        @Test
        void findAllPaginated_ShouldReturnAppException_WhenPageNumerIs0() {
            AppException exception = assertThrows(AppException.class, () -> {
                dao.findAllPaginated(0, 1);
            });

            verifyNoInteractions(em);
        }

        @Test
        void findAllPaginated_ShouldReturnAppException_WhenPageSizeIs0() {
            AppException exception = assertThrows(AppException.class, () -> {
                dao.findAllPaginated(1, 0);
            });

            verifyNoInteractions(em);
        }

        @Test
        void findAllPaginated_ShuldCalcutateRightOffset() {
            int page = 2;
            int size = 10;
            int oracolo = 10;

            TypedQuery<Utente> mockedQuery = mock(TypedQuery.class);
            when(em.createNamedQuery("Utente.findAll", Utente.class)).thenReturn(mockedQuery);
            when(mockedQuery.setFirstResult(anyInt())).thenReturn(mockedQuery);
            when(mockedQuery.setMaxResults(anyInt())).thenReturn(mockedQuery);
            when(mockedQuery.getResultList()).thenReturn(List.of(new Utente()));

            dao.findAllPaginated(page, size);

            verify(mockedQuery).setFirstResult(oracolo);
            verify(mockedQuery).setMaxResults(size);
        }

        //findById test
        @Test
        void findById_ShouldReturnAppException_whenIdisInvalid() {
            AppException exception = assertThrows(AppException.class, () -> {
                dao.findById(0);
            });

            verifyNoInteractions(em);
        }

        @Test
        void findById_ShouldReturnUtente_whenIdisValid() {
            int id = 1;
            Utente fintoUtente = new Utente();
            fintoUtente.setId(id);

            TypedQuery<Utente> mockedQuery = mock(TypedQuery.class);
            when(em.createNamedQuery("Utente.findById", Utente.class)).thenReturn(mockedQuery);
            when(mockedQuery.setParameter("id", id)).thenReturn(mockedQuery);
            when(mockedQuery.getSingleResult()).thenReturn(fintoUtente);

            Utente result = dao.findById(id);

            assertEquals(fintoUtente, result);
            verify(mockedQuery).getSingleResult();
        }

        @Test
        void findById_ShouldThrowUserNotFoundException_WhenIdDoesNotExist() {
            Integer id = 999;
            TypedQuery<Utente> mockedQuery = mock(TypedQuery.class);

            when(em.createNamedQuery("Utente.findById", Utente.class)).thenReturn(mockedQuery);

            // Usa any() o il tipo specifico per essere sicuri che il mock risponda
            when(mockedQuery.setParameter(eq("id"), any(Integer.class))).thenReturn(mockedQuery);

            when(mockedQuery.getSingleResult()).thenThrow(new NoResultException());

            assertThrows(UserNotFoundException.class, () -> {
                dao.findById(id);
            });
        }

        //test register
        @Test
        void register_ShuldReturnRegisterFailed_whenUtenteIsNull() {
            RegisterFailed exception = assertThrows(RegisterFailed.class, () -> {
                dao.register(null);
            });

            verifyNoInteractions(em);
        }

        @Test
        void register_ShouldPersistUser_WhenDataIsValid() throws RegisterFailed {
            Utente u = new Utente();
            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);


            dao.register(u);

            verify(tx).begin();
            verify(em).persist(u);
            verify(tx).commit();
        }

        @Test
        void register_ShouldRollback_WhenExceptionOccurs() {
            Utente u = new Utente();
            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);
            when(tx.isActive()).thenReturn(true);

            doThrow(new RuntimeException()).when(em).persist(u);

            assertThrows(RegisterFailed.class, () -> dao.register(u));

            verify(tx).rollback();
            verify(tx, never()).commit();
        }

        //test update

        @Test
        void update_ShouldReturnEmptyFild_WhenUtenteIsNull() {
            assertThrows(EmptyFild.class, () -> {
                dao.update(null);
            });
        }

        @Test
        void update_ShouldReturnEmptyFild_WhenUtenteIdIsNull() {
            Utente u = new Utente();
            u.setId(null);

            assertThrows(EmptyFild.class, () -> {
                dao.update(u);
            });
        }

        @Test
        void update_ShouldReturnEmptyFild_WhenUtenteIdIsZero() {
            Utente u = new Utente();
            u.setId(0);

            assertThrows(EmptyFild.class, () -> {
                dao.update(u);
            });
        }

        @Test
        void update_ShouldMergeAndCommit_WhenUserIsValid() throws EmptyFild, AppException {
            Utente u = new Utente();
            u.setId(1);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);
            when(em.find(Utente.class, 1)).thenReturn(u);

            dao.update(u);

            verify(tx).begin();
            verify(em).merge(u);
            verify(tx).commit();
            verify(tx, never()).rollback();
        }

        @Test
        void update_ShouldRollback_WhenDatabaseErrorsOrEntity() {
            Utente u = new Utente();
            u.setId(1);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);
            when(tx.isActive()).thenReturn(true);

            when(em.find(Utente.class, 1)).thenReturn(u);

            doThrow(new RuntimeException("DB Crash")).when(em).merge(any());

            assertThrows(AppException.class, () -> dao.update(u));

            verify(tx).rollback();
            verify(tx, never()).commit();
        }

        @Test
        void update_ShouldThrowEntityNotFoundException_WhenUserDoesNotExistInDB() {
            Utente u = new Utente();
            u.setId(999);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);
            when(tx.isActive()).thenReturn(true);

            when(em.find(Utente.class, 999)).thenReturn(null);

            assertThrows(EntityNotFoundException.class, () -> dao.update(u));

            verify(tx).rollback();
            verify(em, never()).merge(any());
        }

        //test delete
        @Test
        void delete_ShouldRemoveUser_WhenUserIsValid() throws Exception {
            Utente u = new Utente();
            u.setId(1);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);

            when(em.merge(u)).thenReturn(u);

            dao.delete(u);

            verify(tx).begin();
            verify(em).merge(u);
            verify(em).remove(u);
            verify(tx).commit();
        }

        @Test
        void delete_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
            Utente u = new Utente();
            u.setId(999);

            EntityTransaction tx = mock(EntityTransaction.class);
            when(em.getTransaction()).thenReturn(tx);
            when(tx.isActive()).thenReturn(true);

            when(em.merge(any())).thenThrow(new jakarta.persistence.EntityNotFoundException());

            assertThrows(UserNotFoundException.class, () -> dao.delete(u));

            verify(tx).rollback();
            verify(tx, never()).commit();
        }

        @Test
        void delete_ShouldThrowEmptyFild_WhenIdIsInvalid() {
            Utente u = new Utente();
            u.setId(0);

            assertThrows(EmptyFild.class, () -> dao.delete(u));
            verifyNoInteractions(em);
        }

        @Test
        void delete_ShouldThrowEmptyFild_WhenIdIsNull() {
            Utente u = new Utente();
            u.setId(null);

            assertThrows(EmptyFild.class, () -> dao.delete(u));
            verifyNoInteractions(em);
        }

        @Test
        void delete_ShouldThrowEmptyFild_WhenUtenteIsInvalid() {
            Utente u = null;

            assertThrows(EmptyFild.class, () -> dao.delete(u));
            verifyNoInteractions(em);
        }
    }

    // =========================
    // === INTEGRATION TESTS ===
    // =========================
    @Nested
    @Tag("integration")
    class IntegrationTests {
        EntityManagerFactory emf;
        Utente testUser, testUser2, testUser3;

        @BeforeEach
        void setup() throws Exception {
            emf = Persistence.createEntityManagerFactory("testPU");
            em = emf.createEntityManager();

            dao = new UtenteDAO();
            injectEm(dao, em);

            em.getTransaction().begin();
            em.createQuery("DELETE FROM Utente").executeUpdate();

            testUser = creaUtenteDiTest("Mario", "Rossi", "mariorossi", "hash123");
            testUser2 = creaUtenteDiTest("Pippo", "Alberti", "pippo12", "sc2435");
            testUser3 = creaUtenteDiTest("Ciro", "Di Somma", "caef", "forzaNapoli25");
            em.persist(testUser);
            em.persist(testUser2);
            em.persist(testUser3);

            em.getTransaction().commit();
            em.clear();
        }

        @AfterEach
        void tearDown() {
            if (em.isOpen()) em.close();
            if (emf.isOpen()) emf.close();
        }

        @Test
        @DisplayName("findAllPaginated deve ritornare una lista paginata di utenti")
        void findAllPaginated_Integration() throws Exception {
            int pageNumber = 1, pageSize = 2;
            List<Utente> result = dao.findAllPaginated(pageNumber, pageSize);

            assertNotNull(result);
            assertEquals(pageSize, result.size());

            List<Utente> secondaPagina = dao.findAllPaginated(pageNumber + 1, pageSize);

            assertNotNull(secondaPagina);
            assertEquals(1, secondaPagina.size());//perche ho definito solo 3 uternti di test

            assertNotEquals(result.get(0).getId(), secondaPagina.get(0).getId());
        }

        @Test
        @DisplayName("findForLogin deve trovare un utente esistente")
        void findForLogin_Integration() throws Exception {
            Utente result = dao.findForLogin("mariorossi");

            assertNotNull(result);
            assertEquals("Mario", result.getNome());
            assertEquals("Rossi", result.getCognome());

            assertNotNull(result.getId());
            assertTrue(result.getId() > 0);
        }

        @Test
        @DisplayName("findById deve recuperare l'utente tramite ID")
        void findById_Integration() throws Exception {
            Integer idGenerato = testUser.getId();

            Utente result = dao.findById(idGenerato);

            assertNotNull(result);
            assertEquals("mariorossi", result.getUsername());
        }

        @Test
        @DisplayName("register deve persistere un NUOVO utente valido")
        void register_Integration() throws Exception {
            Utente nuovo = creaUtenteDiTest("Luigi", "Verdi", "luigiverdi", "pass456");

            dao.register(nuovo);

            em.clear();

            Utente recuperato = em.find(Utente.class, nuovo.getId());
            assertNotNull(recuperato);
            assertEquals("Luigi", recuperato.getNome());
            assertEquals("Verdi", recuperato.getCognome());
        }

        @Test
        @DisplayName("update deve aggiornare i dati (es. cambio password o cognome)")
        void update_Integration() throws Exception {
            Utente daAggiornare = em.find(Utente.class, testUser.getId());

            daAggiornare.setCognome("Bianchi");
            daAggiornare.setPasswordHash("nuovoHashSegreto");

            dao.update(daAggiornare);

            em.clear();

            Utente verificato = em.find(Utente.class, testUser.getId());
            assertEquals("Bianchi", verificato.getCognome());
            assertEquals("nuovoHashSegreto", verificato.getPasswordHash());
            assertEquals("Mario", verificato.getNome()); // Questo non deve essere cambiato
        }

        @Test
        @DisplayName("delete deve rimuovere fisicamente l'utente")
        void delete_Integration() throws Exception {
            Utente daCancellare = em.find(Utente.class, testUser.getId());

            dao.delete(daCancellare);

            em.clear();

            Utente phantom = em.find(Utente.class, testUser.getId());
            assertNull(phantom, "L'utente dovrebbe essere null dopo la cancellazione");
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
    private void injectEm(UtenteDAO dao, EntityManager em) throws Exception {
        Field f = UtenteDAO.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(dao, em);
    }
}
