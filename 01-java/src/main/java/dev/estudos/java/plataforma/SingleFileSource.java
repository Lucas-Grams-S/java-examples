package dev.estudos.java.plataforma;

import dev.estudos.java.Procs;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * J01-12 · Executar um arquivo único (Java 11+).
 * Documento: java-docs/01-java/01-plataforma-java.md §5.2.
 *
 * <p>Executa {@code java Hello.java} (compila na memória, sem gerar {@code .class}) com o {@code main} de instância
 * do documento. No Java 25 isso é recurso final (JEP 512); nos Java 21 a 24 exigiria {@code --enable-preview}.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * java Hello.java -&gt; Olá, pedidos!
 * nenhum .class gerado -&gt; true
 * </pre>
 */
public class SingleFileSource {
    public static List<String> run() throws Exception {
        Path dir = Files.createTempDirectory("single");
        try {
            Path hello = dir.resolve("Hello.java");
            Files.writeString(hello, """
                    void main() {
                        System.out.println("Olá, pedidos!");
                    }
                    """);
            String out = Procs.java(hello.toString()).strip();
            boolean semClass;
            try (var s = Files.list(dir)) { semClass = s.noneMatch(p -> p.toString().endsWith(".class")); }
            return List.of("java Hello.java -> " + out, "nenhum .class gerado -> " + semClass);
        } finally {
            HelloSource.deleteRecursively(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        run().forEach(System.out::println);
    }
}
