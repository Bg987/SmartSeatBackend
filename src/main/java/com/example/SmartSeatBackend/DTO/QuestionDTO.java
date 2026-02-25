package com.example.SmartSeatBackend.DTO;

import java.util.List;

public record QuestionDTO(
        String text,
        List<String> options,
        int correctAnswerIndex
) {}
