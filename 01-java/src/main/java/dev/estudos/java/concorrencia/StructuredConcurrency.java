package dev.estudos.java.concorrencia;

import dev.estudos.java.Procs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * J12-11 · Structured Concurrency (PREVIEW no Java 25, JEP 505): StructuredTaskScope.open(), fork, join.
 * Documento: java-docs/01-java/12-concorrencia.md §3.6.
 *
 * <p>Como é preview, o programa é gravado em diretório temporário e executado em subprocesso (primeiro sem e
 * depois com {@code --enable-preview}), para não contaminar o módulo com a flag. Mesma técnica de J05-15.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * sem --enable-preview -&gt; recusado (API em preview)
 * resultado -&gt; ana7
 * falha de uma subtarefa cancela as outras -&gt; falhou
 * </pre>
 */
public class StructuredConcurrency {
    static final String FONTE = """
        import java.util.concurrent.StructuredTaskScope;

        public class Estruturada {
            public static void main(String[] args) throws Exception {
                try (var scope = StructuredTaskScope.open()) {
                    var user  = scope.fork(() -> "ana");
                    var count = scope.fork(() -> 7);
                    scope.join();
                    System.out.println("resultado -> " + user.get() + count.get());
                }
                try (var scope = StructuredTaskScope.open()) {
                    var ok  = scope.fork(() -> { Thread.sleep(5_000); return "lenta"; });
                    var bad = scope.fork(() -> { throw new IllegalStateException("falhou"); });
                    scope.join();
                    System.out.println("não chega aqui");
                } catch (StructuredTaskScope.FailedException e) {
                    System.out.println("falha de uma subtarefa cancela as outras -> " + e.getCause().getMessage());
                }
            }
        }
        """;

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        Path dir = Files.createTempDirectory("structured");
        Path src = dir.resolve("Estruturada.java");
        Files.writeString(src, FONTE);

        String semPreview = Procs.javaExec(src.toString()).output();
        out.add("sem --enable-preview -> " + (semPreview.contains("preview") ? "recusado (API em preview)" : "inesperado: " + semPreview));

        var com = Procs.javaExec("--enable-preview", "--source", "25", src.toString());
        for (String linha : com.output().split("\n")) {
            if (linha.startsWith("resultado") || linha.startsWith("falha")) out.add(linha);
        }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
