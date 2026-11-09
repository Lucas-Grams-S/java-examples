package dev.estudos.boot.autoconfig.starter;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SB02-08 · Auto-configuração própria (biblioteca fictícia de auditoria) e SB02-05 · {@code @ConditionalOnClass}
 * em classe aninhada. Registrada em {@code META-INF/spring/...AutoConfiguration.imports}; fica num pacote
 * <b>irmão</b> (fora do alcance do scan da aplicação), senão viraria configuração comum.
 * Documento: 02-auto-configuracao.md §3.2, §5.1.
 *
 * <p>Saída real ({@code AutoConfigApplication}):
 * <pre>
 * [audit] aplicação iniciada
 * optional-lib presente = true
 * </pre>
 * Variações: {@code --audit.enabled=false} (o bean some e a aplicação falha em quem o injeta);
 * defina seu próprio {@code AuditSink} e ele vence ({@code @ConditionalOnMissingBean}).
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "audit", name = "enabled", matchIfMissing = true)
public class AuditAutoConfiguration {

    public interface AuditSink { void log(String msg); }

    /** Coletor padrão: imprime e guarda (para testes). */
    public static class ConsoleSink implements AuditSink {
        public final List<String> lines = new ArrayList<>();
        @Override public void log(String msg) { lines.add(msg); System.out.println("[audit] " + msg); }
    }

    @Bean
    @ConditionalOnMissingBean            // recua se o usuário definir o seu
    ConsoleSink auditSink() { return new ConsoleSink(); }

    /** O tipo opcional só aparece aqui: a classe aninhada protege o carregamento (lida por ASM). */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(OptionalLib.class)
    static class OptionalLibConfiguration {
        @Bean OptionalLib optionalLib() { return new OptionalLib(); }
    }
}
