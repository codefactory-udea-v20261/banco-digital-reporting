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
        // Extract database name from URL (assuming jdbc:postgresql://host:port/dbname)
        String dbName = dbUrl.substring(dbUrl.lastIndexOf("/") + 1);
        if (dbName.contains("?")) {
            dbName = dbName.substring(0, dbName.indexOf("?"));
        }

        String baseUrl = dbUrl.substring(0, dbUrl.lastIndexOf("/") + 1) + "postgres";

        try (Connection conn = DriverManager.getConnection(baseUrl, username, password);
             Statement stmt = conn.createStatement()) {

            ResultSet resultSet = conn.getMetaData().getCatalogs();
            boolean exists = false;
            while (resultSet.next()) {
                if (resultSet.getString(1).equals(dbName)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                logger.info("Database {} does not exist. Creating...", dbName);
                stmt.executeUpdate("CREATE DATABASE " + dbName);
                logger.info("Database {} created successfully.", dbName);
            } else {
                logger.info("Database {} already exists.", dbName);
            }

        } catch (Exception e) {
            logger.warn("Could not check or create database {}. It might already exist or user has no permissions. Error: {}", dbName, e.getMessage());
        }
    }
}
