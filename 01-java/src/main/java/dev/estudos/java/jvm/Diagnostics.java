package dev.estudos.java.jvm;

import dev.estudos.java.Procs;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * J02-13 · Diagnóstico de um processo em execução.
 * Documento: java-docs/01-java/02-jvm-por-dentro.md §5.4.
 *
 * <p>Sobe um {@link GcDemo} que fica dormindo ({@code sleep}) e o inspeciona com {@code jcmd}: flags, heap,
 * histograma de classes, thread dump e heap dump. O {@code .hprof} pode conter dados sensíveis: aqui é criado em
 * pasta temporária e apagado ao final. Valores e pid variam.
 *
 * <p>Saída real (JDK 25; varia):
 * <pre>
 * jcmd (sem argumentos) lista a JVM alvo -&gt; true
 * VM.flags -&gt; -XX:MaxHeapSize=67108864
 * GC.heap_info (2 primeiras linhas):
 *   garbage-first heap   total reserved 65536K, committed 65536K, used 46297K [0x00000000fc000000, 0x0000000100000000)
 *   region size 1024K, 1 young (1024K), 0 survivors (0K)
 * GC.class_histogram | head -4:
 *   num #instances #bytes class name (module)
 *   -------------------------------------------------------
 *   1: 8652 30386608 [B (java.base@25.0.4.1)
 *   2: 322 1463976 [Ljdk.internal.vm.FillerElement; (java.base@25.0.4.1)
 * Thread.print contém a thread main -&gt; true
 * GC.heap_dump gerou .hprof (bytes &gt; 0) -&gt; true
 * </pre>
 */
public class Diagnostics {
    static String jcmd(long pid, String... cmd) {
        List<String> all = new ArrayList<>(List.of(Procs.tool("jcmd"), Long.toString(pid)));
        all.addAll(List.of(cmd));
        return Procs.run(all, null);
    }

    public static List<String> run() throws Exception {
        Path dir = Files.createTempDirectory("diag");
        Process p = new ProcessBuilder(Procs.tool("java"), "-Xmx64m", "-cp", Procs.classpath(), "dev.estudos.java.jvm.GcDemo", "sleep")
                .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        try {
            long pid = p.pid();
            Thread.sleep(1500);
            List<String> out = new ArrayList<>();
            out.add("jcmd (sem argumentos) lista a JVM alvo -> " + Procs.run(List.of(Procs.tool("jcmd")), null).contains(Long.toString(pid)));
            out.add("VM.flags -> " + jcmd(pid, "VM.flags").lines().filter(l -> l.contains("MaxHeapSize")).findFirst().map(l -> l.replaceAll(".*(-XX:MaxHeapSize=\\d+).*", "$1")).orElse("?"));
            out.add("GC.heap_info (2 primeiras linhas):");
            jcmd(pid, "GC.heap_info").lines().skip(1).limit(2).forEach(l -> out.add("  " + l.trim()));
            out.add("GC.class_histogram | head -4:");
            jcmd(pid, "GC.class_histogram").lines().skip(1).limit(4).forEach(l -> out.add("  " + l.trim().replaceAll("\\s+", " ")));
            out.add("Thread.print contém a thread main -> " + jcmd(pid, "Thread.print").contains("\"main\""));
            Path hprof = dir.resolve("heap.hprof");
            jcmd(pid, "GC.heap_dump", hprof.toString());
            out.add("GC.heap_dump gerou .hprof (bytes > 0) -> " + (Files.exists(hprof) && Files.size(hprof) > 0));
            return out;
        } finally {
            p.destroyForcibly();
            try (var s = Files.list(dir)) { s.forEach(f -> f.toFile().delete()); }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        run().forEach(System.out::println);
    }
}
