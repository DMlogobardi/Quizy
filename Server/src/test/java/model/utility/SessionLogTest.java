package model.utility;

import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;
import model.exception.InvalidToken;
import model.exception.TokenExpiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionLogTest {

    JWT_Provider jwtProvider;
    SessionLog sessionLog;

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
            sessionLog = new SessionLog();
            jwtProvider = jwtProviderMocked;
            injectProvider(sessionLog, jwtProvider);
        }

        //test aggiungi
        @Test
        void aggiungi_ShouldReturnIllegalArgumentException_WhenTokenIsEmpty() {
            Utente u = new Utente();
            u.setId(1);

            assertThrows(IllegalArgumentException.class, () -> sessionLog.aggiungi("", u));
        }

        @Test
        void aggiungi_ShouldReturnIllegalArgumentException_WhenTokenIsNull() {
            Utente u = new Utente();
            u.setId(1);

            assertThrows(IllegalArgumentException.class, () -> sessionLog.aggiungi(null, u));
        }

        @Test
        void aggiungi_ShouldReturnIllegalArgumentException_WhenUtenteIsNull() {
            String token = "nmfiwshhfgspighj";

            assertThrows(EmptyFild.class, () -> sessionLog.aggiungi(token, null));
        }

        @Test
        void aggiungi_ShouldReturnIllegalArgumentException_WhenUtenteIdIsNull() {
            String token = "nmfiwshhfgspighj";
            Utente u = new Utente();
            u.setId(null);

            assertThrows(EmptyFild.class, () -> sessionLog.aggiungi(token, u));
        }

        @Test
        void aggiungi_ShouldReturnIllegalArgumentException_WhenUtenteIdIsZeroOrNegative() {
            String token = "nmfiwshhfgspighj";
            Utente u = new Utente();
            u.setId(0);

            assertThrows(EmptyFild.class, () -> sessionLog.aggiungi(token, u));
        }

        @Test
        void aggiungi_SessioneEsistenteValida_LanciaAppException() throws Exception {
            Utente u = new Utente();
            u.setId(1);
            String tokenVecchio = "token.vecchio";
            String tokenNuovo = "token.nuovo";

            Field f = SessionLog.class.getDeclaredField("userIdToToken");
            f.setAccessible(true);
            ((Map<Integer, String>) f.get(sessionLog)).put(u.getId(), tokenVecchio);

            doNothing().when(jwtProvider).validateToken(tokenVecchio);

            assertThrows(AppException.class, () -> sessionLog.aggiungi(tokenNuovo, u));
        }

        @Test
        void aggiungi_SessioneEsistenteScaduta_LanciaTokenExpiredExceptionERimuove() throws Exception {
            Utente u = new Utente();
            u.setId(1);
            String tokenVecchio = "token.scaduto";
            String tokenNuovo = "token.nuovo";

            Field f = SessionLog.class.getDeclaredField("userIdToToken");
            f.setAccessible(true);
            Map<Integer, String> userIdToToken = (Map<Integer, String>) f.get(sessionLog);
            userIdToToken.put(u.getId(), tokenVecchio);

            doThrow(new TokenExpiredException("logOut Forzato")).when(jwtProvider).validateToken(tokenVecchio);

            assertThrows(TokenExpiredException.class, () -> sessionLog.aggiungi(tokenNuovo, u));
            assertFalse(userIdToToken.containsKey(u.getId()));
        }

        @Test
        void aggiungi_Successo_AggiungeSessioneCorrettamente() throws Exception {
            Utente u = new Utente();
            u.setId(1);
            String token = "token.valido";

            sessionLog.aggiungi(token, u);

            Field f1 = SessionLog.class.getDeclaredField("userIdToToken");
            f1.setAccessible(true);
            Map<Integer, String> userIdToToken = (Map<Integer, String>) f1.get(sessionLog);

            Field f2 = SessionLog.class.getDeclaredField("logBible");
            f2.setAccessible(true);
            Map<String, Utente> logBible = (Map<String, Utente>) f2.get(sessionLog);

            assertEquals(token, userIdToToken.get(u.getId()));
            assertEquals(u, logBible.get(token));
        }

        //test getUtente
        @Test
        void getUtente_ShouldReturnIllegalArgumentException_whemTokenIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> sessionLog.getUtente(""));
        }

        @Test
        void getUtente_ShouldReturnIllegalArgumentException_whemTokenIsNull() {
            assertThrows(IllegalArgumentException.class, () -> sessionLog.getUtente(null));
        }

        @Test
        void rimuovi_ShouldNotRemoveFromUserIdToToken_WhenUtenteNotFound() throws Exception {
            String tokenInesistente = "token.non.valido";

            // Esecuzione
            sessionLog.rimuovi(tokenInesistente);

            // Verifica che logBible sia stato comunque toccato (remove viene chiamato)
            Field f2 = SessionLog.class.getDeclaredField("logBible");
            f2.setAccessible(true);
            Map<String, Utente> logBible = (Map<String, Utente>) f2.get(sessionLog);

            assertFalse(logBible.containsKey(tokenInesistente));
        }

        @Test
        void rimuovi_ShouldRemoveFromBothMaps_WhenUtenteExists() throws Exception {
            Utente u = new Utente();
            u.setId(1);
            String token = "token.da.rimuovere";

            // Setup: Popolamento manuale delle mappe
            Field f1 = SessionLog.class.getDeclaredField("userIdToToken");
            f1.setAccessible(true);
            Map<Integer, String> userIdToToken = (Map<Integer, String>) f1.get(sessionLog);
            userIdToToken.put(u.getId(), token);

            Field f2 = SessionLog.class.getDeclaredField("logBible");
            f2.setAccessible(true);
            Map<String, Utente> logBible = (Map<String, Utente>) f2.get(sessionLog);
            logBible.put(token, u);

            // Esecuzione
            sessionLog.rimuovi(token);

            // Verifica
            assertFalse(userIdToToken.containsKey(u.getId()));
            assertFalse(logBible.containsKey(token));
        }

        //test IsAllive
        @Test
        void IsAllive_ShouldreturnIllegalArgumentException_WhenTokenIsNull() {
            assertThrows(IllegalArgumentException.class,() -> sessionLog.isAlive(null));
        }

        @Test
        void IsAllive_ShouldreturnIllegalArgumentException_WhenTokenIsEmpty() {
            assertThrows(IllegalArgumentException.class,() -> sessionLog.isAlive(""));
        }

        @Test
        void isAlive_TokenScaduto_LanciaExceptionERimuove() throws Exception {
            String token = "token.scaduto";
            Integer userId = 1;

            Field f1 = SessionLog.class.getDeclaredField("userIdToToken");
            f1.setAccessible(true);
            Map<Integer, String> userIdToToken = (Map<Integer, String>) f1.get(sessionLog);
            userIdToToken.put(userId, token);

            Field f2 = SessionLog.class.getDeclaredField("logBible");
            f2.setAccessible(true);
            Map<String, Utente> logBible = (Map<String, Utente>) f2.get(sessionLog);
            logBible.put(token, new Utente());

            doThrow(new TokenExpiredException("expired")).when(jwtProviderMocked).validateToken(token);
            when(jwtProviderMocked.getIdFromToken(token)).thenReturn(userId);

            assertThrows(TokenExpiredException.class, () -> sessionLog.isAlive(token));
            assertFalse(userIdToToken.containsKey(userId));
            assertFalse(logBible.containsKey(token));
        }

        @Test
        void isAlive_TokenInvalido_LanciaAppExceptionERimuove() throws Exception {
            String token = "token.invalido";
            Integer userId = 2;

            Field f1 = SessionLog.class.getDeclaredField("userIdToToken");
            f1.setAccessible(true);
            Map<Integer, String> userIdToToken = (Map<Integer, String>) f1.get(sessionLog);
            userIdToToken.put(userId, token);

            Field f2 = SessionLog.class.getDeclaredField("logBible");
            f2.setAccessible(true);
            Map<String, Utente> logBible = (Map<String, Utente>) f2.get(sessionLog);
            logBible.put(token, new Utente());

            doThrow(new InvalidToken("invalid")).when(jwtProviderMocked).validateToken(token);
            when(jwtProviderMocked.getIdFromToken(token)).thenReturn(userId);

            assertThrows(AppException.class, () -> sessionLog.isAlive(token));
            assertFalse(userIdToToken.containsKey(userId));
            assertFalse(logBible.containsKey(token));
        }

        @Test
        void isAlive_Successo_RitornaTrue() throws Exception {
            String token = "token.valido";

            Field f2 = SessionLog.class.getDeclaredField("logBible");
            f2.setAccessible(true);
            ((Map<String, Utente>) f2.get(sessionLog)).put(token, new Utente());

            doNothing().when(jwtProviderMocked).validateToken(token);

            assertTrue(sessionLog.isAlive(token));
        }

        //update test
        @Test
        void update_ShouldreturnIllegalArgumentException_WhenUtenteIsNull() {
            assertThrows(EmptyFild.class,() -> sessionLog.update("vshfvlid", null));
        }

        @Test
        void update_ShouldreturnIllegalArgumentException_WhenTokenIsNull() {
            assertThrows(IllegalArgumentException.class,() -> sessionLog.update(null, new Utente()));
        }

        @Test
        void update_ShouldreturnIllegalArgumentException_WhenTokenIsEmpty() {
            assertThrows(IllegalArgumentException.class,() -> sessionLog.update("", new Utente()));
        }

        @Test
        void update_SessioneNonTrovata_LanciaAppException() throws Exception {
            Utente u = new Utente();
            u.setId(1);
            String token = "token.inesistente";

            doNothing().when(jwtProviderMocked).validateToken(token);

            assertThrows(AppException.class, () -> sessionLog.update(token, u));
        }

        @Test
        void update_Successo_AggiornaUtenteInMappa() throws Exception {
            String token = "token.valido";

            Utente vecchio = new Utente();
            vecchio.setId(1);
            vecchio.setNome("Vecchio");

            Utente nuovo = new Utente();
            nuovo.setId(1);
            nuovo.setNome("Nuovo");

            Field f = SessionLog.class.getDeclaredField("logBible");
            f.setAccessible(true);
            Map<String, Utente> logBible = (Map<String, Utente>) f.get(sessionLog);
            logBible.put(token, vecchio);

            doNothing().when(jwtProviderMocked).validateToken(token);

            sessionLog.update(token, nuovo);

            assertEquals("Nuovo", logBible.get(token).getNome());
            assertEquals(nuovo, logBible.get(token));
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
            sessionLog = new SessionLog();
            jwtProvider = new JWT_Provider();
            injectProvider(sessionLog, jwtProvider);
        }

        @Test
        void integration_aggiungiEGetUtente_Successo() throws Exception {
            Utente u = new Utente();
            u.setId(1);
            u.setUsername("testUser");
            String token = jwtProvider.generateToken(u, "compilatore");

            sessionLog.aggiungi(token, u);
            Utente recuperato = sessionLog.getUtente(token);

            assertNotNull(recuperato);
            assertEquals(u.getId(), recuperato.getId());
            assertEquals("testUser", recuperato.getUsername());
        }

        @Test
        void integration_isAlive_Successo() throws Exception {
            Utente u = new Utente();
            u.setId(1);
            String token = jwtProvider.generateToken(u, "compilatore");

            sessionLog.aggiungi(token, u);

            assertTrue(sessionLog.isAlive(token));
        }

        @Test
        void integration_updateUtente_Successo() throws Exception {
            Utente u = new Utente();
            u.setId(1);
            u.setNome("Originale");
            String token = jwtProvider.generateToken(u, "compilatore");

            sessionLog.aggiungi(token, u);

            Utente aggiornato = new Utente();
            aggiornato.setId(1);
            aggiornato.setNome("Modificato");

            sessionLog.update(token, aggiornato);
            Utente recuperato = sessionLog.getUtente(token);

            assertEquals("Modificato", recuperato.getNome());
        }

        @Test
        void integration_rimuoviSessione_Successo() throws Exception {
            Utente u = new Utente();
            u.setId(1);
            String token = jwtProvider.generateToken(u, "compilatore");

            sessionLog.aggiungi(token, u);
            sessionLog.rimuovi(token);

            assertFalse(sessionLog.isAlive(token));
            assertNull(sessionLog.getUtente(token));
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
