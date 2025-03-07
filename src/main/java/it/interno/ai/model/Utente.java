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
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Utente {

    @Id
    @FieldDescription("Identificativo univoco utente")
    private String  id;
    @FieldDescription("ID utente")
    private String idUtente;
    @TextIndexed
    @FieldDescription("Cognome utente")
    private String cognomeUtente;
    @TextIndexed
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
}
