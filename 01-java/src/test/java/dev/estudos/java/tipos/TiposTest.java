package dev.estudos.java.tipos;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Testes dos exemplos J03-01 a J03-13. */
class TiposTest {
    @Test void j0301_primitivos() {
        assertThat(Primitives.run()).contains("0.1 + 0.2 = 0.30000000000000004", "Integer.MAX_VALUE + 1 = -2147483648",
                "5 / 2 = 2", "-5 % 3 = -2", "Math.floorMod(-5, 3) = 1", "(byte) 200 = -56", "NaN == NaN = false",
                "BigDecimal 0.1 + 0.2 = 0.3", "Math.addExact(MAX_VALUE, 1) -> ArithmeticException: integer overflow");
    }

    @Test void j0302_referencias_e_npe_detalhado() {
        var out = ReferencesAndNull.run();
        assertThat(out).contains("a == b (mesmo objeto) -> true", "new Order(1) == new Order(1) -> false");
        assertThat(out).contains("NPE -> Cannot read field \"name\" because \"c\" is null",
                "NPE -> Cannot invoke \"String.length()\" because \"a.name\" is null");
    }

    @Test void j0303_passagem_por_valor() {
        assertThat(PassByValue.run()).containsExactly("a! 1 s");
    }

    @Test void j0304_cache_de_integer() {
        assertThat(WrappersBoxing.run()).contains("127 == 127 -> true", "128 == 128 -> false", "128 equals 128 -> true",
                "unboxing de null -> NullPointerException", "sum com Long -> 499500");
    }

    @Test void j0305_pool_de_strings() {
        assertThat(StringPool.run()).contains("s1 == s2 (pool) -> true", "s1 == s3 (new) -> false", "s1 == s3.intern() -> true",
                "mesmo resultado -> true", "emoji: length()=2 codePoints=1");
    }

    @Test void j0306_contrato_equals_hashcode() {
        assertThat(EqualsHashCode.run()).contains("Bad: contains(new Bad(1)) -> false", "Good: contains(new Good(1)) -> true",
                "Good depois de mutar a chave: contains(g) -> false", "record Product: iguais por valor -> true");
    }

    @Test void j0307_imutabilidade() {
        assertThat(Immutability.run()).contains("quantidade 0 -> quantity must be > 0", "List.of(...).add -> UnsupportedOperationException");
    }

    @Test void j0308_onde_vive_cada_coisa() throws Exception {
        var out = WhereThingsLive.run();
        assertThat(out).contains("objeto em campo static -> coletado: false", "depois de zerar o campo static -> coletado: true");
    }

    @Test void j0309_javap_mostra_boxing_e_indy() throws Exception {
        String all = String.join("\n", CompilerBoxing.run());
        assertThat(all).contains("Integer.valueOf", "Integer.intValue", "invokedynamic", "makeConcatWithConstants");
    }

    @Test void j0310_hashcode() {
        assertThat(HashCodeInPractice.run()).contains("\"hello\".hashCode() -> 99162322", "List.of(1, 2).hashCode() -> 994",
                "Objects.hash(1, 2) -> 994");
        assertThat(Objects.hash(1, 2)).isEqualTo(List_hash());
    }

    private static int List_hash() { return java.util.List.of(1, 2).hashCode(); }

    @Test void j0311_dominio() {
        assertThat(OrdersDomain.run()).contains("10 + 5,50 -> Money[amount=15.50, currency=BRL]",
                "mesmo id, nomes diferentes → equals -> true");
        var a = new OrdersDomain.Money(new BigDecimal("1"), "BRL");
        assertThat(Set.of(a)).contains(new OrdersDomain.Money(new BigDecimal("1"), "BRL"));
    }

    @Test void j0312_copia_defensiva() {
        assertThat(DefensiveCopy.run()).contains("itens do pedido após mutar a lista original -> 1",
                "tentativa de mutar items() -> UnsupportedOperationException");
    }

    @Test void j0313_armadilhas() {
        assertThat(PitfallsAtOnce.run()).contains("true false true", "NPE no unboxing", "BigDecimal 2.0 equals 2.00 -> false",
                "BigDecimal 2.0 compareTo 2.00 -> 0");
    }
}
