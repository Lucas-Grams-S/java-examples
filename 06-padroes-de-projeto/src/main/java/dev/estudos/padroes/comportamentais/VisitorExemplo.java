package dev.estudos.padroes.comportamentais;

import java.util.ArrayList;
import java.util.List;

/**
 * DP04-15 · Visitor: operações como {@code switch} exaustivo com record patterns sobre {@code sealed}.
 * Documento: java-docs/06-padroes-de-projeto/04-comportamentais.md §5.10.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * show -&gt; (1 + 2) * 4
 * eval -&gt; 12
 * </pre>
 *
 * <p>Variações: adicione {@code record Neg(Expr e)} a {@code permits} e leia o erro de compilação nos dois {@code switch}.
 */
public class VisitorExemplo {

    sealed interface Expr permits Num, Add, Mul {}
    record Num(int v) implements Expr {}
    record Add(Expr l, Expr r) implements Expr {}
    record Mul(Expr l, Expr r) implements Expr {}

    static int eval(Expr e) {
        return switch (e) {
            case Num n -> n.v();
            case Add(var l, var r) -> eval(l) + eval(r);
            case Mul(var l, var r) -> eval(l) * eval(r);
        };
    }

    static String show(Expr e) {
        return switch (e) {
            case Num n -> String.valueOf(n.v());
            case Add(var l, var r) -> show(l) + " + " + show(r);
            case Mul(var l, var r) -> wrap(l) + " * " + wrap(r);
        };
    }
    private static String wrap(Expr e) { return e instanceof Add ? "(" + show(e) + ")" : show(e); }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        Expr e = new Mul(new Add(new Num(1), new Num(2)), new Num(4));
        out.add("show -> " + show(e));
        out.add("eval -> " + eval(e));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
