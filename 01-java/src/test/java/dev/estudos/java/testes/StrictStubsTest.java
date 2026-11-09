package dev.estudos.java.testes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** J15-06 (parte 2) · strict stubs: stub não usado derruba o teste com UnnecessaryStubbingException. */
class StrictStubsTest {
    @ExtendWith(MockitoExtension.class)
    static class Interno {
        @Mock OrderService.Repo repo;
        @Test void stubSemUso() { when(repo.exists("nunca-chamado")).thenReturn(true); }
    }

    @Test void j1506_strictStubs() {
        var listener = new SummaryGeneratingListener();
        var req = LauncherDiscoveryRequestBuilder.request().selectors(DiscoverySelectors.selectClass(Interno.class)).build();
        LauncherFactory.create().execute(req, listener);
        var falhas = listener.getSummary().getFailures();
        assertThat(falhas).hasSize(1);
        assertThat(falhas.get(0).getException().getClass().getSimpleName()).isEqualTo("UnnecessaryStubbingException");
    }
}
