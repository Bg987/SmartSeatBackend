package com.example.SmartSeatBackend.utility;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    //called this filter all the api calls exclude route mentioned in permitall in security config
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {


        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {

                if ("AUTH_JWT".equals(cookie.getName())) {
                    String token = cookie.getValue();
                     //check whether token malformed or expired not by verifying signature
                    if(!jwtUtil.validateToken(token)){

                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.setContentType("application/json");
                        //later have to add redirect login as frontend ready instead of message
                        response.getWriter().write(
                                "{ \"error\": \"Access denied\", \"message\": \"cookie modified or expired login again\" }"
                        );
                        return ;
                    }
                    String Id = jwtUtil.extractId(token);
                    String role = jwtUtil.extractRole(token);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    Id,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                            );

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

        }

        filterChain.doFilter(request, response);
    }
}