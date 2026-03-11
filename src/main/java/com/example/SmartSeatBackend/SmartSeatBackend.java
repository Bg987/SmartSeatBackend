package com.example.SmartSeatBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SmartSeatBackend {

	public static void main(String[] args) {

		SpringApplication.run(SmartSeatBackend.class, args);
	}

}