package dev.estudos.padroes.estruturais;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DP03-08 · Composite: produto (folha) e pacote (composto) com a mesma interface, sobre {@code sealed} + {@code record}.
 * Documento: java-docs/06-padroes-de-projeto/03-estruturais.md §5.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * combo (200 - 10%) -&gt; 180
 * kit -&gt; 980 em 3 produtos
 * árvore -&gt; Kit escritório[Periféricos[mouse, teclado], monitor]
 * </pre>
 *
 * <p>Variações: troque o desconto do combo por {@code 0} e veja o total do kit subir de 980 para 1000.
 */
public class CompositeExemplo {

    sealed interface PricedItem permits Product, Bundle {
        BigDecimal price();
        default int count() { return 1; }
    }
    record Product(String name, BigDecimal price) implements PricedItem {}
    record Bundle(String name, List<PricedItem> children, BigDecimal discountPct) implements PricedItem {
        public BigDecimal price() {
            BigDecimal sum = children.stream().map(PricedItem::price).reduce(BigDecimal.ZERO, BigDecimal::add);
            return sum.multiply(BigDecimal.ONE.subtract(discountPct.movePointLeft(2)));
        }
        @Override public int count() { return children.stream().mapToInt(PricedItem::count).sum(); }
    }

    static String describe(PricedItem item) {                 // percorre a árvore com switch sobre os dois casos
        return switch (item) {
            case Product p -> p.name();
            case Bundle b -> b.name() + b.children().stream().map(CompositeExemplo::describe).toList();
        };
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var mouse = new Product("mouse", new BigDecimal("50"));
        var keyboard = new Product("teclado", new BigDecimal("150"));
        var monitor = new Product("monitor", new BigDecimal("800"));
        var combo = new Bundle("Periféricos", List.of(mouse, keyboard), new BigDecimal("10"));
        var kit = new Bundle("Kit escritório", List.of(combo, monitor), BigDecimal.ZERO);
        out.add("combo (200 - 10%) -> " + combo.price().stripTrailingZeros().toPlainString());
        out.add("kit -> " + kit.price().stripTrailingZeros().toPlainString() + " em " + kit.count() + " produtos");
        out.add("árvore -> " + describe(kit));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
