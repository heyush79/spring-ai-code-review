package com.ai.reviewer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.ai.reviewer.model.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadResponse {
    private Long id;
    private String name;
    private Document.DocumentType type;
    private Boolean indexed;
    private String message;
}
