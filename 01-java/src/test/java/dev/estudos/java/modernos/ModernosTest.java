package dev.estudos.java.modernos;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos J05-01 a J05-15. */
class ModernosTest {
    @Test void j0501_var() {
        assertThat(VarLocal.run()).contains("var + diamante infere ArrayList<Object> -> [1, dois]",
                "var n = 1 -> Integer; var d = 1.0f -> Float", "var var = 1 -> 1");
    }

    @Test void j0502_text_blocks() {
        assertThat(TextBlocks.run()).contains("  \"name\": \"Café grande\"", "termina com quebra -> true",
                "\\s preserva espaço final -> [a ]", "formatted -> Olá, mundo!");
    }

    @Test void j0503_switch_expressions() {
        assertThat(SwitchExpressions.run()).contains("PAID -> 4", "SAT -> fim de semana; MON -> dia útil",
                "switch antigo, n=1 (fall-through) -> um dois fim");
    }

    @Test void j0504_records() {
        assertThat(RecordsDemo.run()).contains("accessor -> Ana", "equals por valor -> true", "construtor compacto valida -> lo>hi",
                "componentes por reflection -> [java.lang.String last4, int installments]",
                "imutabilidade rasa: lista mutada por fora -> [a, b]");
    }

    @Test void j0505_instanceof() {
        assertThat(InstanceofPattern.run()).contains("depois -> longa java25 / outro / outro",
                "n continua no escopo -> 42", "não é Integer");
    }

    @Test void j0506_sealed() {
        assertThat(SealedTypes.run()).contains("isSealed -> true", "permitidos -> [Pix, Card, Boleto]",
                "Custom (non-sealed) isSealed -> false; MyCustom estende Custom -> true");
    }

    @Test void j0507_switch_com_padroes() {
        assertThat(SwitchPatterns.run()).contains("fee Card 3x -> parcelado 3", "fee Card 1x -> à vista 1234",
                "desc -> nulo  grande 42  int 5  str 3  linha 1 Point[x=3, y=4]  outro",
                "switch sem case null -> NullPointerException");
    }

    @Test void j0511_antes_e_depois() {
        assertThat(PaymentBeforeAfter.run()).contains("Pix[key=k] -> antes=0.0 depois=0",
                "antes: tipo fora da lista -> IllegalArgumentException em execução");
    }

    @Test void j0512_resultado_tipado() {
        assertThat(ResultType.run()).containsExactly("pedido Order[items=[caneta]]", "falhou: carrinho vazio");
    }

    @Test void j0513_dto_com_validacao() {
        assertThat(DtoValidation.run()).contains("lista original mutada -> 2 itens; no record -> 1", "sem itens -> pedido sem itens");
    }

    @Test void j0514_desestruturacao_aninhada() {
        assertThat(NestedDeconstruction.run()).contains("x + y -> 15", "Box<String> casa com Box(Point)? -> false");
    }

    @Test void j0515_padroes_primitivos_so_com_preview() throws Exception {
        var out = PrimitivePatterns.run();
        assertThat(out.get(0)).contains("exit=1", "primitive patterns are a preview feature");
        assertThat(out.get(1)).contains("exit=0", "padrão primitivo: 3");
    }
}
