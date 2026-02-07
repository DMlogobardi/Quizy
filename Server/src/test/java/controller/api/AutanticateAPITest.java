package controller.api;

import io.jsonwebtoken.MalformedJwtException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.dao.UtenteDAO;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.LoginFailed;
import model.exception.RegisterFailed;
import model.menager.AutanticateMenager;
import model.utility.AccessControlService;
import model.utility.JWT_Provider;
import model.utility.PassCrypt;
import model.utility.SessionLog;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AutanticateAPITest {

    // ===========================
    // === UNIT TESTS (Mockito) ===
    // ===========================
    @Nested
    class UnitTests {
        private AutanticateAPI api;
        private AutanticateMenager authMock;

        @BeforeEach
        void setUp() throws Exception {
            api = new AutanticateAPI();
            authMock = mock(AutanticateMenager.class);
            // Iniezione manuale del mock
            Field field = AutanticateAPI.class.getDeclaredField("auth");
            field.setAccessible(true);
            field.set(api, authMock);
        }

        //login test
        @Test
        void login_ShouldReturn200_WhenCredentialsValid() throws Exception {
            Utente u = new Utente();
            u.setUsername("user");
            u.setPasswordHash("pwd");
            when(authMock.autenticate("pwd", "user")).thenReturn("token-123");

            Response r = api.login(u);
            assertEquals(200, r.getStatus());
        }

        @Test
        void login_ShouldReturn401_WhenLoginFailed() throws Exception {
            Utente u = new Utente();
            u.setId(1);
            u.setUsername("pino");
            u.setPasswordHash("pino1234");

            when(authMock.autenticate(anyString(), anyString())).thenThrow(new LoginFailed(""));
            Response r = api.login(u);
            assertEquals(401, r.getStatus());
        }

        //test register
        @Test
        void register_ShouldReturn204_WhenContentIsNull(){
            Response r = api.register(null);
            assertEquals(204, r.getStatus());
        }

        @Test
        void register_ShouldReturn400_WhenRegitrationFailed(){
            Utente u = new Utente();
            u.setId(1);
            u.setUsername("pino");
            u.setPasswordHash("pino1234");

            doThrow(new RegisterFailed("")).when(authMock).registra(any(Utente.class));

            Response r = api.register(u);
            assertEquals(400, r.getStatus());
        }

        @Test
        void register_ShouldReturn200_WhenRegitrationIsOk(){
            Utente u = new Utente();
            u.setId(1);
            u.setUsername("pino");
            u.setPasswordHash("pino1234");

            Response r = api.register(u);
            assertEquals(200, r.getStatus());
        }

        //test logout
        @Test
        void logout_ShouldReturnUnauthorized_WhenHeaderIsNull() {
            Response response = api.logout(null);
            assertEquals(401, response.getStatus());
        }

        @Test
        void logout_ShouldReturnUnauthorized_WhenHeaderDoesNotStartWithBearer() {
            Response response = api.logout("InvalidToken abc123");
            assertEquals(401, response.getStatus());
        }

        @Test
        void logout_ShouldReturnBadRequest_WhenManagerThrowsAppException() throws AppException {
            String token = "valid-token";
            String header = "Bearer " + token;

            doThrow(new AppException("Error")).when(authMock).logout(token);

            Response response = api.logout(header);
            assertEquals(400, response.getStatus());
        }

        @Test
        void logout_ShouldReturnOk_WhenHappyPath() throws AppException {
            String token = "valid-token";
            String header = "Bearer " + token;

            doNothing().when(authMock).logout(token);

            Response response = api.logout(header);
            assertEquals(200, response.getStatus());
            verify(authMock, times(1)).logout(token);
        }

        //test cambiapassword
        @Test
        void cambiapassword_ShouldReturnNoContent_WhenBodyIsNull() {
            Response response = api.cambiapassword(null, "Bearer token");
            assertEquals(204, response.getStatus());
        }

        @Test
        void cambiapassword_ShouldReturnNoContent_WhenBodyIsEmpty() {
            Response response = api.cambiapassword(new HashMap<>(), "Bearer token");
            assertEquals(204, response.getStatus());
        }

        @Test
        void cambiapassword_ShouldReturnUnauthorized_WhenHeaderIsNull() {
            Map<String, String> body = Map.of("password", "new", "oldPassword", "old");

            Response response = api.cambiapassword(body, null);

            assertEquals(401, response.getStatus());
        }

        @Test
        void cambiapassword_ShouldReturnBadRequest_WhenMalformedJwtExceptionOccurs() throws AppException {
            Map<String, String> body = Map.of("password", "new", "oldPassword", "old");
            String token = "invalid-jwt";
            String header = "Bearer " + token;

            doThrow(MalformedJwtException.class).when(authMock).newPassword("new", "old", token);

            Response response = api.cambiapassword(body, header);
            assertEquals(400, response.getStatus());
        }

        @Test
        void cambiapassword_ShouldReturnUnauthorized_WhenHeaderNotContainsBearer() {
            Map<String, String> body = Map.of("password", "new", "oldPassword", "old");
            String badHeader = "gegazgrs"; // Manca il token effettivo

            Response response = api.cambiapassword(body, badHeader);

            assertEquals(401, response.getStatus());
        }

        @Test
        void cambiapassword_ShouldReturnBadRequest_WhenAppExceptionOccurs() throws AppException {
            Map<String, String> body = Map.of("password", "new", "oldPassword", "old");
            String token = "valid-token";
            String header = "Bearer " + token;

            doThrow(new AppException("Error")).when(authMock).newPassword("new", "old", token);

            Response response = api.cambiapassword(body, header);
            assertEquals(400, response.getStatus());
        }

        @Test
        void cambiapassword_ShouldReturnOk_WhenHappyPath() throws AppException {
            Map<String, String> body = Map.of("password", "newPass123", "oldPassword", "oldPass123");
            String token = "valid-token";
            String header = "Bearer " + token;

            doNothing().when(authMock).newPassword("newPass123", "oldPass123", token);

            Response response = api.cambiapassword(body, header);

            assertEquals(200, response.getStatus());
            verify(authMock, times(1)).newPassword("newPass123", "oldPass123", token);
        }
    }

    // =========================
    // === INTEGRATION TESTS ===
    // =========================
    @Nested
    class IntegrationTests extends JerseyTest {
        // Dipendenze reali (non mock)
        private EntityManagerFactory emf;
        private EntityManager em;
        private JWT_Provider jwtProvider;
        private SessionLog log;
        private PassCrypt crypt;
        private UtenteDAO dao;
        private AccessControlService service;
        private AutanticateMenager menager;

        private Utente alreadyLogTest;
        private String realToken;

        @Override
        protected Application configure() {
            try {
                initFullStack();
            } catch (Exception e) {
                throw new RuntimeException("Errore nel setup dello stack reale", e);
            }

            ResourceConfig config = new ResourceConfig(AutanticateAPI.class);

            config.register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(menager).to(AutanticateMenager.class);
                    bind(log).to(SessionLog.class);
                    bind(service).to(AccessControlService.class);
                    bind(jwtProvider).to(JWT_Provider.class);
                    bind(crypt).to(PassCrypt.class);
                    bind(dao).to(UtenteDAO.class);
                }
            });

            return config;
        }

        // QUESTO È IL METODO CHE CONFIGURA TUTTO
        private void initFullStack() throws Exception {
            // Database H2 in memoria
            emf = Persistence.createEntityManagerFactory("testPU");
            em = emf.createEntityManager();

            // DAO e iniezione EM
            dao = new UtenteDAO();
            injectMethod(dao, em, "em");

            // Utility e Servizi
            jwtProvider = new JWT_Provider();
            crypt = new PassCrypt();

            log = new SessionLog();
            injectMethod(log, jwtProvider, "jwtProvider");

            service = new AccessControlService();
            injectMethod(service, jwtProvider, "jwtProvider");

            // Manager reale con tutte le dipendenze iniettate
            menager = new AutanticateMenager();
            injectMethod(menager, log, "logBeble");
            injectMethod(menager, crypt, "crypt");
            injectMethod(menager, dao, "dao");
            injectMethod(menager, service, "accessControlService");

            // Prepariamo i dati nel DB reale
            em.getTransaction().begin();
            Utente mario = new Utente();
            mario.setNome("Mario");
            mario.setCognome("Rossi");
            mario.setUsername("mariorossi");
            // IMPORTANTE: La password nel DB deve essere già hashata dal tuo crypt
            mario.setPasswordHash(crypt.hashPassword("hash123"));
            mario.setIsCompilatore(true);
            mario.setIsCreatore(false);
            mario.setIsManager(false);
            em.persist(mario);

            alreadyLogTest = new Utente();
            alreadyLogTest.setNome("Pippo");
            alreadyLogTest.setCognome("Alberti");
            alreadyLogTest.setUsername("pippo12");
            alreadyLogTest.setPasswordHash(crypt.hashPassword("sc2435"));
            alreadyLogTest.setIsCompilatore(true);

            em.persist(alreadyLogTest);
            em.getTransaction().commit();

            // Generiamo un token reale e mettiamolo in sessione
            realToken = jwtProvider.generateToken(alreadyLogTest, "compilatore");
            log.aggiungi(realToken, alreadyLogTest);
        }

        @AfterEach
        public void cleanUp() throws NoSuchFieldException, IllegalAccessException {
            Field bibleField = SessionLog.class.getDeclaredField("logBible");
            Field userToTokenField = SessionLog.class.getDeclaredField("userIdToToken");

            bibleField.setAccessible(true);
            userToTokenField.setAccessible(true);

            ((Map<?, ?>) bibleField.get(log)).clear();
            ((Map<?, ?>) userToTokenField.get(log)).clear();

            if (em != null && em.isOpen()) em.close();
            if (emf != null && emf.isOpen()) emf.close();
        }

        @Test
        void login_Success_ShouldReturnToken() {
            // Usiamo l'utente "Mario" che nel setup non è ancora loggato
            Utente loginData = new Utente();
            loginData.setUsername("mariorossi");
            loginData.setPasswordHash("hash123");

            Response response = target("/auth/login")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(loginData));

            assertEquals(200, response.getStatus());
            Map<String, String> resBody = response.readEntity(Map.class);
            assertNotNull(resBody.get("token"));

            // Verifica che il token sia ora presente nel SessionLog reale
            assertTrue(log.isAlive(resBody.get("token")));
        }

        @Test
        void login_Failure_AlreadyLoggedIn() {
            // Usiamo "Pippo" che nel setup() ha già un 'realToken' attivo
            Utente GiàLoggato = new Utente();
            GiàLoggato.setUsername("pippo12");
            GiàLoggato.setPasswordHash("sc2435");

            Response response = target("/auth/login")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(GiàLoggato));

            // Se la tua logica impedisce il doppio login lanciando LoginFailed
            // l'API restituirà 401 Unauthorized come da tuo catch block
            assertEquals(401, response.getStatus());
        }

        @Test
        void login_Failure_WrongCredentials() {
            Utente wrongData = new Utente();
            wrongData.setUsername("mariorossi");
            wrongData.setPasswordHash("password_sbagliata");

            Response response = target("/auth/login")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(wrongData));

            assertEquals(401, response.getStatus());
        }

        @Test
        void login_Error_NoBody() {
            // Caso loginData == null -> 204 No Content
            Response response = target("/auth/login")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(null));

            assertEquals(400, response.getStatus());
        }

        @Test
        void register_Success_ShouldSaveUserToDatabase() {
            // 1. Prepariamo un utente nuovo (non presente nel setup)
            Utente nuovoUtente = new Utente();
            nuovoUtente.setNome("Luigi");
            nuovoUtente.setCognome("Verdi");
            nuovoUtente.setUsername("luigiv");
            nuovoUtente.setPasswordHash("password123");
            nuovoUtente.setIsCompilatore(true);
            nuovoUtente.setIsCreatore(false);
            nuovoUtente.setIsManager(false);

            // 2. Chiamata all'endpoint
            Response response = target("/auth/register")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(nuovoUtente));

            assertEquals(200, response.getStatus());

            // 3. VERIFICA REALE SUL DB: controlliamo se l'utente esiste davvero
            // Puliamo la cache per essere sicuri di leggere dal DB H2
            em.clear();

            TypedQuery<Utente> query = em.createQuery("SELECT u FROM Utente u WHERE u.username = :user", Utente.class);
            Utente trovato = query.setParameter("user", "luigiv").getSingleResult();

            assertNotNull(trovato);
            assertEquals("Luigi", trovato.getNome());
            // Verifica che la password sia stata hashata durante la registrazione
            assertTrue(crypt.verificaPassword("password123", trovato.getPasswordHash()));
        }

        @Test
        void register_Failure_DuplicateUsername() {
            // Usiamo lo username "mariorossi" che abbiamo già inserito nel setup()
            Utente duplicato = new Utente();
            duplicato.setUsername("mariorossi");
            duplicato.setPasswordHash("nuovapass");

            Response response = target("/auth/register")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(duplicato));

            // Se il tuo auth.registra lancia RegisterFailed per username duplicato
            assertEquals(400, response.getStatus());
        }

        @Test
        void logout_Integration_Success() {
            Response response = target("/auth/logout")
                    .request()
                    .header("Authorization", "Bearer " + realToken)
                    .post(Entity.json(null));

            assertEquals(200, response.getStatus());
            assertFalse(log.isAlive(realToken));
        }

        @Test
        void logout_Integration_TokenNotFound() {
            String fakeToken = "Bearer non-existent-token";

            Response response = target("/auth/logout")
                    .request()
                    .header("Authorization", fakeToken)
                    .post(Entity.json(null));

            assertEquals(400, response.getStatus());
        }

        @Test
        void cambiapassword_Integration_Success_ShouldUpdateDatabase() {
            Map<String, String> body = new HashMap<>();
            body.put("password", "newSecret123");
            body.put("oldPassword", "sc2435"); // Password originale di alreadyLogTest

            Response response = target("/auth/newPassword")
                    .request()
                    .header("Authorization", "Bearer " + realToken)
                    .post(Entity.json(body));

            assertEquals(200, response.getStatus());

            em.clear();
            Utente utenteAggiornato = em.find(Utente.class, alreadyLogTest.getId());

            assertFalse(crypt.verificaPassword("sc2435", utenteAggiornato.getPasswordHash()));
            assertTrue(crypt.verificaPassword("newSecret123", utenteAggiornato.getPasswordHash()));
        }

        @Test
        void cambiapassword_Integration_Failure_WrongOldPassword() {
            Map<String, String> body = new HashMap<>();
            body.put("password", "anyPassword");
            body.put("oldPassword", "password_completamente_errata");

            Response response = target("/auth/newPassword")
                    .request()
                    .header("Authorization", "Bearer " + realToken)
                    .post(Entity.json(body));

            assertEquals(400, response.getStatus());

            em.clear();
            Utente utenteInvariato = em.find(Utente.class, alreadyLogTest.getId());
            assertTrue(crypt.verificaPassword("sc2435", utenteInvariato.getPasswordHash()));
        }

        @Test
        void cambiapassword_Integration_Failure_TokenNotAuthorized() {
            Map<String, String> body = Map.of("password", "newPass", "oldPassword", "sc2435");

            Utente fake = new Utente();
            fake.setId(999);
            fake.setUsername("unknown");
            String tokenInesistente = jwtProvider.generateToken(fake, "compilatore");

            Response response = target("/auth/newPassword")
                    .request()
                    .header("Authorization", "Bearer " + tokenInesistente)
                    .post(Entity.json(body));

            assertEquals(400, response.getStatus());
        }
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
