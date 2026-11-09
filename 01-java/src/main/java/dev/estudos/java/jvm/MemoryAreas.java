package dev.estudos.java.jvm;

import dev.estudos.java.Procs;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * J02-03 · Áreas de memória.
 * Documento: java-docs/01-java/02-jvm-por-dentro.md §2.3.
 *
 * <p>Lista as áreas reais da JVM em execução via {@code java.lang.management}: pools de heap, Metaspace, code cache,
 * memória direta e o tamanho padrão da pilha de thread ({@code -Xss}). Também mostra que um objeto vive no heap e
 * uma variável primitiva local no frame (a pilha é inspecionável só indiretamente: pelo {@code StackOverflowError}).
 *
 * <p>Saída real (JDK 25; tamanhos variam com a máquina):
 * <pre>
 * NÃO-HEAP CodeHeap 'non-nmethods'
 * NÃO-HEAP Metaspace
 * NÃO-HEAP CodeHeap 'profiled nmethods'
 * NÃO-HEAP Compressed Class Space
 * HEAP     G1 Eden Space
 * HEAP     G1 Old Gen
 * HEAP     G1 Survivor Space
 * NÃO-HEAP CodeHeap 'non-profiled nmethods'
 * memória direta: 1 buffer(s), 1048576 bytes (capacity=1048576)
 * pilha por thread (ThreadStackSize) -&gt; intx ThreadStackSize = 1024 {pd product} {default}
 * </pre>
 *
 * <p>Variações: rode com {@code -XX:+UseSerialGC} e veja os nomes dos pools de heap mudarem.
 */
public class MemoryAreas {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            out.add((pool.getType() == MemoryType.HEAP ? "HEAP     " : "NÃO-HEAP ") + pool.getName());
        }
        ByteBuffer direct = ByteBuffer.allocateDirect(1 << 20);
        for (BufferPoolMXBean b : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)) {
            if (b.getName().equals("direct")) out.add("memória direta: " + b.getCount() + " buffer(s), " + b.getMemoryUsed() + " bytes (capacity=" + direct.capacity() + ")");
        }
        String flags = Procs.java("-XX:+PrintFlagsFinal", "-version");
        out.add("pilha por thread (ThreadStackSize) -> " + flags.lines().filter(l -> l.contains(" ThreadStackSize ")).findFirst().map(l -> l.trim().replaceAll("\\s+", " ")).orElse("?"));
        return out;
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
