package dev.estudos.padroes.intro;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos DP01-07 e DP01-08. */
class IntroTest {
    @Test void dp0107_solid() {
        assertThat(SolidPrincipios.run()).contains(
            "SRP: total=15.50, salvos=1, nota=<h1>pedido total=15.50</h1>",
            "OCP: sem desconto=100, 10%=90.00, BlackFriday=70.00",
            "LSP: Square no lugar de Rectangle, área esperada 10, obtida 4",
            "ISP: relatório de pedido-1",
            "DIP: CheckoutService só conhece DiscountPolicy -> 50.00");
    }

    @Test void dp0108_dry_kiss_yagni() {
        assertThat(DryKissYagni.run()).anyMatch(l -> l.contains("cliente CPF ok=true") && l.contains("fornecedor sem inscrição ok=false"));
        assertThat(DryKissYagni.validatePersonUnificado("12345678901", false, null)).isTrue();
    }
}
