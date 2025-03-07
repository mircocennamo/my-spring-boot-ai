package it.interno.ai.utils;

import org.springframework.ai.document.Document;
import java.util.ArrayList;
import java.util.List;

public class ContextFilter {

    /**
     * Filtra una lista di JsonNode in base alla rilevanza rispetto alla domanda.
     * Questo esempio utilizza un approccio basato su keyword matching.
     *
     * @param jsonContext Lista dei contesti in formato JsonNode.
     * @param question La domanda posta dall'utente.
     * @return Lista di JsonNode ritenuti rilevanti per la domanda.
     */
    public static List<Document> filterRelevantJson(List<Document> jsonContext, String question) {
        if (jsonContext == null || jsonContext.isEmpty() || question == null || question.isEmpty()) {
            return jsonContext;
        }

        List<Document> relevant = new ArrayList<>();
        // Normalizza la domanda in minuscolo
        String normalizedQuestion = question.toLowerCase();
        // Estrae le parole chiave dalla domanda
        String[] questionKeywords = normalizedQuestion.split("\\s+");

        // Per ogni oggetto JSON, controlla se contiene almeno una parola chiave
        for (Document json : jsonContext) {
            String jsonText = json.toString().toLowerCase();
            int matchCount = 0;
            for (String keyword : questionKeywords) {
                if (jsonText.contains(keyword)) {
                    matchCount++;
                }
            }
            // Se viene trovato almeno un match, consideriamo il JSON rilevante
            if (matchCount > 0) {
                relevant.add(json);
            }
        }
        return relevant;
    }
}