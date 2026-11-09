package dev.estudos.boot.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

/** SB07-10: precisa de Docker (imagem mongo:7). */
@Testcontainers
@SpringBootTest(classes = MongoApplication.class)
class MongoTest {
    @Container @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @Autowired OrderRepository repo;

    @Test void sb0710_mongo() {
        repo.save(new Order(null, "ana", List.of(new Order.Item("a", 1), new Order.Item("b", 2)), BigDecimal.TEN));
        repo.save(new Order(null, "bia", List.of(new Order.Item("b", 1)), BigDecimal.ONE));
        assertThat(repo.findByCustomer("ana")).hasSize(1);
        assertThat(repo.findByItemsSku("b")).hasSize(2);
        assertThat(repo.findByItemsSku("a")).hasSize(1);
    }
}
