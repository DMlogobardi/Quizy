package model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;

@Entity
@Table(name = "risposta")
public class Risposta implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_risposta", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_domanda", nullable = false)
    @JsonBackReference
    private Domanda domanda;

    @Size(max = 150)
    @NotNull
    @Column(name = "affermazione", nullable = false, length = 150)
    private String affermazione;

    @ColumnDefault("0")
    @Column(name = "flag_risposta_corretta")
    private Boolean flagRispostaCorretta;

    public Risposta() {
    }

    public Risposta(String affermazione, Boolean flagRispostaCorretta, Domanda domanda) {
        this.affermazione = affermazione;
        this.flagRispostaCorretta = flagRispostaCorretta;
        this.domanda = domanda;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Domanda getDomanda() {
        return domanda;
    }

    public void setDomanda(Domanda domanda) {
        this.domanda = domanda;
    }

    public String getAffermazione() {
        return affermazione;
    }

    public void setAffermazione(String affermazione) {
        this.affermazione = affermazione;
    }

    public Boolean getFlagRispostaCorretta() {
        return flagRispostaCorretta;
    }

    public void setFlagRispostaCorretta(Boolean flagRispostaCorretta) {
        this.flagRispostaCorretta = flagRispostaCorretta;
    }

}