package dev.estudos.padroes.microsservicos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DP07-13 · Saga orquestrada: executa os passos e, na primeira falha, compensa os concluídos em ordem inversa.
 * Documento: java-docs/06-padroes-de-projeto/07-padroes-de-microsservicos.md §5.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * saga ok:
 *   + reservar estoque
 *   + cobrar
 *   + agendar entrega
 * true
 * saga falha no pagamento:
 *   + reservar estoque
 *   ! cobrar falhou -&gt; compensando
 *   - desfaz reservar estoque
 * false
 * saga falha na entrega:
 *   + reservar estoque
 *   + cobrar
 *   ! agendar entrega falhou -&gt; compensando
 *   - desfaz cobrar
 *   - desfaz reservar estoque
 * false
 * </pre>
 */
public class SagaOrquestrada {
    interface Step {
        String name();
        void execute(Map<String, Object> ctx);
        void compensate(Map<String, Object> ctx);
    }

    static Step step(List<String> log, String name, boolean fails) {
        return new Step() {
            public String name() { return name; }
            public void execute(Map<String, Object> ctx) {
                if (fails) throw new IllegalStateException(name + " falhou");
                log.add("  + " + name);
            }
            public void compensate(Map<String, Object> ctx) { log.add("  - desfaz " + name); }
        };
    }

    static boolean runSaga(List<Step> steps, List<String> log) {
        var ctx = new HashMap<String, Object>();
        var done = new ArrayDeque<Step>();            // pilha: o último concluído é o primeiro a compensar
        for (Step s : steps) {
            try { s.execute(ctx); done.push(s); }
            catch (RuntimeException e) {
                log.add("  ! " + e.getMessage() + " -> compensando");
                while (!done.isEmpty()) done.pop().compensate(ctx);
                return false;
            }
        }
        return true;
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("saga ok:");
        out.add(String.valueOf(runSaga(List.of(step(out, "reservar estoque", false), step(out, "cobrar", false),
            step(out, "agendar entrega", false)), out)));
        out.add("saga falha no pagamento:");
        out.add(String.valueOf(runSaga(List.of(step(out, "reservar estoque", false), step(out, "cobrar", true),
            step(out, "agendar entrega", false)), out)));
        out.add("saga falha na entrega:");
        out.add(String.valueOf(runSaga(List.of(step(out, "reservar estoque", false), step(out, "cobrar", false),
            step(out, "agendar entrega", true)), out)));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
