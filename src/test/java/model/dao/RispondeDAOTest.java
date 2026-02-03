package model.dao;

import jakarta.persistence.*;
import model.entity.*;
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

            Mockito.when(em.find(eq(Risponde.class), any())).thenThrow(new RuntimeException("Database error"));

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
            Mockito.when(em.find(Risponde.class, 1)).thenReturn(test);

            dao.delete(test);

            verify(tx).begin();
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
        Utente u1,u2;
        Risponde ris1, ris2, ris3;

        @BeforeEach
        void setup() throws Exception {
            emf = Persistence.createEntityManagerFactory("testPU");
            em = emf.createEntityManager();

            dao = new RispondeDAO();
            injectEm(dao, em);

            em.getTransaction().begin();
            em.createQuery("DELETE FROM Risposta").executeUpdate();
            em.createQuery("DELETE FROM Domanda").executeUpdate();
            em.createQuery("DELETE FROM Quiz").executeUpdate();
            em.createQuery("DELETE FROM Utente").executeUpdate();
            em.createQuery("DELETE FROM Risponde").executeUpdate();

            u1 = creaUtenteEsempio("fnaf");
            u2 = creaUtenteEsempio("mario.rossi");

            ris1 = creaRispondeCompleto(u1, "matecatica-1");
            ris2 = creaRispondeCompleto(u1, "matecatica-3");
            ris3 = creaRispondeCompleto(u2, "matecatica-45");

            em.persist(ris1);
            em.persist(ris2);
            em.persist(ris3);

            em.getTransaction().commit();
            em.clear();
        }

        @AfterEach
        void tearDown() {
            if (em.isOpen()) em.close();
            if (emf.isOpen()) emf.close();
        }

        @Test
        @DisplayName("findById deve recuperare l'Entity tramite l'id")
        void findById_Integration() throws Exception {
            Integer id = ris1.getId();

            Risponde result = dao.findById(id);

            assertNotNull(result);
            assertEquals("matecatica-1", result.getQuiz());
        }

        @Test
        @DisplayName("findAll deve ritornare una lista paginata di Entity")
        void findAll_Integration() throws Exception {
            int pageNumber = 1, pageSize = 2;
            List<Risponde> page1 = dao.findAll(pageNumber, pageSize);

            assertNotNull(page1);
            assertEquals(pageSize, page1.size());

            List<Risponde> page2 = dao.findAll(pageNumber + 1, pageSize);

            assertNotNull(page2);
            assertEquals(1, page2.size());

            assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
        }

        @Test
        @DisplayName("findAllByUtente deve tornare una lista di entity")
        void findAllByUtente_Integration () {
            int pageNumber = 1, pageSize = 1;
            List<Risponde> page1 = dao.findAllByUtente(u1, pageNumber, pageSize);

            assertNotNull(page1);
            assertEquals(pageSize, page1.size());
            assertEquals(u1.getId(), page1.get(0).getUtente().getId());

            List<Risponde> page2 = dao.findAllByUtente(u1, pageNumber + 1, pageSize);

            assertNotNull(page2);
            assertEquals(pageSize, page2.size());
            assertEquals(u1.getId(), page2.get(0).getUtente().getId());

            assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
        }

        @Test
        @DisplayName("insert deve persistere una NUOVA Entity")
        void insert_Integration() throws Exception {
            em.getTransaction().begin();
            Risponde ris4 = creaRispondeCompleto(u2, "Fisica");
            em.getTransaction().commit();

            dao.insert(ris4);

            Risponde result = em.find(Risponde.class, ris4.getId());
            assertNotNull(result);
            assertEquals(ris4.getQuiz(), result.getQuiz());
        }

        @Test
        @DisplayName("insertAll deve persistere una NUOVA Lista di Entity")
        void insertAll_Integration() throws Exception {
            em.getTransaction().begin();
            Risponde ris4 = creaRispondeCompleto(u2, "Fisica");
            Risponde ris5 = creaRispondeCompleto(u1, "Storia5");
            em.getTransaction().commit();
            List<Risponde> inserimenti = new ArrayList<>();
            inserimenti.add(ris4);
            inserimenti.add(ris5);

            dao.insertAll(inserimenti);

            Risponde result1 = em.find(Risponde.class, ris4.getId());
            Risponde result2 = em.find(Risponde.class, ris5.getId());

            assertAll("verifica inserimento",
                    () -> assertNotNull(result1),
                    () -> assertEquals(ris4.getQuiz(), result1.getQuiz()),
                    () -> assertEquals(ris4.getUtente().getId(),  result1.getUtente().getId()),
                    () -> assertNotNull(result2),
                    () -> assertEquals(ris5.getQuiz(), result2.getQuiz()),
                    () -> assertEquals(ris5.getUtente().getId(),  result2.getUtente().getId())
            );
        }

        @Test
        @DisplayName("update deve aggiornare i dati dell'Entity")
        void update_Integration() throws Exception {
            em.getTransaction().begin();
            Risponde risUpadate = creaRispondeCompleto(u2, "Fisica");
            em.getTransaction().commit();
            risUpadate.setId(ris3.getId());

            dao.update(risUpadate);

            Risponde result = em.find(Risponde.class, ris3.getId());

            assertAll("verify aggiornamento",
                    () -> assertNotNull(result),
                    () -> assertEquals("Fisica", risUpadate.getQuiz())
            );
        }

        @Test
        @DisplayName("delete deve rimuovere solo il record Risponde e non le entità collegate")
        void delete_Integration() throws Exception {
            // 1. Recuperiamo i dati dall'oggetto esistente (ris3) creato nel setup
            Integer idRisponde = ris3.getId();
            Integer idUtente = ris3.getUtente().getId();
            Integer idRisposta = ris3.getRisposta().getId();

            // 2. Creiamo un oggetto "dummy" con solo l'ID per simulare il passaggio da controller
            Risponde test = new Risponde();
            test.setId(idRisponde);

            // 3. Esecuzione
            dao.delete(test);
            em.clear();

            // 4. Verifiche
            assertNull(em.find(Risponde.class, idRisponde), "Il record Risponde deve essere stato rimosso");

            assertAll("Verifica che le entità collegate siano ancora presenti",
                    () -> assertNotNull(em.find(Utente.class, idUtente), "L'utente deve esistere ancora"),
                    () -> assertNotNull(em.find(Risposta.class, idRisposta), "La risposta deve esistere ancora")
            );
        }

        public Utente creaUtenteEsempio(String username) {
            Utente u = new Utente(
                    "Mario",
                    "Rossi",
                    username,
                    "hash_password_123",
                    true,
                    true,
                    false
            );
            em.persist(u);
            return u;
        }

        public Risponde creaRispondeCompleto(Utente utente, String titoloQuiz) {
            if (utente.getId() == null) {
                em.persist(utente);
            }

            Quiz quiz = new Quiz();
            quiz.setUtente(utente);
            quiz.setTitolo(titoloQuiz);
            quiz.setDescrizione("Descrizione Test");
            quiz.setTempo("60");
            quiz.setDifficolta("Media");
            quiz.setNumeroDomande(1);
            em.persist(quiz);

            Domanda domanda = new Domanda();
            domanda.setQuiz(quiz);
            domanda.setQuesito("Domanda Test?");
            domanda.setPuntiRispostaCorretta(1);
            domanda.setPuntiRispostaSbagliata(0);
            em.persist(domanda);

            Risposta risposta = new Risposta();
            risposta.setDomanda(domanda);
            risposta.setAffermazione("Risposta Test");
            risposta.setFlagRispostaCorretta(true);
            em.persist(risposta);

            Fa tentativo = new Fa();
            tentativo.setUtente(utente);
            tentativo.setQuiz(quiz);
            tentativo.setPunteggio(0);
            em.persist(tentativo);

            Risponde risponde = new Risponde();
            risponde.setUtente(utente);
            risponde.setRisposta(risposta);
            risponde.setQuiz(titoloQuiz);
            risponde.setTentativo(tentativo);
            risponde.setSceltoIl(java.time.LocalDateTime.now());

            return risponde;
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
