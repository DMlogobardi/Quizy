package model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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


@Entity
@Table(name = "risponde")
@NamedQueries({
        @NamedQuery(name = "Risponde.faindAll", query = "SELECT ris FROM Risponde ris"),
        @NamedQuery(name = "Risponde.faindAllByUtente", query = "SELECT ris FROM Risponde ris WHERE ris.utente = :utente")
})
@XmlRootElement
public class Risponde implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_risponde", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_utente", nullable = false)
    @JsonBackReference("utente-risponde")
    private Utente utente;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_risposta", nullable = false)
    @JsonBackReference("risponde-risposta")
    private Risposta risposta;

    @Size(max = 200)
    @NotNull
    @Column(name = "quiz", nullable = false, length = 200)
    private String quiz;

    @Column(name = "scelto_il", insertable = false, updatable = false)
    private LocalDateTime sceltoIl;

    @ManyToOne
    @JoinColumn(name = "id_fa") // Colleghiamo la risposta allo specifico tentativo
    @JsonBackReference(value = "fa-risponde")
    private Fa tentativo;

    public Risponde() {}

    public Risponde(Risposta risposta, Utente utente, String quiz, LocalDateTime sceltoIl) {
        this.risposta = risposta;
        this.utente = utente;
        this.quiz = quiz;
        this.sceltoIl = sceltoIl;
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

    public Risposta getRisposta() {
        return risposta;
    }

    public void setRisposta(Risposta risposta) {
        this.risposta = risposta;
    }

    public String getQuiz() {
        return quiz;
    }

    public void setQuiz(String quiz) {
        this.quiz = quiz;
    }

    public LocalDateTime getSceltoIl() {
        return sceltoIl;
    }

    public void setSceltoIl(LocalDateTime sceltoIl) {
        this.sceltoIl = sceltoIl;
    }

    public Fa getTentativo() {
        return tentativo;
    }

    public void setTentativo(Fa tentativo) {
        this.tentativo = tentativo;
    }
}