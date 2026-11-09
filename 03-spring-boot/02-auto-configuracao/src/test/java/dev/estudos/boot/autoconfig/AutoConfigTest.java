package dev.estudos.boot.autoconfig;

import static org.assertj.core.api.Assertions.assertThat;

import dev.estudos.boot.autoconfig.app.AutoConfigApplication;
import dev.estudos.boot.autoconfig.app.DiagnoseConditions;
import dev.estudos.boot.autoconfig.starter.AuditAutoConfiguration;
import dev.estudos.boot.autoconfig.starter.AuditAutoConfiguration.AuditSink;
import dev.estudos.boot.autoconfig.starter.AuditAutoConfiguration.ConsoleSink;
import dev.estudos.boot.autoconfig.starter.OptionalLib;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.WebApplicationType;

/** Testes de SB02-05, 07, 08, 09 e 11. */
class AutoConfigTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AuditAutoConfiguration.class));

    @Test void sb0209_runner_padrao_cria_o_bean() {
        runner.run(c -> assertThat(c).hasSingleBean(ConsoleSink.class));
    }
    @Test void sb0209_propriedade_desliga() {
        runner.withPropertyValues("audit.enabled=false").run(c -> assertThat(c).doesNotHaveBean(ConsoleSink.class));
    }
    @Test void sb0209_bean_do_usuario_vence() {
        runner.withBean(ConsoleSink.class, ConsoleSink::new).run(c -> assertThat(c).hasSingleBean(ConsoleSink.class));
        runner.withBean("meu", AuditSink.class, () -> m -> {}).run(c -> assertThat(c).hasBean("meu"));
    }
    @Test void sb0205_classe_ausente_nao_quebra() {
        runner.run(c -> assertThat(c).hasSingleBean(OptionalLib.class));
        runner.withClassLoader(new FilteredClassLoader(OptionalLib.class)).run(c -> {
            assertThat(c).hasNotFailed();
            assertThat(c).doesNotHaveBean("optionalLib");
            assertThat(c).hasSingleBean(ConsoleSink.class);
        });
    }
    @Test void sb0208_imports_registra_a_auto_configuracao() {
        try (var ctx = new SpringApplicationBuilder(AutoConfigApplication.class).web(WebApplicationType.NONE)
                .logStartupInfo(false).run()) {
            assertThat(ctx.getBean(ConsoleSink.class).lines).containsExactly("aplicação iniciada");
            assertThat(ctx.getBeansOfType(OptionalLib.class)).hasSize(1);
        }
    }
    @Test void sb0207_exclusao_por_propriedade() {
        // sem a auto-configuração, o runner de AutoConfigApplication (que injeta ConsoleSink) falha ao subir
        var builder = new SpringApplicationBuilder(AutoConfigApplication.class).web(WebApplicationType.NONE)
            .logStartupInfo(false)
            .properties("spring.autoconfigure.exclude=" + AuditAutoConfiguration.class.getName());
        org.assertj.core.api.Assertions.assertThatThrownBy(builder::run)
            .hasStackTraceContaining("No qualifying bean of type").hasStackTraceContaining("ConsoleSink");
    }
    @Test void sb0211_relatorio_de_condicoes() {
        assertThat(DiagnoseConditions.run()).containsExactly("negativo: GsonHttpMessageConvertersConfiguration",
            "positivo: AuditAutoConfiguration", "positivo: AuditAutoConfiguration#auditSink",
            "positivo: AuditAutoConfiguration$OptionalLibConfiguration");
    }
}
