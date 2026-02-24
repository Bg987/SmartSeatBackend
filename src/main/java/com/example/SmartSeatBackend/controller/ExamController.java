package com.example.SmartSeatBackend.controller;

import com.example.SmartSeatBackend.DTO.QuestionDTO;
import com.example.SmartSeatBackend.service.ExamAiService;
import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamAiService examService;


    @PostMapping("/generate-from-pdf")
    public ResponseEntity<?> uploadAndGenerate(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Extract text from the PDF
            String pdfText = extractTextFromPdf(file);


            // 3. Call your AI Service
            examService.generateQuestions(pdfText,35);

            return ResponseEntity.ok("waiting... question will print in console");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error processing PDF: " + e.getMessage());
        }
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
