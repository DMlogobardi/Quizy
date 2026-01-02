package model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;

@Entity
@Table(name = "ticket")
@NamedQueries({
        @NamedQuery(name = "Ticket.findAll", query = "SELECT t FROM Ticket t"),
        @NamedQuery(name = "Ticket.faindAllByUtente", query = "SELECT t FROM Ticket t WHERE t.utente = :utente")
})
public class Ticket implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ticket", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_utente", nullable = false)
    @JsonBackReference
    private Utente utente;

    @NotNull
    @Lob
    @Column(name = "descrizione_ticket", nullable = false)
    private String descrizioneTicket;

    @NotNull
    @Lob
    @Column(name = "tipo_richiesta", nullable = false)
    private String tipoRichiesta;

    @NotNull
    @Lob
    @Column(name = "descrizione_richiesta", nullable = false)
    private String descrizioneRichiesta;

    public Ticket() {}

    public Ticket(Utente utente, String descrizioneTicket, String tipoRichiesta, String descrizioneRichiesta) {
        this.utente = utente;
        this.descrizioneTicket = descrizioneTicket;
        this.tipoRichiesta = tipoRichiesta;
        this.descrizioneRichiesta = descrizioneRichiesta;
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

    public String getDescrizioneTicket() {
        return descrizioneTicket;
    }

    public void setDescrizioneTicket(String descrizioneTicket) {
        this.descrizioneTicket = descrizioneTicket;
    }

    public String getTipoRichiesta() {
        return tipoRichiesta;
    }

    public void setTipoRichiesta(String tipoRichiesta) {
        this.tipoRichiesta = tipoRichiesta;
    }

    public String getDescrizioneRichiesta() {
        return descrizioneRichiesta;
    }

    public void setDescrizioneRichiesta(String descrizioneRichiesta) {
        this.descrizioneRichiesta = descrizioneRichiesta;
    }

}