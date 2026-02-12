package com.example.SmartSeatBackend.controller;

import com.example.SmartSeatBackend.DTO.PasswordDTO;
import com.example.SmartSeatBackend.DTO.UserDTO;
import com.example.SmartSeatBackend.service.AuthenticationService;
import com.example.SmartSeatBackend.utility.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService AuthService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO user, HttpServletResponse response){
        try{
            return AuthService.verifyUser(user,response);
        }
        catch(BadCredentialsException e){
            return ResponseEntity.status(400).body(new ApiResponse(false, e.getMessage(), null));
        }

    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response){
        return AuthService.logout(response);
    }

    @PreAuthorize("hasAnyRole('university', 'college', 'student')")
    @PatchMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody PasswordDTO passworddata,@AuthenticationPrincipal String Id){
        return AuthService.passwordchange(passworddata,Id);
    }
}