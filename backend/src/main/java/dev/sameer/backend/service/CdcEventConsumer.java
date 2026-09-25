package dev.sameer.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CdcEventConsumer {

    // Instantiate directly to avoid Spring bean injection errors
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Spring will successfully inject this via @RequiredArgsConstructor
    private final MetricsService metricsService;

    @KafkaListener(topics = "prod.public.test_cdc", groupId = "shadowbase-backend-group")
    public void consumeCdcEvent(String message) {
        try {
            // Parse the raw JSON string
            JsonNode root = objectMapper.readTree(message);
            JsonNode payload = root.path("payload");

            if (!payload.isMissingNode() && !payload.isNull()) {
                String op = payload.path("op").asText();

                // "c" = create, "u" = update, "d" = delete
                if ("c".equals(op) || "u".equals(op) || "d".equals(op)) {
                    metricsService.incrementReplay();
                    System.out.println("Valid CDC operation [" + op + "] parsed. Total Query Replays: " + metricsService.getQueryReplays());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse CDC event: " + e.getMessage());
        }
    }
}