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
public class Applicazione {
    @FieldDescription("Nome dell'applicazione web")
    private String applicazioneWeb;

    @FieldDescription("Nome dell'applicazione chiamante")
    private String applicazioneChiamante;
}
