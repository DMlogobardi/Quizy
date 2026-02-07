package model.utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.entity.Quiz;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;
import model.mapper.EntityRefresher;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class QuizLogTest {

    QuizLog log;
    EntityRefresher refresher;

    // =========================
    // ===== UNIT TESTS ========
    // =========================
    @Nested
    @Tag("unit")
    class UnitTests {
        @Mock
        EntityRefresher refresherMocked;

        @BeforeEach
        void setup() throws Exception {
            log = new QuizLog();
            refresher = refresherMocked;
            injectRefresher(log, refresher);
        }

        //test aggiungi
        @Test
        void aggiungi_ShouldReturnEmptyFild_WhenUtenteIsNull() {
            Quiz q = new Quiz();
            q.setId(1);
            assertThrows(EmptyFild.class, () -> log.aggiungi(null, q));
        }

        @Test
        void aggiungi_ShouldReturnEmptyFild_WhenUtenteIdIsNull() {
            Quiz q = new Quiz();
            Utente u = new Utente();
            q.setId(1);
            u.setId(null);

            assertThrows(EmptyFild.class, () -> log.aggiungi(u , q));
        }

        @Test
        void aggiungi_ShouldReturnEmptyFild_WhenUtenteIdIsZeroOrNegative() {
            Quiz q = new Quiz();
            Utente u = new Utente();
            q.setId(1);
            u.setId(0);

            assertThrows(EmptyFild.class, () -> log.aggiungi(u , q));
        }

        @Test
        void aggiungi_ShouldReturnEmptyFild_WhenQuizIsNull() {
            Utente u = new Utente();
            u.setId(1);
            assertThrows(EmptyFild.class, () -> log.aggiungi(u , null));
        }

        @Test
        void aggiungi_ShouldReturnEmptyFild_WhenQuizIdIsNull() {
            Quiz q = new Quiz();
            Utente u = new Utente();
            q.setId(null);
            u.setId(1);

            assertThrows(EmptyFild.class, () -> log.aggiungi(u , q));
        }

        @Test
        void aggiungi_ShouldReturnEmptyFild_WhenQuizIdIsZeroOrNegative() {
            Quiz q = new Quiz();
            Utente u = new Utente();
            q.setId(0);
            u.setId(1);

            assertThrows(EmptyFild.class, () -> log.aggiungi(u , q));
        }

        @Test
        void aggiungi_ShouldUpdateInternalMap_WhenDataIsValid() throws Exception {
            Utente u = new Utente();
            u.setId(1);
            Quiz q = new Quiz();
            q.setId(10);

            log.aggiungi(u, q);

            Field field = QuizLog.class.getDeclaredField("logQuizBible");
            field.setAccessible(true);
            Map<Utente, Map<Integer, Quiz>> internalMap = (Map<Utente, Map<Integer, Quiz>>) field.get(log);

            assertTrue(internalMap.containsKey(u));
            assertTrue(internalMap.get(u).containsKey(q.getId()));
            assertEquals(q, internalMap.get(u).get(q.getId()));
        }

        //test getQuiz
        @Test
        void getQuiz_ShouldReturnEmptyFild_WhenUtenteIsNull() {
            assertThrows(EmptyFild.class ,() -> log.getQuiz(null));
        }

        @Test
        void getQuiz_ShouldThrowAppException_WhenUserHasNoQuizzes() {
            Utente u = new Utente();
            u.setId(1);

            assertThrows(AppException.class, () -> log.getQuiz(u));
        }

        @Test
        void getQuiz_List_ShouldReturnList_WhenQuizzesArePresent() throws Exception {
            Utente u = new Utente();
            u.setId(1);
            Quiz q1 = new Quiz();
            q1.setId(101);
            Quiz q2 = new Quiz();
            q2.setId(102);

            Map<Integer, Quiz> fakeQuizzes = new java.util.concurrent.ConcurrentHashMap<>();
            fakeQuizzes.put(101, q1);
            fakeQuizzes.put(102, q2);

            Field field = QuizLog.class.getDeclaredField("logQuizBible");
            field.setAccessible(true);
            Map<Utente, Map<Integer, Quiz>> internalMap = (Map<Utente, Map<Integer, Quiz>>) field.get(log);
            internalMap.put(u, fakeQuizzes);

            Mockito.when(refresherMocked.reattach(q1)).thenReturn(q1);
            Mockito.when(refresherMocked.reattach(q2)).thenReturn(q2);

            List<Quiz> result = log.getQuiz(u);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains(q1));
            assertTrue(result.contains(q2));
            Mockito.verify(refresherMocked, Mockito.times(1)).reattach(q1);
            Mockito.verify(refresherMocked, Mockito.times(1)).reattach(q2);
        }

        //test clearQuiz
        @Test
        void clearQuiz_ShouldReturnEmptyFild_whenUtenteIsNull() {
            assertThrows(EmptyFild.class, () -> log.clearQuiz(null));
        }

        @Test
        void clearQuiz_ShouldNotFail_WhenUtenteIsNotInLog() {
            Utente u = new Utente();
            u.setId(99);

            assertDoesNotThrow(() -> log.clearQuiz(u));
        }

        @Test
        void clearQuiz_ShouldRemoveEntry_WithoutUsingAggiungi() throws Exception {
            Utente u = new Utente();
            u.setId(1);
            Map<Integer, Quiz> fakeQuizzes = new java.util.concurrent.ConcurrentHashMap<>();
            fakeQuizzes.put(10, new Quiz());

            Field field = QuizLog.class.getDeclaredField("logQuizBible");
            field.setAccessible(true);
            Map<Utente, Map<Integer, Quiz>> internalMap = (Map<Utente, Map<Integer, Quiz>>) field.get(log);

            internalMap.put(u, fakeQuizzes);
            assertTrue(internalMap.containsKey(u), "Il setup manuale della mappa è fallito");

            log.clearQuiz(u);

            assertFalse(internalMap.containsKey(u));
        }

        //test getQuiz(Utente, int)
        @Test
        void getQuiz_Utente_int_ShouldReturnEmptyFild_WhemUtenteIsNull() {
            assertThrows(EmptyFild.class, () -> log.getQuiz(null, 1));
        }

        @Test
        void getQuiz_Utente_int_ShouldReturnEmptyFild_WhemIdIsNull() {
            assertThrows(EmptyFild.class, () -> log.getQuiz(new Utente(), 0));
        }

        @Test
        void getQuiz_Single_ShouldReturnQuiz_WhenExists() throws Exception {
            Utente u = new Utente();
            u.setId(1);
            Quiz q = new Quiz();
            q.setId(101);

            Map<Integer, Quiz> fakeQuizzes = new java.util.concurrent.ConcurrentHashMap<>();
            fakeQuizzes.put(101, q);

            Field field = QuizLog.class.getDeclaredField("logQuizBible");
            field.setAccessible(true);
            Map<Utente, Map<Integer, Quiz>> internalMap = (Map<Utente, Map<Integer, Quiz>>) field.get(log);
            internalMap.put(u, fakeQuizzes);

            Mockito.when(refresherMocked.reattach(q)).thenReturn(q);

            Quiz result = log.getQuiz(u, 101);

            assertNotNull(result);
            assertEquals(101, result.getId());
            Mockito.verify(refresherMocked, Mockito.times(1)).reattach(q);
        }

        //getQuizPaginati test
        @Test
        void getQuizPaginati_ShouldReturnFirstPage_WhenDataExists() throws Exception {
            Utente u = new Utente();
            u.setId(1);

            // Creazione di 5 quiz per testare la paginazione
            Quiz q1 = new Quiz(); q1.setId(10);
            Quiz q2 = new Quiz(); q2.setId(20);
            Quiz q3 = new Quiz(); q3.setId(30);
            Quiz q4 = new Quiz(); q4.setId(40);
            Quiz q5 = new Quiz(); q5.setId(50);

            Map<Integer, Quiz> fakeQuizzes = new java.util.concurrent.ConcurrentHashMap<>();
            fakeQuizzes.put(10, q1);
            fakeQuizzes.put(20, q2);
            fakeQuizzes.put(30, q3);
            fakeQuizzes.put(40, q4);
            fakeQuizzes.put(50, q5);

            Field field = QuizLog.class.getDeclaredField("logQuizBible");
            field.setAccessible(true);
            Map<Utente, Map<Integer, Quiz>> internalMap = (Map<Utente, Map<Integer, Quiz>>) field.get(log);
            internalMap.put(u, fakeQuizzes);

            Mockito.doAnswer(returnsFirstArg()).when(refresherMocked).reattach(any(Quiz.class));

            List<Quiz> result = log.getQuizPaginati(u, 1, 2);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(10, result.get(0).getId());
            assertEquals(20, result.get(1).getId());

            Mockito.verify(refresherMocked).reattach(q1);
            Mockito.verify(refresherMocked).reattach(q2);
            Mockito.verify(refresherMocked, Mockito.never()).reattach(q3);
        }

        @Test
        void getQuizPaginati_ShouldReturnEmptyList_WhenUserHasNoQuizzes() {
            Utente u = new Utente();
            u.setId(1);

            assertDoesNotThrow(() -> {
                List<Quiz> result = log.getQuizPaginati(u, 1, 10);
                assertTrue(result.isEmpty());
            });
        }

        @Test
        void getQuizPaginati_ShouldThrowAppException_WhenPaginationIsInvalid() {
            Utente u = new Utente();
            u.setId(1);

            assertThrows(AppException.class, () -> log.getQuizPaginati(u, -1, 10));
            assertThrows(AppException.class, () -> log.getQuizPaginati(u, 0, 0));
        }

        @Test
        void getQuizPaginati_ShouldThrowEmptyFild_WhenUtenteIsNull() {
            assertThrows(EmptyFild.class, () -> log.getQuizPaginati(null, 0, 10));
        }

        @Test
        void getQuizPaginati_ShouldHandleOffsetCorrectly() throws Exception {
            // 1. Setup dati
            Utente u = new Utente();
            u.setId(1);
            Quiz q1 = new Quiz(); q1.setId(10);
            Quiz q2 = new Quiz(); q2.setId(20);

            Map<Integer, Quiz> fakeQuizzes = new ConcurrentHashMap<>();
            fakeQuizzes.put(10, q1);
            fakeQuizzes.put(20, q2);

            Field field = QuizLog.class.getDeclaredField("logQuizBible");
            field.setAccessible(true);
            ((Map<Utente, Map<Integer, Quiz>>) field.get(log)).put(u, fakeQuizzes);

            Mockito.doAnswer(invocation -> invocation.getArgument(0))
                    .when(refresherMocked).reattach(any(Quiz.class));

            List<Quiz> result = log.getQuizPaginati(u, 1, 1);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(10, result.get(0).getId());

            List<Quiz> resultPage2 = log.getQuizPaginati(u, 2, 1);
            assertEquals(20, resultPage2.get(0).getId());
        }

        //test rimuoviSingoloQuiz
        @Test
        void rimuoviSingoloQuiz_ShouldRemoveQuiz_WhenDataIsValid() throws Exception {
            Utente u = new Utente();
            u.setId(1);
            Quiz q = new Quiz();
            q.setId(100);

            Map<Integer, Quiz> fakeQuizzes = new java.util.concurrent.ConcurrentHashMap<>();
            fakeQuizzes.put(100, q);

            Field field = QuizLog.class.getDeclaredField("logQuizBible");
            field.setAccessible(true);
            Map<Utente, Map<Integer, Quiz>> internalMap = (Map<Utente, Map<Integer, Quiz>>) field.get(log);
            internalMap.put(u, fakeQuizzes);

            assertDoesNotThrow(() -> log.rimuoviSingoloQuiz(u, 100));
            assertFalse(internalMap.get(u).containsKey(100));
        }

        @Test
        void rimuoviSingoloQuiz_ShouldThrowAppException_WhenQuizNotFound() throws Exception {
            Utente u = new Utente();
            u.setId(1);
            Map<Integer, Quiz> emptyQuizzes = new java.util.concurrent.ConcurrentHashMap<>();

            Field field = QuizLog.class.getDeclaredField("logQuizBible");
            field.setAccessible(true);
            ((Map<Utente, Map<Integer, Quiz>>) field.get(log)).put(u, emptyQuizzes);

            assertThrows(AppException.class, () -> log.rimuoviSingoloQuiz(u, 999));
        }

        @Test
        void rimuoviSingoloQuiz_ShouldThrowAppException_WhenUserIsNotInLog() {
            Utente u = new Utente();
            u.setId(1);

            assertThrows(AppException.class, () -> log.rimuoviSingoloQuiz(u, 100));
        }

        @Test
        void rimuoviSingoloQuiz_ShouldThrowEmptyFild_WhenIdIsInvalid() {
            Utente u = new Utente();
            u.setId(1);

            assertThrows(EmptyFild.class, () -> log.rimuoviSingoloQuiz(u, null));
            assertThrows(EmptyFild.class, () -> log.rimuoviSingoloQuiz(u, 0));
            assertThrows(EmptyFild.class, () -> log.rimuoviSingoloQuiz(u, -1));
        }

        @Test
        void rimuoviSingoloQuiz_ShouldThrowEmptyFild_WhenUtenteIsInvalid() {
            assertThrows(EmptyFild.class, () -> log.rimuoviSingoloQuiz(null, 100));

            Utente u = new Utente();
            u.setId(null);
            assertThrows(EmptyFild.class, () -> log.rimuoviSingoloQuiz(u, 100));

            u.setId(0);
            assertThrows(EmptyFild.class, () -> log.rimuoviSingoloQuiz(u, 100));
        }

        //test aggiornaSingoloQuiz
        @Test
        void aggiornaSingoloQuiz_ShouldUpdateQuiz_WhenDataIsValid() throws Exception {
            Utente u = new Utente();
            u.setId(1);
            Quiz qVecchio = new Quiz();
            qVecchio.setId(100);
            qVecchio.setTitolo("Vecchio");

            Quiz qNuovo = new Quiz();
            qNuovo.setId(100);
            qNuovo.setTitolo("Nuovo");

            Map<Integer, Quiz> fakeQuizzes = new java.util.concurrent.ConcurrentHashMap<>();
            fakeQuizzes.put(100, qVecchio);

            Field field = QuizLog.class.getDeclaredField("logQuizBible");
            field.setAccessible(true);
            Map<Utente, Map<Integer, Quiz>> internalMap = (Map<Utente, Map<Integer, Quiz>>) field.get(log);
            internalMap.put(u, fakeQuizzes);

            assertDoesNotThrow(() -> log.aggiornaSingoloQuiz(u, qNuovo));
            assertEquals("Nuovo", internalMap.get(u).get(100).getTitolo());
        }

        @Test
        void aggiornaSingoloQuiz_ShouldThrowAppException_WhenQuizNotFound() throws Exception {
            Utente u = new Utente();
            u.setId(1);
            Quiz qNuovo = new Quiz();
            qNuovo.setId(999);

            Map<Integer, Quiz> fakeQuizzes = new java.util.concurrent.ConcurrentHashMap<>();

            Field field = QuizLog.class.getDeclaredField("logQuizBible");
            field.setAccessible(true);
            ((Map<Utente, Map<Integer, Quiz>>) field.get(log)).put(u, fakeQuizzes);

            assertThrows(AppException.class, () -> log.aggiornaSingoloQuiz(u, qNuovo));
        }

        @Test
        void aggiornaSingoloQuiz_ShouldThrowAppException_WhenUserIsNotInLog() {
            Utente u = new Utente();
            u.setId(1);
            Quiz q = new Quiz();
            q.setId(100);

            assertThrows(AppException.class, () -> log.aggiornaSingoloQuiz(u, q));
        }

        @Test
        void aggiornaSingoloQuiz_ShouldThrowAppException_WhenQuizIsInvalid() {
            Utente u = new Utente();
            u.setId(1);

            assertThrows(AppException.class, () -> log.aggiornaSingoloQuiz(u, null));

            Quiz qInvalid = new Quiz();
            qInvalid.setId(0);
            assertThrows(AppException.class, () -> log.aggiornaSingoloQuiz(u, qInvalid));
        }

        @Test
        void aggiornaSingoloQuiz_ShouldThrowAppException_WhenUtenteIsInvalid() {
            Quiz q = new Quiz();
            q.setId(100);

            assertThrows(AppException.class, () -> log.aggiornaSingoloQuiz(null, q));

            Utente u = new Utente();
            u.setId(null);
            assertThrows(AppException.class, () -> log.aggiornaSingoloQuiz(u, q));
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
        Utente test, test1;

        @BeforeEach
        void setup() throws Exception {
            emf = Persistence.createEntityManagerFactory("testPU");
            em = emf.createEntityManager();
            log = new QuizLog();
            refresher = new EntityRefresher();
            injectEntityManager(refresher, em);
            injectRefresher(log, refresher);

            test = new Utente();
            test.setId(1); // Fondamentale per la tua validazione
            test.setNome("Nome");
            test.setCognome("Cognome");
            test.setUsername("user1");
            test.setPasswordHash("hash");

            test1 = new Utente();
            test1.setId(2); // Fondamentale per la tua validazione
            test1.setNome("Nome2");
            test1.setCognome("Cognome2");
            test1.setUsername("user2");
            test1.setPasswordHash("hash2");

            Quiz quiz1 = new Quiz();
            quiz1.setId(1); // Fondamentale per la tua validazione
            quiz1.setTitolo("matematica");
            quiz1.setDescrizione("desc");
            quiz1.setDifficolta("Facile");
            quiz1.setTempo("60");

            Quiz quiz2 = new Quiz();
            quiz2.setId(2); // Fondamentale per la tua validazione
            quiz2.setTitolo("matematica1");
            quiz2.setDescrizione("desc");
            quiz2.setDifficolta("Media");
            quiz2.setTempo("45");

            Quiz quiz3 = new Quiz();
            quiz3.setId(3); // Fondamentale per la tua validazione
            quiz3.setTitolo("matematica2");
            quiz3.setDescrizione("desc");
            quiz3.setDifficolta("Difficile");
            quiz3.setTempo("30");

            log.aggiungi(test, quiz1);
            log.aggiungi(test, quiz2);
            log.aggiungi(test1, quiz3);
        }

        @AfterEach
        void tearDown(){
            log.clearQuiz(test);
            log.clearQuiz(test1);
        }

        @Test
        void getQuiz_Integratio() throws Exception {
            EntityManager em = emf.createEntityManager();
            injectEntityManager(refresher, em);

            List<Quiz> result = log.getQuiz(test);

            assertNotNull(result);
            assertEquals("matematica", result.get(0).getTitolo());
            assertEquals("matematica1", result.get(1).getTitolo());

            // Verifica che siano Managed
            assertTrue(em.contains(result.get(0)), "Il quiz 1 deve essere managed");
            assertTrue(em.contains(result.get(1)), "Il quiz 2 deve essere managed");

            em.close();
        }

        @Test
        void getQuiz_Single_Integratio() throws Exception {
            EntityManager em = emf.createEntityManager();
            injectEntityManager(refresher, em);

            Quiz result = log.getQuiz(test, 1);

            assertNotNull(result);
            assertEquals("matematica", result.getTitolo());

            // Verifica che sia Managed
            assertTrue(em.contains(result), "Il quiz deve essere managed");

            em.close();
        }

        @Test
        void getQuizPaginati_Integration_Success() throws AppException {

            List<Quiz> pagina1 = log.getQuizPaginati(test, 1, 1);
            List<Quiz> pagina2 = log.getQuizPaginati(test, 2, 1);

            assertEquals(1, pagina1.size());
            assertEquals(1, pagina1.get(0).getId());
            assertTrue(em.contains(pagina1.get(0)), "Il quiz della pagina 1 deve essere managed");

            assertEquals(1, pagina2.size());
            assertEquals(2, pagina2.get(0).getId());
            assertTrue(em.contains(pagina2.get(0)), "Il quiz della pagina 2 deve essere managed");

            em.close();
        }

    }

    // =========================
    // ===== UTIL METHOD =======
    // =========================
    private void injectRefresher(Object log, EntityRefresher provider) throws Exception {
        Class<?> clazz = log.getClass();

        Field f = clazz.getDeclaredField("refresher");

        f.setAccessible(true);
        f.set(log, provider);
    }

    private void injectEntityManager(EntityRefresher refresher, EntityManager em) throws Exception {
        Field f = refresher.getClass().getDeclaredField("em");
        f.setAccessible(true);
        f.set(refresher, em);
    }
}
