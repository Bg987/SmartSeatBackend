package com.example.SmartSeatBackend.controller;

import com.example.SmartSeatBackend.DTO.userDTO;
import com.example.SmartSeatBackend.service.AuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

        catch(Exception e){
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('university')")
    @GetMapping("/university")
    public String university(){
        return "hello univeristy";
    }
}