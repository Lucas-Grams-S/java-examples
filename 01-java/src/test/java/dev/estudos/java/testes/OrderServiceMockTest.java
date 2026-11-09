package dev.estudos.java.testes;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** J15-06 · Mockito: @Mock, @InjectMocks, stubbing, verify, captor, never, strict stubs, spy. */
@ExtendWith(MockitoExtension.class)
class OrderServiceMockTest {
    @Mock OrderService.Repo repo;
    @Mock OrderService.Notifier notifier;
    @InjectMocks OrderService service;
    @Captor ArgumentCaptor<String> captor;

    @Test void j1506_placesAndNotifies() {
        when(repo.exists("o1")).thenReturn(false);
        service.place("o1");
        verify(repo).save("o1");
        verify(notifier).notify(captor.capture());
        assertThat(captor.getValue()).isEqualTo("o1");
    }

    @Test void j1506_rejectsDuplicate() {
        when(repo.exists("o1")).thenReturn(true);
        assertThatThrownBy(() -> service.place("o1")).isInstanceOf(IllegalStateException.class).hasMessageContaining("o1");
        verify(repo, never()).save(any());
        verifyNoInteractions(notifier);
    }

    @Test void j1506_mockDevolveValoresPadrao() {
        assertThat(repo.exists("qualquer")).isFalse();   // mock sem stub: false/0/null/vazio
    }

    @Test void j1506_stubComDoThrow() {
        doThrow(new IllegalArgumentException("sem rede")).when(notifier).notify("o9");
        assertThatThrownBy(() -> service.place("o9")).isInstanceOf(IllegalArgumentException.class);
        verify(repo).save("o9");   // salvou antes de notificar falhar
    }

    @Test void j1506_matchers() {
        when(repo.exists(anyString())).thenReturn(true);
        assertThatThrownBy(() -> service.place("x")).isInstanceOf(IllegalStateException.class);
        verify(repo).exists(argThat(s -> s.startsWith("x")));
    }

    @Test void j1506_spyEhObjetoRealParcialmenteInterceptado() {
        var real = new java.util.ArrayList<String>();
        var spy = spy(real);
        spy.add("a");
        doReturn(99).when(spy).size();
        assertThat(spy.get(0)).isEqualTo("a");   // método real
        assertThat(spy.size()).isEqualTo(99);    // método com stub
        verify(spy).add("a");
    }
}
