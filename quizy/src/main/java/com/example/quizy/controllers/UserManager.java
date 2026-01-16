package com.example.quizy.controllers;

public class UserManager {

    // Questo metodo controlla solo se la password rispetta le regole di sicurezza
    // (Lunghezza, numeri, lettere). Non controlla se è giusta nel database!
    public boolean validazionePassword(String password){

        if (password == null) {
            return false;
        }

        if (password.length() < 6) {
            return false;
        }

        boolean haLettere = false;
        boolean haNumeri = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                haLettere = true;
            }
            if (Character.isDigit(c)) {
                haNumeri = true;
            }
        }

        return haLettere && haNumeri;
    }

    // Aggiungiamo un controllo base anche per lo username
    public boolean validazioneUsername(String username) {
        return username != null && !username.trim().isEmpty();
    }
}



/*



package com.example.quizy.controllers;
// Questa classe gestisce il login, la registrazione
//  ed il cambio ruolo


//  PARTE DI LOGIN
public class UserManager {
    public boolean autenticazioneLogin(String username, String password){

        if (password == null) {
            return false;
        }

        if (password.length() < 6) {
            return false;
        }

        boolean haLettere = false;
        boolean haNumeri = false;

        // Scansiono ogni carattere della password
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                haLettere = true;
            }
            if (Character.isDigit(c)) {
                haNumeri = true;
            }
        }

        if (!haLettere || !haNumeri) {
            return false;
        }

        return true;
    }


    // PARTE DELLA REGISTRAZIONE

}


 */
