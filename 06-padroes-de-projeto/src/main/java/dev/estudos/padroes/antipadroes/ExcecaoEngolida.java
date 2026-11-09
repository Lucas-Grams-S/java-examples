package dev.estudos.padroes.antipadroes;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * DP08-13 · Exceção engolida: o {@code catch} que ignora devolve o controle normalmente, e há commit.
 * Mostra também a correção com {@code setRollbackOnly}.
 * Documento: java-docs/06-padroes-de-projeto/08-anti-padroes.md §5.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * swallow: seguiu como se nada
 * linhas após engolir + corrigir -&gt; [engolido] (engolido: commit; corrigido: rollback)
 * </pre>
 */
public class ExcecaoEngolida {
    static void swallow(List<String> out) {
        try { Integer.parseInt("x"); } catch (Exception e) { /* ignora */ }
        out.add("swallow: seguiu como se nada");
    }

    public static class Servico {
        private final JdbcClient jdbc;
        Servico(JdbcClient j) { jdbc = j; }

        @Transactional
        public void engolindo() {
            jdbc.sql("insert into t(v) values ('engolido')").update();
            try { Integer.parseInt("x"); } catch (Exception e) { /* ignora */ }
        }

        @Transactional
        public void corrigindo() {
            jdbc.sql("insert into t(v) values ('corrigido')").update();
            try { Integer.parseInt("x"); }
            catch (Exception e) { TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); }
        }
    }

    @Configuration
    @EnableTransactionManagement
    public static class Cfg {
        @Bean DataSource ds() {
            return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).setName("dp0813").build();
        }
        @Bean JdbcClient jdbc(DataSource ds) { return JdbcClient.create(ds); }
        @Bean PlatformTransactionManager tm(DataSource ds) { return new DataSourceTransactionManager(ds); }
        @Bean Servico servico(JdbcClient j) { return new Servico(j); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        swallow(out);
        try (var ctx = new AnnotationConfigApplicationContext(Cfg.class)) {
            var jdbc = ctx.getBean(JdbcClient.class);
            jdbc.sql("create table t(v varchar(20))").update();
            var s = ctx.getBean(Servico.class);
            s.engolindo();
            s.corrigindo();
            out.add("linhas após engolir + corrigir -> "
                + jdbc.sql("select v from t order by v").query(String.class).list()
                + " (engolido: commit; corrigido: rollback)");
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
