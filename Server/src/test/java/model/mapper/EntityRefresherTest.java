package model.mapper;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EntityRefresherTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private EntityRefresher entityRefresher;

    // =========================
    // ===== UNIT TESTS ========
    // =========================

    //test del metodo reattach
    @Test
    void reattach_ShouldReturnNull_WhenEntityIsNull() {
        assertNull(entityRefresher.reattach(null));

        verify(em, never()).merge(any());
    }

    @Test
    void reattach_ShouldReturnManageEntity_WhenEntityIsValid() {
        Object detachedEntity = new Object();
        Object managedEntity = new Object();

        when(em.merge(detachedEntity)).thenReturn(managedEntity);

        Object result = new Object();
        result = entityRefresher.reattach(detachedEntity);

        assertEquals(managedEntity, result);
        verify(em).merge(detachedEntity);
    }
}
