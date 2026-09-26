package dev.sameer.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MetricsService {

    private final AtomicInteger queryReplays = new AtomicInteger(0);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void incrementReplay() {
        int currentReplays = queryReplays.incrementAndGet();
        broadcastMetrics(currentReplays);
    }

    public int getQueryReplays() {
        return queryReplays.get();
    }

    public SseEmitter addEmitter() {
        // Create emitter with no timeout
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        this.emitters.add(emitter);

        // Remove emitter when client disconnects
        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        emitter.onError((e) -> this.emitters.remove(emitter));

        // Send initial state immediately upon connection
        try {
            emitter.send(SseEmitter.event().name("metrics").data(queryReplays.get()));
        } catch (IOException e) {
            this.emitters.remove(emitter);
        }

        return emitter;
    }

    private void broadcastMetrics(int replays) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("metrics").data(replays));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}