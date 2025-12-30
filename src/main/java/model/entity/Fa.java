package model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "fa")
public class Fa implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fa", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_utente", nullable = false)
    @JsonBackReference
    private Utente utente;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_quiz", nullable = false)
    @JsonBackReference
    private Quiz quiz;

    @NotNull
    @Column(name = "punteggio")
    private Integer punteggio;

    @OneToMany(mappedBy = "tentativo", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "fa-risponde")
    private List<Risponde> risposteDate;

    public Fa() {
    }

    public Fa(Utente utente, Quiz quiz, Integer punteggio) {
        this.utente = utente;
        this.quiz = quiz;
        this.punteggio = punteggio;
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

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public List<Risponde> getRisposteDate() {
        return risposteDate;
    }

    public void setRisposteDate(List<Risponde> risposteDate) {
        this.risposteDate = risposteDate;
    }

    public @NotNull Integer getPunteggio() {
        return punteggio;
    }

    public void setPunteggio(@NotNull Integer punteggio) {
        this.punteggio = punteggio;
    }
}