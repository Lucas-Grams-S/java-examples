package dev.estudos.padroes.arquiteturais;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * DP06-16 · DDD tático: a raiz do agregado estende {@code AbstractAggregateRoot}, acumula eventos com
 * {@code registerEvent} e o Spring Data os publica quando o repositório chama {@code save}.
 * Documento: java-docs/06-padroes-de-projeto/06-padroes-arquiteturais.md §5.5.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * pay() sem save (dirty checking) -&gt; listener recebeu []
 * pay() + save -&gt; listener recebeu [OrderPaid(7)]
 * segundo save (eventos limpos) -&gt; [OrderPaid(7)]
 * </pre>
 */
public class DddTaticoEventos {
    public record OrderPaid(long id) {}

    @Entity @Table(name = "ddd_order")
    public static class Order extends AbstractAggregateRoot<Order> {
        @Id Long id; boolean paid;
        protected Order() {}
        Order(long id) { this.id = id; }
        void pay() {
            if (paid) throw new IllegalStateException("já pago");
            paid = true;
            registerEvent(new OrderPaid(id));   // publicado quando o repositório chamar save(order)
        }
    }

    public interface OrderRepository extends JpaRepository<Order, Long> {}

    public static class Listener {
        final List<String> recebidos = new ArrayList<>();
        @EventListener void on(OrderPaid e) { recebidos.add("OrderPaid(" + e.id() + ")"); }
    }

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = DddTaticoEventos.class)
    @EnableJpaRepositories(basePackageClasses = DddTaticoEventos.class, considerNestedRepositories = true)
    public static class Cfg { @Bean Listener listener() { return new Listener(); } }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new SpringApplicationBuilder(Cfg.class).web(WebApplicationType.NONE)
                .properties("spring.datasource.url=jdbc:h2:mem:dp0616;DB_CLOSE_DELAY=-1",
                            "spring.jpa.hibernate.ddl-auto=create-drop",
                            "spring.main.banner-mode=off", "logging.level.root=WARN").run()) {
            var repo = ctx.getBean(OrderRepository.class);
            var l = ctx.getBean(Listener.class);
            var tx = ctx.getBean(TransactionTemplate.class);
            var o = new Order(7);
            tx.executeWithoutResult(s -> repo.save(o));
            l.recebidos.clear();
            tx.executeWithoutResult(s -> {
                var m = repo.findById(7L).orElseThrow();
                m.pay();                                   // dirty checking, sem save
            });
            out.add("pay() sem save (dirty checking) -> listener recebeu " + l.recebidos);
            var o2 = repo.findById(7L).orElseThrow();
            o2.paid = false;
            o2.pay();
            tx.executeWithoutResult(s -> repo.save(o2));
            out.add("pay() + save -> listener recebeu " + l.recebidos);
            tx.executeWithoutResult(s -> repo.save(o2));
            out.add("segundo save (eventos limpos) -> " + l.recebidos);
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
