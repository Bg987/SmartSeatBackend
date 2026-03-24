package com.example.SmartSeatBackend.controller;

import com.example.SmartSeatBackend.entity.Notification;
import com.example.SmartSeatBackend.service.NotificationService;
import com.example.SmartSeatBackend.utility.HelperMethods;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final HelperMethods helper;
    private Long x= 1L;

    @PreAuthorize("hasAnyRole('university', 'college', 'student')")
    @GetMapping("/unread-count")
    public Long getUnreadCount() {
        //return x++;
        String id = notificationService.getId();
        return notificationService.unReadCount(id);
    }

    @PreAuthorize("hasAnyRole('university', 'college', 'student')")
    @GetMapping("/latest")
    public ResponseEntity<List<Notification>> getLatestUnread(Authentication auth) {
        String id = notificationService.getId();
        return ResponseEntity.ok(notificationService.getNewestUnread(id));
    }

    @PreAuthorize("hasAnyRole('university', 'college', 'student')")
    @PutMapping("/mark-as-read")
    public ResponseEntity<Void> markAllRead() {

        String id = notificationService.getId();
        notificationService.markUserNotificationsAsRead(id);
        return ResponseEntity.ok().build();
    }
}
