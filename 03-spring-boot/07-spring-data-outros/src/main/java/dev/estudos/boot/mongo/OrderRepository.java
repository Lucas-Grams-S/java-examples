package dev.estudos.boot.mongo;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByCustomer(String customer);
    List<Order> findByItemsSku(String sku);
}
