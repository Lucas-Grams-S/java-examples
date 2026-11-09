package dev.estudos.java.concorrencia;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * J12-14 · Realista: carregar pedido e cliente em paralelo (CompletableFuture + executor de I/O + orTimeout),
 * e a mesma coisa em estilo síncrono com virtual threads.
 * Documento: java-docs/01-java/12-concorrencia.md §5.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * resultado -&gt; OrderView[order=Order[id=1], customer=Customer[id=9, nome=ana]]
 * em paralelo: ~200ms e não ~400ms -&gt; true
 * estouro de prazo -&gt; CompletionException(TimeoutException)
 * com virtual threads -&gt; OrderView[order=Order[id=1], customer=Customer[id=9, nome=ana]]
 * </pre>
 */
public class ChamadasEmParalelo {
    record Order(long id) {}
    record Customer(long id, String nome) {}
    record OrderView(Order order, Customer customer) {}

    static Order findOrder(long id, long delayMs) { dorme(delayMs); return new Order(id); }
    static Customer findCustomer(long id, long delayMs) { dorme(delayMs); return new Customer(id, "ana"); }
    static void dorme(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }

    static OrderView load(long orderId, long customerId, ExecutorService io, long delayMs, long timeoutMs) {
        var o = CompletableFuture.supplyAsync(() -> findOrder(orderId, delayMs), io);
        var c = CompletableFuture.supplyAsync(() -> findCustomer(customerId, delayMs), io);
        return o.thenCombine(c, OrderView::new).orTimeout(timeoutMs, TimeUnit.MILLISECONDS).join();
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        try (ExecutorService io = Executors.newFixedThreadPool(2)) {
            long ini = System.nanoTime();
            OrderView v = load(1, 9, io, 200, 2_000);
            long ms = (System.nanoTime() - ini) / 1_000_000;
            out.add("resultado -> " + v);
            out.add("em paralelo: ~200ms e não ~400ms -> " + (ms < 390));
            try { load(1, 9, io, 500, 50); }
            catch (CompletionException e) { out.add("estouro de prazo -> CompletionException(" + e.getCause().getClass().getSimpleName() + ")"); }
        }
        // Mesmo resultado com código síncrono em virtual threads
        try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<Order> o = ex.submit(() -> findOrder(1, 100));
            Future<Customer> c = ex.submit(() -> findCustomer(9, 100));
            out.add("com virtual threads -> " + new OrderView(o.get(), c.get()));
        }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
