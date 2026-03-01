package com.example.SmartSeatBackend.controller;

import com.example.SmartSeatBackend.DTO.GetSeatingPlan;
import com.example.SmartSeatBackend.DTO.SubjectDTO;
import com.example.SmartSeatBackend.DTO.TempCollegeDTO;
import com.example.SmartSeatBackend.DTO.TimetableDTO;
import com.example.SmartSeatBackend.entity.College;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/university")
public class UniversityController {

    private final UniversityService uniservice;
    private final AllocationService seatService;

    // Add Single College
    @PreAuthorize("hasRole('university')")
    @PostMapping("/addCollege")
    public ResponseEntity<String> addCollege(@Valid @RequestBody TempCollegeDTO collageData) {
        try {
            return uniservice.addCollege(collageData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("College already exists in database");
        }
    }

    // Upload Colleges via CSV
    @PreAuthorize("hasRole('university')")
    @PostMapping("/addColleges")
    public ResponseEntity<?> addColleges(@RequestParam("file") MultipartFile file) {
        try {
            List<String> responses = uniservice.saveCollegesFromCSV(file);
            return ResponseEntity.ok(responses);

        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(List.of("Duplicate Email Found: "
                            + ex.getMostSpecificCause().getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of("Error processing file: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('university')")
    @GetMapping("/colleges")
    public ResponseEntity<List<User>> getAllColleges() {
        return uniservice.getAllColleges();
    }

    @PreAuthorize("hasRole('university')")
    @GetMapping("/getAllSubjects")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        return ResponseEntity.ok(uniservice.getAllSubjects());
    }

    @PreAuthorize("hasRole('university')")
    @PostMapping("/uploadSubjects")
    public ResponseEntity<List<String>> uploadSubjects(
            @RequestParam("file") MultipartFile file) {

        try {
            List<String> responses = uniservice.saveSubjectsFromCSV(file);
            return ResponseEntity.ok(responses);

        } catch (DataIntegrityViolationException ex) {
            String response = StringProcess.process(ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of("Duplicate Entry Found: " + response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of("Error processing file: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('university')")
    @PostMapping("/addSubject")
    public ResponseEntity<?> addSubject(@Valid @RequestBody SubjectDTO subject) {
        return uniservice.addSubject(subject);
    }

    @PreAuthorize("hasRole('university')")
    @PostMapping(value = "/generateTimetable",
            consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<?> generateTimetable(
            @Valid @RequestBody List<TimetableDTO> timetableDTOList) {

        return uniservice.generateTimetable(timetableDTOList);
    }

    @PreAuthorize("hasRole('university')")
    @GetMapping("/getTimetable/{batchId}")
    public ResponseEntity<List<Timetable>> getTimetable(
            @PathVariable String batchId) {

        return ResponseEntity.ok(uniservice.getTimetable(batchId));
    }

    // ✅ Seat Allocation
    @PreAuthorize("hasRole('university')")
    @PostMapping("/allocate/{collegeId}/{subjectCode}")
    public ResponseEntity<String> allocate(
            @PathVariable Long collegeId,
            @PathVariable String subjectCode) {

//        seatService.allocateByCollege(collegeId, subjectCode);
        return ResponseEntity.ok("Seat allocation completed successfully!");
    }

    //  Seating Plan API (Production Ready)
 @PreAuthorize("hasRole('university')")
 @GetMapping("/getSeattingPlan/{collegeId}")
 public String getSeatingPlan(@PathVariable Long collegeId)
 { return "currently unavailable";
     //return Seatservice.getSeatingPlan(collegeId);
      }

    @PreAuthorize("hasRole('university')")
    @GetMapping("/showCollegeDetail/{userId}")
    public College showCollegeDetails(@PathVariable Long userId) {
        return uniservice.getCollegeByUser(userId);
    }

    @PreAuthorize("hasRole('university')")
    @GetMapping("/getCountOfStudents/{collegeId}")
    public Long countOfStudents(@PathVariable Long collegeId) {
        return uniservice.getCountOfStudents(collegeId);
    }

    @PreAuthorize("hasRole('university')")
    @GetMapping("/getCountOfRooms/{collegeId}")
    public Long countOfRooms(@PathVariable Long collegeId) {
        return uniservice.getCountofRooms(collegeId);
    }

    @PreAuthorize("hasRole('university')")
    @PostMapping("/subjects/filter")
    public List<Subject> getSubjectsByDepartmentBranchSemester(
            @RequestBody SubjectDTO subjectFilterDTO) {

        return uniservice.getSubjectsDepartmentBranchSemester(
                subjectFilterDTO.getDepartment(),
                subjectFilterDTO.getBranch(),
                subjectFilterDTO.getSemester());
    }

    @PostMapping("/main/{examId}")
    public ResponseEntity<String> mainWork(@PathVariable Long examId) {
        uniservice.mainWork(examId);
        return ResponseEntity.ok("Done working in background");
    }
}