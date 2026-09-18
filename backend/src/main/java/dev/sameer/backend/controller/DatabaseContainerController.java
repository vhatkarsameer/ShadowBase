package dev.sameer.backend.controller;

import dev.sameer.backend.service.DatabaseContainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/containers")
@RequiredArgsConstructor
public class DatabaseContainerController {

    private final DatabaseContainerService containerService;

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startContainer() {
        String containerId = containerService.startContainer();
        String jdbcUrl = containerService.getContainerJdbcUrl(containerId);

        return ResponseEntity.ok(Map.of(
                "containerId", containerId,
                "jdbcUrl", jdbcUrl,
                "status", "Running"
        ));
    }
    @DeleteMapping("/{containerId}/stop")
    public ResponseEntity<String> stopContainer(@PathVariable String containerId) {
        containerService.stopContainer(containerId);
        return ResponseEntity.ok("Container" + containerId + " stopped and destroyed.");
    }

    @PostMapping("/{containerId}/execute")
    public ResponseEntity<String> executeSql(@PathVariable String containerId, @RequestBody String sql) {
        try {
            containerService.executeSql(containerId, sql);
            return ResponseEntity.ok("SQL executed successfully on container " + containerId);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
