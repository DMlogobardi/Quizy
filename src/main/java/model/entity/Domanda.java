package model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.util.List;


@Entity
@Table(name = "domanda")
@NamedQueries({
        @NamedQuery(name = "Domanda.findAll", query = "SELECT d FROM Domanda d"),
        @NamedQuery(name = "Domanda.findAllByQuiz", query = "SELECT d FROM Domanda d WHERE d.quiz = :quiz")
})
@XmlRootElement
public class Domanda implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_domanda", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_quiz", nullable = false)
    @JsonBackReference("quiz-domande")
    private Quiz quiz;

    @NotNull
    @Lob
    @Column(name = "quesito", nullable = false)
    private String quesito;

    @NotNull
    @Column(name = "punti_risposta_corretta", nullable = false)
    private Integer puntiRispostaCorretta;

    @NotNull
    @Column(name = "punti_risposta_sbagliata", nullable = false)
    private Integer puntiRispostaSbagliata;

    @OneToMany(mappedBy = "domanda", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("domanda-risposte")
    private List<Risposta> risposte;

    public Domanda() {
    }

    public Domanda(String quesito, Integer puntiRispostaCorretta, Quiz quiz, Integer puntiRispostaSbagliata) {
        this.quesito = quesito;
        this.puntiRispostaCorretta = puntiRispostaCorretta;
        this.quiz = quiz;
        this.puntiRispostaSbagliata = puntiRispostaSbagliata;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public String getQuesito() {
        return quesito;
    }

    public void setQuesito(String quesito) {
        this.quesito = quesito;
    }

    public Integer getPuntiRispostaCorretta() {
        return puntiRispostaCorretta;
    }

    public void setPuntiRispostaCorretta(Integer puntiRispostaCorretta) {
        this.puntiRispostaCorretta = puntiRispostaCorretta;
    }

    public Integer getPuntiRispostaSbagliata() {
        return puntiRispostaSbagliata;
    }

    public void setPuntiRispostaSbagliata(Integer puntiRispostaSbagliata) {
        this.puntiRispostaSbagliata = puntiRispostaSbagliata;
    }

    public List<Risposta> getRisposte() {
        return risposte;
    }

    public void setRisposte(List<Risposta> risposte) {
        this.risposte = risposte;
    }
}