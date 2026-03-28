package com.ai.reviewer.service;

import com.ai.reviewer.model.Document;
import com.ai.reviewer.repository.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class DocumentIngestionService {

    private final DocumentRepository documentRepository;

    public DocumentIngestionService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Document uploadDocument(MultipartFile file, Document.DocumentType type) throws IOException {
        String content = new String(file.getBytes());

        Document doc = new Document();
        doc.setName(file.getOriginalFilename());
        doc.setContent(content);
        doc.setType(type);
        doc.setUploadedAt(LocalDateTime.now());
        doc.setIndexed(true);

        Document savedDoc = documentRepository.save(doc);
        log.info("Document uploaded: {} with ID: {}", file.getOriginalFilename(), savedDoc.getId());

        return savedDoc;
    }

    public Document getDocument(Long id) {
        return documentRepository.findById(id).orElse(null);
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public List<Document> searchDocuments(String keyword) {
        return documentRepository.findByContentContainingIgnoreCase(keyword);
    }
}
