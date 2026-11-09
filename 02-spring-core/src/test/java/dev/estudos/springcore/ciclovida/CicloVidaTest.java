package dev.estudos.springcore.ciclovida;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos SC04-01 a 05, 09 a 12 (alguns compartilham a classe). */
class CicloVidaTest {
    @Test void sc0401_ordem_completa() {
        assertThat(OrdemCompleta.run()).containsExactly(
            "0 BFPP: definições registradas, nenhuma instância de 'stock' ainda", "1 construtor", "2 BeanNameAware: stock",
            "3 BPP before", "4 @PostConstruct", "5 afterPropertiesSet", "6 initMethod", "7 BPP after", "   (bean em uso)",
            "8 @PreDestroy", "9 destroy()", "10 destroyMethod");
    }
    @Test void sc0403_bfpp_vs_bpp() {
        assertThat(BfppVsBpp.run()).containsExactly("BFPP rodou antes de qualquer instância -> true",
            "BFPP mudou a receita: scope de 'stock' -> prototype", "getBean('stock') duas vezes -> instâncias diferentes: true",
            "BPP substituiu o objeto: etiqueta -> trocado-pelo-BPP");
    }
    @Test void sc0404_escopos() throws Exception {
        assertThat(Escopos.run()).containsExactly("singleton: getBean == getBean -> true", "prototype: getBean == getBean -> false",
            "dois contextos = dois \"singletons\" -> true", "escopo 'thread': mesma thread -> true, outra thread -> false",
            "após close(): @PreDestroy do singleton rodou -> true, do prototype -> false");
    }
    @Test void sc0405_escopo_curto_em_longo() throws Exception {
        var out = EscopoCurtoEmLongo.run();
        assertThat(out.get(0)).endsWith("-> true");
        // ids exatos dependem da ordem de criação; só exigimos que sejam distintos
        String ids = out.get(1).substring(out.get(1).indexOf('[') + 1, out.get(1).indexOf(']'));
        assertThat(java.util.Arrays.stream(ids.split(", ")).distinct().count()).isEqualTo(3);
        assertThat(out.get(2)).startsWith("proxy de escopo: o singleton guarda um proxy -> true");
        assertThat(out.get(3)).endsWith("-> true");
    }
    @Test void sc0409_recurso() {
        assertThat(RecursoComCicloDeVida.run()).containsExactly("broker: open", "conectado após refresh -> true",
            "broker: close", "conexão fechada após close() -> true");
    }
    @Test void sc0410_aware() {
        assertThat(BeanAware.run()).containsExactly("nome do bean -> diagnostico",
            "contexto recebido é o mesmo que criou o bean -> true", "contexto vê o próprio bean -> true");
    }
    @Test void sc0411_timing_bpp() {
        assertThat(TimingBpp.run()).containsExactly("charge devolveu -> cobrou 100 via card",
            "medições registradas pelo proxy -> [card.charge]", "o bean injetado é um proxy JDK -> true",
            "não é um proxy do Spring AOP (é o java.lang.reflect.Proxy puro) -> true");
    }
}
