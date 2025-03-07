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
public class Richiesta {
    @FieldDescription("Data della richiesta")
    private String dataRichiesta;

    @FieldDescription("Ora della richiesta")
    private String oraRichiesta;
}
