package dev.estudos.java.testes;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** J15-14 · Dependências de teste no Maven sem Spring: versões vêm do BOM e há ByteBuddy compatível com o Java 25. */
class DependenciasTest {
    static String jar(Class<?> c) throws Exception {
        return Path.of(c.getProtectionDomain().getCodeSource().getLocation().toURI()).getFileName().toString();
    }

    @Test void versoesGerenciadasPeloBom() throws Exception {
        assertThat(jar(org.junit.jupiter.api.Test.class)).isEqualTo("junit-jupiter-api-6.0.3.jar");
        assertThat(jar(org.assertj.core.api.Assertions.class)).isEqualTo("assertj-core-3.27.7.jar");
        assertThat(jar(org.mockito.Mockito.class)).isEqualTo("mockito-core-5.23.0.jar");
        assertThat(jar(org.mockito.junit.jupiter.MockitoExtension.class)).isEqualTo("mockito-junit-jupiter-5.23.0.jar");
        assertThat(jar(net.bytebuddy.ByteBuddy.class)).startsWith("byte-buddy-1.18.");
    }

    @Test void byteBuddyDoBomEntendeBytecodeDoJava25() {
        assertThat(Runtime.version().feature()).isEqualTo(25);
        // classe criada em runtime pelo Mockito (inline mock maker) em JDK 25 prova que o par Mockito/ByteBuddy do BOM funciona
        assertThat(org.mockito.Mockito.mock(java.util.List.class).size()).isZero();
    }
}
