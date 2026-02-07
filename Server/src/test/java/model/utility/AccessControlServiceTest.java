package model.utility;

import jakarta.persistence.EntityManager;
import model.entity.Utente;
import model.exception.InvalidRole;
import model.mapper.EntityRefresher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AccessControlServiceTest {

    JWT_Provider jwtProvider;
    AccessControlService service;

    // =========================
    // ===== UNIT TESTS ========
    // =========================
    @Nested
    @Tag("unit")
    class UnitTests {
        @Mock
        JWT_Provider jwtProviderMocked;

        @BeforeEach
        void setup() throws Exception {
            service = new AccessControlService();
            jwtProvider = jwtProviderMocked;
            injectProvider(service, jwtProvider);
        }

        //test checkCreatore
        @Test
        void checkCreatore_ShouldPass_WhenRoleIsCreatore() throws Exception {
            String fakeToken = "valid.token";
            Mockito.when(jwtProviderMocked.getRoleFromToken(fakeToken)).thenReturn("creatore");

            assertDoesNotThrow(() -> service.checkCreatore(fakeToken));
        }

        @Test
        void checkCreatore_ShouldThrowInvalidRole_WhenRoleIsWrong() throws Exception {
            String fakeToken = "valid.token";
            Mockito.when(jwtProviderMocked.getRoleFromToken(fakeToken)).thenReturn("manager");

            assertThrows(InvalidRole.class, () -> service.checkCreatore(fakeToken));
        }

        //test checkCompilatore
        @Test
        void checkCompilatore_ShouldPass_WhenRoleIsCompilatore() throws Exception {
            String fakeToken = "valid.token";
            Mockito.when(jwtProviderMocked.getRoleFromToken(fakeToken)).thenReturn("compilatore");

            assertDoesNotThrow(() -> service.checkCompilatore(fakeToken));
        }

        @Test
        void checkCompilatore_ShouldThrowInvalidRole_WhenRoleIsWrong() throws Exception {
            String fakeToken = "valid.token";
            Mockito.when(jwtProviderMocked.getRoleFromToken(fakeToken)).thenReturn("manager");

            assertThrows(InvalidRole.class, () -> service.checkCompilatore(fakeToken));
        }

        //test checkManager
        @Test
        void checkManager_ShouldPass_WhenRoleIsCompilatore() throws Exception {
            String fakeToken = "valid.token";
            Mockito.when(jwtProviderMocked.getRoleFromToken(fakeToken)).thenReturn("manager");

            assertDoesNotThrow(() -> service.checkManager(fakeToken));
        }

        @Test
        void checkManager_ShouldThrowInvalidRole_WhenRoleIsWrong() throws Exception {
            String fakeToken = "valid.token";
            Mockito.when(jwtProviderMocked.getRoleFromToken(fakeToken)).thenReturn("compilatore");

            assertThrows(InvalidRole.class, () -> service.checkManager(fakeToken));
        }

        //test newTokenByRole
        @Test
        void newTokenByRole_ShouldReturnToken_WhenProviderIsCalled() {
            Utente u = new Utente();
            u.setId(1);
            String role = "manager";
            String expectedToken = "fake.jwt.token";

            Mockito.when(jwtProviderMocked.generateToken(u, role)).thenReturn(expectedToken);

            String result = service.newTokenByRole(role, u);

            assertEquals(expectedToken, result);
            Mockito.verify(jwtProviderMocked, Mockito.times(1)).generateToken(u, role);
        }

        @Test
        void newTokenByRole_ShouldThrowIllegalArgumentException_WhenProviderThrowsIt() {
            Mockito.when(jwtProviderMocked.generateToken(null, "manager"))
                    .thenThrow(new IllegalArgumentException("Utente non valido"));

            assertThrows(IllegalArgumentException.class, () -> service.newTokenByRole("manager", null));
        }
    }

    // =========================
    // === INTEGRATION TESTS ===
    // =========================
    @Nested
    @Tag("integration")
    class IntegrationTests {
        @BeforeEach
        void setup() throws Exception {
            service = new AccessControlService();
            jwtProvider = new JWT_Provider();
            injectProvider(service, jwtProvider);
        }

        @Test
        void newTokenByRole_ShouldGenerateValidVerifiableToken() {
            Utente u = new Utente();
            u.setId(99);
            String role = "creatore";

            String token = service.newTokenByRole(role, u);

            assertNotNull(token);
            assertDoesNotThrow(() -> jwtProvider.validateToken(token));
            assertEquals(role, assertDoesNotThrow(() -> jwtProvider.getRoleFromToken(token)));
            assertEquals(99, assertDoesNotThrow(() -> jwtProvider.getIdFromToken(token)));
        }

        @Test
        void checkCreatore_ShouldPass_WhenTokenIsCorrect() {
            Utente u = new Utente();
            u.setId(1);
            String token = jwtProvider.generateToken(u, "creatore");

            assertDoesNotThrow(() -> service.checkCreatore(token));
        }

        @Test
        void checkCreatore_ShouldThrowInvalidRole_WhenTokenHasWrongRole() {
            Utente u = new Utente();
            u.setId(1);
            String token = jwtProvider.generateToken(u, "manager");

            assertThrows(InvalidRole.class, () -> service.checkCreatore(token));
        }

        @Test
        void checkCompilatore_ShouldPass_WhenTokenIsCorrect() {
            Utente u = new Utente();
            u.setId(1);
            String token = jwtProvider.generateToken(u, "compilatore");

            assertDoesNotThrow(() -> service.checkCompilatore(token));
        }

        @Test
        void checkCompilatore_ShouldThrowInvalidRole_WhenTokenHasWrongRole() {
            Utente u = new Utente();
            u.setId(1);
            String token = jwtProvider.generateToken(u, "creatore");

            assertThrows(InvalidRole.class, () -> service.checkCompilatore(token));
        }

        @Test
        void checkManager_ShouldPass_WhenTokenIsCorrect() {
            Utente u = new Utente();
            u.setId(1);
            String token = jwtProvider.generateToken(u, "manager");

            assertDoesNotThrow(() -> service.checkManager(token));
        }

        @Test
        void checkManager_ShouldThrowInvalidRole_WhenTokenHasWrongRole() {
            Utente u = new Utente();
            u.setId(1);
            String token = jwtProvider.generateToken(u, "compilatore");

            assertThrows(InvalidRole.class, () -> service.checkManager(token));
        }

        @Test
        void newTokenByRole_ShouldGenerateVerifiableToken() {
            Utente u = new Utente();
            u.setId(50);
            String role = "manager";

            String token = service.newTokenByRole(role, u);

            assertNotNull(token);
            assertEquals(role, assertDoesNotThrow(() -> jwtProvider.getRoleFromToken(token)));
            assertEquals(50, assertDoesNotThrow(() -> jwtProvider.getIdFromToken(token)));
        }

        @Test
        void serviceMethods_ShouldThrowTokenExpiredException_WhenTokenIsOld() {
            final String SECRET_STRING = "z2p7W8p5v9B8R3M1x8C4k7J2q5N0t3Z9";
            javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

            String expiredToken = io.jsonwebtoken.Jwts.builder()
                    .subject("creatore")
                    .expiration(new java.util.Date(System.currentTimeMillis() - 600000))
                    .signWith(key)
                    .compact();

            assertThrows(model.exception.TokenExpiredException.class, () -> service.checkCreatore(expiredToken));
        }
    }

    // =========================
    // ===== UTIL METHOD =======
    // =========================
    private void injectProvider(Object service, JWT_Provider provider) throws Exception {
        Class<?> clazz = service.getClass();

        Field f = clazz.getDeclaredField("jwtProvider");

        f.setAccessible(true);
        f.set(service, provider);
    }
}
