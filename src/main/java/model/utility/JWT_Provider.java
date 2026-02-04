package model.utility;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.InvalidToken;
import model.exception.TokenExpiredException;

import javax.crypto.SecretKey;
import java.util.Date;

@ApplicationScoped
public class JWT_Provider {

    private static final long EXPIRATION_TIME = 86_400_000; // un giorno
    private static final String SECRET_STRING = "z2p7W8p5v9B8R3M1x8C4k7J2q5N0t3Z9";//da sostituire con variabile d'ambiente o mettere a caso
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

    public JWT_Provider() {
    }

    public String generateToken(Utente utente, String roole) {
        if (utente == null || utente.getId() == null || utente.getId() <= 0) {
            throw new IllegalArgumentException("Impossibile generare token: Utente o ID non validi");
        }
        if (!"creatore".equals(roole) && !"compilatore".equals(roole) && !"manager".equals(roole)) {
            throw new IllegalArgumentException("Impossibile generare token: Ruolo non valido");
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject(roole)
                .claim("id", utente.getId())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(SECRET_KEY)
                .compact();
    }

    public void validateToken(String token) throws TokenExpiredException, InvalidToken {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("token is empty or null");
        }

        try {
            Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .clockSkewSeconds(60)
                    .build()
                    .parseSignedClaims(token);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new TokenExpiredException("Il token è scaduto. Effettua di nuovo il login.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidToken("token invalido");
        }
    }

    public Integer getIdFromToken(String token) throws TokenExpiredException, AppException {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("token is empty or null");
        }

        try {
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("id", Integer.class);
        }  catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new TokenExpiredException("Il token è scaduto. Effettua di nuovo il login.");
        } catch (Exception e) {
            System.out.println("errore di validazione: " + e.getMessage());
            throw new AppException("critical error");
        }
    }

    public String getRoleFromToken(String token) throws TokenExpiredException, AppException {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("token is empty or null");
        }

        try {
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        }  catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new TokenExpiredException("Il token è scaduto. Effettua di nuovo il login.");
        } catch (Exception e) {
            System.out.println("errore di validazione: " + e.getMessage());
            throw new AppException("critical error");
        }
    }
}
