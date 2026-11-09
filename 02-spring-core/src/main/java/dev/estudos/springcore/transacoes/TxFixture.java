package dev.estudos.springcore.transacoes;

import java.io.IOException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/** Infraestrutura compartilhada dos exemplos SC09 (não é um exemplo): H2 em memória, beans e serviço de teste. */
public class TxFixture {
    @Repository
    public static class Orders {
        private final JdbcClient jdbc;
        Orders(JdbcClient jdbc) { this.jdbc = jdbc; }
        public void insert(String id) { jdbc.sql("insert into orders(id) values (?)").param(id).update(); }
        public int count() { return jdbc.sql("select count(*) from orders").query(Integer.class).single(); }
        public void clear() { jdbc.sql("delete from orders").update(); jdbc.sql("delete from audit").update(); }
        public int auditCount() { return jdbc.sql("select count(*) from audit").query(Integer.class).single(); }
    }

    @Service
    public static class Audit {
        private final JdbcClient jdbc;
        Audit(JdbcClient jdbc) { this.jdbc = jdbc; }
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void logNew(String id) { jdbc.sql("insert into audit(id) values (?)").param(id).update(); }
        @Transactional   // REQUIRED: participa da transação do chamador e a marca rollback-only
        public void fail() { throw new IllegalStateException("audit falhou"); }
        @Transactional(propagation = Propagation.MANDATORY)
        public void mandatory() {}
    }

    @Service
    public static class OrderService {
        private final Orders orders; private final Audit audit;
        OrderService(Orders o, Audit a) { orders = o; audit = a; }

        @Transactional public void okCommit(String id) { orders.insert(id); }

        @Transactional public void runtimeFail(String id) {
            orders.insert(id); throw new IllegalStateException("boom");
        }

        @Transactional public void checkedFail(String id) throws IOException {
            orders.insert(id); throw new IOException("checked");
        }

        @Transactional(rollbackFor = IOException.class)
        public void checkedRollback(String id) throws IOException {
            orders.insert(id); throw new IOException("checked");
        }

        @Transactional public void withAudit(String id) {
            orders.insert(id); audit.logNew(id);
            throw new IllegalStateException("boom");
        }

        /** audit.fail() participa (REQUIRED) da transação externa e a marca rollback-only; engolir a exceção não adianta. */
        @Transactional public void swallow(String id) {
            orders.insert(id);
            try { audit.fail(); } catch (IllegalStateException e) { /* engolida */ }
        }

        @Transactional public void setRollbackOnly(String id) {
            orders.insert(id);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        public void inner(String id) { outerSelf(id); }                 // self-invocation
        @Transactional public void outerSelf(String id) { orders.insert(id); throw new IllegalStateException("x"); }

        public void noTx() { audit.mandatory(); }
    }

    @Configuration @EnableTransactionManagement
    public static class Cfg {
        @Bean DataSource dataSource() {
            return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).generateUniqueName(true)
                .addScript("classpath:schema-tx.sql").build();
        }
        @Bean PlatformTransactionManager transactionManager(DataSource ds) { return new DataSourceTransactionManager(ds); }
        @Bean JdbcClient jdbcClient(DataSource ds) { return JdbcClient.create(ds); }
    }

    public static AnnotationConfigApplicationContext novoContexto() {
        return new AnnotationConfigApplicationContext(Cfg.class, Orders.class, Audit.class, OrderService.class);
    }

    /** Roda o cenário e devolve "exceção -> linhas". */
    public interface Cenario { void run() throws Exception; }

    public static String executar(Orders orders, Cenario c) {
        orders.clear();
        String ex = "nenhuma";
        try { c.run(); } catch (Exception e) { ex = e.getClass().getSimpleName(); }
        return ex + " | linhas=" + orders.count() + " | auditoria=" + orders.auditCount();
    }
}
