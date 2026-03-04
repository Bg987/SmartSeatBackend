package com.example.SmartSeatBackend.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MessageService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String TOPIC1 = "collegeRegisterTopic";
    private final String TOPIC2 = "studentRegisterTopic";
    public MessageService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendRegistrationEvent(String email, String password, String name,String collegeId) {
        Map<String, String> data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);
        data.put("name", name);
        if(collegeId!=null){//check weather student data or not
            data.put("collegeId",collegeId);//put college id in case of student data
        }
        //select topic based on studnet or college data
        kafkaTemplate.send((collegeId==null)?TOPIC1:TOPIC2, data);
    }
}

//.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
//.\bin\windows\kafka-server-start.bat .\config\server.properties
//.\bin\windows\kafka-topics.bat --create --topic collegeRegisterTopic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
