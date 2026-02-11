package com.example.SmartSeatBackend.controller;

import com.example.SmartSeatBackend.DTO.CollegeDTO;
import com.example.SmartSeatBackend.DTO.SubjectDTO;
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

    private final UniversityService uniservice;

    @PreAuthorize("hasRole('university')")
    @PostMapping("/addCollege")

    public ResponseEntity<String> addCollege(@RequestBody TempCollegeDTO collageData)
    {
        return  uniservice.addCollege(collageData);
    }




    @PostMapping("/addColleges")
    public ResponseEntity<List<String>> addColleges(@RequestParam("file") MultipartFile file) {
        try {
            List<String> responses = uniservice.saveCollegesFromCSV(file);
            System.out.println(responses);
            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of("Error processing file: " + e.getMessage()));
        }
    }



//    public ResponseEntity<String> addCollege(@RequestBody TempCollegeDTO collageData){
//        try{
//            return  uniservice.addCollege(collageData);
//        }
//        catch(Exception e){
//            System.out.println("error in college insert "+e.getMessage());
//            return ResponseEntity.status(400).body("college already exist in database");
//        }
//    }


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

