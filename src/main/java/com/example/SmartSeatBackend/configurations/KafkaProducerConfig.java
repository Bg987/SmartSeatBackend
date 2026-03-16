package com.example.SmartSeatBackend.configurations;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory() throws IOException {
        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "smartseat-bhavyagodhaniya2004-2234.k.aivencloud.com:26025");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        props.put("security.protocol", "SSL");

        // 1. Truststore (CA Certificate)
        // It's safer to provide the absolute path or the content string
        String caContent = new String(new ClassPathResource("ca.pem").getInputStream().readAllBytes());
        props.put("ssl.truststore.type", "PEM");
        props.put("ssl.truststore.certificates", caContent);

        // 2. Keystore (Service Cert and Private Key)
        String serviceCert = new String(new ClassPathResource("service.cert").getInputStream().readAllBytes());
        String serviceKey = new String(new ClassPathResource("service.key").getInputStream().readAllBytes());

        props.put("ssl.keystore.type", "PEM");
        props.put("ssl.keystore.certificate.chain", serviceCert);
        props.put("ssl.keystore.key", serviceKey);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() throws IOException {
        return new KafkaTemplate<>(producerFactory());
    }
}