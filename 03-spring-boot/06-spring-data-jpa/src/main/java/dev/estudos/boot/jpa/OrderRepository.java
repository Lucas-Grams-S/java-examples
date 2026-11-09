package dev.estudos.boot.jpa;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** SB06-05 (repositório), SB06-06 (query methods, @Query, paginação), SB06-07 (projeções). */
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerNameOrderByTotalDesc(String name);

    Page<Order> findByTotalGreaterThan(BigDecimal min, Pageable pageable);

    @Query("select o from Order o join fetch o.customer")
    List<Order> allWithCustomer();

    @EntityGraph(attributePaths = "items")
    @Query("select o from Order o")
    List<Order> allWithItems();

    @Modifying
    @Query("update Order o set o.total = 0 where o.total < :x")
    int zero(@Param("x") BigDecimal x);

    List<OrderSummary> findSummaryBy();

    @Query("select new dev.estudos.boot.jpa.OrderRow(o.id, o.customer.name, o.total) from Order o where o.total >= :min order by o.id")
    List<OrderRow> rows(@Param("min") BigDecimal min);
}
