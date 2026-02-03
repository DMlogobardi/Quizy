package model.utility;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import model.entity.Utente;
import model.exception.InvalidRole;

@ApplicationScoped
public class AccessControlService {

    @Inject
    JWT_Provider jwtProvider;

    public AccessControlService() {
    }

    public void checkCreatore(String token)throws InvalidRole {
        String role = jwtProvider.getRoleFromToken(token);

        if(!role.equals("creatore")) {
            throw new InvalidRole("Unauthorized");
        }
    }

    public void checkCompilatore(String token)throws InvalidRole {
        String role = jwtProvider.getRoleFromToken(token);

        if(!role.equals("compilatore")) {
            throw new InvalidRole("Unauthorized");
        }
    }

    public void checkManager(String token)throws InvalidRole {
        String role = jwtProvider.getRoleFromToken(token);

        if(!role.equals("menager")) {
            throw new InvalidRole("Unauthorized");
        }
    }

    public String newTokenByRole(String role, Utente u) {
        return jwtProvider.generateToken(u, role);
    }
}
