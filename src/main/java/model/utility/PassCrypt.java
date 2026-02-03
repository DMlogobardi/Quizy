package model.utility;

import jakarta.enterprise.context.ApplicationScoped;
import model.exception.EmptyFild;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class PassCrypt {

    private static final int COSTO = 12;

    public PassCrypt() {
    }

    private void emptyPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new EmptyFild("Il campo " + "password" + " non può essere vuoto");
        }
    }

    public String hashPassword(String passwordInChiaro) throws EmptyFild {
        emptyPassword(passwordInChiaro);

        return BCrypt.hashpw(passwordInChiaro, BCrypt.gensalt(COSTO));
    }

    public boolean verificaPassword(String passwordInChiaro, String hashDalDatabase) throws EmptyFild {
        emptyPassword(passwordInChiaro);
        emptyPassword(hashDalDatabase);
        try {
            return BCrypt.checkpw(passwordInChiaro, hashDalDatabase);
        } catch (Exception e) {

            return false;
        }
    }
}
