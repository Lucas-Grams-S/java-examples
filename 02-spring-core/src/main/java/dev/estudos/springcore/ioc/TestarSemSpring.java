package dev.estudos.springcore.ioc;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * SC02-12 · Testar sem Spring: a vantagem do construtor. Com construtor, {@code new OrderService(repo)}
 * basta; com injeção em campo, sem o contêiner o campo fica {@code null} (NPE) e só reflection resolve.
 * (O código de teste usa um laço manual, sem {@code spring-test}, para manter o módulo enxuto.)
 * Documento: 02-spring-core/02-ioc-e-injecao-de-dependencia.md §5.4.
 *
 * <p>Saída real:
 * <pre>
 * construtor: repo.findAll().size() -&gt; 1
 * campo sem contêiner -&gt; NullPointerException
 * campo com reflection (setField manual) -&gt; 1
 * </pre>
 */
public class TestarSemSpring {
    static class InMemoryOrderRepository {
        final List<String> data = new ArrayList<>();
        List<String> findAll() { return data; }
    }

    static class ComConstrutor {
        private final InMemoryOrderRepository repo;
        ComConstrutor(InMemoryOrderRepository repo) { this.repo = repo; }
        void place(String order) { repo.data.add(order); }
    }

    static class ComCampo {
        @Autowired private InMemoryOrderRepository repo;
        void place(String order) { repo.data.add(order); }
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        var repo = new InMemoryOrderRepository();
        new ComConstrutor(repo).place("p-1");                       // sem contêiner, sem reflection
        out.add("construtor: repo.findAll().size() -> " + repo.findAll().size());

        try { new ComCampo().place("p-1"); }
        catch (NullPointerException e) { out.add("campo sem contêiner -> NullPointerException"); }

        var repo2 = new InMemoryOrderRepository();
        var comCampo = new ComCampo();
        var f = ComCampo.class.getDeclaredField("repo");
        f.setAccessible(true);
        f.set(comCampo, repo2);                                     // o que o contêiner faria por você
        comCampo.place("p-1");
        out.add("campo com reflection (setField manual) -> " + repo2.findAll().size());
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
