package com.example.SmartSeatBackend.controller;

import com.example.SmartSeatBackend.DTO.SubjectDTO;
import com.example.SmartSeatBackend.DTO.TempCollegeDTO;
import com.example.SmartSeatBackend.entity.User;
import com.example.SmartSeatBackend.service.UniversityService;
import com.example.SmartSeatBackend.utility.StringProcess;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@AllArgsConstructor
@RestController
@RequestMapping("/api/university")
public class UniversityController {

    private final UniversityService uniservice;


    //only single college
    @PreAuthorize("hasRole('university')")
    @PostMapping("/addCollege")
    public ResponseEntity<String> addCollege(@Valid @RequestBody TempCollegeDTO collageData)
    {
        try{
            return  uniservice.addCollege(collageData);
        }
        catch(Exception e){
            System.out.println("error in college insert "+e.getMessage());
            return ResponseEntity.status(409).body("college already exist in database");
        }
    }

    //using csv
    @PreAuthorize("hasRole('university')")
    @PostMapping("/addColleges")
    public ResponseEntity<?> addColleges(@RequestParam("file") MultipartFile file) {
        try {
                List<String> responses = uniservice.saveCollegesFromCSV(file);

                System.out.println(responses);
                return ResponseEntity.ok(responses);

            } catch (DataIntegrityViolationException ex) {

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(List.of("Duplicate Email Found: " +
                                ex.getMostSpecificCause().getMessage()));

            } catch (Exception e) {

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(List.of("Error processing file: " + e.getMessage()));
            }
    }

    @PreAuthorize("hasRole('university')")
    @GetMapping("/colleges")
    public ResponseEntity<List<User>> getAllColleges() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("CONTROLLER AUTH: " + auth);
        return uniservice.getAllColleges();
    }

    @PreAuthorize("hasRole('university')")
    @PostMapping("/addSubject")
    public ResponseEntity addSubject(@RequestBody SubjectDTO subject){
        return uniservice.addSubject(subject);
    }
}