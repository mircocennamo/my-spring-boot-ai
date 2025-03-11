package it.interno.ai.service;


import it.interno.ai.model.*;
import it.interno.ai.tools.GeneratorTools;
import it.interno.ai.utils.ContextFilter;
import org.apache.poi.ss.usermodel.*;
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
        List<Utente> documents = readExcelFile(resource);

        // Sending batch of documents to vector store
        // applying tokenizer
        //TextSplitter textSplitter = new TokenTextSplitter();
        //List<Document> splitDocuments = textSplitter.apply(documents);
        vectorStore.add(convertToDocumentList(documents));
     }


    public List<Utente> readExcelFile(Resource resource) throws IOException {
        List<Utente> results = new ArrayList<>();
        log.info("Iniziando la lettura del file Excel: {}", resource.getFilename());

        try (InputStream inputStream = resource.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = 0;
            int processedRows = 0;

            for (Row row : sheet) {
                totalRows++;
                if (row.getRowNum() == 0) continue; // Skip header row

                try {
                    Utente ppe = processExcelRow(row, resource.getFilename());
                    results.add(ppe);
                    processedRows++;
                } catch (Exception e) {
                    log.warn("Errore durante l'elaborazione della riga {}: {}", row.getRowNum(), e.getMessage());
                }
            }

            log.info("Lettura Excel completata. Processate {} righe su {} totali", processedRows, totalRows - 1);
        }

        return results;
    }

    /**
     * Processa una singola riga Excel e crea un oggetto Utente
     *
     * @param row La riga Excel da processare
     * @param filename Il nome del file Excel
     * @return Oggetto Utente popolato con i dati della riga
     */
    private Utente processExcelRow(Row row, String filename) {
        // Creazione e configurazione dei metadati
        Metadata metadata = new Metadata();
        metadata.setFileType("xlsx");
        metadata.setSource(filename);
        metadata.setIndexedAt(new java.util.Date());
        metadata.setRowCount(row.getRowNum());

        // Creazione dell'utente e impostazione dei dati base
        Utente ppe = new Utente();
        ppe.setMetadata(metadata);
        ppe.setIdUtente(getCellStringValue(row, 0));
        ppe.setCognomeUtente(getCellStringValue(row, 1));
        ppe.setNomeUtente(getCellStringValue(row, 2));

        // Configurazione dell'ufficio
        Ufficio ufficio = new Ufficio();
        ufficio.setIdUfficio(getCellStringValue(row, 3));
        ufficio.setDescrizioneUfficio(getCellStringValue(row, 4));
        ppe.setUfficio(ufficio);

        // Configurazione della richiesta
        Richiesta richiesta = new Richiesta();
        richiesta.setDataRichiesta(formatDateCell(row, 5));
        richiesta.setOraRichiesta(getCellStringValue(row, 6));
        ppe.setRichiesta(richiesta);

        // Configurazione del comandante
        Comandante comandante = new Comandante();
        comandante.setIdComandante(getCellStringValue(row, 7));
        comandante.setCognomeComandante(getCellStringValue(row, 8));
        comandante.setNomeComandante(getCellStringValue(row, 9));
        ppe.setComandante(comandante);

        // Configurazione dell'applicazione
        Applicazione applicazione = new Applicazione();
        applicazione.setApplicazioneWeb(getCellStringValue(row, 10));
        applicazione.setApplicazioneChiamante(getCellStringValue(row, 11));
        ppe.setApplicazione(applicazione);

        // Impostazione del soggetto controllato
        ppe.setSoggettoControllato(getCellNumericValue(row, 12));

        // Configurazione della motivazione
        Motivazione motivazione = new Motivazione();
        motivazione.setMotivazione(getCellStringValue(row, 13));
        motivazione.setMotivazioneEstesa(getCellStringValue(row, 14));
        ppe.setMotivazione(motivazione);

        // Impostazione di ruolo ed ente
        ppe.setRuolo(getCellStringValue(row, 15));
        ppe.setEnteDiAppartenenza(getCellStringValue(row, 16));

        return ppe;
    }

    /**
     * Ottiene il valore di una cella come stringa con gestione degli errori
     */
    private String getCellStringValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return "";

        try {
            return cell.getStringCellValue();
        } catch (Exception e) {
            // Fallback per celle non di tipo stringa
            return cell.toString();
        }
    }

    /**
     * Ottiene il valore numerico di una cella come stringa con gestione degli errori
     */
    private String getCellNumericValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return "0";

        try {
            return String.valueOf(cell.getNumericCellValue());
        } catch (Exception e) {
            // Fallback per celle non numeriche
            return cell.toString();
        }
    }

    /**
     * Formatta una cella data secondo il formato specificato
     */
    private String formatDateCell(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return "";

        try {
            return dateFormat.format(cell.getDateCellValue());
        } catch (Exception e) {
            // Fallback per celle non di tipo data
            return cell.toString();
        }
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
                        //.topK(10)
                       //.similarityThreshold(0.7)
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
                Fornisci una risposta strutturata e dettagliata alla domanda dell'utente, basandoti esclusivamente sulle informazioni presenti nel contesto fornito.
                
                Contesto:
                -----------
                {documents}
                -----------
                
                Domanda:
                -----------
                {question}
                -----------
                
                Istruzioni per la risposta:
                1. Analizza attentamente il contesto e identifica tutte le informazioni pertinenti alla domanda.
                2. Organizza la risposta in sezioni ben definite secondo la struttura indicata sotto.
                3. Utilizza un linguaggio chiaro, professionale e in italiano.
                4. Evidenzia eventuali informazioni mancanti o ambigue nel contesto.
                5. Includi tutti i metadati disponibili nella sezione dedicata.
                
                Struttura della risposta (mantieni questa organizzazione precisa):
                
                <div class="risposta-container">
                  <h2>Informazioni Utente</h2>
                  <p>[Inserisci qui tutti i dati relativi all'utente: ID, nome, cognome, ruolo, ente di appartenenza]</p>
                  
                  <h2>Informazioni Comandante</h2>
                  <p>[Inserisci qui tutti i dati relativi al comandante: ID, nome, cognome]</p>
                  
                  <h2>Informazioni Applicazione</h2>
                  <p>[Inserisci qui tutti i dati relativi all'applicazione: applicazione web, applicazione chiamante]</p>
                  
                  <h2>Informazioni Ufficio</h2>
                  <p>[Inserisci qui tutti i dati relativi all'ufficio: ID, descrizione]</p>
                  
                  <h2>Informazioni Richiesta</h2>
                  <p>[Inserisci qui tutti i dati relativi alla richiesta: data, ora]</p>
                  
                  <h2>Informazioni Motivazione</h2>
                  <p>[Inserisci qui tutti i dati relativi alla motivazione: motivazione, motivazione estesa]</p>
                  
                  <h2>Metadati</h2>
                  <p>[Inserisci qui tutti i metadati disponibili: tipo file, fonte, data indicizzazione, ecc.]</p>
                  
                  <h2>Riepilogo</h2>
                  <p>[Fornisci un riepilogo conciso delle informazioni principali e eventuali conclusioni]</p>
                </div>
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
        String response =  chatClient.prompt()
                .user(u->u.text(prompt)
                        .param("documents", documents)
                        .param("question", question))
                .call()
                .content();

        log.info(
                "response: {}",
                response
        );

       // return response;

        // Check if we have document metadata to enhance the response
        if (hasPdfMetadata(vectorStoreResult)) {
        // Handle PDF document metadata
        Document firstDocument = vectorStoreResult.getFirst();
        String pageNumber = firstDocument.getMetadata().get(PagePdfDocumentReader.METADATA_START_PAGE_NUMBER).toString();
        String fileName = firstDocument.getMetadata().get(PagePdfDocumentReader.METADATA_FILE_NAME).toString();

        return response +
               System.lineSeparator() +
               String.format("Trovato alla pagina: %s del manuale %s", pageNumber, fileName);
        } else if (hasExcelMetadata(vectorStoreResult)) {
        // Handle Excel document metadata
        Document firstDocument = vectorStoreResult.getFirst();
        String fileName = firstDocument.getMetadata().getOrDefault("source", "documento Excel").toString();
        String rowNumber = firstDocument.getMetadata().getOrDefault("rowCount", "").toString();

        String additionalInfo = !rowNumber.isEmpty() ?
        String.format(" (riga %s)", rowNumber) : "";

        return response +
               System.lineSeparator() +
               String.format("Informazione trovata nel file: %s%s", fileName, additionalInfo);
        } else {
            return response;
        }
    }

    /**
     * Checks if the vector store result contains PDF metadata with page information
     *
     * @param documents List of documents from vector store search
     * @return true if PDF page metadata is available
     */
    private boolean hasPdfMetadata(List<Document> documents) {
        return documents != null &&
               !documents.isEmpty() &&
               documents.getFirst().getMetadata() != null &&
               documents.getFirst().getMetadata().containsKey(PagePdfDocumentReader.METADATA_START_PAGE_NUMBER) &&
               documents.getFirst().getMetadata().containsKey(PagePdfDocumentReader.METADATA_FILE_NAME);
    }

    /**
     * Checks if the vector store result contains Excel metadata
     *
     * @param documents List of documents from vector store search
     * @return true if Excel metadata is available
     */
    private boolean hasExcelMetadata(List<Document> documents) {
        return documents != null &&
               !documents.isEmpty() &&
               documents.getFirst().getMetadata() != null &&
               documents.getFirst().getMetadata().containsKey("fileType") &&
               "xlsx".equals(documents.getFirst().getMetadata().get("fileType"));
    }


}