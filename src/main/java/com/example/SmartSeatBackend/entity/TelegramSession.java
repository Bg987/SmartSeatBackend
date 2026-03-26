package com.example.SmartSeatBackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "telegram_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelegramSession {

    @Id
    private Long chatId;       // From update.getMessage().getChatId()
    private String identifier; //  Email
    private String role;       // To know which repo to search
}
