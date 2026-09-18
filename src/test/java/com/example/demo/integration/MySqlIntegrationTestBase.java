package com.example.demo.integration;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@ContextConfiguration(initializers = MySqlIntegrationTestBase.MySqlInitializer.class)
abstract class MySqlIntegrationTestBase {

    private static final MySQLContainer<?> MYSQL;

    static {
        String existingUrl = System.getProperty("spring.datasource.url");
        if (existingUrl == null || existingUrl.trim().isEmpty()) {
            existingUrl = System.getenv("SPRING_DATASOURCE_URL");
        }

        if (existingUrl != null && !existingUrl.trim().isEmpty()) {
            MYSQL = null;
        } else {
            // Docker Engine 29 rejects docker-java's legacy default API (1.32).
            // Keep this compatibility override in test scope instead of changing the host config.
            if (System.getProperty("api.version") == null) {
                System.setProperty("api.version", "1.44");
            }
            MySQLContainer<?> container = new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("test24")
                    .withUsername("test24")
                    .withPassword("test24")
                    .withReuse(false);
            container.start();
            MYSQL = container;
        }
    }

    static final class MySqlInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            if (MYSQL != null && MYSQL.isRunning()) {
                TestPropertyValues.of(
                        "spring.datasource.url=" + MYSQL.getJdbcUrl(),
                        "spring.datasource.username=" + MYSQL.getUsername(),
                        "spring.datasource.password=" + MYSQL.getPassword(),
                        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver")
                        .applyTo(context.getEnvironment());
            }
        }
    }
}

