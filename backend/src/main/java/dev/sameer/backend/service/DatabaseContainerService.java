package dev.sameer.backend.service;

import org.springframework.stereotype.Service;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DatabaseContainerService {

    private final Map<String, PostgreSQLContainer> activeContainers = new ConcurrentHashMap<>();

    public String startContainer() {
        String containerId = UUID.randomUUID().toString();
        PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("shadow_db")
                .withUsername("admin")
                .withPassword("admin123");

        postgres.start();
        activeContainers.put(containerId, postgres);

        return containerId;
    }

    public void stopContainer(String containerId) {
        PostgreSQLContainer container =  activeContainers.remove(containerId);
        if(container != null)
            container.stop();
    }

    public String getContainerJdbcUrl(String containerId) {
        PostgreSQLContainer container = activeContainers.get(containerId);
        if(container == null)
            throw new IllegalArgumentException("Container not found");
        return container.getJdbcUrl();
    }

    public void executeSql(String containerId, String sql) throws SQLException {
        PostgreSQLContainer container = activeContainers.get(containerId.trim());
        if(container == null)
                throw new IllegalArgumentException("Container ID not found");
        String jdbcUrl = container.getJdbcUrl();
        String username = container.getUsername();
        String password = container.getPassword();

        try(Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
            Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
        }
        catch(SQLException e) {
            throw new RuntimeException("Failed to execute SQL: " + e.getMessage());
        }
    }
}
