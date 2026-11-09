package dev.estudos.cloud.lab;

import static dev.estudos.cloud.lab.Lab.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/** Doc 03 (CL03-09, 10, 11, 12, 13): Eureka, balanceamento, a armadilha do builder e a lista estatica. */
class DiscoveryLabIT {
    @TempDir Path tmp;
    Lab lab;

    @BeforeEach void setUp() { lab = new Lab(tmp); }
    @AfterEach void tearDown() { lab.close(); }

    @Test void cl0309_cl0310_cl0311_registroEBalanceamento() throws Exception {
        int eureka = lab.eureka();
        int a = lab.catalogSemConfig("catalog-a", eureka), b = lab.catalogSemConfig("catalog-b", eureka);
        int orders = lab.orders("orders", eureka, null, Map.of());
        await("registro", 60, () -> registrado(eureka, "catalog", 2) && registrado(eureka, "orders", 1));
        assertThat(registro(eureka, "catalog")).hasSize(2).allMatch(id -> id.startsWith("catalog:"));   // instance-id do doc
        await("orders enxerga as duas instancias", 60, () -> {
            try { return distribuicao(orders, "/orders/1", 6).size() == 2; } catch (Exception e) { return false; }
        });
        // seis chamadas por orders (http://catalog/...): exatamente 3 e 3
        assertThat(distribuicao(orders, "/orders/1", 6)).containsOnlyKeys(String.valueOf(a), String.valueOf(b))
            .containsValues(3, 3);
    }

    @Test void cl0312_armadilhaDoBuilderBalanceado() throws Exception {
        int eureka = lab.eureka();
        lab.catalogSemConfig("catalog", eureka);
        // armadilha: so o builder @LoadBalanced -> o cliente Eureka o usa e tenta resolver "localhost" como servico
        lab.orders("orders-armadilha", eureka, "armadilha", Map.of());
        await("erro de bean em criacao", 90, () -> lab.log("orders-armadilha").contains("BeanCurrentlyInCreationException"));
        assertThat(lab.log("orders-armadilha")).contains("scopedTarget.eurekaClient");
        assertThat(registrado(eureka, "orders", 1)).isFalse();      // nao se registrou

        // correcao (CL05-07): builder comum @Primary + balanceado
        lab.kill("orders-armadilha", false);
        int ok = lab.orders("orders-manual", eureka, "manual", Map.of());
        await("orders registrado", 60, () -> registrado(eureka, "orders", 1));
        await("orders responde", 60, () -> ok(ok, "/orders/1"));
    }

    @Test void cl0313_listaEstatica() throws Exception {
        int a = lab.catalogSemConfig("catalog-a", 0), b = lab.catalogSemConfig("catalog-b", 0);
        int orders = lab.orders("orders-estatico", 0, "estatico", Map.of("CATALOG_A", "" + a, "CATALOG_B", "" + b));
        await("orders", 90, () -> ok(orders, "/orders/1"));
        assertThat(distribuicao(orders, "/orders/1", 6)).containsOnlyKeys(String.valueOf(a), String.valueOf(b)).containsValues(3, 3);
    }
}
