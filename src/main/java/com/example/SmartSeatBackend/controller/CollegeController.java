package com.example.SmartSeatBackend.controller;


import com.example.SmartSeatBackend.DTO.RoomsDTO;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/colleges")
public class CollegeController {


    private final CollegeService colService;

    @PreAuthorize("hasRole('college')")
     @PostMapping("/addStudents")
     public ResponseEntity<String> addStudent(@Valid @RequestBody StudentsDTO studentDTO)
     {
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


    @PreAuthorize("hasRole('college')")
    @PostMapping("/addRooms")
    public ResponseEntity<?> addRooms(@Valid @RequestBody RoomsDTO roomsDTO) {
        try {
            // Service returns RoomsDTO
            RoomsDTO response = colService.addRooms(roomsDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            // Simple map for error message
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Something went wrong: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

   @PostMapping("/uploadRooms")
   public ResponseEntity<?> addRooms(@RequestParam("file") MultipartFile file)
   {
       try {
           List<String> responses = colService.saveRoomsFromCSV(file);

           System.out.println(responses);
           return ResponseEntity.ok(responses);

       } catch (DataIntegrityViolationException ex) {

           return ResponseEntity
                   .status(HttpStatus.CONFLICT)
                   .body(List.of("Duplicate data : " +
                           ex.getMostSpecificCause().getMessage()));

       } catch (Exception e) {

           return ResponseEntity
                   .status(HttpStatus.BAD_REQUEST)
                   .body(List.of("Error processing file: " + e.getMessage()));
       }
   }
}