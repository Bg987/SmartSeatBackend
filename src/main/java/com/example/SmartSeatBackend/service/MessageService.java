package com.example.SmartSeatBackend.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MessageService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String TOPIC = "collegeRegisterTopic";

    public MessageService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCollegeRegistrationEvent(String email, String password, String name) {
        Map<String, String> data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);
        data.put("name", name);
        kafkaTemplate.send(TOPIC, data);
        System.out.println(">> Event sent to Kafka: ");
    }
}

//.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
//.\bin\windows\kafka-server-start.bat .\config\server.properties
//.\bin\windows\kafka-topics.bat --create --topic collegeRegisterTopic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
