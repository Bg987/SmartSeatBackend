package com.example.SmartSeatBackend.utility;

import com.example.SmartSeatBackend.entity.College;
import com.example.SmartSeatBackend.repository.CollegeRepository;
import com.example.SmartSeatBackend.repository.StudentRepository;
import com.example.SmartSeatBackend.service.MessageService;
import com.example.SmartSeatBackend.service.UniversityService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@AllArgsConstructor
public class HelperMethods {

    private final CollegeRepository collegeRepo;
    private final StudentRepository stuRepo;
    private final MessageService msgService;

    public Long getCollegeIdByUserId() {
        String userId= getId();
        return collegeRepo.findByUser_userId(Long.parseLong(userId)) // Or the method we fixed earlier
                .map(College::getCollegeId)
                .orElseThrow(() -> new RuntimeException("College not found for User ID: " + userId));
    }

    //fetch enrolement number using student_id
    public String getEnrNumberIdByUserId() {
        String studentId = getId();
        return stuRepo.findEnrollmentNoByStudentId(Long.parseLong(studentId)) // Or the method we fixed earlier
                .orElseThrow(() -> new RuntimeException("enr. number not found for student ID: " + studentId));
    }

    public String getId(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 2. Extract the Principal (which is "752" in your case)
        return auth.getPrincipal().toString();
    }

    public String getRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Spring Security stores roles in the Authorities collection
        // We find the first authority and return it as a string
        return auth.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null); // Default fallback
    }


    //send batch email in the case of csv college upload
    public void sendRegistrationBatch(List<UniversityService.RegistrationDetail> details) {
        for (UniversityService.RegistrationDetail detail : details) {
            try {
                msgService.sendRegistrationEvent(
                        detail.email(),
                        detail.password(),
                        detail.name(),
                        null
                );
            } catch (Exception e) {
                // Log the error but don't stop the whole process
                // since the DB save is already finished.
                System.err.println("Failed to send event for: " + detail.email());
            }
        }
    }
}
