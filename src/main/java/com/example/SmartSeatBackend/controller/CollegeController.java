package com.example.SmartSeatBackend.controller;


import com.example.SmartSeatBackend.DTO.RoomsDTO;
import com.example.SmartSeatBackend.DTO.StudentsDTO;
import com.example.SmartSeatBackend.entity.Rooms;
import com.example.SmartSeatBackend.entity.Students;
import com.example.SmartSeatBackend.entity.Timetable;
import com.example.SmartSeatBackend.repository.RoomsRepository;
import com.example.SmartSeatBackend.repository.StudentRepository;
import com.example.SmartSeatBackend.repository.TimetableRepo;
import com.example.SmartSeatBackend.service.CollegeService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/colleges")
public class CollegeController {


    private final CollegeService colService;
    private final StudentRepository studentRepo;
    private final RoomsRepository roomRepo;
    private final HelperMethods helper;
    private final RoomsRepository roomsRepository;
    private final TimetableRepo timetableRepo;
    //Returns students information college vise----
    @PreAuthorize("hasRole('college')")
    @GetMapping("/students")
    public List<Students> getStudentsByCollege() {
        Long collegeId = helper.getCollegeIdByUserId();
        System.out.println(collegeId);;
        return studentRepo.findByCollegeId(collegeId);
    }


    @PreAuthorize("hasRole('college')")
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
    @PreAuthorize("hasRole('college')")
    @GetMapping("/rooms")
    public Page<Rooms> getRoomsByCollege(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

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
    @PostMapping("/uploadStudents")
    public ResponseEntity<?> uploadStudents(@RequestParam("file") MultipartFile file)
    {
        try {
            List<String> responses = colService.saveStudentsFromCSV(file);

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
    public Map<String, Object>  getRoomNumberAndCapacity(@PathVariable Long roomId)
    {
        Long collegeId = helper.getCollegeIdByUserId();
        return roomRepo.findCapacityAndRoomnumber(collegeId,roomId);
    }

  //get details of exam for particuler college whose students appear in it which is incomplete
    @PreAuthorize("hasRole('college')")
    @GetMapping("/getExamDetails")
    public ResponseEntity<List<Map<String, Object>>> getExamDetails() {

        Long collegeId = helper.getCollegeIdByUserId();
        List<Map<String, Object>> result = timetableRepo.findActiveExamNamesByCollege(collegeId);
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build(); // Returns 204 if no exams found
        }
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('college')")
    @GetMapping("/getRoomInfoOfCollege")
    public ResponseEntity<?>  getRoomInfoOfCollege()
    {
        Long collegeId = helper.getCollegeIdByUserId();
        List<Rooms> response = roomRepo.findByCollegeId(collegeId);
        if(response.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }
}