package dev.estudos.springcore.aot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes de SC10-04, 08, 09 e 11. */
class AotTest {
    @Test void sc1008_gera_codigo() throws Exception {
        var out = GerarAot.run();
        assertThat(out.get(0)).isEqualTo("initializer: aot.Aot__ApplicationContextInitializer");
        assertThat(out.get(1)).contains("sources/aot/Aot__ApplicationContextInitializer.java", "SpringCGLIB");
        assertThat(out.subList(2, 5)).containsExactly("OrderService criado com new direto -> true",
            "DevOnly aparece no código gerado -> false", "hint templates/*.txt registrado -> true");
    }
    @Test void sc1004_antes_e_depois() throws Exception {
        var linhas = AntesDepois.run();
        assertThat(linhas).anyMatch(l -> l.contains("forConstructor(GerarAot.OrderRepository.class, GerarAot.PaymentGateway.class)"));
        assertThat(linhas).anyMatch(l -> l.contains("new GerarAot.OrderService(args.get(0), args.get(1))"));
        assertThat(linhas).anyMatch(l -> l.contains("forFactoryMethod") && l.contains("\"banner\""));
    }
    @Test void sc1009_hints_proprios() throws Exception {
        assertThat(HintsProprios.run()).containsExactly("conteúdo lido dinamicamente -> relatório diário",
            "hint reports/daily.txt registrado -> true", "hint reports/ qualquer outro padrão (other/x.txt) -> false",
            "reflection de OrderDto (registerType) -> true");
    }
    @Test void sc1011_amigo_do_aot() throws Exception {
        assertThat(AmigoDoAot.run()).containsExactly("@Bean com tipo concreto: tipo do bean conhecido no build -> true",
            "método de fábrica gerado referencia CardGateway e Clock -> true");
    }
}
