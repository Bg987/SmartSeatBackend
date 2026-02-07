package com.example.SmartSeatBackend.controller;

import com.example.SmartSeatBackend.DTO.userDTO;
import com.example.SmartSeatBackend.service.AuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class authenticationController {

    @Autowired
    private AuthenticationService AuthService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody userDTO user, HttpServletResponse response){
        try{
            String result = AuthService.verifyUser(user,response);
            return ResponseEntity.ok(result);
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
        return "helle student";
    }
}