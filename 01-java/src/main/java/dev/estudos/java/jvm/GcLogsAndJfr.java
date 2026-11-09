package dev.estudos.java.jvm;

import dev.estudos.java.Procs;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * J02-12 · Logs de GC e JFR.
 * Documento: java-docs/01-java/02-jvm-por-dentro.md §5.3.
 *
 * <p>Roda o {@link GcDemo} com {@code -Xlog:gc*:file=...} e {@code -XX:StartFlightRecording}, depois consulta a
 * gravação com {@code jfr summary} e {@code jfr print --events jdk.GarbageCollection}. Tempos e contagens variam.
 *
 * <p>Saída real (JDK 25; varia):
 * <pre>
 * gc.log: 503 linhas; primeiras:
 *   [hora][0.002s] CardTable entry size: 512
 *   [hora][0.002s] Using G1
 *   [hora][0.002s] Version: 25.0.4.1+1-1-26.04.4-Ubuntu (release)
 * app.jfr existe -&gt; true
 * jfr summary: jdk.GarbageCollection 30 581
 * jfr summary: jdk.ExecutionSample 0 0
 * jfr print --events jdk.GarbageCollection | head:
 *   jdk.GarbageCollection {
 *     startTime = 11:39:45.148 (2026-11-05)
 *     duration = 3.91 ms
 *     gcId = 0
 *     name = "G1New"
 *     cause = "G1 Humongous Allocation"
 *     sumOfPauses = 3.91 ms
 *     longestPause = 3.91 ms
 * </pre>
 */
public class GcLogsAndJfr {
    public static List<String> run() throws Exception {
        Path dir = Files.createTempDirectory("jfr");
        try {
            Path log = dir.resolve("gc.log"), jfr = dir.resolve("app.jfr");
            Procs.java("-Xmx64m", "-Xlog:gc*:file=" + log + ":time,uptime:filecount=5,filesize=10m",
                    "-XX:StartFlightRecording=filename=" + jfr, "-cp", Procs.classpath(), "dev.estudos.java.jvm.GcDemo");
            List<String> out = new ArrayList<>();
            List<String> gcLines = Files.readAllLines(log);
            out.add("gc.log: " + gcLines.size() + " linhas; primeiras:");
            gcLines.stream().limit(3).forEach(l -> out.add("  " + l.replaceFirst("^\\[[^]]*\\]", "[hora]")));
            out.add("app.jfr existe -> " + Files.exists(jfr));
            String summary = Procs.tool("jfr", "summary", jfr.toString());
            summary.lines().filter(l -> l.contains("jdk.GarbageCollection ") || l.contains("jdk.ExecutionSample ")).forEach(l -> out.add("jfr summary: " + l.trim().replaceAll("\\s+", " ")));
            String events = Procs.tool("jfr", "print", "--events", "jdk.GarbageCollection", jfr.toString());
            out.add("jfr print --events jdk.GarbageCollection | head:");
            events.lines().limit(8).forEach(l -> out.add("  " + l));
            return out;
        } finally {
            try (var s = Files.list(dir)) { s.forEach(p -> p.toFile().delete()); }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        run().forEach(System.out::println);
    }
}
