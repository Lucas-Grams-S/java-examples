package dev.estudos.springcore.configuracao;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * SC05-13 · Trocar a implementação por profile: gateway real em {@code prod}, fake nos demais. O método
 * {@code @Bean} com {@code @Profile("prod")} (que exige {@code payments.url}) nem é registrado fora de prod.
 * Documento: 02-spring-core/05-configuracao.md §5.2.
 *
 * <p>Saída real:
 * <pre>
 * prod -&gt; HttpPaymentGateway(http://pagamentos.local/api)
 * dev -&gt; InMemoryPaymentGateway
 * sem perfil -&gt; InMemoryPaymentGateway
 * </pre>
 */
public class TrocarPorProfile {
    interface PaymentGateway { String describe(); }
    record HttpPaymentGateway(String url) implements PaymentGateway { public String describe() { return "HttpPaymentGateway(" + url + ")"; } }
    static class InMemoryPaymentGateway implements PaymentGateway { public String describe() { return "InMemoryPaymentGateway"; } }

    @Configuration
    @PropertySource("classpath:orders.properties")
    static class GatewayConfig {
        @Bean @Profile("prod")
        PaymentGateway realGateway(@Value("${payments.url}") String url) { return new HttpPaymentGateway(url); }

        @Bean @Profile("!prod")
        PaymentGateway fakeGateway() { return new InMemoryPaymentGateway(); }
    }

    private static String com(String... perfis) {
        try (var c = new AnnotationConfigApplicationContext()) {
            if (perfis.length > 0) c.getEnvironment().setActiveProfiles(perfis);
            c.register(GatewayConfig.class);
            c.refresh();
            return c.getBean(PaymentGateway.class).describe();
        }
    }

    public static List<String> run() {
        return new ArrayList<>(List.of("prod -> " + com("prod"), "dev -> " + com("dev"), "sem perfil -> " + com()));
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
