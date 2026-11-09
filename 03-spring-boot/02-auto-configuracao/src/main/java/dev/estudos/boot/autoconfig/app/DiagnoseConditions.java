package dev.estudos.boot.autoconfig.app;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * SB02-11 · Diagnosticar: lê o {@link ConditionEvaluationReport} (o mesmo do {@code --debug}) por código.
 * Documento: 02-auto-configuracao.md §5.4. Execução: {@code ./scripts/run-example.sh SB02-11}.
 *
 * <p>Saída real:
 * <pre>
 * [audit] aplicação iniciada
 * optional-lib presente = true
 * negativo: GsonHttpMessageConvertersConfiguration
 * positivo: AuditAutoConfiguration
 * positivo: AuditAutoConfiguration#auditSink
 * positivo: AuditAutoConfiguration$OptionalLibConfiguration
 * </pre>
 * (com {@code --debug} o Boot imprime o relatório completo; o Actuator o expõe em /actuator/conditions.)
 */
public class DiagnoseConditions {
    public static List<String> run(String... args) {
        List<String> out = new ArrayList<>();
        try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(AutoConfigApplication.class)
                .web(WebApplicationType.NONE).bannerMode(Banner.Mode.OFF).logStartupInfo(false)
                .properties("spring.main.log-startup-info=false").run(args)) {
            ConditionEvaluationReport report = ConditionEvaluationReport.get(ctx.getBeanFactory());
            report.getConditionAndOutcomesBySource().forEach((source, outcomes) -> {
                String simple = source.substring(source.lastIndexOf('.') + 1);
                if (simple.startsWith("AuditAutoConfiguration")) {
                    if (outcomes.isFullMatch()) out.add("positivo: " + simple);
                } else if (simple.equals("GsonHttpMessageConvertersConfiguration") && !outcomes.isFullMatch()) {
                    out.add("negativo: " + simple);
                }
            });
        }
        out.sort(null);
        return out;
    }

    public static void main(String[] args) { run(args).forEach(System.out::println); }
}
