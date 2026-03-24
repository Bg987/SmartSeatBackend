package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.entity.Notification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    long countByUserIdAndIsReadFalse(String userId);

    List<Notification> findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(String userId);

    // 2. Fetch all notifications for a specific user, newest first
    List<Notification> findAllByUserIdOrderByCreatedAtDesc(String userId);

    // 3. Find all unread notifications to mark them as read later
    List<Notification> findAllByUserIdAndIsReadFalse(String userId);

    // 4. (Optional) If you want to clear notifications for a specific role
    List<Notification> findAllByUserIdAndRole(String userId, String role);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsRead(@Param("userId") String userId);
}
