package dev.estudos.java.testes;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** J15-12 · Given/When/Then com @Nested: o contexto "dado que" vira uma classe interna. */
@ExtendWith(MockitoExtension.class)
class OrderServiceNestedTest {
    @Mock OrderService.Repo repo;
    @Mock OrderService.Notifier notifier;
    OrderService service;

    @BeforeEach void criaServico() { service = new OrderService(repo, notifier); }

    @Nested @DisplayName("quando o pedido é novo")
    class WhenNew {
        @BeforeEach void given() { when(repo.exists("o1")).thenReturn(false); }
        @Test void thenSaves() { service.place("o1"); verify(repo).save("o1"); }
        @Test void thenNotifies() { service.place("o1"); verify(notifier).notify("o1"); }
    }

    @Nested @DisplayName("quando o pedido já existe")
    class WhenDuplicate {
        @BeforeEach void given() { when(repo.exists("o1")).thenReturn(true); }
        @Test void thenFails() { assertThatThrownBy(() -> service.place("o1")).isInstanceOf(IllegalStateException.class); }
        @Test void thenNoSave() {
            try { service.place("o1"); } catch (RuntimeException ignored) { }
            verify(repo, never()).save(any());
        }
    }
}
