package dev.estudos.padroes.arquiteturais;

import java.math.BigDecimal;

/** Eventos compartilhados por DP06-14 (CQRS) e DP06-15 (Event Sourcing); infraestrutura, não é um exemplo. */
public sealed interface EventosDoPedido {
    record Placed(long id, BigDecimal total) implements EventosDoPedido {}
    record Paid(long id) implements EventosDoPedido {}
}
