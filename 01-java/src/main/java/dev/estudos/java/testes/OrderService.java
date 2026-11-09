package dev.estudos.java.testes;

/** Código sob teste (J15-06, J15-12, J15-13): serviço com duas dependências, para mocks e fakes. */
public class OrderService {
    public interface Repo { void save(String id); boolean exists(String id); }
    public interface Notifier { void notify(String id); }

    private final Repo repo;
    private final Notifier notifier;

    public OrderService(Repo repo, Notifier notifier) { this.repo = repo; this.notifier = notifier; }

    public void place(String id) {
        if (repo.exists(id)) throw new IllegalStateException("duplicado: " + id);
        repo.save(id);
        notifier.notify(id);
    }
}
