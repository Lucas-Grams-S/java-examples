package dev.estudos.java.concorrencia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * J12-13 · Produtor/consumidor com BlockingQueue limitada (backpressure) e mensagem sentinela "END".
 * Documento: java-docs/01-java/12-concorrencia.md §5.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * processando order-0
 * processando order-1
 * processando order-2
 * processando order-3
 * processando order-4
 * fila vazia no final -&gt; true
 * </pre>
 */
public class ProdutorConsumidor {
    public static List<String> run() throws Exception {
        List<String> processados = Collections.synchronizedList(new ArrayList<>());
        BlockingQueue<String> q = new ArrayBlockingQueue<>(2);   // capacidade pequena: put() bloqueia quando cheia
        Thread producer = Thread.ofPlatform().start(() -> {
            try { for (int i = 0; i < 5; i++) q.put("order-" + i); q.put("END"); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread consumer = Thread.ofPlatform().start(() -> {
            try { for (String m; !(m = q.take()).equals("END"); ) processados.add("processando " + m); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        producer.join();
        consumer.join();
        List<String> out = new ArrayList<>(processados);
        out.add("fila vazia no final -> " + q.isEmpty());
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
