package dev.estudos.java.testes;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/** J15-03 · Ciclo de vida e anotações: ordem dos callbacks, instância nova por método, @TempDir, @Timeout, @RepeatedTest. */
@TestMethodOrder(MethodOrderer.MethodName.class)
class LifecycleTest {
    static final List<String> eventos = new ArrayList<>();
    static final List<Integer> instancias = new ArrayList<>();
    int contador;   // campo de instância: NÃO vaza entre testes

    @BeforeAll static void once() { eventos.add("beforeAll"); }
    @BeforeEach void setUp() { eventos.add("beforeEach"); }
    @AfterEach void tearDown() { eventos.add("afterEach"); }
    @AfterAll static void end() {
        eventos.add("afterAll");
        assertThat(eventos).startsWith("beforeAll", "beforeEach", "a", "afterEach", "beforeEach", "b", "afterEach").endsWith("afterAll");
    }

    @Test @DisplayName("a: primeiro teste") void a() { eventos.add("a"); contador++; instancias.add(System.identityHashCode(this)); assertThat(contador).isEqualTo(1); }
    @Test void b() { eventos.add("b"); contador++; instancias.add(System.identityHashCode(this)); assertThat(contador).isEqualTo(1);
        assertThat(instancias).doesNotHaveDuplicates(); }   // instâncias diferentes para a e b

    @Test void c_tempDir(@TempDir Path dir) throws Exception {
        Path f = dir.resolve("x.txt");
        Files.writeString(f, "ok");
        assertThat(Files.readString(f)).isEqualTo("ok");
    }

    @Test @Timeout(value = 2, unit = java.util.concurrent.TimeUnit.SECONDS) void d_timeout() throws Exception { Thread.sleep(10); }

    @Test @Disabled("exemplo de teste desligado, com motivo") void e_disabled() { throw new AssertionError("não deve rodar"); }

    @Tag("slow") @Test void f_tag() { assertThat(Duration.ofMillis(1)).isPositive(); }

    @RepeatedTest(3) void g_repeated(RepetitionInfo info) { assertThat(info.getCurrentRepetition()).isBetween(1, 3); }
}
