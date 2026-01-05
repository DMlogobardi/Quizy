package controller.menager;

import controller.utility.JWT_Provider;
import controller.utility.PassCrypt;
import controller.utility.SessionLog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import model.dao.UtenteDAO;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.LoginFailed;
import model.exception.RegisterFailed;

@ApplicationScoped
public class AutanticateMenager {
    @Inject
    private SessionLog logBeble;

    @Inject
    private PassCrypt crypt;

    @Inject
    private JWT_Provider jwtProvider;

    @Inject
    private UtenteDAO dao;

    public AutanticateMenager() {
    }

    public String autenticate(String password, String username) throws LoginFailed {
        String token = "";
        try {
            Utente u = dao.findForLogin(username);

            if (!crypt.verificaPassword(password, u.getPasswordHash())) {
                throw new LoginFailed("login fallito");
            }

            token = jwtProvider.generateToken(u);
            logBeble.aggiungi(token, u);

            return token;

        } catch (AppException e) {
            logBeble.rimuovi(token);
            System.out.println(e.getMessage());
            throw new LoginFailed("Errore durante l'autenticazione");
        }
    }

    public void logout(String token) {
        logBeble.rimuovi(token);
    }

    public void registra(Utente u) throws RegisterFailed {
        try{
            String hash = crypt.hashPassword(u.getPasswordHash());
            u.setPasswordHash(hash);
            dao.register(u);

        } catch (AppException e) {
            System.out.println(e.getMessage());
            throw new RegisterFailed("Errore durante la registrazione");
        }
    }

}
