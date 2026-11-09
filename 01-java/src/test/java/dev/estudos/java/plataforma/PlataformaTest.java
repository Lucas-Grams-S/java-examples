package dev.estudos.java.plataforma;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos J01-05, 08, 10, 11, 12, 13 e 14. */
class PlataformaTest {
    @Test void j0105_bytecode_do_sum_e_cabecalho() throws Exception {
        String all = String.join("\n", SourceToBytecode.run());
        assertThat(all).contains("iload_0", "iload_1", "iadd", "ireturn", "magic -> 0xCAFEBABE", "major -> 69 (Java 25)");
    }

    @Test void j0108_jit_compila_hello_em_niveis() throws Exception {
        assertThat(TieredCompilation.run()).anyMatch(l -> l.contains("Hello::sum"));   // nível exato varia
    }

    @Test void j0110_ferramentas_do_jdk() throws Exception {
        var out = JdkTools.run();
        assertThat(out).noneMatch(l -> l.startsWith("FALTA"));
        assertThat(out).contains("java -jar app.jar -> 599994", "jdeps --print-module-deps -> java.base");
    }

    @Test void j0111_compilar_inspecionar_executar() throws Exception {
        assertThat(CompileInspectRun.run()).contains("out/com/example/orders/Main.class existe -> true", "java -cp out -> pedidos ok");
    }

    @Test void j0112_arquivo_unico() throws Exception {
        assertThat(SingleFileSource.run()).contains("java Hello.java -> Olá, pedidos!", "nenhum .class gerado -> true");
    }

    @Test void j0113_warmup_produz_o_mesmo_resultado_em_todas_as_rodadas() {
        var out = Warmup.run(3, 1_000_000);
        assertThat(out).hasSize(3).allMatch(l -> l.endsWith("(r=2999997)"));
    }

    @Test void j0114_opcoes_de_linha_de_comando() throws Exception {
        var out = CommandLineOptions.run();
        assertThat(out.get(0)).contains("InitialHeapSize = 536870912").contains("MaxHeapSize = 536870912");
        assertThat(out).anyMatch(l -> l.contains("TieredStopAtLevel = 1"));
        assertThat(out).anyMatch(l -> l.contains("interpreted mode"));
        assertThat(out).contains("AppCDS dinâmico: arquivo .jsa criado -> true");
    }
}
