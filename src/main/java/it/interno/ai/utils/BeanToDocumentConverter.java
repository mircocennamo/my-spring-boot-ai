package it.interno.ai.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.document.Document;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BeanToDocumentConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Document convertToDocument(Object bean) throws IOException {
        // Serializza il bean in una stringa JSON
        String jsonString = objectMapper.writeValueAsString(bean);
        // Crea un Document utilizzando la stringa JSON
        return new Document(jsonString);
    }


    public static <T> T convertToBean(Document document, Class<T> beanClass) throws IOException {
        // Estrae la stringa JSON dal Document
        String jsonString = document.toString();
        // Deserializza la stringa JSON in un bean
        return objectMapper.readValue(jsonString, beanClass);
    }
    public static List<Document> convertToDocumentList(List<?> beans) throws IOException {
        return beans.stream()
                .map(bean -> {
                    try {
                        return convertToDocument(bean);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}
