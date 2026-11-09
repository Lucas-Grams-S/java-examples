package dev.estudos.java.generics;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * J06-14 · Resultado tipado com sealed e generics.
 * Documento: java-docs/01-java/06-generics.md §5.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * Ok.map(x -&gt; x * 2) -&gt; Ok[value=40]
 * Err.map(x -&gt; x * 2) -&gt; Err[message=falhou]
 * map muda o tipo -&gt; Ok[value=n=20]
 * </pre>
 *
 * <p>Variações: adicione {@code flatMap} seguindo o mesmo molde de {@code map}.
 */
public class GenericResult {
    public sealed interface Result<T> {
        record Ok<T>(T value) implements Result<T> {}
        record Err<T>(String message) implements Result<T> {}

        default <R> Result<R> map(Function<? super T, ? extends R> f) {
            return switch (this) {
                case Ok<T> ok -> new Ok<>(f.apply(ok.value()));
                case Err<T> err -> new Err<>(err.message());
            };
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        Result<Integer> ok = new Result.Ok<>(20);
        Result<Integer> err = new Result.Err<>("falhou");
        out.add("Ok.map(x -> x * 2) -> " + ok.map(x -> x * 2));
        out.add("Err.map(x -> x * 2) -> " + err.map(x -> x * 2));
        out.add("map muda o tipo -> " + ok.map(x -> "n=" + x));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
