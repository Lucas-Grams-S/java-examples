package dev.estudos.padroes.microsservicos;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos DP07-13 a DP07-16. */
class MicrosservicosTest {
    @Test void dp0713_saga() {
        assertThat(SagaOrquestrada.run()).containsSubsequence(
            "saga falha no pagamento:", "  + reservar estoque", "  ! cobrar falhou -> compensando",
            "  - desfaz reservar estoque", "false")
            .containsSubsequence("saga falha na entrega:", "  ! agendar entrega falhou -> compensando",
                "  - desfaz cobrar", "  - desfaz reservar estoque")
            .contains("true");
    }

    @Test void dp0714_outbox() {
        assertThat(OutboxTransacional.run()).containsExactly("erro: falha antes do commit",
            "orders=[ana]", "outbox=[ana]", "publicando 1", "pendentes=0");
    }

    @Test void dp0715_idempotencia() {
        assertThat(IdempotenciaConsumidor.run()).containsSubsequence("  msg 2:pedido-2 processada",
            "  msg 2:pedido-2 DUPLICADA, ignorada");
    }

    @Test void dp0716_stranglerFig() {
        assertThat(StranglerFigRotas.run()).contains("rotas = [orders-new, legacy]",
            "/orders/42 -> orders-new (lb://orders)", "/customers/7 -> legacy (http://legacy-monolith:8080)",
            "ordem invertida: /orders/42 -> legacy (o legado engole tudo)");
    }
}
