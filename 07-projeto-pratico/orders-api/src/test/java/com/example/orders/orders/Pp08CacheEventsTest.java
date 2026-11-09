package com.example.orders.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.orders.catalog.CatalogService;
import com.example.orders.orders.OrderRequests.CreateOrderRequest;
import com.example.orders.orders.OrderRequests.ItemRequest;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

/** PP08-01: cache de leitura, evento AFTER_COMMIT e expiração agendada. */
@SpringBootTest
@Import(TestData.class)
class Pp08CacheEventsTest {
    @Autowired CatalogService catalog;
    @Autowired OrderService service;
    @Autowired OrderCreatedListener listener;
    @Autowired OrderRepository orders;
    @Autowired TestData data;
    @Autowired JdbcClient jdbc;

    @BeforeEach void limpa() { listener.clear(); }

    @Test void produto_lidoDuasVezes_consultaOBancoUmaSo() {
        var p = data.product("4.00", 5);
        int antes = catalog.databaseLoads();
        catalog.find(p.id()); catalog.find(p.id()); catalog.find(p.id());
        assertThat(catalog.databaseLoads()).isEqualTo(antes + 1);
    }

    @Test void commit_publicaEvento() {
        var c = data.customer("ev"); var p = data.product("1.00", 5);
        var o = service.create(new CreateOrderRequest(c.getId(), List.of(new ItemRequest(p.id(), 1))));
        assertThat(listener.seen()).containsExactly(o.id());
    }

    /** Verificação do roteiro (1): rollback => o ouvinte AFTER_COMMIT nunca roda. */
    @Test void rollback_naoPublicaEvento() {
        var c = data.customer("ev2"); var p = data.product("1.00", 1);
        assertThatThrownBy(() -> service.create(new CreateOrderRequest(c.getId(), List.of(new ItemRequest(p.id(), 9))))).hasMessageContaining("insufficient");
        assertThat(listener.seen()).isEmpty();
    }

    /**
     * Verificação do roteiro (2): exceção no ouvinte AFTER_COMMIT não desfaz o pedido (já confirmado)
     * e também não chega ao chamador: o Spring registra o erro em log e segue.
     */
    @Test void falhaNoOuvinteAposCommit_naoDesfazOPedido_nemChegaAoChamador() {
        var c = data.customer("ev3"); var p = data.product("1.00", 5);
        long antes = orders.count();
        listener.failNext(true);
        var o = service.create(new CreateOrderRequest(c.getId(), List.of(new ItemRequest(p.id(), 1))));
        assertThat(listener.seen()).containsExactly(o.id());     // o ouvinte rodou (e lançou)
        assertThat(orders.count()).isEqualTo(antes + 1);          // o pedido está gravado
    }

    @Test void expiracao_soCancelaNovosAntigos() {
        var c = data.customer("ex"); var p = data.product("1.00", 10);
        long velho = service.create(new CreateOrderRequest(c.getId(), List.of(new ItemRequest(p.id(), 1)))).id();
        long recente = service.create(new CreateOrderRequest(c.getId(), List.of(new ItemRequest(p.id(), 1)))).id();
        jdbc.sql("update orders set created_at = created_at - interval '3' hour where id = ?").param(velho).update();
        assertThat(service.expireOlderThan(Duration.ofHours(1))).isGreaterThanOrEqualTo(1);
        assertThat(jdbc.sql("select status from orders where id = ?").param(velho).query(String.class).single()).isEqualTo("CANCELED");
        assertThat(jdbc.sql("select status from orders where id = ?").param(recente).query(String.class).single()).isEqualTo("NEW");
    }
}
