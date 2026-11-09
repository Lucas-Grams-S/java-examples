package dev.estudos.java.excecoes;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * J08-11 · Traduzindo exceções entre camadas (infraestrutura -> domínio) preservando a causa.
 * Documento: java-docs/01-java/08-excecoes.md §5.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * domínio -&gt; Order 9 not found
 * causa -&gt; SQLException: no rows for id=9
 * SQLState preservado -&gt; 02000
 * </pre>
 *
 * <p>Variações: troque new OrderNotFoundException(id, e) por (id) e veja a causa sumir.
 */
public class LayerTranslation {
    static class OrderNotFoundException extends RuntimeException {
        OrderNotFoundException(long id, Throwable cause) { super("Order %d not found".formatted(id), cause); }
    }

    static class FakeJdbc {
        String query(long id) throws SQLException { throw new SQLException("no rows for id=" + id, "02000"); }
    }

    static String findOrder(FakeJdbc jdbc, long id) {
        try {
            return jdbc.query(id);
        } catch (SQLException e) {
            throw new OrderNotFoundException(id, e);
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try { findOrder(new FakeJdbc(), 9); } catch (OrderNotFoundException e) {
            out.add("domínio -> " + e.getMessage());
            out.add("causa -> " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage());
            out.add("SQLState preservado -> " + ((SQLException) e.getCause()).getSQLState());
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
