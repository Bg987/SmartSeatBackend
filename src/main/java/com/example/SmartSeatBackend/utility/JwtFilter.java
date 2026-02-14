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
            boolean found = false;
            for (Cookie cookie : request.getCookies()) {
                if ("AUTH_JWT".equals(cookie.getName())) {
                    found = true;
                    String token = cookie.getValue();
                    if(!jwtUtil.validateToken(token)){
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
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
            if(!found){
                // Reject request if JWT cookie not present
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{ \"error\": \"Access denied\", \"message\": \"No JWT cookie found\" }"
                );
                return;
            }
        } else {
            // No cookies at all
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{ \"error\": \"Access denied\", \"message\": \"No cookies sent\" }"
            );
            return;
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