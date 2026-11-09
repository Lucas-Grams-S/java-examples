package dev.estudos.springcore.eventos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * SC08-09 · Ouvinte assíncrono ({@code @Async}, outra thread) e ouvinte "após o commit"
 * ({@code @TransactionalEventListener}). O evento só é entregue ao ouvinte transacional depois do commit; com
 * rollback ele é descartado. Documento: 02-spring-core/08-eventos-validacao-e-conversao.md §5.2.
 *
 * <p>Saída real:
 * <pre>
 * ouvinte comum (na transação): o-1
 * após o commit -&gt; AFTER_COMMIT: o-1
 * ouvinte comum (na transação): o-2      (rollback: o AFTER_COMMIT não roda)
 * rollback -&gt; AFTER_COMMIT não recebeu o-2: true
 * @Async rodou em thread diferente da principal: true
 * </pre>
 */
public class AssincronoEPosCommit {
    public record OrderPlaced(String id) {}

    static final List<String> out = Collections.synchronizedList(new ArrayList<>());
    static final CountDownLatch asyncDone = new CountDownLatch(1);
    static volatile String asyncThread;

    @Component static class Listeners {
        @EventListener void comum(OrderPlaced e) { out.add("ouvinte comum (na transação): " + e.id()); }

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        void afterCommit(OrderPlaced e) { out.add("após o commit -> AFTER_COMMIT: " + e.id()); }
    }

    @Component static class AsyncListener {
        @Async @EventListener void on(String msg) { asyncThread = Thread.currentThread().getName(); asyncDone.countDown(); }
    }

    @Component static class OrderService {
        private final ApplicationEventPublisher pub;
        OrderService(ApplicationEventPublisher pub) { this.pub = pub; }
        @Transactional public void place(String id, boolean fail) {
            pub.publishEvent(new OrderPlaced(id));
            if (fail) throw new IllegalStateException("falha -> rollback");
        }
        public void async() { pub.publishEvent("ping"); }
    }

    @Configuration @EnableTransactionManagement @EnableAsync
    static class Cfg {
        @Bean DataSource ds() { return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).generateUniqueName(true).build(); }
        @Bean PlatformTransactionManager tm(DataSource ds) { return new DataSourceTransactionManager(ds); }
    }

    public static List<String> run() throws Exception {
        out.clear();
        try (var ctx = new AnnotationConfigApplicationContext(Cfg.class, Listeners.class, AsyncListener.class, OrderService.class)) {
            var svc = ctx.getBean(OrderService.class);
            svc.place("o-1", false);
            try { svc.place("o-2", true); } catch (IllegalStateException expected) { }
            svc.async();
            asyncDone.await(5, TimeUnit.SECONDS);
        }
        List<String> r = new ArrayList<>(out);
        r.add("rollback -> AFTER_COMMIT não recebeu o-2: " + out.stream().noneMatch(s -> s.contains("AFTER_COMMIT: o-2")));
        r.add("@Async rodou em thread diferente da principal: " + (asyncThread != null && !asyncThread.equals("main")));
        return r;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
