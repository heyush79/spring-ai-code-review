package com.ai.reviewer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AIReviewService {

    private final ChatClient chatClient;
    private final DocumentIngestionService documentIngestionService;

    public AIReviewService(ChatClient.Builder builder, DocumentIngestionService documentIngestionService) {
        this.chatClient = builder.build();
        this.documentIngestionService = documentIngestionService;
    }

    public String reviewCode(String code, String language) {
        // Retrieve relevant context from documents
        String context = retrieveRelevantContext(code, language);

        String prompt = """
                Review the following %s code using the provided context.
                Find bugs, suggest optimizations and rate quality.

                %s

                Code to review:
                %s
                """.formatted(language, context, code);

        return chatClient
                .prompt()
                .system("""
                        You are an expert code reviewer. Use the provided guidelines and context to give insightful reviews.
                        
                        Format your response as follows:
                        - Start with a brief summary
                        - List bugs and issues found
                        - Suggest optimizations
                        - Rate the code quality
                        
                        Do NOT include metadata like reviewer name, timestamp, or any other information not directly related to the code review.
                        Focus only on the technical review content.
                        """)
                .user(prompt)
                .call()
                .content();
    }

    private String retrieveRelevantContext(String code, String language) {
        try {
            // Search for relevant documents by keyword
            String[] keywords = extractKeywords(code, language);
            StringBuilder contextBuilder = new StringBuilder();

            for (String keyword : keywords) {
                var docs = documentIngestionService.searchDocuments(keyword);
                if (!docs.isEmpty()) {
                    contextBuilder.append("Relevant guidelines:\n");
                    docs.stream()
                        .limit(3) // Limit to top 3 matching documents
                        .forEach(doc -> contextBuilder.append("- ").append(doc.getContent().substring(0, Math.min(200, doc.getContent().length()))).append("...\n"));
                    break;
                }
            }

            if (contextBuilder.length() == 0) {
                log.debug("No relevant context found for {} code", language);
                return "No specific guidelines or patterns found.";
            }

            log.info("Retrieved relevant document context");
            return contextBuilder.toString();
        } catch (Exception e) {
            log.error("Error retrieving context", e);
            return "Context retrieval failed, proceeding with general review.";
        }
    }

    private String[] extractKeywords(String code, String language) {
        // Extract simple keywords from code
        return new String[]{ language, "best practices", "standard", "pattern", "guideline" };
    }
}