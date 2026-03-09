package com.example.SmartSeatBackend.controller;
//3d973ee5

import com.example.SmartSeatBackend.entity.College;
import com.example.SmartSeatBackend.entity.Students;
import com.example.SmartSeatBackend.service.CollegeService;
import com.example.SmartSeatBackend.service.StudentService;
import com.example.SmartSeatBackend.utility.HelperMethods;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService stuService;
    private final HelperMethods helper;
    private final CollegeService collegeService;

    @PreAuthorize("hasRole('student')")
    @GetMapping("/getStudentDetails")
    public  ResponseEntity<?> getStudentDetails(){
        Students student = stuService.fetchStudent();
        if(student==null){
            return ResponseEntity.status(404).body("student not found");
        }

        Optional<College> college = collegeService.getCollege(student.getCollegeId());
        if(college.isEmpty()){
            return ResponseEntity.status(404).body("college not found serious error");
        }
        Map<String,Object> response = new HashMap<>();
        response.put("student", student);
        response.put("college", college.get());

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('college', 'student')")
    @PostMapping({"/addStudentImage", "/{enrollmentNo}/addStudentImage"})
    public ResponseEntity<?> addStudentImage(
            @PathVariable(required = false) String enrollmentNo, // Changed to String for safety
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }

        // Check Content Type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/jp")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body("Only JPG/JPEG images are allowed.");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        Long StudentId;

        try {
            if ("ROLE_college".equals(role) && enrollmentNo != null && !enrollmentNo.equals("null")) {

                Long collegeId = helper.getCollegeIdByUserId();
                StudentId = stuService.getVerifiedStudentId(enrollmentNo,collegeId);
            }
            else if ("ROLE_student".equals(role)) {
                StudentId = Long.parseLong(auth.getName());
                if (stuService.checkIfImageExists(StudentId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("You have already uploaded an image. Please contact the college to change it.");
                }
            }
            else {
                return ResponseEntity.badRequest().body("ID missing for college role or unauthorized.");
            }
            return stuService.insertStudentImage(file, StudentId);
        }
        catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid ID format provided.");
        }
        catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}