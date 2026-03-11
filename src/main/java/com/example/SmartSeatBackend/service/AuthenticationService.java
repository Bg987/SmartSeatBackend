package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.DTO.PasswordDTO;
import com.example.SmartSeatBackend.DTO.UserDTO;
import com.example.SmartSeatBackend.controller.StudentController;
import com.example.SmartSeatBackend.entity.Students;
import com.example.SmartSeatBackend.entity.User;
import com.example.SmartSeatBackend.repository.StudentRepository;
import com.example.SmartSeatBackend.repository.UserRepository;
import com.example.SmartSeatBackend.utility.ApiResponse;
import com.example.SmartSeatBackend.utility.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@RequiredArgsConstructor
@Service
public class AuthenticationService {


    private final UserRepository userRepository;
    private final Cookie Cookie;
    private final StudentService stuser;
    private final StudentRepository studentRepo;
    private final StudentController stu;
    private final PasswordEncoder passwordEncoder;

    //for university and colleges
    public ResponseEntity<?> verifyUser(UserDTO userdata, HttpServletResponse response){

        Set<String> validRoles = Set.of("university", "college", "student");

        if (!validRoles.contains(userdata.getRole())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Invalid role", null));
        }
        User u = userRepository.findByMail(userdata.getMail())
                .orElse(null);

        if (u == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse(false, userdata.getRole() + " not found", null));
        }

        if (!userdata.getRole().equals(u.getRole().toString())) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse(false, "You are not registered for this role", null));
        }

        if (!passwordEncoder.matches(userdata.getPassword(), u.getPassword())) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse(false, "Wrong password", null));
        }

        jakarta.servlet.http.Cookie cookie =
                Cookie.setCookie(u.getUserId(), u.getRole().toString());
        response.addCookie(cookie);

        //response data
        Map<String, Object> data = Map.of(
                "name", u.getName(),
                "role", u.getRole().name(),
                "email", u.getMail()
        );

        return ResponseEntity.ok(
                new ApiResponse(true, "Login successful", data)
        );
    }

    //for studdent
    public ResponseEntity<?> verifyStudent(UserDTO userdata, HttpServletResponse response){

        Set<String> validRoles = Set.of("university", "college", "student");
        if (!validRoles.contains(userdata.getRole())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Invalid role", null));
        }
        Students u = studentRepo.findByEmail(userdata.getMail())
                .orElse(null);
        if (u == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse(false, userdata.getRole() + " not found", null));
        }


        if (!passwordEncoder.matches(userdata.getPassword(), u.getPassword())) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse(false, "Wrong password", null));
        }
        jakarta.servlet.http.Cookie cookie =
                Cookie.setCookie(u.getStudentId(), "student");
        response.addCookie(cookie);

        Map<String, Object> data = Map.of(
                "name", u.getName(),
                "role","student",
                "email", u.getEmail()
        );

        return ResponseEntity.ok(
                new ApiResponse(true, "Login successful", data)
        );
    }

    public ResponseEntity<String> logout(HttpServletResponse response){

        jakarta.servlet.http.Cookie cookie= Cookie.delCookie("AUTH_JWT");
        response.addCookie(cookie);
        //later added login/index page of frontend
        return ResponseEntity.status(200).body("logout successfully");
    }

    public ResponseEntity<?> passwordchange(PasswordDTO data, Authentication authentication){
        Long userId = Long.valueOf(authentication.getName()); // username / userId
        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse(null);
        if(role==null){
            new RuntimeException("role not found");
        }
        else if(role.equals("ROLE_student")){

            Students student = studentRepo.findByStudentId(userId);
            if (!passwordEncoder.matches(data.getOldPassword(), student.getPassword())) {
                return ResponseEntity.status(400)
                        .body(new ApiResponse(false, "Old password is wrong", null));
            }

            String encodedPassword = passwordEncoder.encode(data.getNewPassword());
            student.setPassword(encodedPassword);

            // Save
            studentRepo.save(student);
        }
        else{

            User u = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("not found"));
            if (!passwordEncoder.matches(data.getOldPassword(), u.getPassword())) {
                return ResponseEntity.status(400)
                        .body(new ApiResponse(false, "Old password is wrong", null));
            }
            String encodedPassword = passwordEncoder.encode(data.getNewPassword());
            u.setPassword(encodedPassword);

            //Save
            userRepository.save(u);
        }

        return ResponseEntity.ok(
                new ApiResponse(true, "password changed successfully",data));
    }
}