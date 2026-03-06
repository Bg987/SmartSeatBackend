package com.example.SmartSeatBackend.controller;

import com.example.SmartSeatBackend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api1/notifications")
public class NotificationController {

    private final NotificationService notificationService;


    @PreAuthorize("hasRole('university')")
    @GetMapping(value = "/subscribe/university", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Principal principal) {

        // 1. Validation: Ensure the user is actually authenticated
        if (principal == null) {
            // If the cookie is missing or invalid, Principal will be null
            return null;
        }

        // 2. Get the Unique Identifier (Username or ID) from the JWT
        String userId = principal.getName();

        // 3. Create and return the emitter via our Service
        // This 'holds' the connection open
        return notificationService.subscribe(userId);
    }
}
