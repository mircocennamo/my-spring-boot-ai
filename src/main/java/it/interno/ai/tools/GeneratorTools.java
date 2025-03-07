package it.interno.ai.tools;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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

    @Tool(description = "Ordina una lista di JSON in base alla rilevanza rispetto alla query.")
    public List<Document> rankContext(List<Document> jsonList, String query) {
        log.info("start rankContext");
        if (jsonList == null || query == null || query.isEmpty()) {
            return jsonList;
        }
        // Convertiamo la query in minuscolo per uniformare il confronto
        String lowerQuery = query.toLowerCase();
        // Implementazione semplice: filtriamo i JSON che contengono la query
        return jsonList.stream()
                .filter(json -> json.toString().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    @Tool(description = "Normalizza il testo rimuovendo punteggiatura e convertendo in minuscolo.")
    public String normalizeText(String input) {
        log.info("start normalizeText");
        if (input == null) {
            return "";
        }
        // Rimuove caratteri speciali e converte in minuscolo
        return input.replaceAll("[^\\w\\s]", "").toLowerCase();
    }

    @Tool(description = "Espande la query aggiungendo sinonimi per migliorare la ricerca semantica.")
    public String expandQuery(String query) {
        log.info("start expandQuery");
        if (query == null || query.isEmpty()) {
            return "";
        }
        // Implementazione semplificata: in un caso reale si potrebbe consultare un dizionario di sinonimi
        return query + " " + query + " (sinonimi...)";
    }

    @Tool(description = "Calcola l'embedding del testo utilizzando un modello NLP (placeholder).")
    public float[] computeEmbedding(String text) {
        log.info("start computeEmbedding");
        // In una implementazione reale, si richiamerebbe un modello NLP per ottenere l'embedding.
        return new float[]{1.0f, 0.0f, 0.5f};
    }


}
