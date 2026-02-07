package com.example.SmartSeatBackend.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.Cookie;


@Component
public class cookie {

    @Autowired
    private JwtUtil jwt;

    public Cookie setCookie(Long id,String role){

            String jwtToken = jwt.generateToken(id,role);
            Cookie cookie = new Cookie("AUTH_JWT", jwtToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60*10);//10 days
            return cookie;

    }
}
