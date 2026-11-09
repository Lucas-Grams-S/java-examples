package dev.estudos.java.plataforma;

import dev.estudos.java.Procs;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * J01-14 · Opções úteis de linha de comando.
 * Documento: java-docs/01-java/01-plataforma-java.md §5.4.
 *
 * <p>Executa subprocessos {@code java} com {@code -Xmx}, {@code -XX:+PrintFlagsFinal}, {@code --show-version},
 * {@code -Xint} e {@code -XX:+AutoCreateSharedArchive} (AppCDS dinâmico).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * -Xms512m -Xmx512m -&gt; size_t InitialHeapSize = 536870912 {product} {command line} | size_t MaxHeapSize = 536870912 {product} {command line}
 * PrintFlagsFinal | grep tiered -&gt; bool TieredCompilation = true {pd product} {default}
 * -XX:TieredStopAtLevel=1 -&gt; intx TieredStopAtLevel = 1 {product} {command line}
 * --show-version -&gt; openjdk 25.0.4.1 2026-08-18
 * -Xint -&gt; OpenJDK 64-Bit Server VM (build 25.0.4.1+1-1-26.04.4-Ubuntu, interpreted mode, sharing)
 * AppCDS dinâmico: arquivo .jsa criado -&gt; true
 * </pre>
 */
public class CommandLineOptions {
    static String flag(String out, String name) {
        return out.lines().filter(l -> l.contains(" " + name + " ")).findFirst().map(l -> l.trim().replaceAll("\\s+", " ")).orElse(name + "?");
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        String heap = Procs.java("-Xms512m", "-Xmx512m", "-XX:+PrintFlagsFinal", "-version");
        out.add("-Xms512m -Xmx512m -> " + flag(heap, "InitialHeapSize") + " | " + flag(heap, "MaxHeapSize"));
        String tiered = Procs.java("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintFlagsFinal", "-version");
        out.add("PrintFlagsFinal | grep tiered -> " + flag(tiered, "TieredCompilation"));
        out.add("-XX:TieredStopAtLevel=1 -> " + flag(Procs.java("-XX:TieredStopAtLevel=1", "-XX:+PrintFlagsFinal", "-version"), "TieredStopAtLevel"));
        out.add("--show-version -> " + Procs.java("--show-version", "-version").lines().findFirst().orElse("?"));
        out.add("-Xint -> " + Procs.java("-Xint", "-version").lines().filter(l -> l.contains("mixed") || l.contains("interpreted")).findFirst().orElse("?").trim());
        Path jsa = Files.createTempFile("app", ".jsa");
        Files.delete(jsa);
        Procs.java("-XX:+AutoCreateSharedArchive", "-XX:SharedArchiveFile=" + jsa, "-version");
        out.add("AppCDS dinâmico: arquivo .jsa criado -> " + Files.exists(jsa));
        Files.deleteIfExists(jsa);
        return out;
    }

    public static void main(String[] args) throws Exception {
        run().forEach(System.out::println);
    }
}
