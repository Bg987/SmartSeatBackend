package com.example.SmartSeatBackend.controller;


import com.example.SmartSeatBackend.service.CollegeService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/college")
public class CollegeController {


    private final CollegeService colService;

    @PreAuthorize("hasRole('college')")
    @PostMapping("/addStudents/exel")
    public ResponseEntity addStudents(@RequestParam("file") MultipartFile file) throws IOException {
        colService.exelProcess(file);
        return ResponseEntity.ok(" done ");
    }

}