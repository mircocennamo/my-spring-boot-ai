package it.interno.ai.repository;

/**
 * @author mirco.cennamo on 06/03/2025
 * @project my-spring-boot-ai
 */

import it.interno.ai.model.Utente;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PersonaPoliticamenteEspostaRepository extends MongoRepository<Utente, String> {

}