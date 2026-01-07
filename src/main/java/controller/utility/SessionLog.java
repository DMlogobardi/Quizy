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
    @Inject
    private JWT_Provider jwtProvider;

    public SessionLog() {
    }

    public void aggiungi(String token, Utente utente) throws TokenExpiredException, AppException {
        jwtProvider.validateToken(token);

        String tokenEsistente = null;

        for (Map.Entry<String, Utente> entry : logBible.entrySet()) {
            if (entry.getValue().getId().equals(utente.getId())) {
                tokenEsistente = entry.getKey();
                break;
            }
        }

        if (tokenEsistente != null) {
            try {
                jwtProvider.validateToken(tokenEsistente);
                throw new AppException("Sessione già esistente per questo utente.");

            } catch (TokenExpiredException e) {
                logBible.remove(tokenEsistente);
            }
        }
    }

    public Utente getUtente(String token) throws TokenExpiredException {
        jwtProvider.validateToken(token);

        return logBible.get(token);
    }

    public void rimuovi(String token)  {

        logBible.remove(token);
    }

    public boolean isAlive(String token) throws TokenExpiredException, AppException {
        try {
            jwtProvider.validateToken(token); // lancia eccezione se scaduto o invalido
            return logBible.containsKey(token);
        } catch (TokenExpiredException e) {
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

        // Controlla che il token esista nella mappa
        if (!logBible.containsKey(token)) {
            throw new AppException("Sessione non trovata per questo token");
        }

        // Aggiorna l'utente associato al token
        logBible.put(token, updatedUtente);
    }
}
