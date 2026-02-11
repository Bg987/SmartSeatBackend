package com.example.SmartSeatBackend.controller;


import com.example.SmartSeatBackend.DTO.CollegeDTO;
import com.example.SmartSeatBackend.DTO.TempCollegeDTO;
import com.example.SmartSeatBackend.entity.User;
import com.example.SmartSeatBackend.service.UniversityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/university")
public class UniversityController {


    @Autowired
    private UniversityService uniservice;


    @PreAuthorize("hasRole('university')")
    @PostMapping("/addCollege")

    public ResponseEntity<String> addCollege(@RequestBody TempCollegeDTO collageData)
    {
        return  uniservice.addCollege(collageData);
    }



   @PostMapping("/addColleges")
   public ResponseEntity<String> addColleges(@RequestParam("file") MultipartFile file)
   {

       try {
           uniservice.saveCollegesFromCSV(file);
           return ResponseEntity.ok("File uploaded successfully!");
       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Error processing file");
       }
   }



    @GetMapping("/colleges")
    public ResponseEntity<List<User>> getAllColleges() {
        return uniservice.getAllColleges();
    }
}

//college added succesfully email = admin@ldrp.ac.in password 883a3916
//university - registrar@gtu.ac.in b55580de

// college-> mahatmagandhi@gmail.com password 0dd1837d