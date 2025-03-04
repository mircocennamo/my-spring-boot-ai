package it.interno.ai.configuration;

import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FunctionCallingChatConfiguration {
    @Bean
    InMemoryChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }




}
