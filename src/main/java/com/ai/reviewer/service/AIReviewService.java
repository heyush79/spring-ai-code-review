package com.ai.reviewer.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AIReviewService {

    private final ChatClient chatClient;

    public AIReviewService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String reviewCode(String code, String language) {

        String prompt = """
        Review the following %s code.
        Find bugs, suggest optimizations and rate quality.

        Code:
        %s
        """.formatted(language, code);

        return chatClient
            .prompt()
            .system("You are an expert code reviewer.")
            .user(prompt)
            .call()
            .content();
    }
}