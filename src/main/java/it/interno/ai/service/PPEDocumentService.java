package it.interno.ai.service;


import it.interno.ai.model.*;
import it.interno.ai.tools.GeneratorTools;
import it.interno.ai.utils.ContextFilter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static it.interno.ai.utils.BeanToDocumentConverter.convertToDocumentList;

/**
 * @author mirco.cennamo on 02/03/2025
 * @project my-spring-boot-ai
 */
@Service
public class PPEDocumentService {

    private final ChatClient chatClient;
    private final ElasticsearchVectorStore vectorStore;

    private static final Logger log = LoggerFactory.getLogger(PPEDocumentService.class);
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");



    public PPEDocumentService(ChatClient.Builder builder,
                              ElasticsearchVectorStore vectorStore,
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
        List<Utente> documents = readExcelFile2(resource);

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


    public List<Utente>  readExcelFile2(Resource resource) throws IOException {
        List<Utente> results = new ArrayList<>();


        try (InputStream inputStream = resource.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
                Metadata metadata = new Metadata();
                metadata.setFileType("xlsx");
                metadata.setSource(resource.getFilename());
                metadata.setIndexedAt(new java.util.Date());
                metadata.setRowCount(row.getRowNum());



                Utente ppe = new Utente();
                ppe.setMetadata(metadata);
                ppe.setIdUtente(row.getCell(0).getStringCellValue());
                ppe.setCognomeUtente(row.getCell(1).getStringCellValue());
                ppe.setNomeUtente(row.getCell(2).getStringCellValue());
                Ufficio ufficio = new Ufficio();
                ppe.setUfficio(ufficio);
                ppe.getUfficio().setIdUfficio(row.getCell(3).getStringCellValue());
                ppe.getUfficio().setDescrizioneUfficio(row.getCell(4).getStringCellValue());
                Richiesta richiesta = new Richiesta();
                ppe.setRichiesta(richiesta);
                ppe.getRichiesta().setDataRichiesta(dateFormat.format(row.getCell(5).getDateCellValue()));
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


    private String formatJsonContext(List<Document> context) {
        StringBuilder sb = new StringBuilder();
        int count = 1;
        for (Document json : context) {
            sb.append(count++).append(". ").append(json.toString()).append("\n");
        }
        return sb.toString();
    }




    public String queryLLM(String question) {


        //1. Querying the vector store for documents related to the question
        List<Document> vectorStoreResult =
               vectorStore.similaritySearch(SearchRequest.builder().query(question)
                       // .topK(5)
                      // .similarityThreshold(0.6)
                        //.topK(50).similarityThreshold(1)
                       .build());

        log.info(
                "vectorStoreResult: {}",
                vectorStoreResult
        );


        // Merging the documents into a single string
        assert vectorStoreResult != null;

        // 2. Filtra e normalizza il contesto (puoi implementare metodi di pre-processamento)
        List<Document> relevantContext = ContextFilter.filterRelevantJson(vectorStoreResult, question);

        log.info(
                "relevantContext: {}",
                relevantContext
        );

       // String documents = vectorStoreResult.stream()
       //         .map(Document::getText)
       //         .collect(Collectors.joining(System.lineSeparator()));

        String documents = formatJsonContext(relevantContext);

        // 3. Costruisci il prompt combinando il contesto e la domanda
       // String prompt = "Utilizza le seguenti informazioni per rispondere alla domanda:\n" +
       //         documents + "\nDomanda: " + question + "\nRisposta:";


        // Setting the prompt with the context
        //String prompt = """
        //       Utilizza le seguenti informazioni in {documents} estratte dal contesto JSON per rispondere alla domanda:
        //        {question}.
        //        Rispondi sempre in italiano e in modo chiaro e conciso.
        //        Attieniti sempre alla domanda fornita.
        //        Non fornire informazioni sul contesto JSON fornito
        //            Grazie per la tua collaborazione.
        //        """;


        String prompt = """
                Rispondi alla domanda nella sezione "Risposta" utilizzando le informazioni fornite nel contesto.
                Mostra  i dati recuperati dall'oggetto metadata del documento.
                Struttura la risposta in sezioni chiare e distinte e leggibili.
                Dividi ogni sezione in paragrafi distinti e numerati.
                Ogni paragrafo deve essere separato da una riga vuota.
                Concludi la riposta facendo un riepilogo dei punti e fornisci eventuali raccomandazioni o conclusioni finali."
                
                Contesto:
                -----------
                {documents}
                -----------
                
                Domanda:
                -----------
                {question}
                -----------
                
                Istruzioni:
                1. Analizza attentamente il contesto fornito e identifica le informazioni più rilevanti.
                2. Organizza e riassumi i dati  presenti nel contesto.
                3. Fornisci una risposta dettagliata, facendo riferimento alle informazioni estratte dal contesto.
                4. Se alcune informazioni non sono chiare o mancanti, specifica eventuali incertezze o richiedi ulteriori dettagli.
                5. Rispondi in modo chiaro, strutturato e in italiano.
                
                Risposta:
                
                
                
                
                
                
                
                """;

        String prompt2 = """
                "Fornisci una risposta in una struttura HTML dettagliata alla domanda seguente, organizzando il testo in paragrafi distinti e numerati.Rispondi alla domanda utilizzando le informazioni fornite nel contesto.
                Ogni paragrafo deve essere separato da una riga vuota.
                Concludi la riposta facendo un riepilogo dei punti e fornisci conclusioni finali.
                
                Contesto:
                -----------
                {documents}
                -----------
                
                Domanda:
                -----------
                {question}
                -----------
                
                Istruzioni:
                1. Analizza attentamente il contesto fornito e identifica le informazioni più rilevanti.
                2. Organizza e riassumi i dati  presenti nel contesto.
                3. Fornisci una risposta dettagliata, facendo riferimento alle informazioni estratte dal contesto.
                4. Se alcune informazioni non sono chiare o mancanti, specifica eventuali incertezze o richiedi ulteriori dettagli.
                5. Rispondi in modo chiaro, strutturato e in italiano.
                
                Struttura della risposta suddivisa in paragrafi:
                
                Dati utente
                
                Dati Comandante
                
                Dati Applicazione
                
                Dati Ufficio
                
                Dati Richiesta
                
                Dati Motivazione
                
                Metadati
               """;




        log.info(
                "Prompt: {}",
                prompt2
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
        String response =  chatClient.prompt()
                .user(u->u.text(prompt2)
                        .param("documents", documents)
                        .param("question", question))
                .call()
                .content();

        log.info(
                "response: {}",
                response
        );

       // return response;

        if(vectorStoreResult!=null && vectorStoreResult.get(0)!=null &&
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


    }



}