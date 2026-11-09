package dev.estudos.springcore.aot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * SC10-09 · Declarar hints para recursos/reflection próprios: um {@code RuntimeHintsRegistrar} registra o padrão
 * de recurso lido dinamicamente, e o hint é testado com {@code RuntimeHintsPredicates}, sem precisar de nativo.
 * (AssertJ é usado só para o predicado; o JAR de teste não é exigido no código de produção real.)
 * Documento: 02-spring-core/10-aot-e-native.md §5.2.
 *
 * <p>Saída real:
 * <pre>
 * conteúdo lido dinamicamente -&gt; relatório diário
 * hint reports/daily.txt registrado -&gt; true
 * hint reports/ qualquer outro padrão (other/x.txt) -&gt; false
 * reflection de OrderDto (registerType) -&gt; true
 * </pre>
 */
public class HintsProprios {
    @Component @ImportRuntimeHints(ReportTemplates.Hints.class)
    public static class ReportTemplates {
        public String load(String name) throws IOException {
            // nome dinâmico → o GraalVM não descobre sozinho
            return new ClassPathResource("reports/" + name + ".txt").getContentAsString(StandardCharsets.UTF_8);
        }

        public static class Hints implements RuntimeHintsRegistrar {
            public void registerHints(RuntimeHints hints, ClassLoader cl) {
                hints.resources().registerPattern("reports/*.txt");
                hints.reflection().registerType(OrderDto.class,
                    org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
            }
        }
    }

    public record OrderDto(String id, long cents) {}

    public static List<String> run() throws IOException {
        List<String> out = new ArrayList<>();
        out.add("conteúdo lido dinamicamente -> " + new ReportTemplates().load("daily").strip());
        var hints = new RuntimeHints();
        new ReportTemplates.Hints().registerHints(hints, HintsProprios.class.getClassLoader());
        out.add("hint reports/daily.txt registrado -> " + RuntimeHintsPredicates.resource().forResource("reports/daily.txt").test(hints));
        out.add("hint reports/ qualquer outro padrão (other/x.txt) -> " + RuntimeHintsPredicates.resource().forResource("other/x.txt").test(hints));
        out.add("reflection de OrderDto (registerType) -> "
            + RuntimeHintsPredicates.reflection().onType(OrderDto.class).test(hints));
        return out;
    }

    public static void main(String[] args) throws IOException { run().forEach(System.out::println); }
}
