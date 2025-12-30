package model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "quiz")
public class Quiz implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_quiz", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "id_utente")
    @JsonBackReference
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

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "creato_il")
    private Instant creatoIl;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Domanda> domande;

    public Quiz() {
    }

    public Quiz(Utente utente, String tempo, String difficolta, String titolo, String descrizione, Integer numeroDomande, Instant creatoIl) {
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

    public Instant getCreatoIl() {
        return creatoIl;
    }

    public void setCreatoIl(Instant creatoIl) {
        this.creatoIl = creatoIl;
    }

    public List<Domanda> getDomande() {
        return domande;
    }

    public void setDomande(List<Domanda> domande) {
        this.domande = domande;
    }
}