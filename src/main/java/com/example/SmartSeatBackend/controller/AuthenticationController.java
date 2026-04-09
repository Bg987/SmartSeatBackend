package com.example.SmartSeatBackend.controller;

import com.example.SmartSeatBackend.DTO.PasswordDTO;
import com.example.SmartSeatBackend.DTO.UserDTO;
import com.example.SmartSeatBackend.service.AuthenticationService;
import com.example.SmartSeatBackend.service.MessageService;
import com.example.SmartSeatBackend.utility.ApiResponse;
import com.example.SmartSeatBackend.utility.CacheUtil;
import com.example.SmartSeatBackend.utility.HelperMethods;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService AuthService;
    private final PasswordEncoder pass;
    private final HelperMethods helper;
    private final CacheUtil cache;
    private final MessageService msgService;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO user, HttpServletResponse response){

        try{
            if(user.getRole().equals("student")){
                return  AuthService.verifyStudent(user,response);
            }
            return AuthService.verifyUser(user,response);
        }
        catch(BadCredentialsException e){
            return ResponseEntity.status(400).body(new ApiResponse(false, e.getMessage(), null));
        }

    }

    @PreAuthorize("hasAnyRole('university', 'college', 'student')")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response){

        cache.deleteCache();
        return AuthService.logout(response);
    }

    @PreAuthorize("hasAnyRole('university', 'college', 'student')")
    @PatchMapping("/changePassword")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordDTO passworddata, Authentication authentication){
        return AuthService.passwordchange(passworddata,authentication);
    }
}