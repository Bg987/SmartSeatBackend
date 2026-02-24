package com.example.SmartSeatBackend.service;



import com.example.SmartSeatBackend.DTO.QuestionDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
@Service
public class ExamAiService {

    private final ChatClient chatClient;

    public ExamAiService(ChatClient.Builder builder) {
        // Spring AI autoconfigures this builder with Gemini properties from your .yml
        this.chatClient = builder.build();
    }

    /**
     * Generates questions and returns a List of QuestionDTOs.
     * Use this if you want to save them to a database in your Controller.
     */
    public List<QuestionDTO> generateQuestions(String contextText, int totalQuestions) {
        int batchSize = 5;
        int numberOfBatches = (int) Math.ceil((double) totalQuestions / batchSize);
        List<QuestionDTO> allGeneratedQuestions = new ArrayList<>();

        System.out.println("Starting Gemini generation for " + totalQuestions + " questions...");

        for (int i = 1; i <= numberOfBatches; i++) {
            // Calculate how many questions to ask for in this specific batch
            int questionsToAsk = Math.min(batchSize, totalQuestions - allGeneratedQuestions.size());

            // Limit context size to avoid token limits on 15MB files
            String limitedContext = truncateContext(contextText, 30000);

            String batchPrompt = """
                Context: {context}
                
                Task: You are an expert exam setter. Generate {count} unique MCQs based on the context.
                
                Requirements:
                - Return ONLY a valid JSON array.
                - Each object must have: 'text', 'options' (List of 4 strings), and 'correctAnswerIndex' (0-3).
                - Do not include markdown code blocks or introductory text.
                """;

            System.out.println("\n--- Processing Batch " + i + " of " + numberOfBatches + " ---");

            try {
                // .entity() handles the JSON parsing into your DTO automatically
                List<QuestionDTO> batchResult = this.chatClient.prompt()
                        .user(u -> u.text(batchPrompt)
                                .param("context", limitedContext)
                                .param("count", questionsToAsk))
                        .call()
                        .entity(new ParameterizedTypeReference<List<QuestionDTO>>() {});

                if (batchResult != null && !batchResult.isEmpty()) {
                    allGeneratedQuestions.addAll(batchResult);
                    // Print progress to console
                    // Change q.getText() to q.text()
                    batchResult.forEach(q -> System.out.println("AI Generated Question: " + q.text()));
                }

            } catch (Exception e) {
                System.err.println("Error in Batch " + i + ": " + e.getMessage());
                // Continue to next batch instead of failing the whole process
            }
        }

        System.out.println("\n--- All " + allGeneratedQuestions.size() + " Questions Successfully Collected ---");
        return allGeneratedQuestions;
    }

    private String truncateContext(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}