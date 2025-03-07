package it.interno.ai.utils;

import it.interno.ai.annotations.FieldDescription;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mirco.cennamo on 07/03/2025
 * @project my-spring-boot-ai
 */
public class AnnotationUtils {
    public static Map<String, String> getFieldDescriptions(Class<?> clazz) {
        Map<String, String> fieldDescriptions = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(FieldDescription.class)) {
                FieldDescription annotation = field.getAnnotation(FieldDescription.class);
                fieldDescriptions.put(field.getName(), annotation.value());
            }
        }
        return fieldDescriptions;
    }
}
