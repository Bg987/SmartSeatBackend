package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.DTO.userDTO;
import com.example.SmartSeatBackend.entity.user;
import com.example.SmartSeatBackend.repository.userRepository;
import com.example.SmartSeatBackend.utility.cookie;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class AuthenticationService {

    @Autowired
    private userRepository userRepository;

    @Autowired
    private cookie Cookie;

    public String verifyUser(userDTO userdata, HttpServletResponse response){
        Set<String> validRoles = Set.of("university", "college", "student");

        if (!validRoles.contains(userdata.getRole())) {
            throw new BadCredentialsException("invalid role");
        }
        user u = userRepository.findByMail(userdata.getMail())
                .orElseThrow(()->new BadCredentialsException(userdata.getRole()+" not found"));

        if(!userdata.getRole().equals(u.getRole().toString())){
            throw new BadCredentialsException("You are not registered for this role");
        }

        if(!userdata.getPassword().equals(u.getPassword())){
            throw  new BadCredentialsException("wrong password");
        }

        Cookie cookie =  Cookie.setCookie(u.getUserId(),u.getRole().toString());
        response.addCookie(cookie);
        //as frontend ready redirect to respective role dashboard
        return "welcome "+u.getName();
    }

    public ResponseEntity<String> logout(HttpServletResponse response){
        Cookie cookie= Cookie.delCookie("AUTH_JWT");
        response.addCookie(cookie);
        //later added login/index page of frontend
        return ResponseEntity.status(200).body("logout successfully");
    }
}