package dev.estudos.padroes.estruturais;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos DP03-06 a DP03-12. */
class EstruturaisTest {
    @Test void dp0306_adapter() throws Exception {
        assertThat(AdapterExemplo.run()).contains(
            "charge(c-1, 49.90) -> PaymentResult[approved=true, transactionId=TX-0001]",
            "charge(c-1, 2000.00) -> PaymentResult[approved=false, transactionId=null]",
            "fração de centavo -> ArithmeticException (longValueExact)",
            "InputStreamReader adapta bytes a chars -> olá",
            "Arrays.asList escreve através do array -> arr[0]=z");
    }

    @Test void dp0307_bridge() {
        assertThat(BridgeExemplo.run()).contains(
            "email -> ana@x.com: Pedido 1 confirmado. Obrigado pela compra!",
            "sms   -> +5511999: Pedido 1 confirmado.",
            "sms   -> +5511999: Pedido 1 saiu para e");
    }

    @Test void dp0308_composite() {
        assertThat(CompositeExemplo.run()).contains("combo (200 - 10%) -> 180", "kit -> 980 em 3 produtos",
            "árvore -> Kit escritório[Periféricos[mouse, teclado], monitor]");
    }

    @Test void dp0309_decorator() throws Exception {
        assertThat(DecoratorExemplo.run()).contains(
            "desconto e depois frete -> 110.00", "frete e depois desconto -> 108.00",
            "java.io, 3 decorators empilhados: 700 caracteres -> 34 bytes comprimidos",
            "unmodifiableList decora e bloqueia escrita -> UnsupportedOperationException");
    }

    @Test void dp0310_facade() throws Exception {
        var out = FacadeExemplo.run();
        assertThat(out).containsSubsequence("estoque: reserva 2x SKU-1", "pagamento: ana 30.00", "envio: agendado para ana",
            "e-mail: ana TRACK-9", "checkout -> TRACK-9");
        assertThat(out).contains("sem estoque -> sem estoque (pagamento, envio e e-mail não foram chamados)",
            "JDK: Files.readString esconde canais e charset -> olá");
        assertThat(out.stream().filter(l -> l.startsWith("pagamento")).count()).isEqualTo(1);
    }

    @Test void dp0311_flyweight() {
        assertThat(FlyweightExemplo.run()).contains(
            "100.000 linhas criadas -> regras distintas no pool: 2",
            "lines[0].rule() == lines[2].rule() -> true", "lines[0].rule() == lines[1].rule() -> false",
            "Integer 127 == 127 -> true; Integer 128 == 128 -> false (sempre use equals)");
    }

    @Test void dp0312_proxy() {
        assertThat(ProxyExemplo.run()).contains(
            "proxy de cache: 3 chamadas a findName(A) -> 1 consulta(s) ao banco",
            "proxy de proteção -> acesso negado (consultas extras: 0)",
            "proxy dinâmico JDK -> Produto B, é proxy? true",
            "Spring ProxyFactory -> Produto C, JDK dynamic proxy? true");
    }
}
