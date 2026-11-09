package dev.estudos.padroes.comportamentais;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * DP04-11 · Observer: barramento próprio, {@code PropertyChangeSupport} e eventos síncronos do Spring.
 * Documento: java-docs/06-padroes-de-projeto/04-comportamentais.md §5.6.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * A recebeu [pedido-1], B recebeu [pedido-1, pedido-2]
 * PropertyChangeSupport -&gt; status NEW-&gt;PAID
 * Spring: ouvinte executou antes de publishEvent retornar -&gt; [p-9 thread=(do publicador)]
 * Spring: mesma thread do publicador -&gt; true
 * </pre>
 *
 * <p>Variações: anote o ouvinte com {@code @Async} (e {@code @EnableAsync}) e veja a thread mudar.
 */
public class ObserverExemplo {

    interface Listener<E> { void on(E e); }
    static class Bus<E> {
        final List<Listener<E>> ls = new ArrayList<>();
        Runnable subscribe(Listener<E> l) { ls.add(l); return () -> ls.remove(l); }
        void publish(E e) { new ArrayList<>(ls).forEach(l -> l.on(e)); }
    }

    record OrderPaid(String id) {}
    static final List<String> SPRING_LOG = new ArrayList<>();
    @Component static class PaidListener {
        @EventListener void on(OrderPaid e) { SPRING_LOG.add(e.id() + " thread=" + Thread.currentThread().getName()); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var bus = new Bus<String>();
        var a = new ArrayList<String>();
        var b = new ArrayList<String>();
        Runnable cancelA = bus.subscribe(a::add);
        bus.subscribe(b::add);
        bus.publish("pedido-1");
        cancelA.run();
        bus.publish("pedido-2");
        out.add("A recebeu " + a + ", B recebeu " + b);

        var pcs = new PropertyChangeSupport(new Object());
        pcs.addPropertyChangeListener(ev -> out.add("PropertyChangeSupport -> " + ev.getPropertyName() + " " + ev.getOldValue() + "->" + ev.getNewValue()));
        pcs.firePropertyChange("status", "NEW", "PAID");

        SPRING_LOG.clear();
        try (var ctx = new AnnotationConfigApplicationContext(PaidListener.class)) {
            ctx.publishEvent(new OrderPaid("p-9"));
            out.add("Spring: ouvinte executou antes de publishEvent retornar -> " + SPRING_LOG
                    .stream().map(s -> s.replaceFirst("thread=.*", "thread=(do publicador)")).toList());
            out.add("Spring: mesma thread do publicador -> " + SPRING_LOG.get(0).endsWith(Thread.currentThread().getName()));
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
