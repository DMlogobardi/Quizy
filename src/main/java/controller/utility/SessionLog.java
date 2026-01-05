package controller.utility;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import model.entity.Utente;
import model.exception.AppException;
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

        // Controlla se l'utente è già presente nella mappa (sessione esistente)
        boolean utenteGiaLoggato = logBible.values().stream()
                .anyMatch(u -> u.getId().equals(utente.getId()));

        if (utenteGiaLoggato) {
            throw new AppException("Sessione già esistente per questo utente.");
        }

        // Aggiungi il token solo se l'utente non ha già una sessione
        logBible.put(token, utente);
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
            if(!jwtProvider.validateToken(token)){
                logBible.remove(token);
                throw new AppException("Token non valido");
            }
            return logBible.containsKey(token);
        } catch (TokenExpiredException e) {
            logBible.remove(token);
            throw e;
        }
    }
}
