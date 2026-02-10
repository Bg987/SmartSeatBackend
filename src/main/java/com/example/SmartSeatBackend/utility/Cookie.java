package com.example.SmartSeatBackend.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class Cookie {

    @Autowired
    private JwtUtil jwt;

    public jakarta.servlet.http.Cookie setCookie(Long id, String role){

            return cookieSetting("AUTH_JWT",jwt.generateToken(id,role),(24*60*60*10));

    }
    public jakarta.servlet.http.Cookie delCookie(String cookieName){

        return cookieSetting(cookieName,null,0);

    }
    public jakarta.servlet.http.Cookie cookieSetting(String cookieName, String cookieData, int age){
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(cookieName, cookieData);
        // 2. Set the path to match the original cookie (Crucial!)
        cookie.setPath("/");
        // 3. Set HttpOnly/Secure if the original had them
        cookie.setHttpOnly(true);
        //cookie.setSecure(true);  If using HTTPS-production
        cookie.setMaxAge(age);
        return  cookie;
    }
}
