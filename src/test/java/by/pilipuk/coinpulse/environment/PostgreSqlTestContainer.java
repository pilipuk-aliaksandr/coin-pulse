package by.pilipuk.coinpulse.environment;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;

public final class PostgreSqlTestContainer {

    private static final PostgreSQLContainer<?> CONTAINER = new PostgreSQLContainer<>("postgres:18-alpine")
                    .withInitScript("init.sql");

    static {
        CONTAINER.start();
    }

    private PostgreSqlTestContainer() {
    }

    public static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", CONTAINER::getUsername);
        registry.add("spring.datasource.password", CONTAINER::getPassword);
    }

}
