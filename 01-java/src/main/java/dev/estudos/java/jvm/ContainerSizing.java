package dev.estudos.java.jvm;

import dev.estudos.java.Procs;
import java.util.ArrayList;
import java.util.List;

/**
 * J02-14 · Dimensionamento em contêiner.
 * Documento: java-docs/01-java/02-jvm-por-dentro.md §5.5.
 *
 * <p>Mostra {@code -XX:MaxRAMPercentage} dimensionando o heap como fração da memória disponível (num contêiner,
 * o limite do cgroup; fora dele, a RAM da máquina) e {@code -XX:+ExitOnOutOfMemoryError} derrubando o processo
 * (código de saída 3) em vez de deixá-lo degradado. Para simular um limite real: {@code docker run -m 2g ...}.
 *
 * <p>Saída real (JDK 25; depende da RAM da máquina):
 * <pre>
 * UseContainerSupport -&gt; true
 * MaxRAMPercentage=25 -&gt; heap máximo ≈ 25% da memória visível pela JVM (19186 MiB)
 * MaxRAMPercentage=70 -&gt; heap máximo ≈ 70% da memória visível pela JVM (19186 MiB)
 * -XX:+ExitOnOutOfMemoryError -&gt; código de saída 3 (o orquestrador reiniciaria o processo)
 * </pre>
 */
public class ContainerSizing {
    static long flag(String out, String name) {
        return out.lines().filter(l -> l.contains(" " + name + " ")).findFirst().map(l -> l.trim().split("\\s+")[3]).map(Long::parseLong).orElse(-1L);
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        String base = Procs.java("-XX:+PrintFlagsFinal", "-version");
        long ram = ((com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize();   // respeita o cgroup
        out.add("UseContainerSupport -> " + base.lines().filter(l -> l.contains("UseContainerSupport")).findFirst().map(l -> l.trim().split("\\s+")[3]).orElse("?"));
        for (int pct : new int[] {25, 70}) {
            long max = flag(Procs.java("-XX:MaxRAMPercentage=" + pct, "-XX:+PrintFlagsFinal", "-version"), "MaxHeapSize");
            out.add("MaxRAMPercentage=" + pct + " -> heap máximo ≈ " + Math.round(100.0 * max / ram) + "% da memória visível pela JVM (" + ram / (1024 * 1024) + " MiB)");
        }
        var r = Procs.javaExec("-Xmx16m", "-XX:+ExitOnOutOfMemoryError", "-cp", Procs.classpath(), "dev.estudos.java.jvm.OomProgram");
        out.add("-XX:+ExitOnOutOfMemoryError -> código de saída " + r.exitCode() + " (o orquestrador reiniciaria o processo)");
        return out;
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
