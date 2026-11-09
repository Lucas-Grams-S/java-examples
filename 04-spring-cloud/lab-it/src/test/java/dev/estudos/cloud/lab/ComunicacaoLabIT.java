package dev.estudos.cloud.lab;

import static dev.estudos.cloud.lab.Lab.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/** Doc 05 (CL05-02, 07, 08, 09): HTTP Service Client balanceado (manual e @ImportHttpServices) e destino caido. */
@TestMethodOrder(MethodOrderer.MethodName.class)
class ComunicacaoLabIT {
    @TempDir static Path tmp;
    static Lab lab;
    static int eureka, a, b;

    @BeforeAll static void up() throws Exception {
        lab = new Lab(tmp);
        eureka = lab.eureka();
        a = lab.catalogSemConfig("catalog-a", eureka);
        b = lab.catalogSemConfig("catalog-b", eureka);
        await("catalogs", 60, () -> registrado(eureka, "catalog", 2));
    }

    @AfterAll static void down() { lab.close(); }

    private static void aguardaDuasInstancias(int orders) throws InterruptedException {
        await("orders enxerga as duas instancias", 90, () -> {
            try { return distribuicao(orders, "/orders/1", 6).size() == 2; } catch (Exception e) { return false; }
        });
    }

    @Test void cl0507_formaManual() throws Exception {
        int o = lab.orders("orders-manual", eureka, "manual", Map.of());
        aguardaDuasInstancias(o);
        assertThat(distribuicao(o, "/orders/1", 6)).containsValues(3, 3);
        lab.kill("orders-manual", false);
    }

    @Test void cl0508_cl0502_importHttpServices() throws Exception {
        int o = lab.orders("orders", eureka, null, Map.of());          // sem nenhuma propriedade do cliente
        aguardaDuasInstancias(o);
        assertThat(distribuicao(o, "/orders/1", 4)).containsOnlyKeys(String.valueOf(a), String.valueOf(b)).containsValues(2, 2);
        var r = get(o, "/orders/7");
        assertThat(r.body()).contains("\"orderId\":7", "\"name\":\"Product-7\"");   // a interface CatalogClient devolveu o JSON
    }

    @Test void cl0509_quandoODestinoCai() throws Exception {
        int o = lab.orders("orders-queda", eureka, null, Map.of());
        aguardaDuasInstancias(o);
        lab.kill("catalog-b", true);                                      // kill -9: o Eureka ainda acha que esta UP
        int ok = 0, degradado = 0;
        for (int i = 0; i < 12; i++) {
            String body = get(o, "/orders/1").body();
            if (body.contains("\"unavailable\":true")) degradado++; else if (body.contains("Product-1")) ok++;
        }
        assertThat(ok).as("instancia viva segue atendendo").isPositive();
        assertThat(degradado).as("parte das chamadas cai na instancia morta e degrada (try/catch)").isPositive();
    }
}
