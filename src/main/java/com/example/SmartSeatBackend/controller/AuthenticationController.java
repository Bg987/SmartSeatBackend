package com.example.SmartSeatBackend.controller;

import com.example.SmartSeatBackend.DTO.UserDTO;
import com.example.SmartSeatBackend.service.AuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService AuthService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDTO user, HttpServletResponse response){
        try{
            ResponseEntity result = AuthService.verifyUser(user,response);
            return result;
        }
        catch(BadCredentialsException e){
            return ResponseEntity.status(400).body(e.getMessage());
        }

    }

    @PostMapping("logout")
    public ResponseEntity<String> logout(HttpServletResponse response){
        return AuthService.logout(response);

    }

    @PreAuthorize("hasRole('university')")
    @GetMapping("/university")
    public String university(){
        return "hello univeristy";
    }

    @PreAuthorize("hasRole('college')")
    @GetMapping("/college")
    public String college(){
        return "hello college";
    }

    @PreAuthorize("hasRole('student')")
    @GetMapping("/student")
    public String student(){
        return "hello student";
    }
}
