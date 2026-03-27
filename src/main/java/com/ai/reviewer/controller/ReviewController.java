package com.ai.reviewer.controller;

import com.ai.reviewer.dto.ReviewRequest;
import com.ai.reviewer.dto.ReviewResponse;
import com.ai.reviewer.model.CodeReview;
import com.ai.reviewer.repository.CodeReviewRepository;
import com.ai.reviewer.service.AIReviewService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review")
public class ReviewController {

    private final AIReviewService aiReviewService;
    private final CodeReviewRepository repository;

    public ReviewController(AIReviewService aiReviewService,
                            CodeReviewRepository repository) {
        this.aiReviewService = aiReviewService;
        this.repository = repository;
    }

    @PostMapping
    public ReviewResponse review(@RequestBody ReviewRequest request) {

        String review = aiReviewService.reviewCode(
                request.getCode(),
                request.getLanguage()
        );

        CodeReview entity = new CodeReview();
        entity.setCode(request.getCode());
        entity.setLanguage(request.getLanguage());
        entity.setReview(review);

        repository.save(entity);

        return new ReviewResponse(review);
    }
}