package dev.estudos.cloud.lab;

import static dev.estudos.cloud.lab.Lab.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Doc 01 §5 (CL01-08) e doc 04 (CL04-06, 07, 08, 10): o laboratorio completo
 * (config-server, eureka, catalog x2, orders, gateway WebFlux e gateway Web MVC) em subprocessos.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class GatewayLabIT {
    @TempDir static Path tmp;
    static Lab lab;
    static int eureka, gw, mvc, orders;

    @BeforeAll static void up() throws Exception {
        lab = new Lab(tmp);
        Path repo = newGitRepo(tmp.resolve("config-repo"), Map.of(
            "application.yml", "shop.currency: BRL\n", "catalog.yml", "catalog.discount: 5\n"));
        int cfg = freePort();
        lab.start("config-server", "config-server", Map.of(), "--server.port=" + cfg, "--CONFIG_REPO=file://" + repo);
        await("config server", 90, () -> ok(cfg, "/catalog/default"));
        eureka = lab.eureka();
        for (String id : new String[] {"catalog-1", "catalog-2"}) {   // catalog de verdade: configuracao vem do Config Server
            int p = freePort();
            var args = new java.util.ArrayList<>(java.util.List.of("--server.port=" + p,
                "--spring.config.import=configserver:http://localhost:" + cfg, defaultZone(eureka)));
            args.addAll(RAPIDO);
            lab.start(id, "catalog", Map.of(), args.toArray(String[]::new));
        }
        orders = lab.orders("orders", eureka, null, Map.of());
        gw = freePort(); mvc = freePort();
        var g = new java.util.ArrayList<>(java.util.List.of("--server.port=" + gw, defaultZone(eureka)));
        g.addAll(RAPIDO);
        lab.start("gateway", "gateway", Map.of(), g.toArray(String[]::new));
        var m = new java.util.ArrayList<>(java.util.List.of("--server.port=" + mvc, defaultZone(eureka)));
        m.addAll(RAPIDO);
        lab.start("gateway-mvc", "gateway-mvc", Map.of(), m.toArray(String[]::new));
        await("registro completo", 120, () -> registrado(eureka, "catalog", 2) && registrado(eureka, "orders", 1)
            && registrado(eureka, "gateway", 1) && registrado(eureka, "gateway-mvc", 1));
        await("gateway enxerga orders", 60, () -> ok(gw, "/api/orders/1") && ok(mvc, "/api/orders/1"));
    }

    @AfterAll static void down() { lab.close(); }

    @Test void cl0108_laboratorioPontaAPonta() throws Exception {
        assertThat(registro(eureka, "catalog")).hasSize(2);
        assertThat(registro(eureka, "orders")).hasSize(1);
        assertThat(registro(eureka, "gateway")).hasSize(1);
        var r = get(gw, "/api/orders/1");
        assertThat(r.status()).isEqualTo(200);
        assertThat(r.header("X-Gateway")).isEqualTo("s10");
        assertThat(r.body()).contains("\"orderId\":1", "\"name\":\"Product-1\"", "\"currency\":\"BRL\"", "\"discount\":5");
    }

    @Test void cl0406_rotasEmYaml() throws Exception {
        assertThat(get(gw, "/api/orders/1").status()).isEqualTo(200);
        var c = get(gw, "/api/catalog/products/9");               // StripPrefix=2: /api/catalog/products/9 -> /products/9
        assertThat(c.status()).isEqualTo(200);
        assertThat(c.body()).contains("\"id\":9");
        assertThat(get(gw, "/legacy/products/5").status()).isEqualTo(404);                       // predicate Header nao casou
        var l = get(gw, "/legacy/products/5", "X-Canary", "true");                               // RewritePath
        assertThat(l.status()).isEqualTo(200);
        assertThat(l.body()).contains("\"id\":5");
    }

    @Test void cl0407_filtroGlobalDeCorrelacao() throws Exception {
        String gerado = get(gw, "/api/orders/1").header("X-Correlation-Id");
        assertThat(gerado).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        assertThat(get(gw, "/api/orders/1", "X-Correlation-Id", "abc").header("X-Correlation-Id")).isEqualTo("abc");
    }

    @Test void cl0408_gatewayWebMvcJavaDslEYaml() throws Exception {
        var c = get(mvc, "/api/catalog/products/3");             // rota Java
        assertThat(c.status()).isEqualTo(200);
        assertThat(c.header("X-Gateway")).isEqualTo("mvc");
        assertThat(get(mvc, "/api/orders/3").status()).isEqualTo(200);   // rota YAML
    }

    @Test void cl0410_ordemDasRotas() throws Exception {
        int p = freePort();
        var a = new java.util.ArrayList<>(java.util.List.of("--server.port=" + p, "--spring.profiles.active=ordem-errada", defaultZone(eureka)));
        a.addAll(RAPIDO);
        lab.start("gateway-ordem-errada", "gateway", Map.of(), a.toArray(String[]::new));
        await("gateway ordem-errada", 90, () -> { try { return get(p, "/api/x").status() > 0; } catch (Exception e) { return false; } });
        // rota "pega-tudo" /api/** vem primeiro: /api/orders/1 vai para o catalog (que nao tem esse caminho) e "orders" nunca e alcancada
        assertThat(get(p, "/api/orders/1").status()).isEqualTo(404);
        assertThat(get(gw, "/api/orders/1").status()).isEqualTo(200);            // ordem correta: 200
        // nenhuma rota casa -> 404 gerado pelo proprio gateway
        assertThat(get(gw, "/nada").status()).isEqualTo(404);
    }
}
