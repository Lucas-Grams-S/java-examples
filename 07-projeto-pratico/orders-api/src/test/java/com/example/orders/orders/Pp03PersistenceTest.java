package com.example.orders.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.orders.catalog.CatalogService;
import com.example.orders.customers.CustomerRepository;
import com.example.orders.orders.OrderRequests.CreateOrderRequest;
import com.example.orders.orders.OrderRequests.ItemRequest;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.UUID;
import org.hibernate.LazyInitializationException;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

/** PP03-01: JPA com Postgres-mode/H2 — N+1, OSIV desligado e a matriz de rollback. */
@SpringBootTest
@ActiveProfiles("test")
@Import({TestData.class, TxProbe.class})
class Pp03PersistenceTest {
    @Autowired OrderRepository orders;
    @Autowired OrderService service;
    @Autowired CustomerRepository customers;
    @Autowired TestData data;
    @Autowired TxProbe probe;
    @Autowired TransactionTemplate tx;
    @Autowired EntityManagerFactory emf;

    private void seed(int n) {
        var c = data.customer("n1");
        var p = data.product("1.00", 1000);
        for (int i = 0; i < n; i++)
            service.create(new CreateOrderRequest(c.getId(), List.of(new ItemRequest(p.id(), 1), new ItemRequest(p.id(), 2))));
    }

    @Test void nMaisUm_eCorrecaoComEntityGraph() {
        seed(3);
        long n = orders.count();
        var stats = emf.unwrap(SessionFactory.class).getStatistics();

        stats.clear();
        tx.executeWithoutResult(s -> orders.findAll().forEach(o -> o.getItems().size()));   // lazy: 1 + N consultas
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1 + n);

        stats.clear();
        tx.executeWithoutResult(s -> orders.findAllWithItems().forEach(o -> o.getItems().size()));   // join fetch: 1 consulta
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test void osivDesligado_lazyForaDaTransacaoFalha() {
        seed(1);
        var any = orders.findAll().getFirst();                       // sem transação: entidade "destacada"
        assertThatThrownBy(() -> any.getItems().size()).isInstanceOf(LazyInitializationException.class);
    }

    /** Verificação do roteiro: RuntimeException desfaz; exceção checada, por padrão, NÃO desfaz (a menos que rollbackFor). */
    @Test void matrizDeRollback() {
        var rt = "rt-" + UUID.randomUUID(); var ck = "ck-" + UUID.randomUUID(); var rf = "rf-" + UUID.randomUUID();
        assertThatThrownBy(() -> probe.runtimeFailure(rt)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> probe.checkedFailure(ck)).isInstanceOf(TxProbe.CheckedFailure.class);
        assertThatThrownBy(() -> probe.checkedWithRollbackFor(rf)).isInstanceOf(TxProbe.CheckedFailure.class);
        assertThat(customers.findByEmail(rt)).isEmpty();      // rollback
        assertThat(customers.findByEmail(ck)).isPresent();    // COMMIT apesar da exceção
        assertThat(customers.findByEmail(rf)).isEmpty();      // rollback pedido explicitamente
    }

    @Test void falhaNoMeioDoPedido_desfazTudo_inclusiveEstoque() {
        var c = data.customer("rb");
        var ok = data.product("1.00", 10);
        var pouco = data.product("1.00", 1);
        long antes = orders.count();
        assertThatThrownBy(() -> service.create(new CreateOrderRequest(c.getId(), List.of(new ItemRequest(ok.id(), 3), new ItemRequest(pouco.id(), 2)))))
                .hasMessageContaining("insufficient stock");
        assertThat(orders.count()).isEqualTo(antes);
        // o estoque do primeiro item voltou: ainda dá para reservar 10
        service.create(new CreateOrderRequest(c.getId(), List.of(new ItemRequest(ok.id(), 10))));
    }
}
