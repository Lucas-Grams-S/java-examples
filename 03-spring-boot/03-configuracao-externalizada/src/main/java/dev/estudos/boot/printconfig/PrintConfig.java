package dev.estudos.boot.printconfig;

import dev.estudos.boot.config.OrdersProperties;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * SB03-10 / SB03-14 · Sobe o Boot sem servidor web, liga {@link OrdersProperties} e imprime o resultado.
 * Serve de programa-alvo para os exemplos de configuração externa (args, ambiente, perfis, imports).
 * Documento: 03-configuracao-externalizada.md §5.1 e §5.5.
 * Fica num pacote irmão (fora do scan de {@code ConfigApplication}) para não virar uma segunda configuração principal.
 *
 * <p>Uso: {@code java -cp ... PrintConfig --spring.profiles.active=prod --orders.currency=USD}
 * <pre>
 * OrdersProperties[currency=USD, paymentTimeout=PT45S, limits=Limits[maxItems=10], paymentMethods=[CARD]]
 * </pre>
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableConfigurationProperties(OrdersProperties.class)
public class PrintConfig {

    /** Sobe o contexto e devolve a primeira linha (propriedades ligadas) + os valores pedidos de {@code keys}. */
    public static List<String> run(List<String> keys, String... args) {
        try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(PrintConfig.class)
                .web(WebApplicationType.NONE).bannerMode(Banner.Mode.OFF).logStartupInfo(false).run(args)) {
            List<String> out = new ArrayList<>();
            out.add(ctx.getBean(OrdersProperties.class).toString());
            for (String k : keys) out.add(k + " = " + ctx.getEnvironment().getProperty(k));
            return out;
        }
    }

    public static void main(String[] args) {
        run(List.of(), args).forEach(System.out::println);
    }
}
