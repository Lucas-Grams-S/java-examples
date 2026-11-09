package dev.estudos.springcore.eventos;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes de SC08-08 a 11. */
class EventosTest {
    @Test void sc0808_eventos() {
        var out = Eventos.run();
        assertThat(out).hasSize(6);
        assertThat(out.get(0)).isEqualTo("publicando");
        assertThat(out.subList(1, 3)).containsExactly("[mail] first", "[chained] xo-1");
        assertThat(out.get(3)).startsWith("[billing] OrderPlaced[orderId=o-1, cents=1500] thread=");
        assertThat(out.subList(4, 6)).containsExactly("[big] o-1", "publicou");   // síncrono: 'publicou' por último
    }
    @Test void sc0809_async_e_apos_commit() throws Exception {
        assertThat(AssincronoEPosCommit.run()).containsExactly("ouvinte comum (na transação): o-1",
            "após o commit -> AFTER_COMMIT: o-1", "ouvinte comum (na transação): o-2",
            "rollback -> AFTER_COMMIT não recebeu o-2: true", "@Async rodou em thread diferente da principal: true");
    }
    @Test void sc0810_bean_validation() {
        assertThat(BeanValidationDemo.run()).containsExactly("age: must be greater than or equal to 18",
            "email: must be a well-formed email address", "name: must not be blank",
            "tags: size must be between 1 and 2147483647", "errors.getErrorCount() -> 4", "código do erro de age -> Min",
            "sku \"abc\" -> invalid SKU; sku \"ABC-1234\" -> sem violação");
    }
    @Test void sc0811_conversao() {
        assertThat(ConversaoFormatacao.run()).containsExactly("\"R$ 12,50\" -> Money[amount=12.50]",
            "\"1,2,3\" -> int[] de tamanho 3", "\"42\" -> 42", "canConvert(String, Money) -> true",
            "print(Money 9.9) -> R$ 9.9", "parse(\"R$ 7,5\") -> Money[amount=7.5]");
    }
}
