package dev.estudos.padroes.comportamentais;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * DP04-06 · Chain of Responsibility: cadeia que para no primeiro tratador e variante "pipeline" com {@code next}.
 * Documento: java-docs/06-padroes-de-projeto/04-comportamentais.md §5.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * total 50, 2 itens -&gt; aprovado
 * total 5000 -&gt; revisão manual
 * total 0, 0 itens -&gt; rejeitado: vazio
 * pipeline com next -&gt; [log core(ABC) log]
 * </pre>
 *
 * <p>Variações: inverta a ordem das regras e veja qual rejeição aparece para o pedido vazio.
 */
public class ChainOfResponsibilityExemplo {

    record OrderReq(String customer, BigDecimal total, int items) {}

    static final List<Function<OrderReq, Optional<String>>> CHAIN = List.of(
        r -> r.items() == 0 ? Optional.of("rejeitado: vazio") : Optional.empty(),
        r -> r.total().signum() <= 0 ? Optional.of("rejeitado: total") : Optional.empty(),
        r -> r.total().compareTo(new BigDecimal("1000")) > 0 ? Optional.of("revisão manual") : Optional.empty());

    static String avaliar(OrderReq req) {
        return CHAIN.stream().map(f -> f.apply(req)).flatMap(Optional::stream).findFirst().orElse("aprovado");
    }

    interface Step { String call(String in, Function<String, String> next); }

    static Function<String, String> montar(List<Step> steps, Function<String, String> core) {
        Function<String, String> f = core;
        for (int i = steps.size() - 1; i >= 0; i--) {              // montada de trás para frente
            Step s = steps.get(i);
            Function<String, String> next = f;
            f = in -> s.call(in, next);
        }
        return f;
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("total 50, 2 itens -> " + avaliar(new OrderReq("ana", new BigDecimal("50"), 2)));
        out.add("total 5000 -> " + avaliar(new OrderReq("ana", new BigDecimal("5000"), 2)));
        out.add("total 0, 0 itens -> " + avaliar(new OrderReq("ana", BigDecimal.ZERO, 0)));

        var pipeline = montar(List.of(
            (in, next) -> "[log " + next.apply(in) + " log]",
            (in, next) -> next.apply(in.toUpperCase())),
            s -> "core(" + s + ")");
        out.add("pipeline com next -> " + pipeline.apply("abc"));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
