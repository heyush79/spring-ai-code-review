package com.ai.reviewer.dto;

public class ReviewResponse {

    private String review;

    public ReviewResponse(String review) {
        this.review = review;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }
}
