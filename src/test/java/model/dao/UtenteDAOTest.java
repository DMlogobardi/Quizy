package model.dao;

import jakarta.persistence.*;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;
import model.exception.RegisterFailed;
import model.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UtenteDAOTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private UtenteDAO dao;

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
           dao.findAllPaginated(0,1);
        });

        verifyNoInteractions(em);
    }

    @Test
    void findAllPaginated_ShouldReturnAppException_WhenPageSizeIs0() {
        AppException exception = assertThrows(AppException.class, () -> {
            dao.findAllPaginated(1,0);
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
        verify(tx, never()).rollback(); // Non deve esserci rollback
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

        assertThrows(AppException.class, () -> dao.update(u));

        verify(tx).rollback(); // Verifichiamo che la transazione venga annullata
        verify(em, never()).merge(any()); // Il merge non deve essere chiamato se non esiste!
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
    }

    @Test
    void delete_ShouldThrowEmptyFild_WhenIdIsNull() {
        Utente u = new Utente();
        u.setId(null);

        assertThrows(EmptyFild.class, () -> dao.delete(u));
    }

    @Test
    void delete_ShouldThrowEmptyFild_WhenUtenteIsInvalid() {
        Utente u = null;

        assertThrows(EmptyFild.class, () -> dao.delete(u));
    }
}
