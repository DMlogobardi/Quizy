package controller.utility;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.TokenExpiredException;

import javax.crypto.SecretKey;
import java.util.Date;

//token di accesso
@ApplicationScoped
public class JWT_Provider {

    private static final long EXPIRATION_TIME = 86_400_000; // un giorno
    private static final String SECRET_STRING = "z2p7W8p5v9B8R3M1x8C4k7J2q5N0t3Z9";//da sostituire con variabile d'ambiente o mettere a caso
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

    public JWT_Provider() {
    }

    public String generateToken(Utente utente) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject(utente.getUsername())   // Username (Standard Claim)
                .claim("id", utente.getId())     // ID Utente (Custom Claim)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(SECRET_KEY)
                .compact();
    }

    public boolean validateToken(String token) throws TokenExpiredException {
        try {
            Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new TokenExpiredException("Il token è scaduto. Effettua di nuovo il login.");
        } catch (Exception e) {
            return false;
        }
    }

    public Integer getIdFromToken(String token) throws TokenExpiredException, AppException {
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

    public String getUsernameFromToken(String token) throws TokenExpiredException, AppException {
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
