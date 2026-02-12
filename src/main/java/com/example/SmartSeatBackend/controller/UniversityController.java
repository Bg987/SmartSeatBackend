package com.example.SmartSeatBackend.controller;

import com.example.SmartSeatBackend.DTO.CollegeDTO;
import com.example.SmartSeatBackend.DTO.SubjectDTO;
import com.example.SmartSeatBackend.DTO.TempCollegeDTO;
import com.example.SmartSeatBackend.entity.User;
import com.example.SmartSeatBackend.service.UniversityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/university")
public class UniversityController {

    private final UniversityService uniservice;

    @PreAuthorize("hasRole('university')")
    @PostMapping("/addCollege")
    public ResponseEntity<?> addCollege(@RequestBody TempCollegeDTO collageData){
        System.out.println("calls");
        try{
            return  uniservice.addCollege(collageData);
        }
        catch(Exception e){
            System.out.println("error in college insert "+e.getMessage());
            return ResponseEntity.status(400).body("college already exist in database");
        }
    }


    @PreAuthorize("hasRole('university')")
    @GetMapping("/colleges")
    public ResponseEntity<List<User>> getAllColleges() {

        System.out.println("call ");
        return uniservice.getAllColleges();
    }

    @PreAuthorize("hasRole('university')")
    @PostMapping("/addSubject")
    public ResponseEntity addSubject(@RequestBody SubjectDTO subject){
        return uniservice.addSubject(subject);
    }
}

//university - registrar@gtu.ac.in 85297175
//college added admin@ldrp.ac.in 224619b2
//college principal@vgecg.ac.in fa24f07c
//college principal@gecg28.ac.in  13190ccf