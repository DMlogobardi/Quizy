package model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.hibernate.annotations.ColumnDefault;

import java.io.Serializable;
import java.util.List;


@Entity
@Table(name = "utente")
@NamedQueries({
        @NamedQuery(name = "Utente.findAll", query = "SELECT u FROM Utente u "),
        @NamedQuery(name = "Utente.login", query = "SELECT u FROM Utente u WHERE u.username = :username"),
        @NamedQuery(name = "Utente.findById", query = "SELECT u FROM Utente u WHERE u.id = :id")
})
@XmlRootElement
public class Utente implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utente", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "nome", nullable = false, length = 50)
    private String nome;

    @Size(max = 50)
    @NotNull
    @Column(name = "cognome", nullable = false, length = 50)
    private String cognome;

    @Size(max = 50)
    @NotNull
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Size(max = 100)
    @NotNull
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @ColumnDefault("1")
    @Column(name = "is_creatore")
    private Boolean isCreatore;

    @ColumnDefault("0")
    @Column(name = "is_compilatore")
    private Boolean isCompilatore;

    @ColumnDefault("0")
    @Column(name = "is_manager")
    private Boolean isManager;

    @OneToMany(mappedBy = "utente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("utente-quiz")
    private List<Quiz> quizCreati;

    @OneToMany(mappedBy = "utente")
    @JsonManagedReference("utente-fa")
    private List<Fa> partecipazioni;

    @OneToMany(mappedBy = "utente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("utente-ticket")
    private List<Ticket> ticketCreati;

    public Utente() {}

    public Utente(String nome, String cognome, String username, String passwordHash, Boolean isCreatore, Boolean isCompilatore, Boolean isManager) {
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.passwordHash = passwordHash;
        this.isCreatore = isCreatore;
        this.isCompilatore = isCompilatore;
        this.isManager = isManager;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Boolean getIsCreatore() {
        return isCreatore;
    }

    public void setIsCreatore(Boolean isCreatore) {
        this.isCreatore = isCreatore;
    }

    public Boolean getIsCompilatore() {
        return isCompilatore;
    }

    public void setIsCompilatore(Boolean isCompilatore) {
        this.isCompilatore = isCompilatore;
    }

    public Boolean getIsManager() {
        return isManager;
    }

    public void setIsManager(Boolean isManager) {
        this.isManager = isManager;
    }

    public List<Quiz> getQuizCreati() {
        return quizCreati;
    }

    public void setQuizCreati(List<Quiz> quizCreati) {
        this.quizCreati = quizCreati;
    }

    public List<Ticket> getTicketCreati() {
        return ticketCreati;
    }

    public void setTicketCreati(List<Ticket> ticketCreati) {
        this.ticketCreati = ticketCreati;
    }

    public List<Fa> getPartecipazioni() {
        return partecipazioni;
    }

    public void setPartecipazioni(List<Fa> partecipazioni) {
        this.partecipazioni = partecipazioni;
    }
}