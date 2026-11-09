package dev.estudos.springcore.aot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.aot.generate.ClassNameGenerator;
import org.springframework.aot.generate.DefaultGenerationContext;
import org.springframework.aot.generate.FileSystemGeneratedFiles;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.context.annotation.Profile;
import org.springframework.context.aot.ApplicationContextAotGenerator;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.javapoet.ClassName;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * SC10-08 · Gerar o código AOT e inspecioná-lo: o mesmo mecanismo do {@code process-aot} do Boot, chamado à mão.
 * Os arquivos vão para um diretório temporário. Confirma: {@code OrderService} é criado com {@code new} direto,
 * {@code DevOnly} ({@code @Profile("dev")}) não aparece no código gerado e o hint de recurso foi registrado.
 * Documento: 02-spring-core/10-aot-e-native.md §5.1.
 *
 * <p>Saída real:
 * <pre>
 * initializer: aot.Aot__ApplicationContextInitializer
 * arquivos gerados: [classes/dev/estudos/springcore/aot/GerarAot$Cfg$$SpringCGLIB$$0.class (+2 FastClass),
 *   sources/aot/Aot__ApplicationContextInitializer.java, sources/aot/Aot__BeanFactoryRegistrations.java,
 *   sources/dev/estudos/springcore/aot/GerarAot__BeanDefinitions.java,
 *   sources/org/springframework/context/event/{DefaultEventListenerFactory,EventListenerMethodProcessor}__BeanDefinitions.java]
 * OrderService criado com new direto -&gt; true
 * DevOnly aparece no código gerado -&gt; false
 * hint templates/*.txt registrado -&gt; true
 * </pre>
 */
public class GerarAot {
    public interface PaymentGateway { String charge(long c); }
    @Component public static class CardGateway implements PaymentGateway { public String charge(long c) { return "ok"; } }
    @Repository public static class OrderRepository {}

    @Service @ImportRuntimeHints(OrderService.Hints.class)
    public static class OrderService {
        public OrderService(OrderRepository r, PaymentGateway g) {}
        public static class Hints implements RuntimeHintsRegistrar {
            public void registerHints(RuntimeHints h, ClassLoader cl) { h.resources().registerPattern("templates/*.txt"); }
        }
    }

    @Profile("dev") @Component public static class DevOnly {}

    @Configuration @ComponentScan(basePackageClasses = GerarAot.class)
    public static class Cfg { @Bean String banner() { return "hi"; } }

    /** Gera o AOT em {@code dir}; devolve o nome do initializer. */
    static String gerar(Path dir, RuntimeHints[] hintsOut) {
        var ctx = new GenericApplicationContext();
        new AnnotatedBeanDefinitionReader(ctx).register(Cfg.class);
        var files = new FileSystemGeneratedFiles(dir);
        var gen = new DefaultGenerationContext(new ClassNameGenerator(ClassName.get("aot", "Aot")), files);
        var initializer = new ApplicationContextAotGenerator().processAheadOfTime(ctx, gen);
        gen.writeGeneratedContent();
        hintsOut[0] = gen.getRuntimeHints();
        return initializer.toString();
    }

    public static List<String> run() throws IOException {
        List<String> out = new ArrayList<>();
        Path dir = Files.createTempDirectory("aot-gen");
        try {
            var hints = new RuntimeHints[1];
            out.add("initializer: " + gerar(dir, hints));
            List<String> arquivos;
            try (Stream<Path> s = Files.walk(dir)) {
                arquivos = s.filter(Files::isRegularFile).map(p -> dir.relativize(p).toString()).sorted().toList();
            }
            out.add("arquivos gerados: " + arquivos);
            StringBuilder todos = new StringBuilder();
            try (Stream<Path> s = Files.walk(dir.resolve("sources"))) {
                for (Path p : s.filter(f -> f.toString().endsWith(".java")).toList()) todos.append(Files.readString(p));
            }
            out.add("OrderService criado com new direto -> "
                + (todos.toString().contains("new GerarAot.OrderService(") || todos.toString().contains("new OrderService(")));
            out.add("DevOnly aparece no código gerado -> " + todos.toString().contains("DevOnly"));
            out.add("hint templates/*.txt registrado -> "
                + RuntimeHintsPredicates.resource().forResource("templates/a.txt").test(hints[0]));
        } finally {
            try (Stream<Path> s = Files.walk(dir)) { s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
        }
        return out;
    }

    public static void main(String[] args) throws IOException { run().forEach(System.out::println); }
}
