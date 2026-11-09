package dev.estudos.springcore.aot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.aot.hint.RuntimeHints;

/**
 * SC10-04 · Antes e depois: o mesmo bean. Em runtime clássico o {@code ConstructorResolver} usa reflection;
 * no AOT o gerador emite chamada direta ao construtor ({@code forConstructor(...).withGenerator(... new ...)})
 * e, para {@code @Bean}, chamada direta ao método de fábrica. Este exemplo gera o código AOT de
 * {@link GerarAot} e mostra as linhas relevantes do fonte gerado.
 * Documento: 02-spring-core/10-aot-e-native.md §3.1.
 *
 * <p>Saída real: veja {@code run()} (trechos do arquivo {@code GerarAot__BeanDefinitions.java}); o teste
 * afirma que há {@code forConstructor}, {@code withGenerator} com {@code new} e {@code forFactoryMethod}.
 */
public class AntesDepois {
    public static List<String> run() throws IOException {
        Path dir = Files.createTempDirectory("aot-antes-depois");
        try {
            GerarAot.gerar(dir, new RuntimeHints[1]);
            Path f = dir.resolve("sources/dev/estudos/springcore/aot/GerarAot__BeanDefinitions.java");
            List<String> out = new ArrayList<>();
            for (String l : Files.readAllLines(f)) {
                String t = l.strip();
                if (t.contains("forConstructor") || t.contains("withGenerator") || t.contains("forFactoryMethod")
                    || t.contains("setInstanceSupplier")) out.add(t);
            }
            return out;
        } finally {
            try (Stream<Path> s = Files.walk(dir)) { s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
        }
    }

    public static void main(String[] args) throws IOException { run().forEach(System.out::println); }
}
