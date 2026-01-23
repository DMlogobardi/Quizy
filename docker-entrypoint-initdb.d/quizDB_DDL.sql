USE quizy;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS Risponde;
DROP TABLE IF EXISTS Fa;
DROP TABLE IF EXISTS Ticket;
DROP TABLE IF EXISTS Risposta;
DROP TABLE IF EXISTS Domanda;
DROP TABLE IF EXISTS Quiz;
DROP TABLE IF EXISTS Utente;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE Utente(
id_utente int AUTO_INCREMENT NOT NULL,
nome varchar (50) NOT NULL,
cognome varchar (50) NOT NULL,
username varchar (50) NOT NULL unique,
password_hash varchar (100) NOT NULL,
is_creatore BOOLEAN DEFAULT TRUE,
is_compilatore BOOLEAN DEFAULT FALSE,
is_manager BOOLEAN DEFAULT FALSE,
PRIMARY KEY (id_utente)
);

CREATE TABLE Quiz ( 
id_utente int,
id_quiz int AUTO_INCREMENT NOT NULL,
tempo varchar (50) NOT NULL,
difficolta varchar (50) NOT NULL, 
titolo varchar(200)  NOT NULL,
descrizione varchar (200) NOT NULL,
numero_domande int, 
creato_il timestamp DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (id_quiz),

CONSTRAINT unique_quiz_utente UNIQUE (id_utente, titolo),

FOREIGN KEY (id_utente) REFERENCES Utente (id_utente) ON DELETE SET NULL
);

CREATE TABLE Domanda(
id_domanda int AUTO_INCREMENT NOT NULL,
id_quiz int NOT NULL,
quesito TEXT NOT NULL,
punti_risposta_corretta int NOT NULL,
punti_risposta_sbagliata int NOT NULL,
PRIMARY KEY (id_domanda),
FOREIGN KEY (id_quiz) REFERENCES Quiz (id_quiz) ON DELETE CASCADE
);

CREATE TABLE Risposta (
id_risposta int AUTO_INCREMENT NOT NULL,
id_domanda int NOT NULL,
affermazione varchar(150) NOT NULL,
flag_risposta_corretta boolean DEFAULT FALSE,
PRIMARY KEY (id_risposta),
FOREIGN KEY (id_domanda) REFERENCES Domanda (id_domanda) ON DELETE CASCADE
);
 
CREATE TABLE Ticket(
id_ticket int AUTO_INCREMENT NOT NULL,
id_utente int NOT NULL,
descrizione_ticket text NOT NULL, 
tipo_richiesta enum ('cambio ruolo', 'problemi tecnici') NOT NULL,
descrizione_richiesta text NOT NULL, 
PRIMARY KEY (id_ticket),
FOREIGN KEY (id_utente) REFERENCES Utente (id_utente) ON DELETE CASCADE
);

CREATE TABLE Fa (
id_fa int AUTO_INCREMENT NOT NULL,
id_utente int NOT NULL,
id_quiz int NOT NULL,
PRIMARY KEY (id_fa),
FOREIGN KEY (id_utente) REFERENCES Utente (id_utente) ON DELETE CASCADE,
FOREIGN KEY (id_quiz) REFERENCES Quiz (id_quiz) ON DELETE CASCADE
);

CREATE TABLE Risponde (
id_risponde int AUTO_INCREMENT NOT NULL,
id_utente int NOT NULL,
id_risposta int NOT NULL,
quiz varchar(200) NOT NULL,
scelto_il timestamp DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (id_risponde),
FOREIGN KEY (id_utente) REFERENCES Utente (id_utente) ON DELETE CASCADE,
FOREIGN KEY (id_risposta) REFERENCES Risposta (id_risposta) ON DELETE CASCADE
);