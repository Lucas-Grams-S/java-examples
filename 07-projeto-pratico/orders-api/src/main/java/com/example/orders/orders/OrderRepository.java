package com.example.orders.orders;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    /** Corrige o N+1: itens vêm no mesmo SELECT (join). */
    @EntityGraph(attributePaths = "items")
    @Query("select o from OrderEntity o")
    List<OrderEntity> findAllWithItems();

    @Query("select o.customerId from OrderEntity o where o.id = :id")
    java.util.Optional<Long> customerIdOf(@Param("id") Long id);

    @Modifying
    @Query("update OrderEntity o set o.status = com.example.orders.orders.OrderStatus.CANCELED where o.status = com.example.orders.orders.OrderStatus.NEW and o.createdAt < :cutoff")
    int expireNewBefore(@Param("cutoff") Instant cutoff);
}
