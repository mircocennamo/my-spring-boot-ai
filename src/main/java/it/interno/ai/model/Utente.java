package it.interno.ai.model;

/**
 * @author mirco.cennamo on 06/03/2025
 * @project my-spring-boot-ai
 */


import it.interno.ai.annotations.FieldDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;



@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Utente {


    @FieldDescription("Identificativo univoco utente")
    private String id;
    @FieldDescription("ID utente")
    private String idUtente;

    @FieldDescription("Cognome utente")
    private String cognomeUtente;

    @FieldDescription("Nome utente")
    private String nomeUtente;
    @FieldDescription("Dettagli del comandante")
    private Comandante comandante;
    @FieldDescription("Dettagli dell'applicazione")
    private Applicazione applicazione;
    @FieldDescription("Dettagli dell'ufficio")
    private Ufficio ufficio;
    @FieldDescription("Dettagli della richiesta")
    private Richiesta richiesta;
    @FieldDescription("Soggetto controllato")
    private String soggettoControllato;
    @FieldDescription("Dettagli della motivazione")
    private Motivazione motivazione;
    @FieldDescription("Ruolo utente")
    private String ruolo;
    @FieldDescription("Ente di appartenenza")
    private String enteDiAppartenenza;
    private Metadata metadata;

    public Utente(String idUtente, String cognomeUtente, String nomeUtente, String soggettoControllato,
                  String ruolo, String enteDiAppartenenza) {
        this.idUtente = idUtente;
        this.cognomeUtente = cognomeUtente;
        this.nomeUtente = nomeUtente;
        this.soggettoControllato = soggettoControllato;
        this.ruolo = ruolo;
        this.enteDiAppartenenza = enteDiAppartenenza;
    }

    public Utente(String idUtente, String cognomeUtente, String nomeUtente, Comandante comandante, String soggettoControllato,
                  String ruolo, String enteDiAppartenenza) {
        this.idUtente = idUtente;
        this.cognomeUtente = cognomeUtente;
        this.nomeUtente = nomeUtente;
        this.comandante = comandante;
        this.soggettoControllato = soggettoControllato;
        this.ruolo = ruolo;
        this.enteDiAppartenenza = enteDiAppartenenza;
    }

    public Utente(String idUtente, String cognomeUtente, String nomeUtente, Applicazione applicazione, String soggettoControllato,
                  String ruolo, String enteDiAppartenenza) {
        this.idUtente = idUtente;
        this.cognomeUtente = cognomeUtente;
        this.nomeUtente = nomeUtente;
        this.applicazione = applicazione;
        this.soggettoControllato = soggettoControllato;
        this.ruolo = ruolo;
        this.enteDiAppartenenza = enteDiAppartenenza;
    }

    public Utente(String idUtente, String cognomeUtente, String nomeUtente, Ufficio ufficio, String soggettoControllato,
                  String ruolo, String enteDiAppartenenza) {
        this.idUtente = idUtente;
        this.cognomeUtente = cognomeUtente;
        this.nomeUtente = nomeUtente;
        this.ufficio = ufficio;
        this.soggettoControllato = soggettoControllato;
        this.ruolo = ruolo;
        this.enteDiAppartenenza = enteDiAppartenenza;
    }

    public Utente(String idUtente, String cognomeUtente, String nomeUtente, Richiesta richiesta, String soggettoControllato,
                  String ruolo, String enteDiAppartenenza) {
        this.idUtente = idUtente;
        this.cognomeUtente = cognomeUtente;
        this.nomeUtente = nomeUtente;
        this.richiesta = richiesta;
        this.soggettoControllato = soggettoControllato;
        this.ruolo = ruolo;
        this.enteDiAppartenenza = enteDiAppartenenza;
    }

    public Utente(String idUtente, String cognomeUtente, String nomeUtente,Motivazione motivazione, String soggettoControllato,
                   String ruolo, String enteDiAppartenenza) {
        this.idUtente = idUtente;
        this.cognomeUtente = cognomeUtente;
        this.nomeUtente = nomeUtente;
        this.soggettoControllato = soggettoControllato;
        this.motivazione = motivazione;
        this.ruolo = ruolo;
        this.enteDiAppartenenza = enteDiAppartenenza;
    }
}

