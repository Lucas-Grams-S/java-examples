package dev.estudos.java.excecoes;

import java.util.ArrayList;
import java.util.List;

/**
 * J08-04 · Exceções próprias: unchecked de domínio (com causa) e checked de integração.
 * Documento: java-docs/01-java/08-excecoes.md §2.4.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * unchecked -&gt; Order 42 not found id=42 causa=null
 * com causa -&gt; Order 7 not found causa=java.lang.IllegalStateException: linha ausente
 * checked -&gt; falha ao cobrar &lt;- gateway fora do ar
 * é RuntimeException? -&gt; false
 * </pre>
 *
 * <p>Variações: troque o construtor sem causa e compare getCause() (null).
 */
public class CustomExceptions {
    public static class OrderNotFoundException extends RuntimeException {
        private final long orderId;
        public OrderNotFoundException(long orderId) {
            super("Order %d not found".formatted(orderId));
            this.orderId = orderId;
        }
        public OrderNotFoundException(long orderId, Throwable cause) {
            super("Order %d not found".formatted(orderId), cause);
            this.orderId = orderId;
        }
        public long orderId() { return orderId; }
    }

    public static class PaymentException extends Exception {
        public PaymentException(String message, Throwable cause) { super(message, cause); }
    }

    static void charge() throws PaymentException {
        try {
            throw new java.io.IOException("gateway fora do ar");
        } catch (java.io.IOException e) {
            throw new PaymentException("falha ao cobrar", e);
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var e1 = new OrderNotFoundException(42);
        out.add("unchecked -> " + e1.getMessage() + " id=" + e1.orderId() + " causa=" + e1.getCause());
        var e2 = new OrderNotFoundException(7, new IllegalStateException("linha ausente"));
        out.add("com causa -> " + e2.getMessage() + " causa=" + e2.getCause());
        try { charge(); } catch (PaymentException e) {
            out.add("checked -> " + e.getMessage() + " <- " + e.getCause().getMessage());
            Throwable t = e; out.add("é RuntimeException? -> " + (t instanceof RuntimeException));
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
