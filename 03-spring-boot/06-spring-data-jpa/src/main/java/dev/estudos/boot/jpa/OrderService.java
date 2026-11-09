package dev.estudos.boot.jpa;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.stream.IntStream;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** SB06-13..16: N+1, dirty checking, lock otimista e serviço com DTO. */
@Service
public class OrderService {
    private final OrderRepository repo;
    private final EntityManager em;
    private final TransactionTemplate tx;

    OrderService(OrderRepository repo, EntityManager em, TransactionTemplate tx) { this.repo = repo; this.em = em; this.tx = tx; }

    private Statistics stats() { return em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics(); }

    @Transactional
    public void seed(int n) {
        IntStream.range(0, n).forEach(i -> {
            Order o = new Order(em.merge(new Customer("c" + i)));
            o.add("sku" + i, 1, BigDecimal.valueOf(10 + i));
            repo.save(o);
        });
    }

    @Transactional
    public String nPlusOne() {
        em.clear(); stats().clear();
        repo.findAll().forEach(o -> o.getCustomer().getName());
        return "N+1: queries=" + stats().getPrepareStatementCount();
    }

    @Transactional
    public String joinFetch() {
        em.clear(); stats().clear();
        repo.allWithCustomer().forEach(o -> o.getCustomer().getName());
        return "JOIN FETCH: queries=" + stats().getPrepareStatementCount();
    }

    @Transactional
    public String entityGraph() {
        em.clear(); stats().clear();
        repo.allWithItems().forEach(o -> o.getItems().size());
        return "EntityGraph items: queries=" + stats().getPrepareStatementCount();
    }

    /** SB06-14: sem repo.save(); o flush grava. */
    @Transactional
    public void addItem(Long id) {
        Order o = repo.findById(id).orElseThrow();
        o.add("sku", 1, BigDecimal.ONE);
    }

    /** SB06-15: lock otimista (a exceção sai da transação, que dá rollback). */
    public String optimisticLock(Long id) {
        try {
            tx.executeWithoutResult(st -> {
                var a = repo.findById(id).get();
                em.detach(a);
                var b = repo.findById(id).get();
                b.add("z", 1, BigDecimal.ONE);
                em.flush(); em.clear();
                a.setTotal(BigDecimal.valueOf(999));
                repo.saveAndFlush(a);
            });
            return "sem conflito";
        } catch (Exception e) {
            return "LOCK=" + e.getClass().getSimpleName();
        }
    }

    /** SB06-06: update em massa precisa de transação. */
    @Transactional
    public int zero(BigDecimal x) { return repo.zero(x); }

    /** SB06-16: DTO na saída, dentro da transação. */
    @Transactional(readOnly = true)
    public OrderDto find(Long id) {
        Order o = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("pedido " + id));
        return new OrderDto(o.getId(), o.getCustomer().getName(), o.getTotal());
    }
}
