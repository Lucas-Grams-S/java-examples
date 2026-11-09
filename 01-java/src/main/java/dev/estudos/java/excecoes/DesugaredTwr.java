package dev.estudos.java.excecoes;

import java.util.ArrayList;
import java.util.List;

/**
 * J08-08 · try-with-resources desaçucarado: a expansão manual produz o mesmo resultado.
 * Documento: java-docs/01-java/08-excecoes.md §3.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * try-with-resources -&gt; body suprimidas=[close]
 * expansão manual   -&gt; body suprimidas=[close]
 * </pre>
 *
 * <p>Variações: troque o catch (Throwable) por Exception e veja Errors escaparem do addSuppressed.
 */
public class DesugaredTwr {
    static class Res implements AutoCloseable {
        final List<String> log;
        Res(List<String> log) { this.log = log; }
        @Override public void close() { log.add("close"); throw new IllegalStateException("close"); }
    }

    static Throwable withTwr() {
        try (Res r = new Res(new ArrayList<>())) {
            throw new RuntimeException("body");
        } catch (RuntimeException e) { return e; }
    }

    static Throwable manual() {
        try {
            Res r = new Res(new ArrayList<>());
            Throwable primary = null;
            try {
                throw new RuntimeException("body");
            } catch (Throwable t) { primary = t; throw t; }
            finally {
                if (r != null) {
                    if (primary != null) {
                        try { r.close(); } catch (Throwable s) { primary.addSuppressed(s); }
                    } else r.close();
                }
            }
        } catch (RuntimeException e) { return e; }
    }

    static String describe(Throwable t) {
        return t.getMessage() + " suprimidas=" + java.util.Arrays.stream(t.getSuppressed()).map(Throwable::getMessage).toList();
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("try-with-resources -> " + describe(withTwr()));
        out.add("expansão manual   -> " + describe(manual()));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
