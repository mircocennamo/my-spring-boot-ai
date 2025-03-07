package it.interno.ai.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.interno.ai.model.*;
import it.interno.ai.tools.GeneratorTools;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static it.interno.ai.utils.BeanToDocumentConverter.convertToDocumentList;

/**
 * @author mirco.cennamo on 02/03/2025
 * @project my-spring-boot-ai
 */
@Service
public class PPEDocumentService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    private static final Logger log = LoggerFactory.getLogger(PPEDocumentService.class);


    private MongoTemplate mongoTemplate;
    public PPEDocumentService(ChatClient.Builder builder,
                              VectorStore vectorStore,
                              ChatMemory chatMemory,
                              GeneratorTools generatorTools,
                              ChatClient chatClient
    ) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
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

    public void ingestExcel(Resource resource) throws IOException {
        List<PersonaPoliticamenteEsposta> documents = readExcelFile2(resource);

        // Sending batch of documents to vector store
        // applying tokenizer
        //TextSplitter textSplitter = new TokenTextSplitter();
        //List<Document> splitDocuments = textSplitter.apply(documents);
        vectorStore.add(convertToDocumentList(documents));
     }



    public List<Document> readExcelFile(Resource resource) throws IOException {
        List<Document> documents = new ArrayList<>();
        try (InputStream inputStream = resource.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                StringBuilder content = new StringBuilder();
                for (Cell cell : row) {
                    content.append(cell.toString()).append(" ");
                }
                Document document = new Document(content.toString().trim());
                documents.add(document);
            }
        }
        return documents;
    }


    public List<PersonaPoliticamenteEsposta>  readExcelFile2(Resource resource) throws IOException {
        List<PersonaPoliticamenteEsposta> results = new ArrayList<>();


        try (InputStream inputStream = resource.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
                PersonaPoliticamenteEsposta ppe = new PersonaPoliticamenteEsposta();
                ppe.setIdUtente(row.getCell(0).getStringCellValue());
                ppe.setCognome(row.getCell(1).getStringCellValue());
                ppe.setNome(row.getCell(2).getStringCellValue());
                Ufficio ufficio = new Ufficio();
                ppe.setUfficio(ufficio);
                ppe.getUfficio().setIdUfficio(row.getCell(3).getStringCellValue());
                ppe.getUfficio().setDescrizioneUfficio(row.getCell(4).getStringCellValue());
                Richiesta richiesta = new Richiesta();
                ppe.setRichiesta(richiesta);
                ppe.getRichiesta().setDataRichiesta(row.getCell(5).getNumericCellValue() + "");
                ppe.getRichiesta().setOraRichiesta(row.getCell(6).getStringCellValue());
                Comandante comandante = new Comandante();
                ppe.setComandante(comandante);
                ppe.getComandante().setIdComandante(row.getCell(7).getStringCellValue());
                ppe.getComandante().setCognomeComandante(row.getCell(8).getStringCellValue());
                ppe.getComandante().setNomeComandante(row.getCell(9).getStringCellValue());
                Applicazione applicazione = new Applicazione();
                ppe.setApplicazione(applicazione);
                ppe.getApplicazione().setApplicazioneWeb(row.getCell(10).getStringCellValue());
                ppe.getApplicazione().setApplicazioneChiamante(row.getCell(11).getStringCellValue());
                ppe.setSoggettoControllato(row.getCell(12).getNumericCellValue() + "");
                Motivazione motivazione = new Motivazione();
                ppe.setMotivazione(motivazione);
                ppe.getMotivazione().setMotivazione(row.getCell(13).getStringCellValue());
                ppe.getMotivazione().setMotivazioneEstesa(row.getCell(14).getStringCellValue());

                ppe.setRuolo(row.getCell(15).getStringCellValue());
                ppe.setEnteDiAppartenenza(row.getCell(16).getStringCellValue());
                results.add(ppe);
            }

        }

       return results;
    }







    public String queryLLM(String question) {


        // Querying the vector store for documents related to the question
        List<Document> vectorStoreResult =
               vectorStore.similaritySearch(SearchRequest.builder().query(question)
                        //.topK(50).similarityThreshold(1)
                       .build());


        // Merging the documents into a single string
        assert vectorStoreResult != null;
        String documents = vectorStoreResult.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));


        // Setting the prompt with the context
        String prompt = """
                Utilizza le informazioni in {documents} alla
                domanda nella sezione {question}.
                Rispondi sempre in italiano e in modo chiaro e conciso.
                    Grazie per la tua collaborazione.
                """;





        log.info(
                "Prompt: {}",
                prompt
        );

        log.info(
                "documents: {}",
                documents
        );

        log.info(
                "question: {}",
                question
        );


        // Calling the chat model with the question
        return chatClient.prompt()
                .user(u->u.text(prompt)
                        .param("documents", documents)
                        .param("question", question))
                .call()
                .content();


        //return response;

       /* if(vectorStoreResult!=null && vectorStoreResult.get(0)!=null &&
                vectorStoreResult.getFirst().getMetadata()!=null &&
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
        */

    }



}