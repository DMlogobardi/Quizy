package model.utility;

import model.exception.EmptyFild;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PassCryptTest {

    private final PassCrypt crypt = new PassCrypt();

    //test hashPassword
    @Test
    void hashPassword_ShouldGenerateValidHash_WhenPasswordIsNotEmpty() {
        String password = "mySecretPassword123";

        String hash = crypt.hashPassword(password);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$12$"));
        assertTrue(BCrypt.checkpw(password, hash));
    }

    @Test
    void hashPassword_ShouldThrowEmptyFild_WhenPasswordIsNull() {
        assertThrows(EmptyFild.class, () -> crypt.hashPassword(null));
    }

    @Test
    void hashPassword_ShouldThrowEmptyFild_WhenPasswordIsBlank() {
        assertThrows(EmptyFild.class, () -> crypt.hashPassword("   "));
    }

    //test verificaPassword
    @Test
    void verificaPassword_ShouldReturnTrue_WhenPasswordMatchesHash() {
        String password = "correctPassword";
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));

        boolean result = crypt.verificaPassword(password, hash);

        assertTrue(result);
    }

    @Test
    void verificaPassword_ShouldReturnFalse_WhenPasswordDoesNotMatchHash() {
        String password = "correctPassword";
        String wrongPassword = "wrongPassword";
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));

        boolean result = crypt.verificaPassword(wrongPassword, hash);

        assertFalse(result);
    }

    @Test
    void verificaPassword_ShouldReturnFalse_WhenHashIsInvalid() {
        String password = "password";
        String invalidHash = "$2a$12$invalidhashformat";

        boolean result = crypt.verificaPassword(password, invalidHash);

        assertFalse(result);
    }

    @Test
    void verificaPassword_ShouldThrowEmptyFild_WhenPasswordInChiaroIsEmpty() {
        String hash = "$2a$12$somevalidlookinghashstring";

        assertThrows(EmptyFild.class, () -> crypt.verificaPassword("", hash));
    }

    @Test
    void verificaPassword_ShouldThrowEmptyFild_WhenHashDalDatabaseIsEmpty() {
        assertThrows(EmptyFild.class, () -> crypt.verificaPassword("password", null));
    }
}
