package dev.estudos.java.proxies;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * J11-07 · Proxy genérico de log reutilizável (wrap por interface).
 * Documento: java-docs/01-java/11-proxies-dinamicos-e-bytecode.md §5.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * &gt;&gt; place
 * resultado placed 7
 * &gt;&gt; send
 * !! IllegalStateException
 * propagou smtp fora
 * </pre>
 *
 * <p>Variações: faça wrap de outra interface (ex.: Runnable) com o mesmo método wrap.
 */
public class LoggingProxy {
    private LoggingProxy() {}

    @SuppressWarnings("unchecked")
    public static <T> T wrap(T target, Class<T> iface, List<String> log) {
        InvocationHandler h = (proxy, m, args) -> {
            log.add(">> " + m.getName());
            try {
                return m.invoke(target, args);
            } catch (InvocationTargetException e) {
                log.add("!! " + e.getCause().getClass().getSimpleName());
                throw e.getCause();
            }
        };
        return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[]{iface}, h);
    }

    public interface OrderService { String place(String id); }
    public interface Mailer { void send(String to) throws Exception; }

    public static List<String> run() {
        List<String> log = new ArrayList<>();
        OrderService s = wrap(id -> "placed " + id, OrderService.class, log);
        log.add("resultado " + s.place("7"));
        Mailer m = wrap(to -> { throw new IllegalStateException("smtp fora"); }, Mailer.class, log);
        try { m.send("a@x"); } catch (Exception e) { log.add("propagou " + e.getMessage()); }
        return log;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
