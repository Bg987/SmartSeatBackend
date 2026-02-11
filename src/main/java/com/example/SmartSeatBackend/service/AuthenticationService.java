package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.DTO.UserDTO;
import com.example.SmartSeatBackend.entity.User;
import com.example.SmartSeatBackend.repository.UserRepository;
import com.example.SmartSeatBackend.utility.ApiResponse;
import com.example.SmartSeatBackend.utility.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthenticationService {


    private final UserRepository userRepository;
    private final Cookie Cookie;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<?> verifyUser(UserDTO userdata, HttpServletResponse response){

        Set<String> validRoles = Set.of("university", "college", "student");

        if (!validRoles.contains(userdata.getRole())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Invalid role", null));
        }

        User u = userRepository.findByMail(userdata.getMail())
                .orElse(null);

        if (u == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse(false, userdata.getRole() + " not found", null));
        }

        if (!userdata.getRole().equals(u.getRole().toString())) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse(false, "You are not registered for this role", null));
        }

        if (!passwordEncoder.matches(userdata.getPassword(), u.getPassword())) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse(false, "Wrong password", null));
        }

        jakarta.servlet.http.Cookie cookie =
                Cookie.setCookie(u.getUserId(), u.getRole().toString());
        response.addCookie(cookie);

        Map<String, Object> data = Map.of(
                "name", u.getName(),
                "role", u.getRole().name(),
                "email", u.getMail()
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
}