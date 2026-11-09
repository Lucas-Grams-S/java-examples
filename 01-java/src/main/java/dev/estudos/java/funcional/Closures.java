package dev.estudos.java.funcional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * J09-03 · Captura de variáveis, closures e o significado de this em lambda vs classe anônima.
 * Documento: java-docs/01-java/09-programacao-funcional.md §2.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * addBase(5) -&gt; 15
 * soma via array -&gt; 6
 * this em lambda -&gt; Closures
 * this em classe anônima -&gt; instância anônima
 * sem captura: mesma instância a cada avaliação -&gt; true
 * com captura: nova instância a cada avaliação -&gt; true
 * </pre>
 *
 * <p>Variações: tente alterar 'base' depois da lambda e leia o erro do compilador.
 */
public class Closures {
    private final String name = "Closures";

    String lambdaThis() {
        Supplier<String> s = () -> this.name;               // this = classe envolvente
        return s.get();
    }

    @SuppressWarnings("Convert2Lambda")
    String anonymousThis() {
        Supplier<String> s = new Supplier<>() {
            @Override public String get() { return this.getClass().getName().contains("$") ? "instância anônima" : "?"; }
        };
        return s.get();
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        int base = 10;                                       // efetivamente final
        Function<Integer, Integer> addBase = x -> x + base;
        out.add("addBase(5) -> " + addBase.apply(5));
        int[] cnt = {0};                                     // contorno: array mutável (sinal de que reduce seria melhor)
        List.of(1, 2, 3).forEach(i -> cnt[0] += i);
        out.add("soma via array -> " + cnt[0]);
        Closures c = new Closures();
        out.add("this em lambda -> " + c.lambdaThis());
        out.add("this em classe anônima -> " + c.anonymousThis());
        out.add("sem captura: mesma instância a cada avaliação -> " + (semCaptura() == semCaptura()));
        out.add("com captura: nova instância a cada avaliação -> " + (comCaptura(1) != comCaptura(1)));
        return out;
    }

    static Supplier<String> semCaptura() { return () -> "x"; }
    static Supplier<Integer> comCaptura(int n) { return () -> n; }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
