package com.example.orders;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

/**
 * PP09-01: configuração por variáveis de ambiente (a imagem não muda entre ambientes).
 * Empacotamento em jar em camadas, JarLauncher, AOT e Docker já estão provados em SB13-02/SB13-10.
 */
class Pp09ConfigTest {
    @Test void variaveisDeAmbiente_viramPropriedades() {
        var env = new StandardEnvironment();
        env.getPropertySources().addFirst(new SystemEnvironmentPropertySource("test-env", Map.of(
                "ORDERS_EXPIRY_MINUTES", "5",
                "ORDERS_SECURITY_JWT_SECRET", "segredo-de-producao-com-32-bytes-ok!",
                "DB_URL", "jdbc:postgresql://db:5432/orders")));
        assertThat(env.getProperty("orders.expiry-minutes")).isEqualTo("5");
        assertThat(env.getProperty("orders.security.jwt-secret")).startsWith("segredo-de-producao");
        assertThat(env.getProperty("db.url")).startsWith("jdbc:postgresql");
    }
}
