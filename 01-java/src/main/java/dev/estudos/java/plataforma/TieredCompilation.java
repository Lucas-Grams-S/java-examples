package dev.estudos.java.plataforma;

import dev.estudos.java.Procs;
import java.nio.file.Path;
import java.util.List;

/**
 * J01-08 · Compilação em camadas (tiered compilation).
 * Documento: java-docs/01-java/01-plataforma-java.md §3.4.
 *
 * <p>Roda {@code Hello} com {@code -XX:+PrintCompilation} e filtra as linhas de {@code Hello::}.
 * Colunas: tempo (ms), id, flags ({@code %} = OSR), nível (3 = C1 com perfil, 4 = C2), método.
 * <b>A saída varia a cada execução</b> (o JIT é concorrente e adaptativo); por isso os testes só verificam que
 * aparecem níveis de compilação.
 *
 * <p>Saída real (JDK 25; varia):
 * <pre>
 * 11    8       3       Hello::sum (4 bytes)
 * 12    9       4       Hello::sum (4 bytes)
 * 12    8       3       Hello::sum (4 bytes)   made not entrant: not used
 * 13   10 %     3       Hello::main @ 4 (33 bytes)
 * 13   11       3       Hello::main (33 bytes)
 * 13   12 %     4       Hello::main @ 4 (33 bytes)
 * 14   12 %     4       Hello::main @ 4 (33 bytes)   blocked
 * </pre>
 *
 * <p>Variações: adicione {@code -XX:TieredStopAtLevel=1} (só C1) ou {@code -Xint} (nenhuma linha).
 */
public class TieredCompilation {
    public static List<String> run() throws Exception {
        Path dir = HelloSource.write();
        try {
            Procs.tool("javac", "-d", dir.toString(), dir.resolve("Hello.java").toString());
            String out = Procs.java("-XX:+PrintCompilation", "-cp", dir.toString(), "Hello");
            return out.lines().filter(l -> l.contains("Hello::")).toList();
        } finally {
            HelloSource.deleteRecursively(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        run().forEach(System.out::println);
    }
}
