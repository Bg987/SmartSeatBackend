package com.example.SmartSeatBackend.controller;


import com.example.SmartSeatBackend.DTO.StudentsDTO;
import com.example.SmartSeatBackend.service.CollegeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/colleges")
public class CollegeController {


    private final CollegeService colService;


     @PostMapping("/addStudents")
     public ResponseEntity<String> addStudent(@Valid @RequestBody StudentsDTO studentDTO) {
     try {

         String response = colService.addStudent(studentDTO);

         return new ResponseEntity<>(response, HttpStatus.CREATED);
     } catch (RuntimeException e) {
         return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
     } catch (Exception e) {
         return new ResponseEntity<>("Something went wrong: " + e.getMessage(),
                 HttpStatus.INTERNAL_SERVER_ERROR);
     }
 }


}