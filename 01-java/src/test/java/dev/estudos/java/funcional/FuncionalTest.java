package dev.estudos.java.funcional;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos J09-03 a J09-14. */
class FuncionalTest {
    @Test void j0903_closures() {
        assertThat(Closures.run()).contains("addBase(5) -> 15", "soma via array -> 6", "this em lambda -> Closures",
                "this em classe anônima -> instância anônima",
                "sem captura: mesma instância a cada avaliação -> true",
                "com captura: nova instância a cada avaliação -> true");
    }

    @Test void j0904_pipeline() {
        assertThat(StreamPipeline.run()).contains("total pago -> 245", "antes do terminal -> []",
                "depois de findFirst -> [peek 1, peek 2]",
                "reuso -> stream has already been operated upon or closed", "takeWhile/dropWhile -> [1, 2] [3, 1]");
    }

    @Test void j0905_collectors() {
        assertThat(CollectorsDemo.run()).contains("contagem -> {Ana=2, Bia=2, Caio=1}", "ids -> {Ana=[1, 3], Bia=[2, 5], Caio=[4]}",
                "pago? -> {false=2, true=3}", "total por cliente -> {Ana=370, Bia=75, Caio=80}",
                "toMap sem merge -> Duplicate key Ana (attempted merging values 120 and 250)", "média (teeing) -> 105");
    }

    @Test void j0906_optional() {
        assertThat(OptionalDemo.run()).contains("Ana -> Recife", "Bia (cidade vazia) -> desconhecida",
                "presente: orElse avaliou 1x, orElseGet 0x", "or -> Caio", "stream -> [Ana, Caio]");
    }

    @Test void j0908_preguica() {
        assertThat(StreamLaziness.run()).contains("findFirst -> [peek 1, peek 2]",
                "um a um (sem coleção intermediária) -> [filter a, map a, filter b, map b]",
                "sorted é stateful (barreira) -> [antes 3, antes 1, antes 2, depois 1, depois 2, depois 3]",
                "limit em fonte infinita -> [gera 1, gera 2, gera 3]");
    }

    @Test void j0909_paralelo() {
        var out = ParallelStreams.run();
        assertThat(out).contains("toList paralelo -> tamanho 1000, ordem preservada true",
                "soma paralela (associativa) -> 500500");
        assertThat(out).anyMatch(l -> l.startsWith("forEach em ArrayList (indefinido"));
    }

    @Test void j0910_gatherers() {
        assertThat(GatherersDemo.run()).contains("windowFixed(2) -> [[1, 2], [3, 4], [5, 6]]",
                "windowFixed(4) -> [[1, 2, 3, 4], [5, 6]]", "windowSliding(3) -> [[1, 2, 3], [2, 3, 4], [3, 4, 5]]",
                "scan (soma acumulada) -> [1, 3, 6, 10]", "fold -> [10]", "mapConcurrent -> [10, 20, 30]");
    }

    @Test void j0911_relatorio() {
        assertThat(OrderReport.run()).contains("ranking -> [3:250, 1:120, 4:80, 5:45]", "clientes -> [Ana, Bia, Caio]",
                "toList() é imutável");
    }

    @Test void j0912_flatmap() {
        assertThat(FlatMapDemo.run()).contains("itens -> [caneta, caderno, lápis, mochila, caneta]",
                "com map (lista de listas) -> [2, 1, 1, 0, 1]", "total de unidades -> 10");
    }

    @Test void j0913_ordem_superior() {
        assertThat(HigherOrder.run()).contains("add(2)(3) -> 5", "inc.andThen(dbl)(3) -> 8", "inc.compose(dbl)(3) -> 7",
                "depois do removeIf -> [1, 3, 4, 5]", "caros -> [1, 3]");
    }

    @Test void j0914_estrategia() {
        assertThat(StrategyLambda.run()).contains("none -> pedido1=120 pedido4=80", "tenOff -> pedido1=108 pedido4=72",
                "bigOrders -> pedido1=108 pedido4=80");
    }
}
