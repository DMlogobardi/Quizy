package controller.menager;

import controller.utility.AccessControlService;
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
import model.exception.TokenExpiredException;

@ApplicationScoped
public class AutanticateMenager {
    @Inject
    private SessionLog logBeble;

    @Inject
    private PassCrypt crypt;

    @Inject
    private UtenteDAO dao;

    @Inject
    private AccessControlService accessControlService;

    public AutanticateMenager() {
    }

    public String autenticate(String password, String username) throws LoginFailed {
        String token = "";
        try {
            Utente u = dao.findForLogin(username);

            if (!crypt.verificaPassword(password, u.getPasswordHash())) {
                throw new LoginFailed("login fallito");
            }

            token = accessControlService.newTokenByRole("compilatore", u);
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
            u.setIsCompilatore(true);
            u.setIsCreatore(false);
            u.setIsManager(false);
            dao.register(u);

        } catch (AppException e) {
            System.out.println(e.getMessage());
            throw new RegisterFailed("Errore durante la registrazione");
        }
    }

    public void newPassword(String password, String oldPassword, String token) throws AppException {
        try {
            if (!logBeble.isAlive(token)) {
                throw new AppException("Sessione non attiva o token non valido");
            }

            Utente u = logBeble.getUtente(token);
            if(!crypt.verificaPassword(oldPassword, u.getPasswordHash())){
                throw new AppException("password non cambiata");
            }

            u.setPasswordHash(crypt.hashPassword(password));
            dao.update(u);
            logBeble.update(token, u);
        } catch (TokenExpiredException e) {
            throw e;
        } catch (AppException e) {
            throw new AppException("password non cambiata");
        }
    }

}
