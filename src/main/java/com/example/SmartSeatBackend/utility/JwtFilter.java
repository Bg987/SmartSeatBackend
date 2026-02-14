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


    private final  JwtUtil jwtUtil;

    //called this filter all the api calls exclude route mentioned in permitall in security config
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        // Skip filter logic for login/logout/public endpoints
        if (path.startsWith("/api/auth/login")||path.startsWith("/api/auth/logout") || path.startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {

                if ("AUTH_JWT".equals(cookie.getName())) {
                    String token = cookie.getValue();
                     //check whether token malformed or expired not by verifying signature
                    if(!jwtUtil.validateToken(token)){

                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        //later have to add redirect login as frontend ready instead of message
                        response.getWriter().write(
                                "{ \"error\": \"Access denied\", \"message\": \"cookie modified or expired login again\" }"
                        );
                        return ;
                    }
                    String Id = jwtUtil.extractId(token);
                    String role = jwtUtil.extractRole(token);
                    System.out.println(Id+" "+role);
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

      try{

          filterChain.doFilter(request, response);
      }
      catch(Exception e){
          //System.out.println("error after filter = "+e.getMessage());
          throw e;
      }
    }
}