package com.example.SmartSeatBackend.controller;

import com.example.SmartSeatBackend.DTO.SubjectDTO;
import com.example.SmartSeatBackend.DTO.TempCollegeDTO;
import com.example.SmartSeatBackend.DTO.TimetableDTO;
import com.example.SmartSeatBackend.entity.Subject;
import com.example.SmartSeatBackend.entity.Timetable;
import com.example.SmartSeatBackend.entity.User;
import com.example.SmartSeatBackend.service.AllocationService;
import com.example.SmartSeatBackend.service.UniversityService;
import com.example.SmartSeatBackend.utility.StringProcess;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final AllocationService Seatservice;


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
        String role = auth.getAuthorities().toString();
        System.out.println(role);
        return uniservice.getAllColleges();
    }

    @PreAuthorize("hasRole('university')")
    @GetMapping("/getAllSubjects")
    public ResponseEntity<List<Subject>> getAllSubjects() {

        List<Subject> subjects = uniservice.getAllSubjects();
        return ResponseEntity.ok(subjects);
    }


    @PreAuthorize("hasRole('university')")
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


    @PreAuthorize("hasRole('university')")
    @PostMapping("/addSubject")
    public ResponseEntity addSubject(@Valid  @RequestBody SubjectDTO subject){
        return uniservice.addSubject(subject);
    }



    @PreAuthorize("hasRole('university')")
    @PostMapping(
            value = "/generateTimetable",
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<?> generateTimetable(
            @Valid @RequestBody List<TimetableDTO> timetableDTOList
    ) {
        return uniservice.generateTimetable(timetableDTOList);
    }


    @PreAuthorize("hasRole('university')")
    @GetMapping("/getTimetable/{batchId}")
    public ResponseEntity<List<Timetable>> getTimetable(@PathVariable String batchId) {

        List<Timetable>timeTable = uniservice.getTimetable(batchId);

        return ResponseEntity.ok(timeTable);
    }


    @PreAuthorize("hasRole('university')")
        @PostMapping("/allocate/{collegeId}")
        public String allocate(
                @PathVariable Long collegeId) {

            Seatservice.allocateByCollege(collegeId);

            return "Seat allocation completed!";

    }
}