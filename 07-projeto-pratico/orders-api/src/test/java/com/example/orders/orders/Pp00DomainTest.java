package com.example.orders.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.orders.shared.DomainException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** PP00-01: regras do domínio sem Spring (milissegundos). */
class Pp00DomainTest {
    static final Instant NOW = Instant.parse("2026-11-05T12:00:00Z");
    static OrderItem item(long p, int q, String price) { return new OrderItem(p, q, new BigDecimal(price)); }

    @Test void pedidoSemItens_ehInvalido() {
        assertThatThrownBy(() -> Order.create(1L, List.of(), NOW)).isInstanceOf(DomainException.class);
    }

    @Test void quantidadeTemQueSerPositiva() {
        assertThatThrownBy(() -> item(1, 0, "1.00")).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> item(1, -2, "1.00")).isInstanceOf(DomainException.class);
    }

    @Test void totalEhSomaDeQuantidadeVezesPreco_comBigDecimal() {
        var o = Order.create(1L, List.of(item(1, 3, "0.10"), item(2, 1, "0.20")), NOW);
        assertThat(o.total()).isEqualByComparingTo("0.50");
        assertThat(0.1 + 0.2).isNotEqualTo(0.3);   // com double dá 0.30000000000000004
    }

    /** Verificação do roteiro: sem cópia imutável, quem tem a lista altera o agregado por fora. */
    @Test void listaDeItens_ehCopiaImutavel() {
        var origem = new ArrayList<>(List.of(item(1, 1, "5.00")));
        var o = Order.create(1L, origem, NOW);
        origem.add(item(2, 1, "5.00"));
        assertThat(o.items()).hasSize(1);
        assertThatThrownBy(() -> o.items().add(item(3, 1, "1"))).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test void cancelar_novo_funcionaEDevolveNovoPedido() {
        var o = Order.create(1L, List.of(item(1, 1, "5.00")), NOW);
        var r = o.cancel();
        assertThat(r).isInstanceOf(CancelResult.Canceled.class);
        assertThat(((CancelResult.Canceled) r).order().status()).isEqualTo(OrderStatus.CANCELED);
        assertThat(o.status()).isEqualTo(OrderStatus.NEW);
    }

    @Test void cancelar_enviadoOuCancelado_ehRejeitado() {
        var enviado = Order.restore(1L, 1L, List.of(item(1, 1, "5.00")), OrderStatus.SHIPPED, NOW);
        assertThat(enviado.cancel()).isEqualTo(new CancelResult.Rejected("shipped order cannot be canceled"));
        var cancelado = Order.restore(1L, 1L, List.of(item(1, 1, "5.00")), OrderStatus.CANCELED, NOW);
        assertThat(cancelado.cancel()).isInstanceOf(CancelResult.Rejected.class);
    }
}
