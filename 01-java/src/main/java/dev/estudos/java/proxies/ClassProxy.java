package dev.estudos.java.proxies;

import java.util.ArrayList;
import java.util.List;

/**
 * J11-02 · Proxy de classe: a subclasse que um gerador (CGLIB/ByteBuddy) produziria, escrita à mão, e seus limites.
 * Documento: java-docs/01-java/11-proxies-dinamicos-e-bytecode.md §2.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * é subtipo da classe -&gt; true
 * getClass difere -&gt; true (OrderService$$Proxy)
 * place -&gt; placed 42 / audit
 * interceptados até aqui -&gt; [place, audit]
 * método final -&gt; interceptado? false
 * viaThis: chamada interna this.audit() também passa (é subclasse) -&gt; [viaThis, audit]
 * </pre>
 *
 * <p>Variações: torne place() final e veja o compilador impedir a sobrescrita (é o mesmo limite que o CGLIB tem).
 */
public class ClassProxy {
    interface Interceptor { Object intercept(String method, java.util.function.Supplier<Object> superCall); }

    static class OrderService {
        public String place(String id) { return "placed " + id + " / " + audit(); }
        public String audit() { return "audit"; }
        public final String finalMethod() { return "final"; }
        public String viaThis() { return "viaThis -> " + audit(); }
    }

    // o que o gerador produz (conceitualmente)
    static class OrderService$$Proxy extends OrderService {
        private final Interceptor interceptor; final List<String> seen;
        OrderService$$Proxy(Interceptor i, List<String> seen) { this.interceptor = i; this.seen = seen; }
        @Override public String place(String id) { return (String) interceptor.intercept("place", () -> super.place(id)); }
        @Override public String audit() { return (String) interceptor.intercept("audit", super::audit); }
        @Override public String viaThis() { return (String) interceptor.intercept("viaThis", super::viaThis); }
        // finalMethod não pode ser sobrescrito: não é interceptado
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        List<String> seen = new ArrayList<>();
        OrderService p = new OrderService$$Proxy((m, sup) -> { seen.add(m); return sup.get(); }, seen);
        out.add("é subtipo da classe -> " + (p instanceof OrderService));
        out.add("getClass difere -> " + (p.getClass() != OrderService.class) + " (" + p.getClass().getSimpleName() + ")");
        out.add("place -> " + p.place("42"));
        out.add("interceptados até aqui -> " + seen);
        seen.clear();
        p.finalMethod();
        out.add("método final -> interceptado? " + seen.contains("finalMethod"));
        seen.clear();
        p.viaThis();
        out.add("viaThis: chamada interna this.audit() também passa (é subclasse) -> " + seen);
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
