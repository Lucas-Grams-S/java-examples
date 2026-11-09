package dev.estudos.boot.dados;

import java.util.List;
import org.springframework.data.repository.ListCrudRepository;

public interface OrderRepository extends ListCrudRepository<Order, Long> {
    List<Order> findByCustomer(String customer);
}
