package com.ai.reviewer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Slf4j
@Component
public class VectorStoreInitializer implements CommandLineRunner {

    private final DataSource dataSource;

    public VectorStoreInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {

            // Enable pgvector extension
            try {
                stmt.execute("CREATE EXTENSION IF NOT EXISTS vector");
                log.info("pgvector extension enabled");
            } catch (Exception e) {
                log.warn("pgvector extension might already exist or failed to enable: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error initializing vector store", e);
        }
    }
}
