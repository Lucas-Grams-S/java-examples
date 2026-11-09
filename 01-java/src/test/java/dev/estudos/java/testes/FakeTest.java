package dev.estudos.java.testes;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** J15-13 · Fake em vez de mock: testa ESTADO (resultado observável), não interação. */
class FakeTest {
    static class InMemoryRepo implements OrderService.Repo {
        private final Set<String> ids = new HashSet<>();
        public void save(String id) { ids.add(id); }
        public boolean exists(String id) { return ids.contains(id); }
    }

    @Test void secondPlaceFails() {
        var svc = new OrderService(new InMemoryRepo(), id -> { });
        svc.place("o1");
        assertThatThrownBy(() -> svc.place("o1")).isInstanceOf(IllegalStateException.class);
    }

    @Test void notificaSoUmaVez() {
        List<String> enviados = new ArrayList<>();
        var repo = new InMemoryRepo();
        var svc = new OrderService(repo, enviados::add);
        svc.place("a");
        try { svc.place("a"); } catch (IllegalStateException ignored) { }
        assertThat(enviados).containsExactly("a");   // estado final, sem verify(): sobrevive a refatorações
        assertThat(repo.exists("a")).isTrue();
    }
}
