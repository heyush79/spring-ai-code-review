package com.ai.reviewer.repository;

import com.ai.reviewer.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByContentContainingIgnoreCase(String keyword);
}
