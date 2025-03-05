package it.interno.ai.service;

import it.interno.ai.tools.GeneratorTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mirco.cennamo on 02/03/2025
 * @project my-spring-boot-ai
 */
@Service
public class PPEDocumentService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;


    public PPEDocumentService(ChatClient.Builder builder, VectorStore vectorStore, ChatMemory chatMemory, GeneratorTools generatorTools) {

        this.chatClient = builder
                .defaultAdvisors(
                        new PromptChatMemoryAdvisor(chatMemory),
                        new SimpleLoggerAdvisor())
                //.defaultTools(generatorTools)
                .build();
        this.vectorStore = vectorStore;

    }

    public void ingestPDF(Resource resource) {
        // Spring AI utility class to read a PDF file page by page
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource);
        List<Document> docbatch = pdfReader.read();

        // Sending batch of documents to vector store
        // applying tokenizer
        docbatch = new TokenTextSplitter().apply(docbatch);
        vectorStore.add(docbatch);
    }

    public String queryLLM(String question) {


        // Querying the vector store for documents related to the question
        List<Document> vectorStoreResult =
                vectorStore.similaritySearch(SearchRequest.builder().query(question)
                        .topK(5).similarityThreshold(0.6).build());


        // Merging the documents into a single string
        assert vectorStoreResult != null;
        String documents = vectorStoreResult.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));


        // Setting the prompt with the context
        String prompt = """
                
                Utilizza le informazioni della sezione DOCUMENTI per fornire risposte precise alle
                domanda nella sezione DOMANDE.
                 Se non sei sicuro, dichiara semplicemente che non lo sai.
                 Rispondi sempre in italiano


       DOCUMENTI:
       """ + documents
                + """
       DOMANDE:
       """ + question;




        // Calling the chat model with the question
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        if(vectorStoreResult!=null && vectorStoreResult.get(0)!=null && vectorStoreResult.getFirst().getMetadata()!=null &&
                vectorStoreResult.get(0).getMetadata().get(PagePdfDocumentReader.METADATA_START_PAGE_NUMBER)!=null){
            return response +
                    System.lineSeparator() +
                    "Trovato alla pagina: " +
                    // Retrieving the first ranked page number from the document metadata
                    vectorStoreResult.getFirst().getMetadata().get(PagePdfDocumentReader.METADATA_START_PAGE_NUMBER) +
                    " del manuale " + vectorStoreResult.getFirst().getMetadata().get(PagePdfDocumentReader.METADATA_FILE_NAME);
        }else{
            return response;
        }
    }
}