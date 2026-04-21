package com.example.SmartSeatBackend.controller;


import com.example.SmartSeatBackend.DTO.GetSeatByCollege;
import com.example.SmartSeatBackend.DTO.RoomsDTO;
import com.example.SmartSeatBackend.DTO.StudentsDTO;
import com.example.SmartSeatBackend.entity.Rooms;
import com.example.SmartSeatBackend.entity.Students;
import com.example.SmartSeatBackend.entity.Timetable;
import com.example.SmartSeatBackend.repository.RoomsRepository;
import com.example.SmartSeatBackend.repository.StudentRepository;
import com.example.SmartSeatBackend.repository.TimetableRepo;
import com.example.SmartSeatBackend.service.CollegeService;
import com.example.SmartSeatBackend.service.UniversityService;
import com.example.SmartSeatBackend.utility.HelperMethods;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/colleges")
public class CollegeController {


    private final CollegeService colService;
    private final UniversityService uniService;
    private final StudentRepository studentRepo;
    private final RoomsRepository roomRepo;
    private final HelperMethods helper;
    private final RoomsRepository roomsRepository;
    private final TimetableRepo timetableRepo;


    //Returns students information college vise----

    @PreAuthorize("hasRole('college')")
    @GetMapping("/students")
    public List<Students> getStudentsByCollege() throws Exception {
        Long collegeId = helper.getCollegeIdByUserId();
        return  colService.getStudents(collegeId);
    }


    @PreAuthorize("hasRole('college')")
    @PostMapping("/addStudents")
    public ResponseEntity<String> addStudent(@Valid @RequestBody StudentsDTO studentDTO) throws Exception {
        Long collegeID = helper.getCollegeIdByUserId();

        // Service handles all logic and throws errors if validation fails
        String enrollmentNo = colService.addStudent(studentDTO, collegeID);

        return new ResponseEntity<>(
                "Student registered successfully with enrollment: " + enrollmentNo,
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("hasRole('college')")
    @PostMapping("/uploadStudents")
    public ResponseEntity<?> uploadStudents(@RequestParam("file") MultipartFile file) throws Exception {
        Long collegeID = helper.getCollegeIdByUserId();
        try {
            // If any row fails, this line throws an exception and nothing below it runs
            List<String> responses = colService.saveStudentsFromCSV(file, collegeID);

            return ResponseEntity.ok(responses);

        } catch (ResponseStatusException ex) {
            // This catches your business logic errors (e.g., Duplicate Email, Sem 1 Backlog)
            // and returns the specific reason you defined in the service.
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(List.of(ex.getReason()));

        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(List.of("Database Constraint Violation: " + ex.getMostSpecificCause().getMessage()));

        } catch (Exception e) {
            // General fallback for formatting errors or IO issues
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(List.of("Bulk Upload Failed: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('college')")
    @GetMapping("/rooms")
    public Page<Rooms> getRoomsByCollege(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) throws Exception {

        Long collegeId = helper.getCollegeIdByUserId();
        Pageable pageable = PageRequest.of(page, size);
        return roomRepo.findByCollegeCollegeId(collegeId, pageable);
    }

    @PreAuthorize("hasRole('college')")
    @PostMapping("/addRooms")
    public ResponseEntity<?> addRooms(@Valid @RequestBody RoomsDTO roomsDTO) {
        try {
            // Service returns RoomsDTO
            System.out.println(roomsDTO.getBlock());
            RoomsDTO response = colService.addRooms(roomsDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (DataIntegrityViolationException e) {
            // Simple map for error message
            Map<String, String> error = new HashMap<>();
            error.put("message","room number "+roomsDTO.getRoomNumber()+" is already exist in "+roomsDTO.getBlock()+" block in college");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Something went wrong: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PreAuthorize("hasRole('college')")
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

    @PreAuthorize("hasRole('college')")
    @GetMapping("/getTimetable/{branch}/{semester}")

    public ResponseEntity<List<Timetable>> getTimetable(@PathVariable String branch,@PathVariable Integer semester) {

        List<Timetable>timeTable = colService.getTimetable(branch,semester);

        return ResponseEntity.ok(timeTable);
    }


    @PreAuthorize("hasRole('college')")
    @GetMapping("/getRoomInfo/{roomId}")
    public Map<String, Object>  getRoomNumberAndCapacity(@PathVariable Long roomId) throws Exception {
        Long collegeId = helper.getCollegeIdByUserId();
        return roomRepo.findCapacityAndRoomnumber(collegeId,roomId);
    }

  //get details of exam for particuler college whose students appear in it which is incomplete
    @PreAuthorize("hasRole('college')")
    @GetMapping("/getExamDetails")
    public ResponseEntity<List<Map<String, Object>>> getExamDetails() throws Exception {

        Long collegeId = helper.getCollegeIdByUserId();

        List<Map<String, Object>> result = timetableRepo.findExamNamesByCollegeAndStatus(collegeId,false);
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build(); // Returns 204 if no exams found
        }
        return ResponseEntity.ok(result);
    }

    //fetch complete exams for college for analysis
    @PreAuthorize("hasRole('college')")
    @GetMapping("/getCompletedExamDetails")
    public ResponseEntity<List<Map<String, Object>>> getCompleteExamDetails() throws Exception {

        Long collegeId = helper.getCollegeIdByUserId();

        List<Map<String, Object>> result = timetableRepo.findExamNamesByCollegeAndStatus(collegeId,true);
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build(); // Returns 204 if no exams found
        }
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('college')")
    @GetMapping("/getRoomInfoOfCollege")
    public ResponseEntity<?>  getRoomInfoOfCollege() throws Exception {
        Long collegeId = helper.getCollegeIdByUserId();
        List<Rooms> response = roomRepo.findByCollegeId(collegeId);
        if(response.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    //fetch seating allocation of particuler exam of logged in college
    @PreAuthorize("hasRole('college')")
    @GetMapping("/getSeatBYCollege/{exam_id}")
    public ResponseEntity<List<GetSeatByCollege>> getSeatByCollege(
            @PathVariable Long exam_id) throws Exception {

        //fetch college id based on userid stored in jwt cookie
        Long college_id= helper.getCollegeIdByUserId();
        List<GetSeatByCollege> seats =
                colService.getSeatBYCollege(college_id, exam_id);

        return ResponseEntity.ok(seats);
    }

    @PreAuthorize("hasRole('college')")
    @GetMapping("/getBranch")
    public ResponseEntity<?> getBranch(){

        return ResponseEntity.ok(uniService.getAllSubjects());
    }
}