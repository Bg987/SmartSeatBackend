package com.example.SmartSeatBackend.controller;

import com.example.SmartSeatBackend.DTO.SubjectDTO;
import com.example.SmartSeatBackend.DTO.TempCollegeDTO;
import com.example.SmartSeatBackend.entity.Subject;
import com.example.SmartSeatBackend.entity.User;
import com.example.SmartSeatBackend.service.UniversityService;
import com.example.SmartSeatBackend.utility.StringProcess;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/university")
public class UniversityController {

    private final UniversityService uniservice;

    // ==============================
    // ADD SINGLE COLLEGE
    // ==============================
    @PreAuthorize("hasRole('university')")
    @PostMapping("/addCollege")
    public ResponseEntity<String> addCollege(@Valid @RequestBody TempCollegeDTO collageData) {
        try {
            return uniservice.addCollege(collageData);
        } catch (Exception e) {
            System.out.println("Error in college insert: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("College already exists in database");
        }
    }

    // ==============================
    // ADD MULTIPLE COLLEGES USING CSV
    // ==============================
    @PreAuthorize("hasRole('university')")
    @PostMapping("/addColleges")
    public ResponseEntity<List<String>> addColleges(@RequestParam("file") MultipartFile file) {

        try {
            List<String> responses = uniservice.saveCollegesFromCSV(file);
            return ResponseEntity.ok(responses);

        } catch (DataIntegrityViolationException ex) {

            String response = StringProcess.process(ex.getMessage());

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(List.of("Duplicate Entry Found: " + response));

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(List.of("Error processing file: " + e.getMessage()));
        }
    }

    // ==============================
    // GET ALL COLLEGES
    // ==============================
    @PreAuthorize("hasRole('university')")
    @GetMapping("/colleges")
    public ResponseEntity<List<User>> getAllColleges() {
        return uniservice.getAllColleges();
    }

    // ==============================
    // ADD SUBJECT
    // ==============================
    @PreAuthorize("hasRole('university')")
    @PostMapping("/addSubject")
    public ResponseEntity<?> addSubject(@RequestBody SubjectDTO subject) {
        return uniservice.addSubject(subject);
    }

    @PreAuthorize("hasRole('university')")
    @GetMapping("/getAllSubjects")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        List<Subject> subjects = uniservice.getAllSubjects();
        return ResponseEntity.ok(subjects);
    }



    @PostMapping("/uploadSubjects")

    public ResponseEntity<List<String>> uploadSubjects(@RequestParam("file") MultipartFile file) {

        try {
            List<String> responses = uniservice.saveSubjectsFromCSV(file);
            return ResponseEntity.ok(responses);

        } catch (DataIntegrityViolationException ex) {

            String response = StringProcess.process(ex.getMessage());

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(List.of("Duplicate Entry Found: " + response));

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(List.of("Error processing file: " + e.getMessage()));
        }
    }

}
