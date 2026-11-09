package dev.estudos.java.generics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * J06-12 · Repositório genérico no domínio de pedidos.
 * Documento: java-docs/01-java/06-generics.md §5.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * findById(1) -&gt; Optional[Customer[id=1, name=Ana]]
 * findById(9) -&gt; Optional.empty
 * findAll -&gt; [Customer[id=1, name=Ana], Customer[id=2, name=Bia]]
 * </pre>
 *
 * <p>Variações: crie um {@code InMemoryRepository<Order, UUID>} reaproveitando a mesma classe.
 */
public class GenericRepository {
    public interface Repository<T, ID> {
        Optional<T> findById(ID id);
        List<T> findAll();
        T save(T entity);
    }

    public static class InMemoryRepository<T, ID> implements Repository<T, ID> {
        private final Map<ID, T> store = new LinkedHashMap<>();
        private final Function<? super T, ? extends ID> idOf;

        public InMemoryRepository(Function<? super T, ? extends ID> idOf) { this.idOf = idOf; }
        public Optional<T> findById(ID id) { return Optional.ofNullable(store.get(id)); }
        public List<T> findAll() { return List.copyOf(store.values()); }
        public T save(T e) { store.put(idOf.apply(e), e); return e; }
    }

    record Customer(Long id, String name) {}

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var customers = new InMemoryRepository<Customer, Long>(Customer::id);
        customers.save(new Customer(1L, "Ana"));
        customers.save(new Customer(2L, "Bia"));
        out.add("findById(1) -> " + customers.findById(1L));
        out.add("findById(9) -> " + customers.findById(9L));
        out.add("findAll -> " + customers.findAll());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
