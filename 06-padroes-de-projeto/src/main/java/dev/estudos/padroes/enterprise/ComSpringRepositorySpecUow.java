package dev.estudos.padroes.enterprise;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * DP05-15 · Com Spring: Repository (proxy), Specification (Criteria API) e Unit of Work (dirty checking) reais.
 * Documento: java-docs/06-padroes-de-projeto/05-padroes-enterprise.md §5.6 (e §3.1–3.3).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * repo é proxy? true
 * identity map: mesma instância = true
 * antes do flush: updates=0
 * depois do flush: updates=1
 * pago e &gt;60: [Cid]
 * pago ou &gt;60: 2
 * </pre>
 */
public class ComSpringRepositorySpecUow {

    @Entity @Table(name = "orders")
    public static class Order {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
        String customerName; String status = "NEW"; BigDecimal total;
        protected Order() {}
        Order(String c, BigDecimal t) { customerName = c; total = t; }
        void pay() { status = "PAID"; }
    }

    public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {}

    static final class OrderSpecs {
        static Specification<Order> paid() { return (r, q, cb) -> cb.equal(r.get("status"), "PAID"); }
        static Specification<Order> totalAbove(BigDecimal v) { return (r, q, cb) -> cb.greaterThan(r.get("total"), v); }
    }

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = ComSpringRepositorySpecUow.class)
    @EnableJpaRepositories(basePackageClasses = ComSpringRepositorySpecUow.class,
                           considerNestedRepositories = true)
    public static class Config {}

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(Config.class)
                .web(org.springframework.boot.WebApplicationType.NONE)
                .properties("spring.datasource.url=jdbc:h2:mem:dp0515;DB_CLOSE_DELAY=-1",
                            "spring.jpa.hibernate.ddl-auto=create-drop",
                            "spring.jpa.properties.hibernate.generate_statistics=true",
                            "spring.main.banner-mode=off", "logging.level.root=WARN")
                .run()) {
            var repo = ctx.getBean(OrderRepository.class);
            var tx = ctx.getBean(TransactionTemplate.class);
            var em = ctx.getBean(EntityManager.class);
            var stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();

            out.add("repo é proxy? " + java.lang.reflect.Proxy.isProxyClass(repo.getClass()));
            Long anaId = tx.execute(s -> {
                var a = repo.save(new Order("Ana", new BigDecimal("50")));
                repo.save(new Order("Bob", new BigDecimal("10")));
                repo.save(new Order("Cid", new BigDecimal("90")));
                return a.id;
            });
            // Cid já pago (inserido pago): ajuste dentro da transação
            tx.executeWithoutResult(s -> repo.findAll().stream()
                .filter(o -> o.customerName.equals("Cid")).forEach(Order::pay));

            stats.clear();
            tx.executeWithoutResult(s -> {
                Order o = repo.findById(anaId).orElseThrow();
                Order de_novo = repo.findById(anaId).orElseThrow();
                out.add("identity map: mesma instância = " + (o == de_novo));
                o.pay();                                    // sem repo.save(o)
                out.add("antes do flush: updates=" + stats.getEntityUpdateCount());
                repo.flush();
                out.add("depois do flush: updates=" + stats.getEntityUpdateCount());
            });

            var pagoEMaior = repo.findAll(OrderSpecs.paid().and(OrderSpecs.totalAbove(new BigDecimal("60"))));
            out.add("pago e >60: " + pagoEMaior.stream().map(o -> o.customerName).toList());
            var pagoOuMaior = repo.findAll(OrderSpecs.paid().or(OrderSpecs.totalAbove(new BigDecimal("60"))));
            out.add("pago ou >60: " + pagoOuMaior.size());
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
