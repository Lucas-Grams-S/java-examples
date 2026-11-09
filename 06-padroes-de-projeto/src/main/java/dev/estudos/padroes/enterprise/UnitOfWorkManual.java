package dev.estudos.padroes.enterprise;

import dev.estudos.padroes.enterprise.DominioPedidos.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DP05-14 · Unit of Work manual: um objeto novo não vira UPDATE.
 * Documento: java-docs/06-padroes-de-projeto/05-padroes-enterprise.md §5.5.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * commit: inserts=[10] updates=[11]
 * no repositório: [10, 11]
 * commit: inserts=[] updates=[]
 * </pre>
 */
public class UnitOfWorkManual {
    static class UnitOfWork {
        private final Map<Long, Order> news = new LinkedHashMap<>();
        private final Map<Long, Order> dirty = new LinkedHashMap<>();
        private final InMemoryOrderRepository repo;
        private final List<String> log;
        UnitOfWork(InMemoryOrderRepository r, List<String> log) { repo = r; this.log = log; }

        void registerNew(Order o)   { news.put(o.id(), o); }
        void registerDirty(Order o) { if (!news.containsKey(o.id())) dirty.put(o.id(), o); }

        void commit() {
            log.add("commit: inserts=" + news.keySet() + " updates=" + dirty.keySet());
            news.values().forEach(repo::save);
            dirty.values().forEach(repo::save);
            news.clear(); dirty.clear();
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var repo = new InMemoryOrderRepository();
        var c = new Customer(1, "Ana", false);
        var uow = new UnitOfWork(repo, out);
        var o10 = new Order(10, c);
        var o11 = new Order(11, c);
        uow.registerNew(o10);
        uow.registerDirty(o10);   // ignorado: já é novo
        uow.registerDirty(o11);
        uow.commit();
        out.add("no repositório: " + repo.findAll().stream().map(Order::id).toList());
        uow.commit();
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
