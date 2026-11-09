package dev.estudos.java.excecoes;

import java.util.ArrayList;
import java.util.List;

/**
 * J08-15 · Resultado explícito (Result) como alternativa a exceções para falhas esperadas.
 * Documento: java-docs/01-java/08-excecoes.md §5.5.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * 42 -&gt; ok 42
 * x7 -&gt; erro não é número: x7
 * -1 -&gt; ok -1
 * </pre>
 *
 * <p>Variações: adicione um terceiro caso ao sealed e veja o switch deixar de compilar.
 */
public class ExplicitResult {
    sealed interface Result<T> {
        record Ok<T>(T v) implements Result<T> {}
        record Err<T>(String msg) implements Result<T> {}
    }

    static Result<Integer> parse(String s) {
        try { return new Result.Ok<>(Integer.parseInt(s)); }
        catch (NumberFormatException e) { return new Result.Err<>("não é número: " + s); }
    }

    static String show(Result<Integer> r) {
        return switch (r) {
            case Result.Ok<Integer>(var v) -> "ok " + v;
            case Result.Err<Integer>(var m) -> "erro " + m;
        };
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        for (String s : List.of("42", "x7", "-1")) out.add(s + " -> " + show(parse(s)));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
