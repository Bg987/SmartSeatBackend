package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.entity.TelegramSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelegramSessionRepository extends JpaRepository<TelegramSession, Long> {
    // Finds if an email is already "Owned" by any ChatID
    Optional<TelegramSession> findByIdentifier(String identifier);
}
