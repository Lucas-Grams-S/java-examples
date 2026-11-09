package dev.estudos.java.evolucao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos J00-01 a J00-09 (um método por ID). */
class EvolucaoTest {
    @Test void j0001_generics_enums_annotations() throws Exception {
        assertThat(GenericsAndEnums.run()).contains("sem generics -> ClassCastException só em runtime",
                "@Deprecated visível por reflection em runtime -> true");
    }

    @Test void j0002_try_with_resources_registra_suprimidas() throws Exception {
        assertThat(TryWithResources.run()).contains("principal -> erro no corpo", "suprimida -> falha ao fechar B",
                "suprimida -> falha ao fechar A", "primeira linha -> id;total");
    }

    @Test void j0003_streams_equivalem_ao_laco() {
        assertThat(LambdasStreamsTime.paidWithStreams(LambdasStreamsTime.sample()))
                .isEqualTo(LambdasStreamsTime.paidOldStyle(LambdasStreamsTime.sample()));
        assertThat(LambdasStreamsTime.run()).contains("vencimento (2026-01-15 + 30 dias) -> 2026-02-14");
    }

    @Test void j0004_modulos_e_jshell() {
        assertThat(ModulesAndJShell.run()).contains("String está no módulo -> java.base (nomeado=true)",
                "java.base exporta jdk.internal.misc -> false", "jshell -> $1 ==> 6");
    }

    @Test void j0005_var() {
        assertThat(VarInference.run()).contains("elemento -> pedido-1 (String)", "var orders -> ArrayList");
    }

    @Test void j0006_http_client_local() throws Exception {
        assertThat(HttpClientDemo.run()).contains("status -> 200", "corpo -> [{\"id\":1}]", "sendAsync -> [{\"id\":1}]");
    }

    @Test void j0007_java_moderno() {
        assertThat(ModernJava.label(ModernJava.Status.SHIPPED)).isEqualTo("Outro");
        assertThat(ModernJava.describe(new ModernJava.Card("1234567890123456"))).isEqualTo("Cartão 3456");
        assertThat(ModernJava.describe(new ModernJava.Pix("k"))).isEqualTo("Pix k");
        assertThat(ModernJava.run()).anyMatch(l -> l.startsWith("text block -> 3 linhas"));
    }

    @Test void j0008_virtual_threads() throws Exception {
        assertThat(VirtualThreads.run(1000)).contains("executadas em virtual threads -> 1000");
    }

    @Test void j0009_java25() throws Exception {
        assertThat(Java25Features.run()).contains("flexible constructor -> deve ser positivo",
                "ScopedValue dentro do escopo -> lucas", "ScopedValue fora do escopo -> false",
                "java Hello.java (compacto) -> Olá, pedidos");
    }
}
