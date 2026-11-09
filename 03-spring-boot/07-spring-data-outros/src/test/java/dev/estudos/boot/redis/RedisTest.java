package dev.estudos.boot.redis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** SB07-11: precisa de Docker (imagem redis:8-alpine). */
@Testcontainers
@SpringBootTest(classes = RedisApplication.class)
class RedisTest {
    @Container @ServiceConnection(name = "redis")
    static GenericContainer<?> redis = new GenericContainer<>("redis:8-alpine").withExposedPorts(6379);

    @Autowired StockCounter stock;

    @Test void sb0711_redis() {
        assertThat(stock.reserve("x")).isEqualTo(1);
        assertThat(stock.reserve("x")).isEqualTo(2);
        stock.cacheOrder("1", "{\"id\":1}");
        assertThat(stock.order("1")).isEqualTo("{\"id\":1}");
        assertThat(stock.ttlSeconds("1")).isBetween(590L, 600L);
    }
}
