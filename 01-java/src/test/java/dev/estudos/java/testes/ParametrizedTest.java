package dev.estudos.java.testes;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

/** J15-04 · Testes parametrizados: @CsvSource, @ValueSource, @EnumSource, @MethodSource. */
class ParametrizedTest {
    @ParameterizedTest(name = "{0} + {1} = {2}")
    @CsvSource({"1,2,3", "10,5,15", "0,0,0"})
    void sum(int a, int b, int expected) {
        assertThat(new Money(BigDecimal.valueOf(a)).plus(new Money(BigDecimal.valueOf(b))).amount())
            .isEqualByComparingTo(BigDecimal.valueOf(expected));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "-0.01"})
    void rejectsNegative(String v) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Money(new BigDecimal(v))).withMessage("negative");
    }

    @ParameterizedTest
    @EnumSource(value = TimeUnit.class, names = {"SECONDS", "MINUTES"})
    void enumSource(TimeUnit u) { assertThat(u.toMillis(1)).isGreaterThanOrEqualTo(1000); }

    @ParameterizedTest
    @MethodSource("valoresValidos")
    void methodSource(String texto, int tamanho) { assertThat(texto).hasSize(tamanho); }

    static Stream<Arguments> valoresValidos() {
        return Stream.of(Arguments.of("abc", 3), Arguments.of("", 0), Arguments.of("java", 4));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void nullEmptyBlank(String s) { assertThat(s == null || s.isBlank()).isTrue(); }
}
