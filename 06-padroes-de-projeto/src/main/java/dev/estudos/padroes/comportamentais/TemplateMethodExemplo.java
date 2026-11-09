package dev.estudos.padroes.comportamentais;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * DP04-14 · Template Method: esqueleto {@code final} com {@code finally} e a variante por callback.
 * Documento: java-docs/06-padroes-de-projeto/04-comportamentais.md §5.9.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * process lança, log do esqueleto -&gt; [open, process, close]
 * callback com sucesso -&gt; ok [begin, commit]
 * callback que falha -&gt; [begin, rollback]
 * </pre>
 *
 * <p>Variações: faça o {@code Supplier} lançar {@code RuntimeException} e veja {@code rollback} no log.
 */
public class TemplateMethodExemplo {

    static abstract class Importer {
        final List<String> log = new ArrayList<>();
        final void run() {
            open();
            try { log.add("process"); process(); } finally { close(); }
        }
        void open() { log.add("open"); }
        abstract void process();
        void close() { log.add("close"); }
    }

    static class Tx {
        final List<String> log = new ArrayList<>();
        <T> T inTransaction(Supplier<T> work) {
            log.add("begin");
            try { T r = work.get(); log.add("commit"); return r; }
            catch (RuntimeException e) { log.add("rollback"); throw e; }
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var falha = new Importer() { void process() { throw new IllegalStateException("arquivo corrompido"); } };
        try {
            falha.run();
        } catch (IllegalStateException e) {
            out.add("process lança, log do esqueleto -> " + falha.log);
        }
        var tx = new Tx();
        out.add("callback com sucesso -> " + tx.inTransaction(() -> "ok") + " " + tx.log);
        var tx2 = new Tx();
        try {
            tx2.inTransaction(() -> { throw new IllegalArgumentException("x"); });
        } catch (IllegalArgumentException e) {
            out.add("callback que falha -> " + tx2.log);
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
