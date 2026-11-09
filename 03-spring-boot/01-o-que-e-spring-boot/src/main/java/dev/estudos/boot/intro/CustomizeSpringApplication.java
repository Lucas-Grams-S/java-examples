package dev.estudos.boot.intro;

import java.util.List;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * SB01-10 · Customizar o {@code SpringApplication} com {@link SpringApplicationBuilder}: sem banner,
 * perfil {@code local}, sem servidor web ({@code WebApplicationType.NONE}, como um job).
 * Documento: 01-o-que-e-spring-boot.md §5.4. Execução: {@code ./scripts/run-example.sh SB01-10}.
 *
 * <p>Saída real:
 * <pre>
 * perfis ativos = [local]
 * tipo de contexto = AnnotationConfigApplicationContext
 * </pre>
 */
public class CustomizeSpringApplication {
    @Configuration static class Minimal {}

    public static List<String> run(String... args) {
        try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(Minimal.class)
                .bannerMode(Banner.Mode.OFF)
                .profiles("local")
                .web(WebApplicationType.NONE)
                .run(args)) {
            return List.of("perfis ativos = " + List.of(ctx.getEnvironment().getActiveProfiles()),
                           "tipo de contexto = " + ctx.getClass().getSimpleName());
        }
    }

    public static void main(String[] args) { run(args).forEach(System.out::println); }
}
