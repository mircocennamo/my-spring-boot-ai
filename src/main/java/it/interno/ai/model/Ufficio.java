package it.interno.ai.model;

import it.interno.ai.annotations.FieldDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author mirco.cennamo on 07/03/2025
 * @project my-spring-boot-ai
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Ufficio {
    @FieldDescription("ID dell'ufficio")
    private String idUfficio;

    @FieldDescription("Descrizione dell'ufficio")
    private String descrizioneUfficio;
}
