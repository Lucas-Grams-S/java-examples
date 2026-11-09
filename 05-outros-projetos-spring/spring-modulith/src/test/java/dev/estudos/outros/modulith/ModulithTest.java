package dev.estudos.outros.modulith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import dev.estudos.outros.modulith.inventory.Baixas;
import dev.estudos.outros.modulith.orders.OrderService;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

/** OP03-02. */
@SpringBootTest
class ModulithTest {
    @Autowired OrderService orders;

    @Test void op0302_modulos_orders_e_inventory_eVerifica() {
        var modules = ApplicationModules.of(ModulithApplication.class);
        List<String> ids = modules.stream().map(m -> m.getIdentifier().toString()).sorted().toList();
        assertThat(ids).containsExactly("inventory", "orders");
        modules.verify();   // sem ciclos nem acesso a internos
    }

    @Test void op0302_inventoryDependeDeOrders_ordersDeNinguem() {
        var modules = ApplicationModules.of(ModulithApplication.class);
        var inv = modules.getModuleByName("inventory").orElseThrow();
        var ord = modules.getModuleByName("orders").orElseThrow();
        assertThat(inv.getDirectDependencies(modules).stream().map(d -> d.getTargetModule().getIdentifier().toString()))
                .contains("orders");
        assertThat(ord.getDirectDependencies(modules).stream()).isEmpty();
    }

    @Test void op0302_comTransacao_listenerRoda() {
        Baixas.limpar();
        orders.place("1", "ABC", 2);
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(Baixas.todas()).containsExactly("ABCx2"));
    }

    @Test void op0302_semTransacao_listenerNaoRoda() throws Exception {
        Baixas.limpar();
        orders.placeSemTx("2", "ZZZ", 1);
        Thread.sleep(500);
        assertThat(Baixas.todas()).isEmpty();
    }
}
