package com.example.SmartSeatBackend.configurations;

import com.example.SmartSeatBackend.service.SmartSeatBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


@Configuration
public class TelegramConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(SmartSeatBot smartSeatBot) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            botsApi.registerBot(smartSeatBot);
            System.out.println("🚀 TELEGRAM BOT REGISTERED SUCCESSFULLY!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return botsApi;
    }
}