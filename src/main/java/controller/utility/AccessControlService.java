package controller.utility;


import jakarta.enterprise.context.ApplicationScoped;
import model.entity.Utente;
import model.exception.InvalidRole;

@ApplicationScoped
public class AccessControlService {

    public AccessControlService() {
    }

    public void checkCreatore(Utente u)throws InvalidRole {
        if(u == null || !u.getIsCreatore()){
            throw new InvalidRole("Unauthorized");
        }
    }

    public void checkCompilatore(Utente u)throws InvalidRole {
        if(u == null || !u.getIsCompilatore()){
            throw new InvalidRole("Unauthorized");
        }
    }

    public void checkManager(Utente u)throws InvalidRole {
        if(u == null || !u.getIsManager()){
            throw new InvalidRole("Unauthorized");
        }
    }
}
