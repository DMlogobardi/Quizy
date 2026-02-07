package model.utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.InvalidToken;
import model.exception.TokenExpiredException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JWT_PtoviderTest {

    private JWT_Provider jwtProvider = new JWT_Provider();

    // =========================
    // ===== UNIT TESTS ========
    // =========================

    //test generateToken

    @Test
    void generateToken_ShouldReturnIllegalArgumentException_WhenUtenteIsNull() {
        assertThrows(IllegalArgumentException.class , () -> jwtProvider.generateToken(null, "compilatore"));
    }

    @Test
    void generateToken_ShouldReturnIllegalArgumentException_WhenUtenteIdIsNull() {
        Utente test = new Utente();
        test.setId(null);

        assertThrows(IllegalArgumentException.class , () -> jwtProvider.generateToken(test, "compilatore"));
    }

    @Test
    void generateToken_ShouldReturnIllegalArgumentException_WhenUtenteIdIsZeroOrNegative() {
        Utente test = new Utente();
        test.setId(0);

        assertThrows(IllegalArgumentException.class , () -> jwtProvider.generateToken(test, "compilatore"));
    }

    @Test
    void generateToken_ShouldReturnIllegalArgumentException_WhenRoleIdIsNull() {
        Utente test = new Utente();
        test.setId(1);

        assertThrows(IllegalArgumentException.class , () -> jwtProvider.generateToken(test, null));
    }

    @Test
    void generateToken_ShouldReturnIllegalArgumentException_WhenRoleIdIsNotValid() {
        Utente test = new Utente();
        test.setId(1);

        assertThrows(IllegalArgumentException.class , () -> jwtProvider.generateToken(test, "sfhlifaofjòlokasjoòa"));
    }

    @Test
    void generateToken_ShouldReturnIllegalArgumentException_WhenRoleIdIsEmpty() {
        Utente test = new Utente();
        test.setId(1);

        assertThrows(IllegalArgumentException.class , () -> jwtProvider.generateToken(test, ""));
    }

    @Test
    void generateToken_ShouldReturnValidToken_WhenInputIsValid() {
        String SECRET_STRING = "z2p7W8p5v9B8R3M1x8C4k7J2q5N0t3Z9";
        final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

        Utente test = new Utente();
        test.setId(42);
        String role = "manager";

        String token = jwtProvider.generateToken(test, role);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY) // La stessa chiave usata nel provider
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(role, claims.getSubject(), "Il ruolo nel subject non corrisponde");
        assertEquals(42, claims.get("id", Integer.class), "L'ID utente nei claims non corrisponde");

        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    //test validateToken
    @Test
    void validateToken_ShouldReturnIllegalArgumentException_WhenTokenIsNull() {
        assertThrows(IllegalArgumentException.class, () -> jwtProvider.validateToken(null));
    }

    @Test
    void validateToken_ShouldReturnIllegalArgumentException_WhenTokenIsEmpty() {
        String token = "";

        assertThrows(IllegalArgumentException.class, () -> jwtProvider.validateToken(token));
    }

    @Test
    void validateToken_ShouldThrowInvalidToken_WhenSignedWithWrongKey() {
        javax.crypto.SecretKey wrongKey = Jwts.SIG.HS256.key().build();

        String wrongToken = Jwts.builder()
                .subject("manager")
                .signWith(wrongKey)
                .compact();

        assertThrows(InvalidToken.class, () -> jwtProvider.validateToken(wrongToken));
    }

    @Test
    void validateToken_ShouldThrowTokenExpiredException_WhenTokenIsPastExpiry() {
        final String SECRET_STRING = "z2p7W8p5v9B8R3M1x8C4k7J2q5N0t3Z9";
        final SecretKey testKey = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

        long ora = System.currentTimeMillis();
        Date emissione = new Date(ora - 600_000);
        Date scadenza = new Date(ora - 300_000);

        String expiredToken = Jwts.builder()
                .subject("creatore")
                .issuedAt(emissione)
                .expiration(scadenza)
                .signWith(testKey)
                .compact();

        assertThrows(TokenExpiredException.class, () -> jwtProvider.validateToken(expiredToken));
    }

    @Test
    void validateToken_ShouldSucceed_WithManuallyGeneratedToken() {
         final String SECRET_STRING = "z2p7W8p5v9B8R3M1x8C4k7J2q5N0t3Z9";//da sostituire con variabile d'ambiente o mettere a caso
         final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

        String manualToken = Jwts.builder()
                .subject("manager")
                .claim("id", 1)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(SECRET_KEY)
                .compact();

        // Verifico che il tuo metodo lo accetti
        assertDoesNotThrow(() -> jwtProvider.validateToken(manualToken));
    }

    //test getIdFromToken
    @Test
    void getIdFromToken_ShouldReturnIllegalArgumentException_WhenTokenIsNull() {
        assertThrows(IllegalArgumentException.class, () -> jwtProvider.getIdFromToken(null));
    }

    @Test
    void getIdFromToken_ShouldReturnIllegalArgumentException_WhenTokenIsEmpty() {
        String token = "";

        assertThrows(IllegalArgumentException.class, () -> jwtProvider.getIdFromToken(token));
    }

    @Test
    void getIdFromToken_ShouldThrowTokenExpiredException_WhenTokenIsExpired() {
        final String SECRET_STRING = "z2p7W8p5v9B8R3M1x8C4k7J2q5N0t3Z9";
        final SecretKey key = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

        String expiredToken = Jwts.builder()
                .claim("id", 1)
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(key)
                .compact();

        assertThrows(TokenExpiredException.class, () -> jwtProvider.getIdFromToken(expiredToken));
    }

    @Test
    void getIdFromToken_ShouldThrowAppException_WhenSignatureIsInvalid() {
        final SecretKey wrongKey = Keys.hmacShaKeyFor("chiave_completamente_diversa_e_molto_lunga".getBytes());

        String invalidToken = Jwts.builder()
                .claim("id", 1)
                .signWith(wrongKey)
                .compact();

        assertThrows(AppException.class, () -> jwtProvider.getIdFromToken(invalidToken));
    }

    @Test
    void getIdFromToken_ShouldThrowAppException_WhenTokenIsMalformatted() {
        assertThrows(AppException.class, () -> jwtProvider.getIdFromToken("header.payload.signature_falsa"));
    }

    @Test
    void getIdFromToken_ShouldReturnCorrectId_WhenTokenIsValid() {
        final String SECRET_STRING = "z2p7W8p5v9B8R3M1x8C4k7J2q5N0t3Z9";
        final SecretKey key = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());
        Integer expectedId = 123;

        String token = Jwts.builder()
                .claim("id", expectedId)
                .signWith(key)
                .compact();

        Integer actualId = assertDoesNotThrow(() -> jwtProvider.getIdFromToken(token));
        assertEquals(expectedId, actualId);
    }

    //test getRoleFromToken
    @Test
    void getRoleFromToken_ShouldReturnCorrectRole_WhenTokenIsValid() {
        final String SECRET_STRING = "z2p7W8p5v9B8R3M1x8C4k7J2q5N0t3Z9";
        final SecretKey key = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());
        String expectedRole = "manager";

        String token = Jwts.builder()
                .subject(expectedRole)
                .signWith(key)
                .compact();

        String actualRole = assertDoesNotThrow(() -> jwtProvider.getRoleFromToken(token));
        assertEquals(expectedRole, actualRole);
    }

    @Test
    void getRoleFromToken_ShouldThrowIllegalArgumentException_WhenTokenIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> jwtProvider.getRoleFromToken(""));
    }

    @Test
    void getRoleFromToken_ShouldThrowIllegalArgumentException_WhenTokenIsNull() {
        assertThrows(IllegalArgumentException.class, () -> jwtProvider.getRoleFromToken(null));
    }

    @Test
    void getRoleFromToken_ShouldThrowTokenExpiredException_WhenTokenIsExpired() {
        final String SECRET_STRING = "z2p7W8p5v9B8R3M1x8C4k7J2q5N0t3Z9";
        final SecretKey key = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

        String expiredToken = Jwts.builder()
                .subject("creatore")
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(key)
                .compact();

        assertThrows(TokenExpiredException.class, () -> jwtProvider.getRoleFromToken(expiredToken));
    }

    @Test
    void getRoleFromToken_ShouldThrowAppException_WhenSignatureIsInvalid() {
        final SecretKey wrongKey = Keys.hmacShaKeyFor("chiave_completamente_diversa_e_molto_lunga_123".getBytes());

        String invalidToken = Jwts.builder()
                .subject("manager")
                .signWith(wrongKey)
                .compact();

        assertThrows(AppException.class, () -> jwtProvider.getRoleFromToken(invalidToken));
    }

    @Test
    void getRoleFromToken_ShouldThrowAppException_WhenTokenIsMalformatted() {
        assertThrows(AppException.class, () -> jwtProvider.getRoleFromToken("token.non.valido"));
    }
}
