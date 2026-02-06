package controller.api;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Nested;

import java.lang.reflect.Field;

public class QuizCreatorAPITest {

    // ===========================
    // === UNIT TESTS (Mockito) ===
    // ===========================
    @Nested
    class UnitTests {

    }

    // =========================
    // === INTEGRATION TESTS ===
    // =========================
    @Nested
    class IntegrationTests extends JerseyTest {

    }

    // =========================
    // ===== UTIL METHOD =======
    // =========================
    private void injectMethod(Object component, Object injectComponent, String nameFild) throws Exception {
        Class<?> clazz = component.getClass();

        Field f = clazz.getDeclaredField(nameFild);

        f.setAccessible(true);
        f.set(component, injectComponent);
    }
}
