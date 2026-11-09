package dev.estudos.java.modernos;

import java.util.ArrayList;
import java.util.List;

/**
 * J05-12 · Resultado tipado (tipo soma) sem exceções para fluxo de negócio.
 * Documento: java-docs/01-java/05-recursos-modernos-da-linguagem.md §5.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * pedido Order[items=[caneta]]
 * falhou: carrinho vazio
 * </pre>
 *
 * <p>Variações: remova o caso {@code Err} do {@code switch} e veja o erro de exaustividade.
 */
public class ResultType {
    public sealed interface Result<T> permits Result.Ok, Result.Err {
        record Ok<T>(T value) implements Result<T> {}
        record Err<T>(String reason) implements Result<T> {}
    }

    record Cart(List<String> items) {
        boolean isEmpty() { return items.isEmpty(); }
    }

    record Order(List<String> items) {
        static Order from(Cart cart) { return new Order(cart.items()); }
    }

    static Result<Order> place(Cart cart) {
        return cart.isEmpty() ? new Result.Err<>("carrinho vazio") : new Result.Ok<>(Order.from(cart));
    }

    static String message(Cart cart) {
        return switch (place(cart)) {
            case Result.Ok<Order>(var order) -> "pedido " + order;
            case Result.Err<Order>(var why) -> "falhou: " + why;
        };
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add(message(new Cart(List.of("caneta"))));
        out.add(message(new Cart(List.of())));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
