package dev.sameer.backend.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MetricsService {

    private final AtomicInteger queryReplays = new AtomicInteger(0);

    public void incrementReplay() {
        queryReplays.incrementAndGet();
    }

    public int getQueryReplays() {
        return queryReplays.get();
    }
}