package dev.estudos.java.jvm;

import dev.estudos.java.Procs;
import java.util.ArrayList;
import java.util.List;

/**
 * J02-06 · Os coletores do HotSpot.
 * Documento: java-docs/01-java/02-jvm-por-dentro.md §3.2.
 *
 * <p>Sobe uma JVM com cada flag de coletor e lê a linha {@code Using ...} do log unificado; depois observa o G1
 * com {@code -Xmx64m -Xlog:gc} sobre o {@link GcDemo}. Os números do log (tempo, pausa) variam.
 * Shenandoah depende da distribuição; Epsilon exige {@code -XX:+UnlockExperimentalVMOptions}.
 *
 * <p>Saída real (JDK 25; varia):
 * <pre>
 * Serial     -&gt; [0.002s][info][gc] Using Serial
 * Parallel   -&gt; [0.002s][info][gc] Using Parallel
 * G1         -&gt; [0.002s][info][gc] Using G1
 * ZGC        -&gt; [0.054s][info][gc] Using The Z Garbage Collector
 * Shenandoah -&gt; [0.003s][info][gc] Using Shenandoah
 * Epsilon    -&gt; [0.002s][info][gc] Using Epsilon
 * G1 sobre GcDemo (-Xmx64m -Xlog:gc):
 *   [0.002s][info][gc] Using G1
 *   [0.023s][info][gc] GC(0) Pause Young (Concurrent Start) (G1 Humongous Allocation) 28M-&gt;4M(64M) 0.720ms
 *   [0.023s][info][gc] GC(1) Concurrent Undo Cycle
 *   [0.023s][info][gc] GC(1) Concurrent Undo Cycle 0.122ms
 *   [0.026s][info][gc] GC(2) Pause Young (Concurrent Start) (G1 Humongous Allocation) 27M-&gt;6M(64M) 0.281ms
 * </pre>
 */
public class Collectors {
    static String using(String... flags) {
        List<String> args = new ArrayList<>(List.of(flags));
        args.addAll(List.of("-Xlog:gc", "-version"));
        return Procs.java(args.toArray(String[]::new)).lines().filter(l -> l.contains("Using") || l.contains("Error") || l.contains("Unrecognized")).findFirst().orElse("(sem linha Using)");
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("Serial     -> " + using("-XX:+UseSerialGC"));
        out.add("Parallel   -> " + using("-XX:+UseParallelGC"));
        out.add("G1         -> " + using("-XX:+UseG1GC"));
        out.add("ZGC        -> " + using("-XX:+UseZGC"));
        out.add("Shenandoah -> " + using("-XX:+UseShenandoahGC"));
        out.add("Epsilon    -> " + using("-XX:+UnlockExperimentalVMOptions", "-XX:+UseEpsilonGC"));
        out.add("G1 sobre GcDemo (-Xmx64m -Xlog:gc):");
        Procs.java("-Xmx64m", "-Xlog:gc", "-cp", Procs.classpath(), "dev.estudos.java.jvm.GcDemo").lines().limit(5).forEach(l -> out.add("  " + l));
        return out;
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
