package model.menager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.dao.UtenteDAO;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.LoginFailed;
import model.exception.RegisterFailed;
import model.exception.UserNotFoundException;
import model.utility.AccessControlService;
import model.utility.JWT_Provider;
import model.utility.PassCrypt;
import model.utility.SessionLog;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AutenticateMenagerTest {

    AutanticateMenager menager;

    // =========================
    // ===== UNIT TESTS ========
    // =========================
    @Nested
    @Tag("unit")
    class UnitTests {
        @Mock
        SessionLog logMock;

        @Mock
        PassCrypt cryptMock;

        @Mock
        UtenteDAO daoMock;

        @Mock
        AccessControlService serviceMock;

        @BeforeEach
        void setup() throws Exception {
            menager = new AutanticateMenager();

            injectMethod(menager, logMock, "logBeble");
            injectMethod(menager, cryptMock, "crypt");
            injectMethod(menager, daoMock, "dao");
            injectMethod(menager, serviceMock, "accessControlService");
        }

        //test autenticate
        @Test
        void autenticate_ShouldReturnLoginFailed_WhenUserNotExistInDB() {
            String username = "mario.rossi";

            when(daoMock.findForLogin(username)).thenThrow(new UserNotFoundException(""));

            assertThrows(LoginFailed.class, () -> menager.autenticate("paswordDiprova123", username));
        }

        @Test
        void autenticate_ShouldReturnLoginFailed_WhenPasswordIsWrong() {
            String username = "mario.rossi";
            String passwordSbagliata = "1234";
            Utente test = new Utente();
            test.setId(1);
            test.setUsername(username);
            test.setPasswordHash("paswordDiprova123");
            test.setIsCompilatore(true);


            when(daoMock.findForLogin(username)).thenReturn(test);
            when(cryptMock.verificaPassword(passwordSbagliata, test.getPasswordHash())).thenReturn(false);

            assertThrows(LoginFailed.class, () -> menager.autenticate(passwordSbagliata, username));
        }

        @Test
        void autenticate_ShouldReturnLoginFailed_WhenSessionAlreadyExist() {
            String username = "mario.rossi";
            String password = "paswordDiprova123";
            Utente test = new Utente();
            test.setId(1);
            test.setUsername(username);
            test.setPasswordHash(password);
            test.setIsCompilatore(true);

            String fakeToken = "&token_Finto-Che_Più-Finto_Non-Si_Puo&";
            when(daoMock.findForLogin(username)).thenReturn(test);
            when(cryptMock.verificaPassword(password, test.getPasswordHash())).thenReturn(true);
            when(serviceMock.newTokenByRole("compilatore", test)).thenReturn(fakeToken);
            doThrow(new AppException("Sessione già presente")).when(logMock).aggiungi(fakeToken, test);

            assertThrows(LoginFailed.class, () -> menager.autenticate(password, username));
        }

        @Test
        void autenticate_ShouldReturnToken_WhenCredentialsIsCorrect() {
            String username = "mario.rossi";
            String password = "paswordDiprova123";
            Utente test = new Utente();
            test.setId(1);
            test.setUsername(username);
            test.setPasswordHash(password);
            test.setIsCompilatore(true);

            String fakeToken = "&token_Finto-Che_Più-Finto_Non-Si_Puo&";
            when(daoMock.findForLogin(username)).thenReturn(test);
            when(cryptMock.verificaPassword(password, test.getPasswordHash())).thenReturn(true);
            when(serviceMock.newTokenByRole("compilatore", test)).thenReturn(fakeToken);

            String token = menager.autenticate(password, username);

            assertNotNull(token);
            assertEquals(fakeToken, token);
            verify(logMock).aggiungi(eq(token), any(Utente.class));
        }

        //test logout
        @Test
        void logout_ShouldCallRimuoviOnLog() {
            String token = "token-da-eliminare";

            menager.logout(token);

            verify(logMock, times(1)).rimuovi(token);
        }

        //test registra
        @Test
        void registra_ShouldReturnRegisterFailed_WhenUsernameAlreadyExist() {
            String username = "mario.rossi";
            String password = "paswordDiprova123";
            Utente test = new Utente();
            test.setId(1);
            test.setUsername(username);
            test.setPasswordHash(password);
            test.setIsCompilatore(true);

            when(daoMock.findForLogin(username)).thenReturn(test);

            assertThrows(RegisterFailed.class, () -> menager.registra(test));
        }

        @Test
        void registra_ShouldReturnRegisterFailed_WhenExceptionOccurs() {
            String username = "mario.rossi";
            String password = "paswordDiprova123";
            Utente test = new Utente();
            test.setId(1);
            test.setUsername(username);
            test.setPasswordHash(password);
            test.setIsCompilatore(true);

            when(daoMock.findForLogin(username)).thenReturn(null);
            doThrow(new RuntimeException()).when(daoMock).register(any(Utente.class));

            assertThrows(RegisterFailed.class, () -> menager.registra(test));
        }

        @Test
        void registra_ShouldRegister_WhenUsernameIsValid() {
            String username = "mario.rossi";
            String password = "paswordDiprova123";
            String passwordHashata = "HASH_SECRET_999";
            Utente test = new Utente();
            test.setId(1);
            test.setUsername(username);
            test.setPasswordHash(password);
            test.setIsCompilatore(true);

            when(daoMock.findForLogin(username)).thenReturn(null);
            when(cryptMock.hashPassword(password)).thenReturn(passwordHashata);

            menager.registra(test);

            verify(daoMock).register(argThat(u ->
                    u.getUsername().equals(username) &&
                            u.getPasswordHash().equals(passwordHashata) &&
                            u.getIsCompilatore() &&
                            !u.getIsCreatore() &&
                            !u.getIsManager()
            ));
        }

        //test newPassword
        @Test
        void newPassword_SessioneNonAttiva_LanciaAppException() throws Exception {
            String token = "invalid-token";
            when(logMock.isAlive(token)).thenReturn(false);

            assertThrows(AppException.class, () -> menager.newPassword("new", "old", token));

            verify(cryptMock, times(0)).verificaPassword(anyString(), anyString());
            verifyNoInteractions(daoMock);
        }

        @Test
        void newPassword_VecchiaPasswordErrata_LanciaAppException() throws Exception {
            String token = "valid-token";
            String oldP = "wrong-old";
            Utente u = new Utente();
            u.setPasswordHash("hash-reale");

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(cryptMock.verificaPassword(eq(oldP), anyString())).thenReturn(false);

            assertThrows(AppException.class, () -> menager.newPassword("new", oldP, token));

            verify(daoMock, times(0)).update(any());
        }

        @Test
        void newPassword_ErroreDatabase_LanciaAppException() throws Exception {
            String token = "valid-token";
            Utente u = new Utente();

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(cryptMock.verificaPassword(anyString(), any())).thenReturn(true);
            doThrow(new RuntimeException()).when(daoMock).update(any());

            assertThrows(AppException.class, () -> menager.newPassword("new", "old", token));
        }

        @Test
        void newPassword_Successo_AggiornaDati() throws Exception {
            String token = "valid-token";
            String oldP = "old-pass";
            String newP = "new-pass";
            String newHash = "hashed-new-pass";
            Utente u = new Utente();
            u.setPasswordHash("old-hash");

            when(logMock.isAlive(token)).thenReturn(true);
            when(logMock.getUtente(token)).thenReturn(u);
            when(cryptMock.verificaPassword(eq(oldP), anyString())).thenReturn(true);
            when(cryptMock.hashPassword(newP)).thenReturn(newHash);

            menager.newPassword(newP, oldP, token);

            assertEquals(newHash, u.getPasswordHash());
            verify(daoMock).update(u);
            verify(logMock).update(token, u);
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
        JWT_Provider jwtProvider;

        SessionLog log;
        PassCrypt crypt;
        UtenteDAO dao;
        AccessControlService service;

        Utente logTest, alreadyLogTest;
        String realToken;

        @BeforeEach
        void setup() throws Exception {
            emf = Persistence.createEntityManagerFactory("testPU");
            em = emf.createEntityManager();

            dao = new UtenteDAO();
            injectMethod(dao, em, "em");

            jwtProvider = new JWT_Provider();

            log = new SessionLog();
            injectMethod(log, jwtProvider, "jwtProvider");

            service = new AccessControlService();
            injectMethod(service, jwtProvider, "jwtProvider");

            crypt = new PassCrypt();

            menager = new AutanticateMenager();
            injectMethod(menager, log, "logBeble");
            injectMethod(menager, crypt, "crypt");
            injectMethod(menager, dao, "dao");
            injectMethod(menager, service, "accessControlService");

            //add utente per i test
            logTest = creaUtenteDiTest("Mario", "Rossi", "mariorossi", "hash123");
            alreadyLogTest = creaUtenteDiTest("Pippo", "Alberti", "pippo12", "sc2435");
            em.getTransaction().begin();
            em.persist(logTest);
            em.persist(alreadyLogTest);
            em.getTransaction().commit();

            //log utente
            realToken = jwtProvider.generateToken(alreadyLogTest, "compilatore");
            log.aggiungi(realToken, alreadyLogTest);

        }

        @AfterEach
        void tearDown() {
            if (em.isOpen()) em.close();
            if (emf.isOpen()) emf.close();
        }

        @Test
        @Tag("integration")
        void autenticate_Integrazione_Successo() throws Exception {
            String passwordChiaro = "testPass123";
            em.getTransaction().begin();
            logTest.setPasswordHash(crypt.hashPassword(passwordChiaro));
            em.merge(logTest);
            em.getTransaction().commit();

            String token = menager.autenticate(passwordChiaro, logTest.getUsername());

            assertNotNull(token);
            assertTrue(log.isAlive(token));
            assertEquals(logTest.getUsername(), log.getUtente(token).getUsername());
        }

        @Test
        @Tag("integration")
        void autenticate_Integrazione_PasswordErrata() throws Exception {
            String passwordChiaro = "veraPassword";
            em.getTransaction().begin();
            logTest.setPasswordHash(crypt.hashPassword(passwordChiaro));
            em.merge(logTest);
            em.getTransaction().commit();

            assertThrows(LoginFailed.class, () -> menager.autenticate("passwordSbagliata", logTest.getUsername()));
        }

        @Test
        @Tag("integration")
        void logout_ShouldActuallyRemoveSessionFromLog() throws Exception {
            assertTrue(log.isAlive(realToken));

            menager.logout(realToken);

            assertFalse(log.isAlive(realToken));
        }

        @Test
        @Tag("integration")
        void registra_Integrazione_Successo() throws Exception {
            Utente nuovo = creaUtenteDiTest("Ciro", "Di Somma", "nuovo.utente", "password123");

            menager.registra(nuovo);

            Utente salvato = dao.findForLogin("nuovo.utente");
            assertNotNull(salvato);
            assertTrue(crypt.verificaPassword("password123", salvato.getPasswordHash()));
            assertTrue(salvato.getIsCompilatore());
            assertFalse(salvato.getIsCreatore());
        }

        @Test
        @Tag("integration")
        void registra_Integrazione_UsernameGiaEsistente() {
            Utente duplicato = new Utente();
            duplicato.setUsername(logTest.getUsername());
            duplicato.setPasswordHash("any");

            assertThrows(RegisterFailed.class, () -> menager.registra(duplicato));
        }

        @Test
        @Tag("integration")
        void newPassword_Integrazione_Successo() throws Exception {
            String passwordInChiaro = "vecchiaPassword";
            String nuovaPassword = "nuovaPassword123";

            em.getTransaction().begin();
            alreadyLogTest.setPasswordHash(crypt.hashPassword(passwordInChiaro));
            em.merge(alreadyLogTest);
            em.getTransaction().commit();

            menager.newPassword(nuovaPassword, passwordInChiaro, realToken);

            Utente utenteSuDb = dao.findForLogin(alreadyLogTest.getUsername());
            assertTrue(crypt.verificaPassword(nuovaPassword, utenteSuDb.getPasswordHash()));
            assertFalse(crypt.verificaPassword(passwordInChiaro, utenteSuDb.getPasswordHash()));

            Utente utenteInSessione = log.getUtente(realToken);
            assertEquals(utenteSuDb.getPasswordHash(), utenteInSessione.getPasswordHash());
        }

        // --- HELPER METHOD PER CREARE UTENTI VALIDI ---
        private Utente creaUtenteDiTest(String nome, String cognome, String username, String pwd) {
            Utente u = new Utente();
            u.setNome(nome);
            u.setCognome(cognome);
            u.setUsername(username);
            u.setPasswordHash(pwd);

            u.setIsCreatore(false);
            u.setIsCompilatore(true);
            u.setIsManager(false);
            return u;
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
