package dev.estudos.java.plataforma;

import dev.estudos.java.Procs;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * J01-11 · Compilar, inspecionar e executar.
 * Documento: java-docs/01-java/01-plataforma-java.md §5.1.
 *
 * <p>Reproduz {@code javac -d out}, {@code java -cp out} e {@code javap -cp out -c -p} com uma classe num pacote
 * ({@code com.example.orders.Main}), mostrando que o {@code -d} cria a árvore de diretórios do pacote.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * out/com/example/orders/Main.class existe -&gt; true
 * java -cp out -&gt; pedidos ok
 * javap -&gt; 3: ldc           #13                 // String pedidos ok
 * </pre>
 */
public class CompileInspectRun {
    public static List<String> run() throws Exception {
        Path dir = Files.createTempDirectory("orders");
        try {
            Path src = dir.resolve("src/com/example/orders");
            Files.createDirectories(src);
            Files.writeString(src.resolve("Main.java"), """
                    package com.example.orders;
                    public class Main {
                        public static void main(String[] args) { System.out.println("pedidos ok"); }
                    }
                    """);
            Path out = dir.resolve("out");
            Procs.tool("javac", "-d", out.toString(), src.resolve("Main.java").toString());
            boolean arvore = Files.exists(out.resolve("com/example/orders/Main.class"));
            String run = Procs.java("-cp", out.toString(), "com.example.orders.Main").strip();
            String javap = Procs.tool("javap", "-cp", out.toString(), "-c", "-p", "com.example.orders.Main");
            String ldc = javap.lines().filter(l -> l.contains("pedidos ok")).findFirst().orElse("?").strip();
            return List.of("out/com/example/orders/Main.class existe -> " + arvore, "java -cp out -> " + run,
                    "javap -> " + ldc);
        } finally {
            HelloSource.deleteRecursively(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        run().forEach(System.out::println);
    }
}
