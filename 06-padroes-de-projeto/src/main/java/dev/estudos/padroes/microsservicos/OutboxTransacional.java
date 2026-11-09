package dev.estudos.padroes.microsservicos;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * DP07-14 · Outbox transacional (H2): pedido e mensagem na mesma transação; relay por polling.
 * Documento: java-docs/06-padroes-de-projeto/07-padroes-de-microsservicos.md §5.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * erro: falha antes do commit
 * orders=[ana]
 * outbox=[ana]
 * publicando 1
 * pendentes=0
 * </pre>
 */
public class OutboxTransacional {

    @Service
    public static class Placer {
        private final JdbcClient jdbc;
        Placer(JdbcClient j) { jdbc = j; }

        @Transactional
        public void place(String customer, boolean fail) {
            jdbc.sql("insert into orders(customer_name, status, total) values (?, 'NEW', 1)")
                .param(customer).update();
            jdbc.sql("insert into outbox(topic, payload, sent) values ('orders.placed', ?, false)")
                .param(customer).update();
            if (fail) throw new IllegalStateException("falha antes do commit");
        }
    }

    @Configuration
    @EnableTransactionManagement
    public static class Cfg {
        @Bean DataSource dataSource() {
            return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).setName("dp0714")
                .addScript("classpath:outbox-schema.sql").build();
        }
        @Bean JdbcClient jdbcClient(DataSource ds) { return JdbcClient.create(ds); }
        @Bean PlatformTransactionManager tm(DataSource ds) { return new DataSourceTransactionManager(ds); }
        @Bean Placer placer(JdbcClient j) { return new Placer(j); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(Cfg.class)) {
            var placer = ctx.getBean(Placer.class);
            var jdbc = ctx.getBean(JdbcClient.class);
            placer.place("ana", false);
            try { placer.place("bob", true); }
            catch (IllegalStateException e) { out.add("erro: " + e.getMessage()); }
            out.add("orders=" + jdbc.sql("select customer_name from orders order by id").query(String.class).list());
            out.add("outbox=" + jdbc.sql("select payload from outbox order by id").query(String.class).list());

            // relay (polling): publicar, e só então marcar como enviado
            List<Long> ids = jdbc.sql("select id from outbox where sent = false order by id").query(Long.class).list();
            ids.forEach(id -> {
                out.add("publicando " + id);                                   // 1) broker (StreamBridge, KafkaTemplate...)
                jdbc.sql("update outbox set sent = true where id = ?").param(id).update();  // 2) marca
            });
            out.add("pendentes=" + jdbc.sql("select count(*) from outbox where sent = false").query(Long.class).single());
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
