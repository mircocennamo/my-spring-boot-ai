package it.interno.ai.repository;

/**
 * @author mirco.cennamo on 06/03/2025
 * @project my-spring-boot-ai
 */

import it.interno.ai.model.PersonaPoliticamenteEsposta;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PersonaPoliticamenteEspostaRepository extends MongoRepository<PersonaPoliticamenteEsposta, String> {

}