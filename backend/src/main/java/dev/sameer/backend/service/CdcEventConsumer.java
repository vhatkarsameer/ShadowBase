package dev.sameer.backend.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CdcEventConsumer {

    @KafkaListener(topics = "prod.public.test_cdc", groupId = "shadowbase-backend-group")
    public void consumedCdcEvent(String message) {
        System.out.println("Received CDC Event from Kafka: " + message);
    }
}
