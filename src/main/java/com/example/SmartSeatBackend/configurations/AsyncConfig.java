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

        // Minimum threads alive at time
        executor.setCorePoolSize(10);

        // Max threads
        executor.setMaxPoolSize(20);

        executor.setQueueCapacity(100);

        // Thread naming
        executor.setThreadNamePrefix("SmartSeat-Async-");

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }
}
