package dev.estudos.java.colecoes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.PriorityQueue;

/**
 * J07-13 · Fila, pilha e prioridade.
 * Documento: java-docs/01-java/07-colecoes.md §5.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * pilha: pop -&gt; 2 (LIFO)
 * fila: poll -&gt; a (FIFO)
 * prioridade (reverseOrder): poll -&gt; 9
 * prioridade (ordem natural): poll -&gt; 1
 * ArrayDeque não aceita null -&gt; NullPointerException
 * </pre>
 *
 * <p>Variações: troque {@code reverseOrder()} por ordem natural e veja {@code poll()} devolver o menor.
 */
public class QueuesAndStacks {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(1);
        stack.push(2);
        out.add("pilha: pop -> " + stack.pop() + " (LIFO)");

        Deque<String> fila = new ArrayDeque<>();
        fila.offer("a");
        fila.offer("b");
        out.add("fila: poll -> " + fila.poll() + " (FIFO)");

        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.reverseOrder());
        pq.addAll(List.of(3, 9, 1));
        out.add("prioridade (reverseOrder): poll -> " + pq.poll());
        PriorityQueue<Integer> menor = new PriorityQueue<>(List.of(3, 9, 1));
        out.add("prioridade (ordem natural): poll -> " + menor.poll());
        try {
            new ArrayDeque<String>().add(null);
        } catch (NullPointerException e) {
            out.add("ArrayDeque não aceita null -> NullPointerException");
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
