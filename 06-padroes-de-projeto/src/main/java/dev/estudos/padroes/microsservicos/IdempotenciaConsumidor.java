package dev.estudos.padroes.microsservicos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DP07-15 · O lado do consumidor: idempotência (inbox) diante de entrega at-least-once.
 * Documento: java-docs/06-padroes-de-projeto/07-padroes-de-microsservicos.md §5.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * com inbox (chave única no id da mensagem):
 *   msg 1:pedido-1 processada
 *   msg 2:pedido-2 processada
 *   msg 2:pedido-2 DUPLICADA, ignorada
 *   msg 3:pedido-3 processada
 * sem inbox, efeito aplicado 4 vezes (2 duplicou)
 * </pre>
 */
public class IdempotenciaConsumidor {
    static void consume(List<String> broker, Set<Long> processed, List<String> out) {
        for (String s : broker) {
            long id = Long.parseLong(s.split(":")[0]);
            out.add("  msg " + s + (processed.add(id) ? " processada" : " DUPLICADA, ignorada"));
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        // o relay "caiu" depois de enviar 2 e, na repetição, reenviou
        var broker = List.of("1:pedido-1", "2:pedido-2", "2:pedido-2", "3:pedido-3");
        out.add("com inbox (chave única no id da mensagem):");
        consume(broker, new HashSet<>(), out);
        out.add("sem inbox, efeito aplicado " + broker.size() + " vezes (2 duplicou)");
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
