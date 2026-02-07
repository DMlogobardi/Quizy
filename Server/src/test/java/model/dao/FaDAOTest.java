package model.dao;

import jakarta.persistence.*;
import model.entity.*;
import model.exception.AppException;
import model.exception.EmptyFild;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.lang.reflect.Field;
import java.util.List;

import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FaDAOTest {

	EntityManager em;
	FaDAO dao;

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
			dao = new FaDAO();
			em = mockEm;
			injectEm(dao, em);
		}

		/**
		 * category partition per findByUtenteQuiz
		 * 1. input validi(quiz != null, quiz.id !=null, quiz.id>0, u!=null, u.id!= null, u.id>0)
		 * 2. input non validi(quiz = null)
		 * 3. input non valido(quiz.id = null)
		 * 4. input non valido(quiz.id < 0)
		 * 5. input non validi(u = null)
		 * 6. input non valido(u.id = null)
		 * 7. input non valido(u.id < 0)
		 */

		@Test
		void findByUtenteQuiz_shouldReturnFA_withParameterAreValid() {
			Quiz q = new Quiz();
			q.setId(1);
			Utente u = new Utente();
			u.setId(1);
			Fa risultatoAtteso = new Fa();

			TypedQuery<Fa> mockedQuery = mock(TypedQuery.class);
			when(em.createNamedQuery("Fa.findByUserAndQuiz", Fa.class)).thenReturn(mockedQuery);
			when(mockedQuery.setParameter("utente", u)).thenReturn(mockedQuery);
			when(mockedQuery.setParameter("quiz", q)).thenReturn(mockedQuery);
			when(mockedQuery.getSingleResult()).thenReturn(risultatoAtteso);

			Fa result = dao.findByUtenteQuiz(q, u);

			assertNotNull(result);
			assertEquals(risultatoAtteso, result);
			verify(em).createNamedQuery("Fa.findByUserAndQuiz", Fa.class);
			verify(mockedQuery).setParameter("utente", u);
			verify(mockedQuery).setParameter("quiz", q);
			verify(mockedQuery).getSingleResult();
		}

		@Test
		void findByUtenteQuiz_shouldThrowException_whenQuizIsNull() {
			Quiz q = null;
			Utente u = new Utente();
			u.setId(1);

			assertThrows(AppException.class, () -> dao.findByUtenteQuiz(q, u));
		}

		@Test
		void findByUtenteQuiz_shouldThrowException_whenQuizIdIsNull() {
			Quiz q = new Quiz();
			q.setId(null);
			Utente u = new Utente();
			u.setId(1);

			assertThrows(AppException.class, () -> dao.findByUtenteQuiz(q, u));
		}

		@Test
		void findByUtenteQuiz_shouldThrowException_whenQuizIdIsNegative() {
			Quiz q = new Quiz();
			q.setId(-1);
			Utente u = new Utente();
			u.setId(1);

			assertThrows(AppException.class, () -> dao.findByUtenteQuiz(q, u));
		}

		@Test
		void findByUtenteQuiz_shouldThrowException_whenUtenteIsNull() {
			Quiz q = new Quiz();
			q.setId(1);
			Utente u = null;

			assertThrows(AppException.class, () -> dao.findByUtenteQuiz(q, u));
		}

		@Test
		void findByUtenteQuiz_shouldThrowException_whenUtenteIdIsNull() {
			Quiz q = new Quiz();
			q.setId(1);
			Utente u = new Utente();
			u.setId(null);

			assertThrows(AppException.class, () -> dao.findByUtenteQuiz(q, u));
		}

		@Test
		void findByUtenteQuiz_shouldThrowException_whenUtenteIdIsNegative() {
			Quiz q = new Quiz();
			q.setId(1);
			Utente u = new Utente();
			u.setId(-1);

			assertThrows(AppException.class, () -> dao.findByUtenteQuiz(q, u));
		}

		/**
		 * category partition per faintById:
		 * 1. Input validi(id>0)
		 * 2. input non valido(id<=0)
		 */

		@Test
		void faintById_shoudReturnFa_whenParameterIsValid() {
			int id = 1;
			Fa risultatoAtteso = new Fa();

			when(em.find(Fa.class, id)).thenReturn(risultatoAtteso);

			Fa result = dao.faindById(id);

			assertNotNull(result);
			assertEquals(risultatoAtteso, result);
		}

		@Test
		void faintById_shoudThrowException_whenParameterIsNegative() {
			int id = -1;

			assertThrows(AppException.class, () -> dao.faindById(id));
		}

		/**
		 * category partition per findAll:
		 * 1. input validi(pageNumber>0,pageSize>0)
		 * 2. input non validi(pageNumber<=0)
		 * 3. input non validi(pageSize<=0)
		 */

		@Test
		void findAll_shouldReturnList_whenParametersAreValid() {
			int pageNumber = 1;
			int pageSize = 10;
			List<Fa> expectedResults = List.of(new Fa());

			TypedQuery<Fa> mockedQuery = mock(TypedQuery.class);
			when(em.createNamedQuery("Fa.findAll", Fa.class)).thenReturn(mockedQuery);
			when(mockedQuery.setFirstResult(anyInt())).thenReturn(mockedQuery);
			when(mockedQuery.setMaxResults(anyInt())).thenReturn(mockedQuery);
			when(mockedQuery.getResultList()).thenReturn(expectedResults);

			List<Fa> result = dao.findAll(pageNumber, pageSize);

			assertNotNull(result);
			assertEquals(expectedResults, result);
			verify(em).createNamedQuery("Fa.findAll", Fa.class);
			verify(mockedQuery).setFirstResult(anyInt());
			verify(mockedQuery).setMaxResults(anyInt());
			verify(mockedQuery).getResultList();
		}

		@Test
		void findAll_shouldThrowException_whenPageNumberIsNegative() {
			int pageNumber = 0;
			int pageSize = 10;

			assertThrows(AppException.class, () -> dao.findAll(pageNumber, pageSize));
		}

		@Test
		void findAll_shouldThrowException_whenPageSizeIsNegative() {
			int pageNumber = 1;
			int pageSize = 0;

			assertThrows(AppException.class, () -> dao.findAll(pageNumber, pageSize));
		}

		/**
		 * category partition per FindAllByUtente:
		 * 1. input validi(pageNumber>0,pageSize>0,utente!=null,utente.id!=null,utente.id>0)
		 * 2. input non validi(pageNumber<=0)
		 * 3. input non validi(pageSize<=0)
		 * 4. input non validi(utente = null)
		 * 5. input non validi(utente.id = null)
		 * 6. input non validi(utente.id <= 0)
		 */

		@Test
		void findAllByUtente_shouldReturnList_whenParametersAreValid() {
			int pageNumber = 1;
			int pageSize = 10;
			Utente utente = new Utente();
			utente.setId(1);
			List<Fa> expectedResults = List.of(new Fa());

			TypedQuery<Fa> mockedQuery = mock(TypedQuery.class);
			when(em.createNamedQuery("Fa.findAllByUtente", Fa.class)).thenReturn(mockedQuery);
			when(mockedQuery.setParameter("utente", utente)).thenReturn(mockedQuery);
			when(mockedQuery.setFirstResult(anyInt())).thenReturn(mockedQuery);
			when(mockedQuery.setMaxResults(anyInt())).thenReturn(mockedQuery);
			when(mockedQuery.getResultList()).thenReturn(expectedResults);

			List<Fa> result = dao.findAllByUtente(pageNumber, pageSize, utente);

			assertNotNull(result);
			assertEquals(expectedResults, result);
			verify(em).createNamedQuery("Fa.findAllByUtente", Fa.class);
			verify(mockedQuery).setParameter("utente", utente);
			verify(mockedQuery).setFirstResult(anyInt());
			verify(mockedQuery).setMaxResults(anyInt());
			verify(mockedQuery).getResultList();
		}

		@Test
		void findAllByUtente_shouldThrowException_whenPageNumberIsNegative() {
			int pageNumber = 0;
			int pageSize = 10;
			Utente utente = new Utente();
			utente.setId(1);

			assertThrows(AppException.class, () -> dao.findAllByUtente(pageNumber, pageSize, utente));
		}

		@Test
		void findAllByUtente_shouldThrowException_whenPageSizeIsNegative() {
			int pageNumber = 1;
			int pageSize = 0;
			Utente utente = new Utente();
			utente.setId(1);

			assertThrows(AppException.class, () -> dao.findAllByUtente(pageNumber, pageSize, utente));
		}

		@Test
		void findAllByUtente_shouldThrowException_whenUtenteIsNull() {
			int pageNumber = 1;
			int pageSize = 10;
			Utente utente = null;

			assertThrows(EmptyFild.class, () -> dao.findAllByUtente(pageNumber, pageSize, utente));

		}

		//Test Fallito per eccezione diversa
		@Test
		void findAllByUtente_shouldThrowException_whenUtenteIdIsNull() {
			int pageNumber = 1;
			int pageSize = 10;
			Utente utente = new Utente();
			utente.setId(null);

			assertThrows(EmptyFild.class, () -> dao.findAllByUtente(pageNumber, pageSize, utente));
		}

		//Test Fallito per eccezione diversa
		@Test
		void findAllByUtente_shouldThrowException_whenUtenteIdIsNegative() {
			int pageNumber = 1;
			int pageSize = 10;
			Utente utente = new Utente();
			utente.setId(0);

			assertThrows(AppException.class, () -> dao.findAllByUtente(pageNumber, pageSize, utente));
		}

		/**
		 * category partition per insert:
		 * 1. input validi(f!=null,f.utente!=null,f.utente.id!=null,f.utente.id>0,f.quiz!=null,f.quiz.id!=null,f.quiz.id>0)
		 * 2. input non validi(f= null)
		 * 3. input non validi(f.utente =null)
		 * 4. input non validi(f.utente.id =null)
		 * 5. input non validi(f.utente.id <=0)
		 * 6. input non validi(f.quiz =null)
		 * 7. input non validi(f.quiz.id =null)
		 * 8. input non validi(f.quiz.id <=0)
		 */

		@Test
		void insert_shouldPersist_whenParameterIsValid() {
			Utente u = new Utente();
			u.setId(1);
			Quiz q = new Quiz();
			q.setId(1);
			Fa f = new Fa();
			f.setUtente(u);
			f.setQuiz(q);

			EntityTransaction tx = mock(EntityTransaction.class);
			when(em.getTransaction()).thenReturn(tx);

			dao.insert(f);

			verify(tx).begin();
			verify(em).persist(f);
		}

		@Test
		void insert_shouldThrowException_whenUtenteIsInvalid() {
			Fa f = null;
			Fa faUtenteNUll = new Fa();
			faUtenteNUll.setUtente(null);
			Fa faIdUtenteNull = new Fa();
			faIdUtenteNull.setUtente(new Utente());
			faIdUtenteNull.getUtente().setId(null);
			Fa faIdUtenteNonValido = new Fa();
			faIdUtenteNonValido.setUtente(new Utente());
			faIdUtenteNonValido.getUtente().setId(0);

			assertThrows(EmptyFild.class, () -> dao.insert(f));
			assertThrows(EmptyFild.class, () -> dao.insert(faUtenteNUll));
			assertThrows(EmptyFild.class, () -> dao.insert(faIdUtenteNull));
			assertThrows(EmptyFild.class, () -> dao.insert(faIdUtenteNonValido));
		}

		@Test
		void insert_shouldThrowException_whenQuizIsInvalid() {
			Fa faQuizNull = new Fa();
			faQuizNull.setQuiz(null);

			Fa faQuizIdNull = new Fa();
			faQuizIdNull.setQuiz(new Quiz());
			faQuizIdNull.getQuiz().setId(null);

			Fa faQuizIdNonValido = new Fa();
			faQuizIdNonValido.setQuiz(new Quiz());
			faQuizIdNonValido.getQuiz().setId(0);

			assertThrows(EmptyFild.class, () -> dao.insert(faQuizNull));
			assertThrows(EmptyFild.class, () -> dao.insert(faQuizIdNull));
			assertThrows(EmptyFild.class, () -> dao.insert(faQuizIdNonValido));
		}

		/**
		 * category partition per Update:
		 * 1. Input validi(fa != null fa.id != null, fa.id > 0, fa.Utente != null, fa.Utente.id != null, fa.Utente.id > 0)
		 * 2. input non validi(f= null)
		 * 3. input non validi(fa.id = null)
		 * 4. input non validi(fa.id <= 0)
		 * 5. input non validi(f.utente =null)
		 * 6. input non validi(f.utente.id =null)
		 * 7. input non validi(f.quiz =null)
		 * 8. input non validi(f.utente.id <=0)
		 * 9. input non validi(f.quiz.id =null)
		 * 10. input non validi(f.quiz.id <=0)
		 */

		@Test
		void update_shouldUpdate_whenParameterIsValid() {
			Utente u = new Utente();
			u.setId(1);
			Quiz q = new Quiz();
			q.setId(1);
			Fa f = new Fa();
			f.setId(1);
			f.setUtente(u);
			f.setQuiz(q);

			EntityTransaction tx = mock(EntityTransaction.class);
			when(em.getTransaction()).thenReturn(tx);

			dao.update(f);

			verify(tx).begin();
			verify(em).merge(f);
		}

		@Test
		void update_shouldThrowException_whenIdIsInvalid() {
			Fa faIdNull = new Fa();
			faIdNull.setId(null);
			Fa faIdNonValido = new Fa();
			faIdNonValido.setId(0);

			assertThrows(EmptyFild.class, () -> dao.update(faIdNull));
			assertThrows(EmptyFild.class, () -> dao.update(faIdNonValido));
		}

		@Test
		void update_shouldThrowException_whenUtenteIsInvalid() {
			Fa f = null;
			Fa faUtenteNUll = new Fa();
			faUtenteNUll.setUtente(null);
			Fa faIdUtenteNull = new Fa();
			faIdUtenteNull.setUtente(new Utente());
			faIdUtenteNull.getUtente().setId(null);
			Fa faIdUtenteNonValido = new Fa();
			faIdUtenteNonValido.setUtente(new Utente());
			faIdUtenteNonValido.getUtente().setId(0);

			assertThrows(EmptyFild.class, () -> dao.update(f));
			assertThrows(EmptyFild.class, () -> dao.update(faUtenteNUll));
			assertThrows(EmptyFild.class, () -> dao.update(faIdUtenteNull));
			assertThrows(EmptyFild.class, () -> dao.update(faIdUtenteNonValido));
		}

		@Test
		void update_shouldThrowException_whenQuizIsInvalid() {
			Fa faQuizNull = new Fa();
			faQuizNull.setQuiz(null);

			Fa faQuizIdNull = new Fa();
			faQuizIdNull.setQuiz(new Quiz());
			faQuizIdNull.getQuiz().setId(null);

			Fa faQuizIdNonValido = new Fa();
			faQuizIdNonValido.setQuiz(new Quiz());
			faQuizIdNonValido.getQuiz().setId(0);

			assertThrows(EmptyFild.class, () -> dao.update(faQuizNull));
			assertThrows(EmptyFild.class, () -> dao.update(faQuizIdNull));
			assertThrows(EmptyFild.class, () -> dao.update(faQuizIdNonValido));
		}

		/**
		 * category partition per delete:
		 * 1. Input validi(fa != null, fa.id != null, fa.id > 0, fa.utente.id != null, fa.utente.id >0, fa.quiz != null, fa.quiz.id != null, fa.quiz.id > 0)
		 * 2. Input non valido (fa = null)
		 * 3. Input non valido (fa.id = null)
		 * 4. Input non valido (fa.id <= 0)
		 * 5. Input non valido (fa.utente = null)
		 * 6. Input non valido (fa.utente.id = null)
		 * 7. Input non valido (fa.utente.id <= 0)
		 * 8. Input non valido (fa.quiz = null)
		 * 9. Input non valido (fa.quiz.id = null)
		 * 10. Input non valido (fa.quiz.id <= 0)
		 */

		@Test
		void delete_shouldDelete_whenParameterIsValid() {
			Utente u = new Utente();
			u.setId(1);
			Quiz q = new Quiz();
			q.setId(1);
			Fa f = new Fa();
			f.setId(1);
			f.setUtente(u);
			f.setQuiz(q);

			EntityTransaction tx = mock(EntityTransaction.class);
			when(em.getTransaction()).thenReturn(tx);
			when(em.merge(f)).thenReturn(f);

			dao.delete(f);

			verify(tx).begin();
			verify(em).merge(f);
			verify(em).remove(f);
		}

		@Test
		void delete_shouldThrowException_whenFaIsInvalid() {
			Fa faIdNull = new Fa();
			Fa faIdInvalid = new Fa();
			faIdInvalid.setId(0);
			Quiz q = new Quiz();
			q.setId(1);
			Utente u = new Utente();
			u.setId(1);

			faIdNull.setUtente(u);
			faIdNull.setQuiz(q);
			faIdNull.setId(null);

			faIdInvalid.setUtente(u);
			faIdInvalid.setQuiz(q);
			faIdInvalid.setId(0);


			assertThrows(EmptyFild.class, () -> dao.delete(faIdNull));
			assertThrows(EmptyFild.class, () -> dao.delete(faIdInvalid));
		}

		@Test
		void delete_shouldThrowException_whenUtenteIsInvalid() {
			Fa faIdUserNull = new Fa();
			Fa faIdUserInvalid = new Fa();
			Quiz q = new Quiz();
			q.setId(1);

			faIdUserNull.setQuiz(q);
			faIdUserInvalid.setQuiz(q);

			Utente utenteIdNull = new Utente();
			utenteIdNull.setId(null);
			faIdUserNull.setUtente(utenteIdNull);

			Utente utenteIdInvalido = new Utente();
			utenteIdInvalido.setId(0);
			faIdUserInvalid.setUtente(utenteIdInvalido);

			assertThrows(EmptyFild.class, () -> dao.delete(faIdUserNull));
			assertThrows(EmptyFild.class, () -> dao.delete(faIdUserInvalid));
		}

		@Test
		void delete_shouldThrowException_whenQuizIsInvalid() {
			Fa faIdQuizNull = new Fa();
			Fa faIdQuizInvalid = new Fa();
			Utente u = new Utente();
			u.setId(1);
			faIdQuizNull.setUtente(u);
			faIdQuizInvalid.setUtente(u);

			Quiz quizIdNull = new Quiz();
			quizIdNull.setId(null);
			faIdQuizNull.setQuiz(quizIdNull);

			Quiz quizIdInvalid = new Quiz();
			quizIdInvalid.setId(0);
			faIdQuizInvalid.setQuiz(quizIdInvalid);

			assertThrows(EmptyFild.class, () -> dao.delete(faIdQuizNull));
			assertThrows(EmptyFild.class, () -> dao.delete(faIdQuizInvalid));
		}


	}

		@Nested
		@Tag("integration")
		class IntegrationTests {
			EntityManagerFactory emf;

			Utente utente1, utente2;
			Quiz quiz1,quiz2;

			Fa fa1, fa2, fa3;


			@BeforeEach
			void setup() throws Exception {
				emf = Persistence.createEntityManagerFactory("testPU");
				em = emf.createEntityManager();
				dao = new FaDAO();
				injectEm(dao, em);
				em.getTransaction().begin();
				//Pulizia tabelle
				em.createQuery("DELETE FROM Fa");
				em.createQuery("DELETE FROM Utente");
				em.createQuery("DELETE FROM Quiz");


				//Inserimento utenti
				utente1 = new Utente();
				utente1.setNome("Mario");
				utente1.setCognome("Rossi");
				utente1.setUsername("MarioRossi");
				utente1.setPasswordHash("password123");
				em.persist(utente1);

				utente2 = new Utente();
				utente2.setNome("Luca");
				utente2.setCognome("Bianchi");
				utente2.setUsername("LucaBianchi");
				utente2.setPasswordHash("password456");
				em.persist(utente2);

				//Inserimento quiz
				quiz1 = new Quiz();
				quiz1.setUtente(utente1);
				quiz1.setTempo("10:00");
				quiz1.setDifficolta("facile");
				quiz1.setTitolo("Titolo1");
				quiz1.setDescrizione("Descrizione1");

				em.persist(quiz1);

				quiz2 = new Quiz();
				quiz2.setUtente(utente2);
				quiz2.setTempo("12:00");
				quiz2.setDifficolta("facile2");
				quiz2.setTitolo("Titolo2");
				quiz2.setDescrizione("Descrizione2");

				em.persist(quiz2);

				fa1= new Fa();
				fa1.setQuiz(quiz1);
				fa1.setUtente(utente1);

				fa2 = new Fa();
				fa2.setUtente(utente2);
				fa2.setQuiz(quiz2);

				fa3= new Fa();
				fa3.setUtente(utente1);
				fa3.setQuiz(quiz2);


				em.persist(fa1);
				em.persist(fa2);
				em.persist(fa3);

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
				Integer id = fa1.getId();

				Fa result = dao.faindById(id);


				assertNotNull(result);
				assertEquals(fa1.getId(), result.getId());
				assertEquals(fa1.getQuiz().getId(), result.getQuiz().getId());
				assertEquals(fa1.getUtente().getId(), result.getUtente().getId());
			}

			@Test
			@DisplayName("findByUtenteQuiz deve trovare il fa dell'utente del determiato quiz")
			void findByUtenteQuiz_Integration(){

				Fa result = dao.findByUtenteQuiz(quiz1,utente1);

				assertNotNull(result);
				assertEquals(fa1.getId(), result.getId());
				assertEquals(fa1.getQuiz().getId(), result.getQuiz().getId());
			}

			@Test
			@DisplayName("Integrazione: findAll con dati preesistenti")
			void findAll_Integration(){
				List<Fa> result = dao.findAll(1,2);

				assertNotNull(result);
				assertEquals(2, result.size());
				assertEquals(fa1.getId(), result.get(0).getId());
				assertEquals(fa2.getId(), result.get(1).getId());

				List<Fa> pag2 = dao.findAll(2,2);
				assertNotNull(pag2);
				assertEquals(1, pag2.size());
				assertEquals(fa3.getId(), pag2.get(0).getId());
			}

			@Test
			@DisplayName("findByUtente deve trovare tutti i fa dell'utente")
			void findByUtente_Integration(){
				List<Fa> result = dao.findAllByUtente(1,10,utente1);

				assertNotNull(result);
				assertEquals(2, result.size());
				assertEquals(fa1.getId(), result.get(0).getId());
			}

			@Test
			@DisplayName("Inserimento di Fa")
			void insert_Integration() throws Exception {
				Fa faNuovo = new Fa();
				//dati non ancora esistenti
				faNuovo.setUtente(utente2);
				faNuovo.setQuiz(quiz1);

				dao.insert(faNuovo);

				em.getTransaction().begin();

				em.clear();
				Fa recuperato = em.find(Fa.class, faNuovo.getId());
				assertNotNull(recuperato);
				assertEquals(faNuovo.getId(), recuperato.getId());
				assertEquals(faNuovo.getQuiz().getId(), recuperato.getQuiz().getId());
			}

			@Test
			@DisplayName("Integrazione: update di un fa esistente")
			void update_Integration() throws Exception {
				Fa faDaModificare = em.find(Fa.class, fa3.getId());
				Utente nuovoProprietario = em.find(Utente.class, utente2.getId());

				faDaModificare.setQuiz(quiz1);
				faDaModificare.setUtente(nuovoProprietario);

				dao.update(faDaModificare);

				em.clear();

				Fa verificato = em.find(Fa.class, fa3.getId());

				assertNotNull(verificato, "L'oggetto aggiornato non è stato trovato");
				assertAll("Verifica Aggiornamento",
						() -> assertEquals(nuovoProprietario.getId(), verificato.getUtente().getId(), "L'utente non è stato aggiornato"),
						() -> assertEquals(quiz1.getId(), verificato.getQuiz().getId(), "Il quiz non è stato aggiornato")
				);
			}

			@Test
			@DisplayName("Integrazione: delete di un fa esistente")
			void delete_Integration() throws Exception {
				Fa ticketPrecaricato = em.find(Fa.class, fa1.getId());
				dao.delete(ticketPrecaricato);
				em.clear();

				Fa recuperato = em.find(Fa.class, fa1.getId());
				assertNull(recuperato, "Il ticket dovrebbe essere stato rimosso");
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
