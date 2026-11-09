package dev.estudos.java.tipos;

import dev.estudos.java.Procs;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * J03-09 · O que o compilador faz com boxing e {@code String}.
 * Documento: java-docs/01-java/03-tipos-e-memoria.md §3.2.
 *
 * <p>Compila uma classe pequena e filtra a saída de {@code javap -c -p}: {@code Integer j1 = 128} vira
 * {@code Integer.valueOf}, {@code int z = n} vira {@code intValue}, e a concatenação vira {@code invokedynamic}.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * 3: invokestatic #7 // Method java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
 * 8: invokevirtual #13 // Method java/lang/Integer.intValue:()I
 * 14: invokedynamic #17, 0 // InvokeDynamic #0:makeConcatWithConstants:(ILjava/lang/Integer;)Ljava/lang/String;
 * </pre>
 */
public class CompilerBoxing {
    public static List<String> run() throws Exception {
        Path dir = Files.createTempDirectory("t3");
        try {
            Files.writeString(dir.resolve("T3.java"), """
                    public class T3 {
                        static int f(Integer n) { Integer j1 = 128; int z = n; String s = "id=" + z + j1; return s.length(); }
                    }
                    """);
            Procs.tool("javac", "-d", dir.toString(), dir.resolve("T3.java").toString());
            String javap = Procs.tool("javap", "-c", "-p", "-cp", dir.toString(), "T3");
            return javap.lines().filter(l -> l.contains("Integer.valueOf") || l.contains("Integer.intValue") || l.contains("invokedynamic"))
                    .map(l -> l.trim().replaceAll("\\s+", " ")).toList();
        } finally {
            Files.deleteIfExists(dir.resolve("T3.java"));
            Files.deleteIfExists(dir.resolve("T3.class"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        run().forEach(System.out::println);
    }
}
