package dev.estudos.java.evolucao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * J00-02 · Java 7: try-with-resources e diamond.
 * Documento: java-docs/01-java/00-evolucao-do-java.md §4.2.
 *
 * <p>Compara o {@code finally} manual com try-with-resources e mostra exceções suprimidas.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * primeira linha -&gt; id;total
 * abre A
 * abre B
 * fecha B
 * fecha A
 * principal -&gt; erro no corpo
 * suprimida -&gt; falha ao fechar B
 * suprimida -&gt; falha ao fechar A
 * diamond -&gt; {ana=[pedido-1]}
 * </pre>
 *
 * <p>Variações: declare dois recursos e observe a ordem inversa de fechamento.
 */
public class TryWithResources {
    static class Res implements AutoCloseable {
        final String name; final List<String> log;
        Res(String name, List<String> log) { this.name = name; this.log = log; log.add("abre " + name); }
        @Override public void close() { log.add("fecha " + name); throw new IllegalStateException("falha ao fechar " + name); }
    }

    public static List<String> run() throws IOException {
        List<String> out = new ArrayList<>();
        Path csv = Files.createTempFile("orders", ".csv");
        Files.writeString(csv, "id;total\n1;10\n");
        try (var r = Files.newBufferedReader(csv)) {          // close() automático
            out.add("primeira linha -> " + r.readLine());
        }
        Files.delete(csv);

        try (Res a = new Res("A", out); Res b = new Res("B", out)) {
            throw new RuntimeException("erro no corpo");
        } catch (RuntimeException e) {
            out.add("principal -> " + e.getMessage());
            for (Throwable s : e.getSuppressed()) out.add("suprimida -> " + s.getMessage());
        }
        Map<String, List<String>> byCustomer = new HashMap<>();   // diamond
        byCustomer.computeIfAbsent("ana", k -> new ArrayList<>()).add("pedido-1");
        out.add("diamond -> " + byCustomer);
        return out;
    }

    public static void main(String[] args) throws IOException {
        run().forEach(System.out::println);
    }
}
