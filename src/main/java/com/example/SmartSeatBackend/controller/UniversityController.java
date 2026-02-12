package com.example.SmartSeatBackend.controller;

import com.example.SmartSeatBackend.DTO.CollegeDTO;
import com.example.SmartSeatBackend.DTO.SubjectDTO;
import com.example.SmartSeatBackend.DTO.TempCollegeDTO;
import com.example.SmartSeatBackend.entity.User;
import com.example.SmartSeatBackend.service.UniversityService;
import com.example.SmartSeatBackend.utility.StringProcess;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/university")
public class UniversityController {

    private final UniversityService uniservice;


    //only single college
    @PreAuthorize("hasRole('university')")
    @PostMapping("/addCollege")
    public ResponseEntity<String> addCollege(@RequestBody TempCollegeDTO collageData)
    {
        return  uniservice.addCollege(collageData);
    }



    //using csv
    @PreAuthorize("hasRole('university')")
    @PostMapping("/addColleges")
    public ResponseEntity<List<String>> addColleges(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            List<String> responses = uniservice.saveCollegesFromCSV(file);
            System.out.println(responses);
            return ResponseEntity.ok(responses);
        } catch (DataIntegrityViolationException e) {
            String response = StringProcess.process(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of("Error processing file: " +response));
        }
    }

    @PreAuthorize("hasRole('university')")
    @GetMapping("/colleges")
    public ResponseEntity<List<User>> getAllColleges() {
        return uniservice.getAllColleges();
    }

    @PreAuthorize("hasRole('university')")
    @PostMapping("/addSubject")
    public ResponseEntity addSubject(@RequestBody SubjectDTO subject){
        return uniservice.addSubject(subject);
    }
}
//college added succesfully email = admin@ldrp.ac.in password 883a3916
//university - registrar@gtu.ac.in b55580de

// college-> mahatmagandhi@gmail.com password 0dd1837d

//university - registrar@gtu.ac.in 85297175
//college added admin@ldrp.ac.in 224619b2
//college principal@vgecg.ac.in fa24f07c
//college principal@gecg28.ac.in  13190ccf
//ngrok http 8080

