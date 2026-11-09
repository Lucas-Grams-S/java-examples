package dev.estudos.padroes.comportamentais;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * DP04-07 · Command: ações como objetos, com histórico e {@code undo}; sem undo é só um {@code Runnable}.
 * Documento: java-docs/06-padroes-de-projeto/04-comportamentais.md §5.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * após 3 execuções -&gt; [a, b, c]
 * após 2 undo -&gt; [a]
 * Callable entregue ao executor roda em outra thread -&gt; true
 * </pre>
 *
 * <p>Variações: faça o comando {@code RemoveItem} guardar a posição original para o undo restaurá-la.
 */
public class CommandExemplo {

    static class Cart { final List<String> items = new ArrayList<>(); }
    interface Command { void execute(); void undo(); }
    record AddItem(Cart cart, String item) implements Command {
        public void execute() { cart.items.add(item); }
        public void undo() { cart.items.remove(item); }
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        var cart = new Cart();
        Deque<Command> history = new ArrayDeque<>();
        for (String i : List.of("a", "b", "c")) { Command c = new AddItem(cart, i); c.execute(); history.push(c); }
        out.add("após 3 execuções -> " + cart.items);
        history.pop().undo(); history.pop().undo();
        out.add("após 2 undo -> " + cart.items);

        try (var exec = Executors.newSingleThreadExecutor()) {       // Command sem undo: Runnable/Callable
            Thread caller = Thread.currentThread();
            var f = exec.submit(() -> Thread.currentThread() != caller);
            out.add("Callable entregue ao executor roda em outra thread -> " + f.get());
        }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
