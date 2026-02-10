package com.example.SmartSeatBackend.controller;


import com.example.SmartSeatBackend.DTO.CollegeDTO;
import com.example.SmartSeatBackend.DTO.TempCollegeDTO;
import com.example.SmartSeatBackend.service.UniversityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/university")
public class UniversityController {


    @Autowired
    private UniversityService uniservice;


    @PreAuthorize("hasRole('university')")
    @PostMapping("/addCollege")
    public ResponseEntity<String> addCollege(@RequestBody TempCollegeDTO collageData){
        return  uniservice.addCollege(collageData);
    }
}

//college added succesfully email = admin@ldrp.ac.in password 883a3916
//university - registrar@gtu.ac.in b55580de