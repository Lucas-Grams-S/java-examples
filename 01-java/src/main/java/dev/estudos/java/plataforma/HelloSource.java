package dev.estudos.java.plataforma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Código-fonte do programa {@code Hello} dos documentos e utilitário para gravá-lo em um diretório temporário. */
final class HelloSource {
    private HelloSource() {}

    static final String HELLO = """
            public class Hello {
                static int sum(int a, int b) { return a + b; }

                public static void main(String[] args) {
                    int total = 0;
                    for (int i = 0; i < 200_000; i++) total = sum(total, i % 7);
                    System.out.println(total);
                }
            }
            """;

    /** Cria um diretório temporário com {@code Hello.java} e devolve o diretório. */
    static Path write() throws IOException {
        Path dir = Files.createTempDirectory("hello");
        Files.writeString(dir.resolve("Hello.java"), HELLO);
        return dir;
    }

    static void deleteRecursively(Path dir) throws IOException {
        try (var s = Files.walk(dir)) {
            s.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        }
    }
}
