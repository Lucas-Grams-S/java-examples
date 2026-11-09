package dev.estudos.java.excecoes;

import java.util.ArrayList;
import java.util.List;

/**
 * J08-02 · try / catch / finally: ordem de execução e return em finally.
 * Documento: java-docs/01-java/08-excecoes.md §2.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * modo 0 -&gt; ok [try 0, finally 0]
 * modo 1 -&gt; negócio:pagamento [try 1, finally 1]
 * modo 2 -&gt; negócio:estoque [try 2, finally 2]
 * modo 3 -&gt; runtime:bug [try 3, finally 3]
 * try &#123; return 1; &#125; finally &#123; return 2; &#125; -&gt; 2
 * finally com return -&gt; exceção engolida
 * </pre>
 *
 * <p>Variações: troque a ordem dos catch (mais geral primeiro) e veja o erro de compilação.
 */
public class TryCatchFinally {
    static class PaymentException extends Exception { PaymentException(String m) { super(m); } }
    static class InventoryException extends Exception { InventoryException(String m) { super(m); } }

    static String process(int mode, List<String> log) {
        try {
            log.add("try " + mode);
            switch (mode) {
                case 1 -> throw new PaymentException("pagamento");
                case 2 -> throw new InventoryException("estoque");
                case 3 -> throw new IllegalStateException("bug");
                default -> { }
            }
            return "ok";
        } catch (PaymentException | InventoryException e) {   // multi-catch
            return "negócio:" + e.getMessage();
        } catch (RuntimeException e) {
            return "runtime:" + e.getMessage();
        } finally {
            log.add("finally " + mode);
        }
    }

    @SuppressWarnings("finally")
    static int returnInFinally() {
        try { return 1; } finally { return 2; }
    }

    @SuppressWarnings("finally")
    static String finallySwallows() {
        try { throw new IllegalStateException("perdida"); } finally { return "exceção engolida"; }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        for (int m = 0; m <= 3; m++) {
            List<String> log = new ArrayList<>();
            String r = process(m, log);
            out.add("modo " + m + " -> " + r + " " + log);
        }
        out.add("try { return 1; } finally { return 2; } -> " + returnInFinally());
        out.add("finally com return -> " + finallySwallows());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
