package com.example.SmartSeatBackend.utility;

import com.example.SmartSeatBackend.entity.College;
import com.example.SmartSeatBackend.repository.CollegeRepository;
import com.example.SmartSeatBackend.repository.StudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class HelperMethods {

    private final CollegeRepository collegeRepo;
    private final StudentRepository stuRepo;

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
}
