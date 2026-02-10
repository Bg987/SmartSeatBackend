package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.DTO.UserDTO;
import com.example.SmartSeatBackend.entity.User;
import com.example.SmartSeatBackend.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;



    @Autowired
    private Cookie Cookie;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity verifyUser(UserDTO userdata, HttpServletResponse response){
        Set<String> validRoles = Set.of("university", "college", "student");

        if (!validRoles.contains(userdata.getRole())) {
            throw new BadCredentialsException("invalid role");
        }
        User u = userRepository.findByMail(userdata.getMail())
                .orElseThrow(()->new BadCredentialsException(userdata.getRole()+" not found"));

        if(!userdata.getRole().equals(u.getRole().toString())){
            throw new BadCredentialsException("You are not registered for this role");
        }

        if (!passwordEncoder.matches(userdata.getPassword(), u.getPassword())) {
            throw new BadCredentialsException("wrong password");
        }


        jakarta.servlet.http.Cookie cookie =  Cookie.setCookie(u.getUserId(),u.getRole().toString());
        response.addCookie(cookie);
        //as frontend ready redirect to respective role dashboard

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "name", u.getName(),
                "role", u.getRole().name(),
                "email", u.getMail()
        ));

    }



    public ResponseEntity<String> logout(HttpServletResponse response){
        jakarta.servlet.http.Cookie cookie= Cookie.delCookie("AUTH_JWT");
        response.addCookie(cookie);
        //later added login/index page of frontend
        return ResponseEntity.status(200).body("logout successfully");
    }
}