package it.interno.ai.tools;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * @author mirco.cennamo on 28/02/2025
 * @project spring-into-ai
 */
@Component
public class GeneratorTools {


    private static final Logger log = LoggerFactory.getLogger(GeneratorTools.class);


    @Autowired
    public GeneratorTools() {

    }

    @Tool(description = "Restituisce la data di oggi")
    public String getCurrentDate() {
        log.info("start getCurrentDate");
        LocalDate currentDate = LocalDate.now();
        log.info("Current date: {}", currentDate);
        return currentDate.toString();
    }


}
