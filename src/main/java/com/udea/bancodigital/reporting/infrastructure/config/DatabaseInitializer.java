package com.udea.bancodigital.reporting.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Configuration
public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @PostConstruct
    public void init() {
        createDatabaseIfNotExists();
    }

    private void createDatabaseIfNotExists() {
        if (dbUrl == null || !dbUrl.contains("/")) {
            logger.warn("Invalid database URL format: {}", dbUrl);
            return;
        }

        // Extract database name from URL (assuming jdbc:postgresql://host:port/dbname)
        String dbName = dbUrl.substring(dbUrl.lastIndexOf("/") + 1);
        if (dbName.contains("?")) {
            dbName = dbName.substring(0, dbName.indexOf("?"));
        }

        // Security: Validate database name to prevent SQL Injection
        if (!dbName.matches("^\\w+$")) {
            logger.error("Security risk: Invalid database name detected in URL: {}", dbName);
            return;
        }

        String baseUrl = dbUrl.substring(0, dbUrl.lastIndexOf("/") + 1) + "postgres";

        try (Connection conn = DriverManager.getConnection(baseUrl, username, password);
             Statement stmt = conn.createStatement()) {

            ResultSet resultSet = conn.getMetaData().getCatalogs();
            boolean exists = false;
            while (resultSet.next()) {
                if (resultSet.getString(1).equalsIgnoreCase(dbName)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                logger.info("Database {} does not exist. Creating...", dbName);
                // The name is validated against a whitelist regex above, so this is safe
                @SuppressWarnings("java:S2077")
                int result = stmt.executeUpdate("CREATE DATABASE " + dbName);
                logger.info("Database {} created successfully. Result: {}", dbName, result);
            } else {
                logger.info("Database {} already exists.", dbName);
            }

        } catch (Exception e) {
            logger.warn("Could not check or create database {}. Error: {}", dbName, e.getMessage());
        }
    }
}
