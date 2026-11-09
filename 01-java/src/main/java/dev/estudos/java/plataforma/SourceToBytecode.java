package dev.estudos.java.plataforma;

import dev.estudos.java.Procs;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * J01-05 · Da fonte ao bytecode.
 * Documento: java-docs/01-java/01-plataforma-java.md §3.1.
 *
 * <p>Compila {@code Hello.java} com o {@code javac} do JDK atual, desassembla {@code sum} com {@code javap -c -p}
 * e lê o cabeçalho do {@code .class} (magic {@code 0xCAFEBABE} e versão major).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 *   static int sum(int, int);
 *     Code:
 *          0: iload_0
 *          1: iload_1
 *          2: iadd
 *          3: ireturn
 * magic -&gt; 0xCAFEBABE
 * major -&gt; 69 (Java 25)
 * </pre>
 *
 * <p>Variações: troque {@code int} por {@code long} em {@code sum} e veja {@code lload}/{@code ladd}/{@code lreturn}.
 */
public class SourceToBytecode {
    public static List<String> run() throws Exception {
        Path dir = HelloSource.write();
        try {
            Procs.tool("javac", "-d", dir.toString(), dir.resolve("Hello.java").toString());
            String javap = Procs.tool("javap", "-c", "-p", "-cp", dir.toString(), "Hello");
            String sum = javap.lines().dropWhile(l -> !l.contains("static int sum(int, int)")).limit(6).reduce((a, b) -> a + "\n" + b).orElse("");
            byte[] head = Files.readAllBytes(dir.resolve("Hello.class"));
            String magic = String.format("%02x%02x%02x%02x", head[0], head[1], head[2], head[3]);
            int major = ((head[6] & 0xff) << 8) | (head[7] & 0xff);
            return List.of(sum, "magic -> 0x" + magic.toUpperCase(), "major -> " + major + " (Java " + (major - 44) + ")");
        } finally {
            HelloSource.deleteRecursively(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        run().forEach(System.out::println);
    }
}
