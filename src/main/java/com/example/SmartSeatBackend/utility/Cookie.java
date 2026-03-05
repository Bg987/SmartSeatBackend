package com.example.SmartSeatBackend.utility;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class Cookie {

    private JwtUtil jwt;

    public jakarta.servlet.http.Cookie setCookie(Long id, String role){

            return cookieSetting("AUTH_JWT",jwt.generateToken(id,role),(24*60*60*10));
    }
    public jakarta.servlet.http.Cookie delCookie(String cookieName){

        return cookieSetting(cookieName,null,0);

    }
    public jakarta.servlet.http.Cookie cookieSetting(String cookieName, String cookieData, int age) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(cookieName, cookieData);

        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(age);

        // REQUIRED for Render -> Vercel communication
        cookie.setSecure(true);

        // REQUIRED for Cross-Site cookie persistence
        cookie.setAttribute("SameSite", "None");

        return cookie;
    }
}
