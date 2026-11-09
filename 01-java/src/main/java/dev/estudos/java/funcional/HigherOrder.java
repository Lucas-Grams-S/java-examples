package dev.estudos.java.funcional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import dev.estudos.java.funcional.Domain.*;

/**
 * J09-13 · Funções de ordem superior: currying, composição andThen/compose e Predicate combinado.
 * Documento: java-docs/01-java/09-programacao-funcional.md §5.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * add(2)(3) -&gt; 5
 * inc.andThen(dbl)(3) -&gt; 8
 * inc.compose(dbl)(3) -&gt; 7
 * depois do removeIf -&gt; [1, 3, 4, 5]
 * caros -&gt; [1, 3]
 * </pre>
 *
 * <p>Variações: troque andThen por compose e compare os resultados.
 */
public class HigherOrder {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        Function<Integer, Function<Integer, Integer>> add = x -> y -> x + y;
        out.add("add(2)(3) -> " + add.apply(2).apply(3));
        Function<Integer, Integer> inc = x -> x + 1, dbl = x -> x * 2;
        out.add("inc.andThen(dbl)(3) -> " + inc.andThen(dbl).apply(3));
        out.add("inc.compose(dbl)(3) -> " + inc.compose(dbl).apply(3));
        Predicate<Order> expensive = o -> o.total().compareTo(new BigDecimal("100")) > 0;
        List<Order> orders = new ArrayList<>(Domain.orders());
        orders.removeIf(expensive.negate().and(o -> o.status() == OrderStatus.CREATED));
        out.add("depois do removeIf -> " + orders.stream().map(Order::id).toList());
        out.add("caros -> " + orders.stream().filter(expensive).map(Order::id).toList());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
