package it.interno.ai.configuration;

import it.interno.ai.tools.GeneratorTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FunctionCallingChatConfiguration {
    @Bean
    InMemoryChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder,   ElasticsearchVectorStore vectorStore) {
        return builder
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory()), // chat-memory advisor
                        new QuestionAnswerAdvisor(vectorStore, org.springframework.ai.vectorstore.SearchRequest.builder().build()) // RAG advisor
                )
                //.defaultTools(new GeneratorTools())
                .build();
    }


}
