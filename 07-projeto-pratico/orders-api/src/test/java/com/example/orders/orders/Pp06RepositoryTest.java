package com.example.orders.orders;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

/** PP06-01 (camada de persistência): {@code @DataJpaTest} usa H2 embutido, roda o Flyway e revela o mapeamento. */
@DataJpaTest
class Pp06RepositoryTest {
    @Autowired OrderRepository repo;

    @Test void expireNewBefore_soCancelaNovosAntigos() {
        var now = Instant.parse("2026-11-05T12:00:00Z");
        // as chaves estrangeiras exigem cliente/produto: os dados vêm direto por SQL
        var jdbc = org.springframework.jdbc.core.simple.JdbcClient.create(dataSource());
        jdbc.sql("insert into customers(name,email) values('a','a@x.com')").update();
        jdbc.sql("insert into products(name,price,stock) values('p',1.00,5)").update();
        Long c = jdbc.sql("select id from customers").query(Long.class).single();
        Long p = jdbc.sql("select id from products").query(Long.class).single();
        var velho = repo.save(new OrderEntity(Order.create(c, List.of(new OrderItem(p, 1, BigDecimal.ONE)), now.minusSeconds(7200))));
        var novo = repo.save(new OrderEntity(Order.create(c, List.of(new OrderItem(p, 1, BigDecimal.ONE)), now)));
        repo.flush();

        assertThat(repo.expireNewBefore(now.minusSeconds(3600))).isEqualTo(1);
        assertThat(jdbc.sql("select status from orders where id = ?").param(velho.getId()).query(String.class).single()).isEqualTo("CANCELED");
        assertThat(jdbc.sql("select status from orders where id = ?").param(novo.getId()).query(String.class).single()).isEqualTo("NEW");
    }

    @Autowired javax.sql.DataSource ds;
    javax.sql.DataSource dataSource() { return ds; }
}
