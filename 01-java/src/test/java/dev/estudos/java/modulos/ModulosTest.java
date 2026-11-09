package dev.estudos.java.modulos;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos J14-02, 03, 04, 08 e 10 (J14-05 Gradle não é executável aqui: Gradle não instalado). */
class ModulosTest {
    @Test void j1402_module_info() throws Exception {
        assertThat(ModuleInfo.run()).contains("compila (sem opens) -> exit 0",
            "[sem opens] ServiceLoader -> cartão 500", "[sem opens] método público -> o-1",
            "[sem opens] reflection no campo private -> InaccessibleObjectException",
            "compila (opens ... to client) -> exit 0", "[com opens] reflection no campo private -> o-1",
            "usar pacote não exportado -> exit 1");
    }
    @Test void j1403_validacao_rapida() throws Exception {
        assertThat(ValidacaoRapida.run()).contains("javac -> exit 0", "java -p out -m ... -> ok com.example.orders",
            "jar -d mostra exports -> true", "jdeps --print-module-deps -> java.base",
            "String está no módulo -> java.base (nomeado=true)", "exporta java.lang -> true",
            "exporta jdk.internal.misc para o unnamed? -> false",
            "este código está em módulo nomeado? (classpath => unnamed) -> false");
    }
    @Test void j1404_maven() throws Exception {
        assertThat(MavenDemo.run()).contains("BUILD SUCCESS -> true", "jar gerado -> true",
            "versão sem <version> no POM (vem do BOM) -> org.junit.jupiter:junit-jupiter:jar:6.0.3:test",
            "tree mostra transitivas (junit-jupiter-engine) -> true", "clean apaga target/ -> true");
    }
    @Test void j1405_gradle() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeTrue(GradleDemo.gradle() != null, "Gradle não instalado");
        assertThat(GradleDemo.run()).contains("build -> exit 0", "jar executável (bootJar) -> true", "jar simples (plain) -> true",
            "implementation sem versão (vem do plugin) -> org.springframework.boot:spring-boot-starter-web -> 4.1.2",
            "runtimeOnly fora do classpath de compilação -> true", "runtimeOnly no classpath de execução -> true");
    }
    @Test void j1408_jlink() throws Exception {
        assertThat(JlinkDemo.run()).contains("jdeps --print-module-deps -> java.base", "jlink -> exit 0",
            "módulos na imagem -> [java.base]", "roda a aplicação com o runtime mínimo -> olá do runtime mínimo",
            "runtime mínimo é menos da metade do JDK -> true");
    }
    @Test void j1410_origem_da_classe() throws Exception {
        assertThat(OrigemDaClasse.run()).contains("esta classe vem de um diretório de classes -> true",
            "classpath [a-1.0, b-2.0] -> 1.0 (de a-1.0.jar)", "classpath [b-2.0, a-1.0] -> 2.0 (de b-2.0.jar)",
            "String (JDK): CodeSource -> null ; módulo -> java.base");
    }
}
