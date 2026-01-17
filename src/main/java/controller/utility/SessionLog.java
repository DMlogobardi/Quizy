package controller.utility;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.InvalidToken;
import model.exception.TokenExpiredException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SessionLog {

    private final Map<String, Utente> logBible = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, String> userIdToToken = new ConcurrentHashMap<>();
    @Inject
    private JWT_Provider jwtProvider;

    public SessionLog() {
    }

    public void aggiungi(String token, Utente utente) throws TokenExpiredException, AppException {
        jwtProvider.validateToken(token);

        String existing = userIdToToken.putIfAbsent(utente.getId(), token);
        if (existing != null) {
            try {
                jwtProvider.validateToken(existing);
                throw new AppException("Sessione già esistente per questo utente");
            } catch (TokenExpiredException e) {
                userIdToToken.remove(utente.getId(), existing);
            }
        }

        logBible.put(token, utente);
    }

    public Utente getUtente(String token) throws TokenExpiredException {
        jwtProvider.validateToken(token);

        return logBible.get(token);
    }

    public void rimuovi(String token)  {
        Utente u = getUtente(token);
        if (u != null) {userIdToToken.remove(u.getId(), token);}
        logBible.remove(token);

    }

    public boolean isAlive(String token) throws TokenExpiredException, AppException {
        try {
            jwtProvider.validateToken(token); // lancia eccezione se scaduto o invalido
            return logBible.containsKey(token);
        } catch (TokenExpiredException e) {
            userIdToToken.remove(jwtProvider.getIdFromToken(token));
            logBible.remove(token); // rimuovi token scaduto
            throw e;
        } catch (InvalidToken e) {
            logBible.remove(token); // rimuovi token invalido
            throw new AppException("Token non valido");
        }
    }

    public void update(String token, Utente updatedUtente) throws TokenExpiredException, AppException {
        // Controlla che il token sia valido
        jwtProvider.validateToken(token);

        Utente precedente = logBible.replace(token, updatedUtente);
        if (precedente == null) {
            throw new AppException("Sessione non trovata per questo token");
        }
    }
}
