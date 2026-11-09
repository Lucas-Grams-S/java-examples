package dev.estudos.boot.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import dev.estudos.boot.printconfig.PrintConfig;
import org.junit.jupiter.api.Test;

/** Testes de SB03-03, 04, 05, 10, 11, 12, 13 e 14. */
class ConfigTest {

    @Test void sb0303_config_import() throws Exception {
        assertThat(ConfigImport.run()).containsExactly("orders.currency com import = EUR",
            "app.extra = vindo do arquivo importado",
            "sem 'optional:' e arquivo ausente -> ConfigDataResourceNotFoundException");
    }
    @Test void sb0304_perfis() {
        assertThat(Perfis.run()).containsExactly("sem perfil : paymentTimeout=PT30S maxItems=50",
            "perfil prod: paymentTimeout=PT45S maxItems=10");
    }
    @Test void sb0305_placeholders_e_random() {
        var out = Placeholders.run();
        assertThat(out).containsExactly("app.description = Orders roda na moeda BRL",
            "app.description com --orders.currency=USD = Orders roda na moeda USD",
            "secret é UUID = true", "port em [1024,65536) = true");
    }
    @Test void sb0310_record_tipado() {
        assertThat(PrintConfig.run(List.of(), "--spring.profiles.active=prod").get(0)).isEqualTo(
            "OrdersProperties[currency=BRL, paymentTimeout=PT45S, limits=Limits[maxItems=10], paymentMethods=[CARD]]");
    }
    @Test void sb0312_validacao_fail_fast() {
        assertThatThrownBy(() -> PrintConfig.run(List.of(), "--orders.currency="))
            .hasStackTraceContaining("BindValidationException").hasStackTraceContaining("orders.currency");
    }
    @Test void sb0313_configtree() throws Exception {
        assertThat(ConfigTreeSecrets.run()).containsExactly("orders.db.password = s3cr3t", "orders.api-key = abc-123");
    }
    @Test void sb0314_precedencia_e_config_externa() throws Exception {
        assertThat(Precedencia.run()).containsExactly(
            "só o YAML                         -> BRL",
            "ORDERS_CURRENCY=EUR               -> EUR",
            "env EUR + SPRING_APPLICATION_JSON -> USD",
            "env EUR + --orders.currency=JPY   -> JPY",
            "additional-location (arquivo)     -> CHF",
            "perfil prod + --orders.limits.max-items=7 -> maxItems=7");
    }
}
