package com.example.SmartSeatBackend.configurations;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Minimum threads always alive
        executor.setCorePoolSize(10);

        // Max threads allowed
        executor.setMaxPoolSize(20);

        // Queue before creating new threads
        executor.setQueueCapacity(100);

        // Thread naming (VERY useful for debugging)
        executor.setThreadNamePrefix("SmartSeat-Async-");

        // Graceful shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }
}
