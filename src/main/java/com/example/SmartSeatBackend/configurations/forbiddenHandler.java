package com.example.SmartSeatBackend.configurations;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class forbiddenHandler implements AccessDeniedHandler {


    //wrong role api call - method calls internallly by spring auth
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        response.getWriter().write(
                "{ \"error\": \"Access denied\", \"message\": \"You are not allowed to access this route\" }"
        );
    }
}
