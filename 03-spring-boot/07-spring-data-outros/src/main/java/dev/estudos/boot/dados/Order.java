package dev.estudos.boot.dados;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

/** SB07-09: agregado como record (raiz). */
@Table("ORDERS")
public record Order(@Id Long id, String customer, BigDecimal total,
                    @MappedCollection(idColumn = "ORDERS", keyColumn = "ORDERS_KEY") List<OrderItem> items) {}
