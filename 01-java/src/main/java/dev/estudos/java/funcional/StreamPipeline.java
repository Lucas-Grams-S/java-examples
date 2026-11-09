package dev.estudos.java.funcional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import dev.estudos.java.funcional.Domain.*;

/**
 * J09-04 · Pipeline de Stream: fonte, intermediárias preguiçosas e operação terminal.
 * Documento: java-docs/01-java/09-programacao-funcional.md §2.4.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * total pago -&gt; 245
 * antes do terminal -&gt; []
 * depois de findFirst -&gt; [peek 1, peek 2]
 * reuso -&gt; stream has already been operated upon or closed
 * IntStream stats -&gt; IntSummaryStatistics&#123;count=5, sum=15, min=1, average=3.000000, max=5&#125;
 * takeWhile/dropWhile -&gt; [1, 2] [3, 1]
 * </pre>
 *
 * <p>Variações: remova a operação terminal e veja que nada executa; reuse o stream e leia o IllegalStateException.
 */
public class StreamPipeline {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        BigDecimal paid = Domain.orders().stream()
            .filter(o -> o.status() == OrderStatus.PAID)
            .map(Order::total)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        out.add("total pago -> " + paid);

        List<String> trace = new ArrayList<>();
        Stream<Integer> lazy = Stream.of(1, 2, 3).peek(i -> trace.add("peek " + i)).filter(i -> i > 1);
        out.add("antes do terminal -> " + trace);
        lazy.findFirst();
        out.add("depois de findFirst -> " + trace);

        Stream<Integer> once = Stream.of(1, 2);
        once.count();
        try { once.count(); } catch (IllegalStateException e) { out.add("reuso -> " + e.getMessage()); }

        var stats = IntStream.rangeClosed(1, 5).summaryStatistics();
        out.add("IntStream stats -> " + stats);
        out.add("takeWhile/dropWhile -> " + Stream.of(1, 2, 3, 1).takeWhile(i -> i < 3).toList()
                + " " + Stream.of(1, 2, 3, 1).dropWhile(i -> i < 3).toList());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
