package com.ai.reviewer.controller;

import com.ai.reviewer.model.Document;
import com.ai.reviewer.repository.DocumentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/")
public class WebController {

    private final DocumentRepository documentRepository;

    public WebController(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @GetMapping
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/upload")
    public String upload(Model model) {
        model.addAttribute("documentTypes", Document.DocumentType.values());
        return "upload";
    }

    @GetMapping("/review")
    public String review(Model model) {
        return "review";
    }

    @GetMapping("/view-documents")
    public String documents(Model model) {
        List<Document> docs = documentRepository.findAll();
        model.addAttribute("documents", docs);
        return "documents";
    }
}
