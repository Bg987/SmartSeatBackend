package com.example.SmartSeatBackend.service;


import com.example.SmartSeatBackend.entity.Notification;
import com.example.SmartSeatBackend.repository.NotificationRepository;
import com.example.SmartSeatBackend.utility.HelperMethods;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class NotificationService {

   private final NotificationRepository notificationRepo;
   private final HelperMethods helper;

    public Long unReadCount(String id){
        return notificationRepo.countByUserIdAndIsReadFalse(id);
    }

    public List<Notification> getNewestUnread(String id) {
        return notificationRepo.findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(id);
    }

    @Transactional
    public void markUserNotificationsAsRead(String userId) {
        notificationRepo.markAllAsRead(userId);
    }

    //fetch id/enrNumber based on role
    public String getId(){
        String role = helper.getRole();
        if(role.equals("ROLE_student"))
            return helper.getEnrNumberIdByUserId();

        return helper.getId();
    }
}
