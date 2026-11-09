package dev.estudos.boot.testes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** SB10-14: PostgreSQL real, {@code @ServiceConnection} (sem URL fixa), Flyway roda no Postgres. Exige Docker. */
@SpringBootTest
@Testcontainers
class PgTest {
    @Container @ServiceConnection
    static PostgreSQLContainer pg = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired JdbcClient jdbc;

    @Test void sb1014_postgresReal() {
        assertThat(jdbc.sql("select version()").query(String.class).single()).contains("PostgreSQL");
        assertThat(jdbc.sql("select count(*) from flyway_schema_history").query(Long.class).single()).isEqualTo(2);
    }
}
