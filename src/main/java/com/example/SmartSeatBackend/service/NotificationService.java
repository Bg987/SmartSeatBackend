package com.example.SmartSeatBackend.service;


import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {

    // Store emitters by UserID (String). ConcurrentHashMap is thread-safe.
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String userId) {
        // Create emitter with a timeout (e.g., 60 seconds)
        SseEmitter emitter = new SseEmitter(60_000L);

        // Cleanup logic
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        emitters.put(userId, emitter);

        // Send an initial "connected" event to prevent immediate timeout
        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected"));
        } catch (IOException e) {
            emitters.remove(userId);
        }

        return emitter;
    }

    public void sendNotification(String userId, Object data) {
        if (emitters.containsKey(userId)) {
            SseEmitter emitter = emitters.get(userId);
            try {
                // "event-complete" is the custom name Angular will listen for
                emitter.send(SseEmitter.event()
                        .name("event-complete")
                        .data(data));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }
}
