package it.interno.ai.controller;


import it.interno.ai.service.PPEDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;



/**
 * @author mirco.cennamo on 27/02/2025
 * @project spring-into-ai
 */
@RestController
public class PPEController {



    private static final Logger log = LoggerFactory.getLogger(PPEController.class);

    private final PPEDocumentService documentService;

    public PPEController(PPEDocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/info")
    public String infoPPE(@RequestParam String question) {
        long startTime = System.currentTimeMillis();

       String response = documentService.queryLLM(question);
        long endTime = System.currentTimeMillis();
        log.info("Time taken to process the request: {} ms", endTime - startTime);
        return response;
    }


    @PostMapping(value = "/uploadPdf", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            // Save the uploaded file to the file system
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
            file.transferTo(convFile);
            documentService.ingestPDF(new FileSystemResource(convFile));

            return ResponseEntity.ok().body("File caricato e processato!");
        } catch (IOException e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body("Errore nel caricamento e nel processamento del file");
        }
    }

    @PostMapping(value = "/uploadExcel", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file){
        try {
            // Save the uploaded file to the file system
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
            file.transferTo(convFile);
            documentService.ingestExcel(new FileSystemResource(convFile));

            return ResponseEntity.ok().body("File caricato e processato!");
        } catch (IOException e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body("Errore nel caricamento e nel processamento del file");
        }
    }



}
