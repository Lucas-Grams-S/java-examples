package dev.estudos.java.jvm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos J02-03, 06, 09, 10, 11, 12, 13 e 14. */
class JvmTest {
    @Test void j0203_areas_de_memoria() {
        var out = MemoryAreas.run();
        assertThat(out).anyMatch(l -> l.contains("Metaspace")).anyMatch(l -> l.startsWith("HEAP"))
                .anyMatch(l -> l.startsWith("memória direta: 1 buffer(s)"));
        assertThat(out.getLast()).contains("ThreadStackSize = 1024");
    }

    @Test void j0206_coletores() {
        assertThat(Collectors.using("-XX:+UseSerialGC")).contains("Using Serial");
        assertThat(Collectors.using("-XX:+UseParallelGC")).contains("Using Parallel");
        assertThat(Collectors.using("-XX:+UseG1GC")).contains("Using G1");
        assertThat(Collectors.using("-XX:+UseZGC")).contains("Z Garbage Collector");
        assertThat(Collectors.run()).anyMatch(l -> l.contains("Humongous Allocation"));
    }

    @Test void j0209_falhas_de_memoria() {
        assertThat(MemoryFailures.run()).containsExactly("profundidade > 1000: true", "Java heap space");
    }

    @Test void j0210_quem_carregou() {
        var out = WhoLoadedClass.run();
        assertThat(out.get(0)).contains("AppClassLoader");
        assertThat(out.get(1)).endsWith("-> null");
        assertThat(out.get(2)).contains("PlatformClassLoader");
    }

    @Test void j0211_classloader_proprio() throws Exception {
        assertThat(DirClassLoaderDemo.run()).contains("hello() -> olá do plugin", "mesma classe nos dois loaders (c1 == c2) -> false",
                "String continua vindo do bootstrap (delegação) -> true");
    }

    @Test void j0212_gc_log_e_jfr() throws Exception {
        var out = GcLogsAndJfr.run();
        assertThat(out).contains("app.jfr existe -> true");
        assertThat(out).anyMatch(l -> l.contains("jdk.GarbageCollection {"));
    }

    @Test void j0213_diagnostico_com_jcmd() throws Exception {
        var out = Diagnostics.run();
        assertThat(out).contains("VM.flags -> -XX:MaxHeapSize=67108864", "Thread.print contém a thread main -> true",
                "GC.heap_dump gerou .hprof (bytes > 0) -> true");
    }

    @Test void j0214_dimensionamento_em_conteiner() {
        var out = ContainerSizing.run();
        assertThat(out).anyMatch(l -> l.startsWith("MaxRAMPercentage=70 -> heap máximo ≈ 70%"));
        assertThat(out.getLast()).contains("código de saída 3");
    }
}
