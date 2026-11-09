package dev.estudos.padroes.arquiteturais;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos DP06-13 a DP06-16. */
class ArquiteturaisTest {
    @Test void dp0613_hexagonal() {
        assertThat(HexagonalJavaPuro.run()).contains("cobrando 50", "pago? true total=50",
            "Money negativo -> negativo", "pedido vazio -> pedido vazio");
    }

    @Test void dp0614_cqrs() {
        assertThat(CqrsReadModel.run()).contains(
            "após Placed(1, 50) -> {1=OrderSummary[id=1, total=50, paid=false]}",
            "Paid de pedido desconhecido não cria nada -> [1, 2]");
    }

    @Test void dp0615_eventSourcing() {
        assertThat(EventSourcingDobra.run()).contains("eventos no log = 3",
            "replay(1) -> OrderState[id=1, total=50, paid=true]",
            "replay(2) -> OrderState[id=2, total=9, paid=false]",
            "assinante viu -> [Placed, Placed, Paid]");
    }

    @Test void dp0616_dddEventos() {
        assertThat(DddTaticoEventos.run()).containsExactly(
            "pay() sem save (dirty checking) -> listener recebeu []",
            "pay() + save -> listener recebeu [OrderPaid(7)]",
            "segundo save (eventos limpos) -> [OrderPaid(7)]");
    }
}
