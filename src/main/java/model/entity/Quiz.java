package model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "quiz")
@NamedQueries({
        @NamedQuery(name = "Quiz.findAll", query = "SELECT q FROM Quiz q"),
        @NamedQuery(name = "Quiz.findAllByUtente", query = "SELECT q FROM Quiz q WHERE q.utente = :utente")
})
@XmlRootElement
public class Quiz implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_quiz", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "id_utente")
    @JsonBackReference("utente-quiz")
    private Utente utente;

    @Size(max = 50)
    @NotNull
    @Column(name = "tempo", nullable = false, length = 50)
    private String tempo;

    @Size(max = 50)
    @NotNull
    @Column(name = "difficolta", nullable = false, length = 50)
    private String difficolta;

    @Size(max = 200)
    @NotNull
    @Column(name = "titolo", nullable = false, length = 200)
    private String titolo;

    @Size(max = 200)
    @NotNull
    @Column(name = "descrizione", nullable = false, length = 200)
    private String descrizione;

    @Column(name = "numero_domande")
    private Integer numeroDomande;

    @Column(name = "creato_il", insertable = false, updatable = false)
    private LocalDateTime creatoIl;

    @Size(max = 100)
    @Column(name = "password", length = 100)
    private String passwordQuiz;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnoreProperties("quiz")
    private List<Domanda> domande;

    public Quiz() {
    }

    public Quiz(Utente utente, String tempo, String difficolta, String titolo, String descrizione, Integer numeroDomande, LocalDateTime creatoIl, String passwordQuiz) {
        this.utente = utente;
        this.tempo = tempo;
        this.difficolta = difficolta;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.numeroDomande = numeroDomande;
        this.creatoIl = creatoIl;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Utente getUtente() {
        return utente;
    }

    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    public String getTempo() {
        return tempo;
    }

    public void setTempo(String tempo) {
        this.tempo = tempo;
    }

    public String getDifficolta() {
        return difficolta;
    }

    public void setDifficolta(String difficolta) {
        this.difficolta = difficolta;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Integer getNumeroDomande() {
        return numeroDomande;
    }

    public void setNumeroDomande(Integer numeroDomande) {
        this.numeroDomande = numeroDomande;
    }

    public LocalDateTime getCreatoIl() {
        return creatoIl;
    }

    public void setCreatoIl(LocalDateTime creatoIl) {
        this.creatoIl = creatoIl;
    }

    public List<Domanda> getDomande() {
        return domande;
    }

    public void setDomande(List<Domanda> domande) {
        this.domande = domande;
    }

    public @Size(max = 100) String getPasswordQuiz() {
        return passwordQuiz;
    }

    public void setPasswordQuiz(@Size(max = 100) String passwordQuiz) {
        this.passwordQuiz = passwordQuiz;
    }
}