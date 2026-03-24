package com.example.SmartSeatBackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_is_read", columnList = "is_read")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    // This can be the User UUID or Enrollment Number
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String role; // UNIVERSITY, COLLEGE, STUDENT

    @Column(nullable = false)
    private String type; // ALLOCATION_DONE, RESULT_OUT, etc.

    @Column(columnDefinition = "TEXT", nullable = false)
    private String msg;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
