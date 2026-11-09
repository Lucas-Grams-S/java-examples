package dev.estudos.java.excecoes;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.Test;

/** Testes dos exemplos J08-02 a J08-15. */
class ExcecoesTest {
    @Test void j0802_try_catch_finally() {
        assertThat(TryCatchFinally.run()).contains("modo 1 -> negócio:pagamento [try 1, finally 1]",
                "modo 3 -> runtime:bug [try 3, finally 3]",
                "try { return 1; } finally { return 2; } -> 2", "finally com return -> exceção engolida");
    }

    @Test void j0803_try_with_resources() {
        assertThat(TryWithResources.run()).contains("log -> [abre A, abre B, corpo, close B, close A]",
                "principal -> body", "suprimidas -> [close B, close A]");
    }

    @Test void j0804_excecoes_proprias() {
        assertThat(CustomExceptions.run()).contains("unchecked -> Order 42 not found id=42 causa=null",
                "é RuntimeException? -> false");
    }

    @Test void j0808_twr_desacucarado() {
        var out = DesugaredTwr.run();
        assertThat(out.get(0).substring(out.get(0).indexOf("->")))
                .isEqualTo(out.get(1).substring(out.get(1).indexOf("->")));
        assertThat(out.get(0)).contains("body suprimidas=[close]");
    }

    @Test void j0811_traducao_entre_camadas() {
        assertThat(LayerTranslation.run()).contains("domínio -> Order 9 not found",
                "SQLState preservado -> 02000");
    }

    @Test void j0812_validacao() {
        assertThat(ArgumentValidation.run()).contains("NullPointerException: name must not be null",
                "IllegalArgumentException: invalid email: sem-arroba", "IllegalArgumentException: invalid email: null");
    }

    @Test void j0813_lambda_checked() throws IOException {
        assertThat(LambdaChecked.run()).contains("lido -> [alfa, beta]", "helper -> [alfa, beta]",
                "falha -> UncheckedIOException, causa NoSuchFileException");
    }

    @Test void j0815_result() {
        assertThat(ExplicitResult.run()).contains("42 -> ok 42", "x7 -> erro não é número: x7");
    }
}
