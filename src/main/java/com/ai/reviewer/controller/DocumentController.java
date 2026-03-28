package com.ai.reviewer.controller;

import com.ai.reviewer.dto.DocumentUploadResponse;
import com.ai.reviewer.model.Document;
import com.ai.reviewer.service.DocumentIngestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentIngestionService documentIngestionService;

    public DocumentController(DocumentIngestionService documentIngestionService) {
        this.documentIngestionService = documentIngestionService;
    }

    @PostMapping("/upload")
    public DocumentUploadResponse uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String docType) {

        try {
            Document.DocumentType type = Document.DocumentType.valueOf(docType.toUpperCase());
            Document doc = documentIngestionService.uploadDocument(file, type);

            return new DocumentUploadResponse(
                    doc.getId(),
                    doc.getName(),
                    doc.getType(),
                    doc.getIndexed(),
                    "Document uploaded and indexed successfully");
        } catch (IOException e) {
            log.error("Failed to upload document", e);
            return new DocumentUploadResponse(
                    null,
                    file.getOriginalFilename(),
                    null,
                    false,
                    "Failed to upload document: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid document type", e);
            return new DocumentUploadResponse(
                    null,
                    file.getOriginalFilename(),
                    null,
                    false,
                    "Invalid document type. Use: CODE, GUIDELINE, COMMIT_HISTORY, DOCUMENTATION");
        }
    }

    @GetMapping
    public List<Document> getAllDocuments() {
        return documentIngestionService.getAllDocuments();
    }

    @GetMapping("/{id}")
    public Document getDocument(@PathVariable Long id) {
        return documentIngestionService.getDocument(id);
    }
}
