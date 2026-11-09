package dev.estudos.boot.mongo;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("orders")
public record Order(@Id String id, String customer, List<Item> items, BigDecimal total) {
    public record Item(String sku, int quantity) {}
}
