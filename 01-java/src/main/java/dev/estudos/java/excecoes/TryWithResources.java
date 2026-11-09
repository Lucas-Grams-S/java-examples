package dev.estudos.java.excecoes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * J08-03 · try-with-resources: ordem inversa de fechamento e exceções suprimidas.
 * Documento: java-docs/01-java/08-excecoes.md §2.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * log -&gt; [abre A, abre B, corpo, close B, close A]
 * principal -&gt; body
 * suprimidas -&gt; [close B, close A]
 * só close lança -&gt; close C suprimidas=0
 * </pre>
 *
 * <p>Variações: tire o throw do corpo e faça só close() lançar: a exceção de close vira a principal.
 */
public class TryWithResources {
    static class Res implements AutoCloseable {
        private final String name; private final List<String> log;
        Res(String name, List<String> log) { this.name = name; this.log = log; log.add("abre " + name); }
        @Override public void close() { log.add("close " + name); throw new IllegalStateException("close " + name); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        List<String> log = new ArrayList<>();
        try (Res a = new Res("A", log); Res b = new Res("B", log)) {
            log.add("corpo");
            throw new RuntimeException("body");
        } catch (RuntimeException e) {
            out.add("log -> " + log);
            out.add("principal -> " + e.getMessage());
            out.add("suprimidas -> " + Arrays.stream(e.getSuppressed()).map(Throwable::getMessage).toList());
        }
        // Java 9+: variável efetivamente final já declarada
        List<String> log2 = new ArrayList<>();
        Res c = new Res("C", log2);
        try (c) {
            log2.add("corpo ok");
        } catch (IllegalStateException e) {
            out.add("só close lança -> " + e.getMessage() + " suprimidas=" + e.getSuppressed().length);
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
