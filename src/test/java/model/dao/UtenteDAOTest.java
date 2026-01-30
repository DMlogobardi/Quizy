package model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import model.entity.Utente;
import model.exception.EmptyFild;
import model.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        assertEquals("Username non trovato", exception.getMessage());
    }

    //test findAllPaginated
    @Test
    void
}
