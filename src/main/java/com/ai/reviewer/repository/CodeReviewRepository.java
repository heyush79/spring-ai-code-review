package com.ai.reviewer.repository;

import com.ai.reviewer.model.CodeReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeReviewRepository
        extends JpaRepository<CodeReview, Long> {
}
