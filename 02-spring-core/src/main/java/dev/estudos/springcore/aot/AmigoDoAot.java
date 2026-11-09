package dev.estudos.springcore.aot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.aot.generate.ClassNameGenerator;
import org.springframework.aot.generate.DefaultGenerationContext;
import org.springframework.aot.generate.FileSystemGeneratedFiles;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.aot.ApplicationContextAotGenerator;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.javapoet.ClassName;

/**
 * SC10-11 · Escrevendo código "amigo do AOT": {@code @Bean} que devolve o TIPO CONCRETO, com parâmetros que são
 * beans. O gerador consegue emitir chamada direta ({@code forFactoryMethod(CardGateway.class, Clock.class)}),
 * conhecendo o tipo do bean em build. Documento: 02-spring-core/10-aot-e-native.md §5.4.
 *
 * <p>Saída real:
 * <pre>
 * @Bean com tipo concreto: tipo do bean conhecido no build -&gt; true
 * método de fábrica gerado referencia CardGateway e Clock -&gt; true
 * </pre>
 */
public class AmigoDoAot {
    public static class CardGateway {
        final Clock clock;
        public CardGateway(Clock clock) { this.clock = clock; }
    }

    @Configuration
    public static class PaymentConfig {
        @Bean Clock clock() { return Clock.systemUTC(); }

        @Bean                                  // retorne o TIPO CONCRETO, não a interface
        CardGateway cardGateway(Clock clock) { // parâmetros simples/beans
            return new CardGateway(clock);
        }
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        Path dir = Files.createTempDirectory("aot-amigo");
        try {
            var ctx = new GenericApplicationContext();
            new AnnotatedBeanDefinitionReader(ctx).register(PaymentConfig.class);
            var gen = new DefaultGenerationContext(new ClassNameGenerator(ClassName.get("aot", "Amigo")), new FileSystemGeneratedFiles(dir));
            new ApplicationContextAotGenerator().processAheadOfTime(ctx, gen);
            gen.writeGeneratedContent();
            StringBuilder src = new StringBuilder();
            try (Stream<Path> s = Files.walk(dir.resolve("sources"))) {
                for (Path p : s.filter(f -> f.toString().endsWith(".java")).toList()) src.append(Files.readString(p));
            }
            String code = src.toString();
            out.add("@Bean com tipo concreto: tipo do bean conhecido no build -> "
                + code.contains("RootBeanDefinition(AmigoDoAot.CardGateway.class)"));
            out.add("método de fábrica gerado referencia CardGateway e Clock -> "
                + (code.contains("forFactoryMethod(AmigoDoAot.PaymentConfig.class, \"cardGateway\", Clock.class)")
                   || (code.contains("forFactoryMethod") && code.contains("cardGateway") && code.contains("Clock.class"))));
        } finally {
            try (Stream<Path> s = Files.walk(dir)) { s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
        }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
