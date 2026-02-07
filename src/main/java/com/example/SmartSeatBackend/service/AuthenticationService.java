package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.DTO.userDTO;
import com.example.SmartSeatBackend.entity.user;
import com.example.SmartSeatBackend.repository.userRepository;
import com.example.SmartSeatBackend.utility.cookie;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    private userRepository userRepository;

    @Autowired
    private cookie Cookie;

    public String verifyUser(userDTO userdata, HttpServletResponse response){

        user u = userRepository.findByMail(userdata.getMail())
                .orElseThrow(()->new RuntimeException(userdata.getRole()+" not found"));
        if(!userdata.getPassword().equals(u.getPassword())){
            throw  new RuntimeException("wrong password");
        }

        if(!userdata.getRole().equals(u.getRole().toString())){
            throw new RuntimeException("You are not registered for this role");
        }
        Cookie cookie =  Cookie.setCookie(u.getUserId(),u.getRole().toString());
        response.addCookie(cookie);
        return "welcome "+u.getName();
    }
}