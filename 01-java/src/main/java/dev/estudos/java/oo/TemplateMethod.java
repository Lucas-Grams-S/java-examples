package dev.estudos.java.oo;

import java.util.ArrayList;
import java.util.List;

/**
 * J04-13 · Template Method com classe abstrata.
 * Documento: java-docs/01-java/04-orientacao-a-objetos.md §5.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * == Relatório ==
 * vendas: 3
 * == fim ==
 * == Relatório ==
 * estoque: 10
 * -- estoque fim --
 * </pre>
 *
 * <p>Variações: sobrescreva {@code footer()} numa subclasse; tente sobrescrever {@code generate()} (não compila: é final).
 */
public class TemplateMethod {
    public abstract static class ReportGenerator {
        public final String generate() { return header() + body() + footer(); }
        protected String header() { return "== Relatório ==\n"; }
        protected abstract String body();
        protected String footer() { return "\n== fim =="; }
    }

    static class SalesReport extends ReportGenerator {
        @Override protected String body() { return "vendas: 3"; }
    }

    static class StockReport extends ReportGenerator {
        @Override protected String body() { return "estoque: 10"; }
        @Override protected String footer() { return "\n-- estoque fim --"; }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.addAll(new SalesReport().generate().lines().toList());
        out.addAll(new StockReport().generate().lines().toList());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
