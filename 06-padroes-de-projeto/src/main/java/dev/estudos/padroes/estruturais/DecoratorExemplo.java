package dev.estudos.padroes.estruturais;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * DP03-09 · Decorator: ordem importa (110 vs 108), pilha de {@code java.io} e {@code unmodifiableList}.
 * Documento: java-docs/06-padroes-de-projeto/03-estruturais.md §5.4.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 *   price(100) = 110.00
 * desconto e depois frete -&gt; 110.00
 *   price(100) = 108.00
 * frete e depois desconto -&gt; 108.00
 * java.io, 3 decorators empilhados: 700 caracteres -&gt; 34 bytes comprimidos
 * unmodifiableList decora e bloqueia escrita -&gt; UnsupportedOperationException
 * </pre>
 *
 * <p>Variações: some um {@code Tax} na pilha e confira, com teste, em qual posição ele precisa ficar.
 */
public class DecoratorExemplo {

    interface PriceCalculator { BigDecimal price(BigDecimal base); }
    static class BasePrice implements PriceCalculator { public BigDecimal price(BigDecimal base) { return base; } }

    static abstract class PriceDecorator implements PriceCalculator {
        protected final PriceCalculator inner;
        protected PriceDecorator(PriceCalculator inner) { this.inner = inner; }
    }
    static class Discount extends PriceDecorator {
        private final BigDecimal pct;
        Discount(PriceCalculator in, String pct) { super(in); this.pct = new BigDecimal(pct); }
        public BigDecimal price(BigDecimal base) { return inner.price(base).multiply(BigDecimal.ONE.subtract(pct.movePointLeft(2))); }
    }
    static class Shipping extends PriceDecorator {
        private final BigDecimal fee;
        Shipping(PriceCalculator in, String fee) { super(in); this.fee = new BigDecimal(fee); }
        public BigDecimal price(BigDecimal base) { return inner.price(base).add(fee); }
    }
    static class Logging extends PriceDecorator {
        private final List<String> log;
        Logging(PriceCalculator in, List<String> log) { super(in); this.log = log; }
        public BigDecimal price(BigDecimal base) {
            BigDecimal r = inner.price(base);
            log.add("  price(" + base + ") = " + r);
            return r;
        }
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        var cem = new BigDecimal("100");
        out.add("desconto e depois frete -> " + new Logging(new Shipping(new Discount(new BasePrice(), "10"), "20"), out).price(cem));
        out.add("frete e depois desconto -> " + new Logging(new Discount(new Shipping(new BasePrice(), "20"), "10"), out).price(cem));

        var bytes = new ByteArrayOutputStream();
        try (var w = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(bytes)))) {
            w.write("pedido ".repeat(100));
        }
        out.add("java.io, 3 decorators empilhados: 700 caracteres -> " + bytes.size() + " bytes comprimidos");

        List<String> ro = Collections.unmodifiableList(new ArrayList<>(List.of("a")));
        try {
            ro.add("b");
        } catch (UnsupportedOperationException e) {
            out.add("unmodifiableList decora e bloqueia escrita -> UnsupportedOperationException");
        }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
