package model.dao;

import jakarta.persistence.*;
import model.entity.Ticket;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketDAOTest {

	EntityManager em;
	TicketDAO ticketDAO;

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
			ticketDAO = new TicketDAO();
			em = mockEm;
			injectEm(ticketDAO, em);
		}

		/**
		 * Category Partition per findAll:
		 * 1. Input Valido (pageNumber > 0, pageSize > 0)
		 * 2. Input invalido (pageNumber=0)
		 * 3. Input invalido (pageSize=0)
		 */
		@Test
		void findAll_shouldReturnList_whenInputsAreValid() throws AppException {
			int pageNumber = 1;
			int pageSize = 10;
			List<Ticket> expectedList = List.of(new Ticket());

			TypedQuery<Ticket> mockedQuery = mock(TypedQuery.class);
			when(em.createNamedQuery("Ticket.findAll", Ticket.class)).thenReturn(mockedQuery);
			when(mockedQuery.setFirstResult(anyInt())).thenReturn(mockedQuery);
			when(mockedQuery.setMaxResults(anyInt())).thenReturn(mockedQuery);
			when(mockedQuery.getResultList()).thenReturn(expectedList);

			List<Ticket> result = ticketDAO.findAll(pageNumber, pageSize);

			assertNotNull(result);
			verify(mockedQuery).setFirstResult(0);
			verify(mockedQuery).setMaxResults(pageSize);
		}

		@Test
		void findAll_shouldThrowException_whenInputsAreInvalid() {
			assertThrows(AppException.class, () -> ticketDAO.findAll(0, 10));
			assertThrows(AppException.class, () -> ticketDAO.findAll(1, 0));
		}

		/**
		 * Category partition per FindById:
		 * 1. Input valido (id>0)
		 * 2. Input invalido (id<=0)
		 */
		@Test
		void findById_shouldReturnTicket_whenIdExists() throws AppException {
			int id = 1;
			Ticket fintoTicket = new Ticket();
			when(em.find(Ticket.class, id)).thenReturn(fintoTicket);

			Ticket result = ticketDAO.findById(id);

			assertEquals(fintoTicket, result);
		}

		@Test
		void findById_shouldThrowException_whenIdIsInvalid() {
			assertThrows(Exception.class, () -> ticketDAO.findById(0));

			verifyNoInteractions(em);
		}

		/**
		 * Category partition per findByUtente:
		 * 1. Input Validi (Utente != Null, pageNumber>0, pageSize>0)
		 * 2. Input invalido (Utente == Null)
		 * 3. Input invalido (utente.id <0)
		 * 4. Input invalido (pageNumber<=0)
		 * 5. Input invalido (pageSize<=0)
		 */
		@Test
		void findByUtente_shouldReturnList_whenInputsAreValid() throws AppException {
			Utente utente = new Utente();
			utente.setId(1);
			TypedQuery<Ticket> mockedQuery = mock(TypedQuery.class);

			when(em.createNamedQuery("Ticket.faindAllByUtente", Ticket.class)).thenReturn(mockedQuery);
			when(mockedQuery.setParameter(eq("utente"), any())).thenReturn(mockedQuery);
			when(mockedQuery.setFirstResult(anyInt())).thenReturn(mockedQuery);
			when(mockedQuery.setMaxResults(anyInt())).thenReturn(mockedQuery);
			when(mockedQuery.getResultList()).thenReturn(new ArrayList<>());

			List<Ticket> result = ticketDAO.findByUtente(utente, 1, 10);
			assertNotNull(result);
		}

		@Test

		void findByUtente_shouldThrowException_whenInputsAreInvalid() {

			Utente utenteNull = null;
			Utente utenteValido = new Utente();
			utenteValido.setId(1);
			Utente utenteNonValido= new Utente();
			utenteNonValido.setId(0);

			assertThrows(AppException.class, () -> ticketDAO.findByUtente(utenteNull, 1, 10));
			assertThrows(AppException.class, () -> ticketDAO.findByUtente(utenteNonValido, 1, 10));
			assertThrows(AppException.class, () -> ticketDAO.findByUtente(utenteValido, 0, 10));
			assertThrows(AppException.class, () -> ticketDAO.findByUtente(utenteValido, 1, 0));

		}

		/**
		 * Category partition per insert:
		 * 1. Input validi (ticket != null)
		 * 2. Input invalidi (ticket = null)
		 * 3. Input invalidi (ticket.id<=0)
		 */
		@Test
		void insert_shouldPersist_whenTicketIsValid() throws Exception {
			Ticket t = new Ticket();
			Utente u = new Utente();
			u.setId(1);
			t.setUtente(u);
			EntityTransaction tx = mock(EntityTransaction.class);
			when(em.getTransaction()).thenReturn(tx);

			ticketDAO.insert(t);

			verify(em).persist(t);
			verify(tx).commit();
		}

		@Test
		void insert_shouldThrowException_whenTicketIsNull() {
			assertThrows(EmptyFild.class, () -> ticketDAO.insert(null));
			verifyNoInteractions(em);
		}

		@Test
		void insert_shouldThrowException_whenTicketUtenteIsNull() {
			Ticket ticket = new Ticket();
			ticket.setUtente(null);
			assertThrows(AppException.class, () -> ticketDAO.insert(ticket));
			verifyNoInteractions(em);
		}

		@Test
		void insert_shouldThrowException_whenTicketUtenteIdIsNull() {
			Ticket t = new Ticket();
			Utente u = new Utente();
			u.setId(null);
			t.setUtente(u);
			assertThrows(AppException.class, () -> ticketDAO.insert(t));
			verifyNoInteractions(em);
		}

		@Test
		void insert_shouldThrowException_whenTicketUtenteIdIsZeroOrNegative() {
			Ticket t = new Ticket();
			Utente u = new Utente();
			u.setId(0);
			t.setUtente(u);
			assertThrows(AppException.class, () -> ticketDAO.insert(t));

			verifyNoInteractions(em);
		}

		/**
		 * Category partition per update:
		 * 1. Input validi (ticket != null, id>0)
		 * 2. Input invalidi (ticket = null)
		 * 3. Input invalidi (id<=0)
		 */
		@Test
		void update_shouldUpdate_whenTicketIsValid() throws Exception {
			Ticket t = new Ticket();
			t.setId(1);
			EntityTransaction tx = mock(EntityTransaction.class);
			when(em.getTransaction()).thenReturn(tx);
			when(em.merge(t)).thenReturn(t);

			ticketDAO.update(t);

			verify(em).merge(t);
		}

		@Test
		void update_shouldThrowException_whenTicketIsNull() {
			Ticket ticket = new Ticket();
			ticket.setId(0);
			assertThrows(EmptyFild.class, () -> ticketDAO.update(null));
			assertThrows(EmptyFild.class, () -> ticketDAO.update(ticket));
		}


		/**
		 * Category partition per Delete:
		 * 1. Input valido (Ticket != null e id>0)
		 * 2. Input invalido (Ticket == null)
		 * 3. Input invalido (id<=0)
		 */
		@Test
		void delete_shouldRemove_whenTicketIsValid() throws Exception {
			Ticket t = new Ticket();
			t.setId(1);
			EntityTransaction tx = mock(EntityTransaction.class);
			when(em.getTransaction()).thenReturn(tx);
			when(em.merge(t)).thenReturn(t);

			ticketDAO.delete(t);

			verify(em).remove(t);
		}
		@Test
		void delete_shouldThrowException_whenTicketIsNull() {
			Ticket ticket = new Ticket();
			ticket.setId(0);
			assertThrows(EmptyFild.class, () -> ticketDAO.delete(null));
			assertThrows(EmptyFild.class, () -> ticketDAO.delete(ticket));
		}

	}

	// =========================
	// === INTEGRATION TESTS ===
	// =========================
	@Nested
	@Tag("integration")
	class IntegrationTests {
		EntityManagerFactory emf;
		Utente proprietario, proprietario2;
		Ticket ticketPrecaricato, ticketPrecaricato2, ticketPrecaricato3; // Riferimento al ticket creato nel setup

		@BeforeEach
		void setup() throws Exception {
			emf = Persistence.createEntityManagerFactory("testPU");
			em = emf.createEntityManager();
			ticketDAO = new TicketDAO();
			injectEm(ticketDAO, em);

			em.getTransaction().begin();
			// Pulizia per evitare interferenze tra test
			em.createQuery("DELETE FROM Ticket").executeUpdate();
			em.createQuery("DELETE FROM Utente").executeUpdate();


			proprietario = new Utente();
			proprietario.setNome("Mario");
			proprietario.setCognome("Rossi");
			proprietario.setUsername("mario_test");
			proprietario.setPasswordHash("hash123");
			em.persist(proprietario);

			proprietario2 = new Utente();
			proprietario2.setNome("Lui");
			proprietario2.setCognome("Rossi");
			proprietario2.setUsername("lui_test");
			proprietario2.setPasswordHash("hash123");
			em.persist(proprietario2);


			//TICKET PER I TEST
			ticketPrecaricato = new Ticket();
			ticketPrecaricato.setUtente(proprietario);
			ticketPrecaricato.setDescrizioneTicket("Descrizione Originale");
			ticketPrecaricato.setTipoRichiesta("Hardware");
			ticketPrecaricato.setDescrizioneRichiesta("PC rotto");
			em.persist(ticketPrecaricato);

			ticketPrecaricato2 = new Ticket();
			ticketPrecaricato2.setUtente(proprietario2);
			ticketPrecaricato2.setDescrizioneTicket("Descrizione Rubata");
			ticketPrecaricato2.setTipoRichiesta("Software");
			ticketPrecaricato2.setDescrizioneRichiesta("APP rotta");
			em.persist(ticketPrecaricato2);

			ticketPrecaricato3 = new Ticket();
			ticketPrecaricato3.setUtente(proprietario);
			ticketPrecaricato3.setDescrizioneTicket("Descrizione truccata");
			ticketPrecaricato3.setTipoRichiesta("Hardware");
			ticketPrecaricato3.setDescrizioneRichiesta("Pagine terminate");
			em.persist(ticketPrecaricato3);

			em.getTransaction().commit();
			em.clear();
		}

		@AfterEach
		void tearDown() {
			if (em.isOpen()) em.close();
			if (emf.isOpen()) emf.close();
		}

		@Test
		@DisplayName("Inserimento di ticket")
		void insert_Integration() throws Exception {
			Ticket t = new Ticket(proprietario,"Descrizione di prova","Prova","Tipo di prova");
			ticketDAO.insert(t);
			em.clear();
			Ticket recuperato = em.find(Ticket.class, t.getId());
			assertNotNull(recuperato);
			assertEquals("Descrizione di prova", recuperato.getDescrizioneTicket());
		}

		@Test
		@DisplayName("Integrazione: findById usando dato precaricato")
		void findById_Integration() throws Exception {
			// Act
			Ticket recuperato = ticketDAO.findById(ticketPrecaricato.getId());

			// Assert
			assertNotNull(recuperato);
			assertEquals("Descrizione Originale", recuperato.getDescrizioneTicket());
		}

		@Test
		@DisplayName("Integrazione: findByUtente usando dato precaricato")
		void findByUtente_Integration() throws Exception {
			int pageNumber = 1;
			List<Ticket> page1 = ticketDAO.findByUtente(proprietario, pageNumber, 2);

			assertNotNull(page1);
			assertEquals(2, page1.size());//perchè ne ho messi 2 in setup

			List<Ticket> page2 = ticketDAO.findByUtente(proprietario, pageNumber+1, 2);

			assertNotNull(page2);
			assertTrue(page2.isEmpty());
		}

		@Test
		@DisplayName("Integrazione: update di un ticket esistente")
		void update_Integration() throws Exception {
			Ticket t = ticketDAO.findById(ticketPrecaricato3.getId());
			t.setDescrizioneTicket("matematica1");
			t.setTipoRichiesta("nuovo");
			t.setUtente(proprietario2);

			ticketDAO.update(t);

			em.clear();

			Ticket verificato = em.find(Ticket.class, ticketPrecaricato3.getId());

			assertAll("Verifica Aggiornamento",
					() -> assertEquals("matematica1", verificato.getDescrizioneTicket()),
					() -> assertEquals("nuovo", verificato.getTipoRichiesta()),
					() -> assertEquals(proprietario2.getId(), verificato.getUtente().getId())
			);
		}

		@Test
		@DisplayName("Integrazione: delete di un ticket esistente")
		void delete_Integration() throws Exception {
			// Act
			ticketDAO.delete(ticketPrecaricato);
			em.clear();

			// Assert
			Ticket recuperato = em.find(Ticket.class, ticketPrecaricato.getId());
			assertNull(recuperato, "Il ticket dovrebbe essere stato rimosso");
		}

		@Test
		@DisplayName("Integrazione: findAll con dati preesistenti")
		void findAll_Integration() throws Exception {
			int pageNumber = 1, pageSize = 2;
			List<Ticket> page1 = ticketDAO.findAll(pageNumber, pageSize);

			assertNotNull(page1);
			assertEquals(pageSize, page1.size());

			List<Ticket> page2 = ticketDAO.findAll(pageNumber + 1, pageSize);

			assertNotNull(page2);
			assertEquals(1, page2.size());

			assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
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