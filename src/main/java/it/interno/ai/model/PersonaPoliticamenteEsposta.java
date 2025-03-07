package it.interno.ai.model;

/**
 * @author mirco.cennamo on 06/03/2025
 * @project my-spring-boot-ai
 */


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
public class PersonaPoliticamenteEsposta {

    @Id
    private String  id;
    private String idUtente;
    @TextIndexed
    private String cognome;
    @TextIndexed
    private String nome;
    private Comandante comandante;
    private Applicazione applicazione;
    private Ufficio ufficio;
    private Richiesta richiesta;
    private String soggettoControllato;
    private Motivazione motivazione;
    private String ruolo;
    private String enteDiAppartenenza;
}
