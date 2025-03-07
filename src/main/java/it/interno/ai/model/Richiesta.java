package it.interno.ai.model;

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
    private String dataRichiesta;

    private String oraRichiesta;
}
