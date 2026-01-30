package model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import model.entity.Quiz;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuizDAOTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private QuizDAO quizDAO;



    /**
     * Category Partition per findById:
     * 1. Input Valido (ID esistente) -> Restituisce l'oggetto Quiz.
     * 2. Input Valido (ID inesistente) -> Restituisce null.
     * 3. Input Invalido (ID <= 0) -> Lancia eccezione.
     */

    @Test
    void findById_shouldReturnQuiz_whenIdExists() throws AppException {
        int id = 1;
        Quiz fintoQuiz = new Quiz();
        when(em.find(Quiz.class, id)).thenReturn(fintoQuiz);


        Quiz result = quizDAO.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(fintoQuiz, result);
        verify(em).find(Quiz.class, id);
    }

    @Test
    void findById_shouldReturnNull_whenIdDoesNotExist() throws AppException {
        int id = 999;
        when(em.find(Quiz.class, id)).thenReturn(null);

        Quiz result = quizDAO.findById(id);

        assertNull(result);
    }

    @Test
    void findById_shouldThrowException_whenIdIsInvalid() {
        assertThrows(AppException.class, () -> quizDAO.findById(0));
    }

    /**
     * Category Partition per findAll:
     * 1. Input Valido (pageNumber > 0, pageSize > 0) -> Restituisce lista di Quiz.
     * 2. Input Invalido (pageNumber <= 0) -> Lancia AppException.
     * 3. Input Invalido (pageSize <= 0) -> Lancia AppException.
     */

    @Test
    void findAll_shouldReturnList_whenInputsAreValid() throws AppException {
        // Arrange
        int pageNumber = 1;
        int pageSize = 10;
        List<Quiz> expectedList = new ArrayList<>();
        
        // Mock della catena di chiamate della TypedQuery
        TypedQuery<Quiz> mockedQuery = mock(TypedQuery.class);
        when(em.createNamedQuery("Quiz.findAll", Quiz.class)).thenReturn(mockedQuery);
        when(mockedQuery.setFirstResult(anyInt())).thenReturn(mockedQuery);
        when(mockedQuery.setMaxResults(anyInt())).thenReturn(mockedQuery);
        when(mockedQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Quiz> result = quizDAO.findAll(pageNumber, pageSize);

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockedQuery).setFirstResult(0); // (1 - 1) * 10 = 0
        verify(mockedQuery).setMaxResults(pageSize);
    }

    @Test
    void findAll_shouldThrowException_whenInputsAreInvalid() {
        assertThrows(AppException.class, () -> quizDAO.findAll(0, 10));
        assertThrows(AppException.class, () -> quizDAO.findAll(1, 0));
    }

    /**
     *  Category Partition per findAllByUtente:
     *  1. Input Valido (pageNumber > 0 , Utente.id >0)
     *  2. Input invalido (pageNumber <=0)
     *  3. Input invalido (Utente.id <=0)
     *  4. Input invalido (Utente == null)
     */

    @Test
    void findAllByUtente_shouldReturnList_whenInputsAreValid() throws EntityNotFoundException, EmptyFild{
        int pageNumber = 1;
        Utente utenteFinto = new Utente();
        utenteFinto.setId(1);
        List<Quiz> expectedList = new ArrayList<>();
        TypedQuery<Quiz> mockedQuery = mock(TypedQuery.class);

        when(em.createNamedQuery("Quiz.findAllByUtente", Quiz.class)).thenReturn(mockedQuery);
        when(mockedQuery.setParameter(anyString(), any())).thenReturn(mockedQuery);
        when(mockedQuery.setFirstResult(anyInt())).thenReturn(mockedQuery);
        when(mockedQuery.setMaxResults(anyInt())).thenReturn(mockedQuery);
        when(mockedQuery.getResultList()).thenReturn(expectedList);

        List<Quiz> result = quizDAO.findAllByUtente(pageNumber, utenteFinto);

        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockedQuery).setFirstResult(0);
    }

    //test failde per mancato controllo, inserito successivamente
    @Test
    void findAllByUtente_shouldThrowException_whenInputsAreInvalid() {
        TypedQuery<Quiz> mockedQuery = mock(TypedQuery.class);

        Utente utenteValido = new Utente();
        utenteValido.setId(1);

        Utente utenteIdZero = new Utente();
        utenteIdZero.setId(0);

        assertThrows(AppException.class, () -> quizDAO.findAllByUtente(0, utenteValido));
        assertThrows(EmptyFild.class, () -> quizDAO.findAllByUtente(1, utenteIdZero));
        assertThrows(EmptyFild.class, () -> quizDAO.findAllByUtente(1, null));
    }

    /**
     * Category Partition per insert:
     * 1. Input Valido (Quiz != null) -> Persist su DB.
     * 2. Input Invalido (Quiz == null) -> Lancia EmptyFild.
     * 3. Errore durante la transazione -> Lancia EntityNotFoundException e fa rollback.
     */

    @Test
    void insert_shouldPersistQuiz_whenQuizIsValid() throws EntityNotFoundException, EmptyFild {
        Quiz quiz = new Quiz();
        EntityTransaction tx = mock(EntityTransaction.class);
        when(em.getTransaction()).thenReturn(tx);

        quizDAO.insert(quiz);

        verify(tx).begin();
        verify(em).persist(quiz);
        verify(tx).commit();
    }

    @Test
    void insert_shouldThrowEmptyFild_whenQuizIsNull() {
        assertThrows(EmptyFild.class, () -> quizDAO.insert(null));
    }

    @Test
    void insert_shouldThrowEntityNotFoundException_andRollback_whenExceptionOccurs() {
        Quiz quiz = new Quiz();
        EntityTransaction tx = mock(EntityTransaction.class);
        when(em.getTransaction()).thenReturn(tx);
        doThrow(new RuntimeException("DB Error")).when(em).persist(quiz);
        when(tx.isActive()).thenReturn(true);

        assertThrows(EntityNotFoundException.class, () -> quizDAO.insert(quiz));

        verify(tx).begin();
        verify(em).persist(quiz);
        verify(tx).rollback();
    }

    /**
     * Category Partition per update:
     * 1. Input validi (quiz nuovo != null, quiz vecchio null, utente valido con id>0)
     * 2. Input validi (quiz nuovo != null, quiz vecchio != null, utente valido con id>0)
     * 3. Input invalido (quiz nuovo null) -> Non gestito esplicitamente nel codice ma implicito se quiz nuovo è usato
     * 4. Input invalido (quiz nuovo != null, quiz vecchio qualsiasi, utente null)
     * 5. Input invalido (quiz nuovo != null, quiz vecchio qualsiasi, utente.id null)
     * 6. Input invalido (quiz nuovo != null, quiz vecchio qualsiasi, utente.id =0) -> Non gestito esplicitamente nel codice
     */

    @Test
    void update_shouldUpdateQuiz_whenInputsAreValid_andOldQuizIsNull() throws EntityNotFoundException, EmptyFild {
        // Arrange
        Quiz newQuiz = new Quiz();
        newQuiz.setId(1);
        newQuiz.setTitolo("Nuovo Titolo");
        newQuiz.setDomande(new ArrayList<>());

        Utente utente = new Utente();
        utente.setId(1);

        Quiz oldQuizFromDb = new Quiz();
        oldQuizFromDb.setId(2);
        oldQuizFromDb.setDomande(new ArrayList<>());

        EntityTransaction tx = mock(EntityTransaction.class);
        when(em.getTransaction()).thenReturn(tx);

        TypedQuery<Quiz> mockedQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Quiz.class))).thenReturn(mockedQuery);
        when(mockedQuery.setParameter(anyString(), any())).thenReturn(mockedQuery);
        when(mockedQuery.getResultStream()).thenReturn(Stream.of(oldQuizFromDb));

        // Act
        quizDAO.update(newQuiz, null, utente);

        // Assert
        verify(tx).begin();
        verify(em).merge(oldQuizFromDb);
        verify(tx).commit();
        assertEquals("Nuovo Titolo", oldQuizFromDb.getTitolo());
    }

    @Test
    void update_shouldUpdateQuiz_whenInputsAreValid_andOldQuizIsNotNull() throws EntityNotFoundException, EmptyFild {
        // Arrange
        Quiz newQuiz = new Quiz();
        newQuiz.setTitolo("Nuovo Titolo");
        newQuiz.setDomande(new ArrayList<>());

        Quiz oldQuiz = new Quiz();
        oldQuiz.setDomande(new ArrayList<>());

        Utente utente = new Utente();
        utente.setId(1);

        EntityTransaction tx = mock(EntityTransaction.class);
        when(em.getTransaction()).thenReturn(tx);

        // Act
        quizDAO.update(newQuiz, oldQuiz, utente);

        // Assert
        verify(tx).begin();
        verify(em).merge(oldQuiz);
        verify(tx).commit();
        assertEquals("Nuovo Titolo", oldQuiz.getTitolo());
    }

    @Test
    void update_shouldThrowEmptyFild_whenUtenteIsNull() {
        Quiz newQuiz = new Quiz();
        assertThrows(EmptyFild.class, () -> quizDAO.update(newQuiz, null, null));
    }

    @Test
    void update_shouldThrowEmptyFild_whenUtenteIdIsNull() {
        Quiz newQuiz = new Quiz();
        Utente utente = new Utente();
        utente.setId(null);
        assertThrows(EmptyFild.class, () -> quizDAO.update(newQuiz, null, utente));
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenOldQuizNotFound() {
        Quiz newQuiz = new Quiz();
        newQuiz.setId(1);
        Utente utente = new Utente();
        utente.setId(1);

        EntityTransaction tx = mock(EntityTransaction.class);
        when(em.getTransaction()).thenReturn(tx);

        TypedQuery<Quiz> mockedQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Quiz.class))).thenReturn(mockedQuery);
        when(mockedQuery.setParameter(anyString(), any())).thenReturn(mockedQuery);
        when(mockedQuery.getResultStream()).thenReturn(Stream.empty());

        assertThrows(EntityNotFoundException.class, () -> quizDAO.update(newQuiz, null, utente));
        verify(tx).begin();
        if (tx != null && tx.isActive()) verify(tx).rollback();
    }

    /**
     *  Category partition per delete:
     *  1. Input validi (idQuiz >0, quiz null, utente.id >0)
     *  2. Input validi (idQuiz >0, quiz != null,  utente.id >0)
     *  3. Input invalidi (idQuiz null)
     *  4. Input invalidi (utente null)
     *  5. Input invalidi (utente.id null)
     */

    @Test
    void delete_shouldRemoveQuiz_whenInputsAreValid_andQuizIsNull() throws EntityNotFoundException, EmptyFild {
        int quizId = 1;
        Utente utente = new Utente();
        utente.setId(1);
        Quiz quizFromDb = new Quiz();

        EntityTransaction tx = mock(EntityTransaction.class);
        when(em.getTransaction()).thenReturn(tx);

        TypedQuery<Quiz> mockedQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Quiz.class))).thenReturn(mockedQuery);
        when(mockedQuery.setParameter(anyString(), any())).thenReturn(mockedQuery);
        when(mockedQuery.getResultStream()).thenReturn(Stream.of(quizFromDb));

        quizDAO.delete(quizId, null, utente);

        verify(tx).begin();
        verify(em).remove(quizFromDb);
        verify(tx).commit();
    }

    @Test
    void delete_shouldRemoveQuiz_whenInputsAreValid_andQuizIsNotNull() throws EntityNotFoundException, EmptyFild {
        int quizId = 1;
        Utente utente = new Utente();
        utente.setId(1);
        Quiz quiz = new Quiz();

        EntityTransaction tx = mock(EntityTransaction.class);
        when(em.getTransaction()).thenReturn(tx);

        quizDAO.delete(quizId, quiz, utente);

        verify(tx).begin();
        verify(em).remove(quiz);
        verify(tx).commit();
    }

    @Test
    void delete_shouldThrowEmptyFild_whenUtenteIsNull() {
        assertThrows(EmptyFild.class, () -> quizDAO.delete(1, null, null));
    }

    @Test
    void delete_shouldThrowEmptyFild_whenUtenteIdIsNull() {
        Utente utente = new Utente();
        utente.setId(null);
        assertThrows(EmptyFild.class, () -> quizDAO.delete(1, null, utente));
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenQuizNotFound() {
        int quizId = 1;
        Utente utente = new Utente();
        utente.setId(1);

        TypedQuery<Quiz> mockedQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Quiz.class))).thenReturn(mockedQuery);
        when(mockedQuery.setParameter(anyString(), any())).thenReturn(mockedQuery);
        when(mockedQuery.getResultStream()).thenReturn(Stream.empty());

        assertThrows(EntityNotFoundException.class, () -> quizDAO.delete(quizId, null, utente));
    }
}
