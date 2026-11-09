package dev.estudos.boot.dados;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

@SpringBootTest
class JdbcTest {
    @Autowired ProductQueries q;
    @Autowired OrderRepository repo;

    @Test void sb0708_jdbcclient() {
        long id = q.insert("Mouse", new BigDecimal("50.00"));
        assertThat(q.cheaperThan(new BigDecimal("100"))).contains(new ProductQueries.Row(id, "Mouse"));
        assertThat(q.nameOf(id)).contains("Mouse");
        assertThat(q.nameOf(-1)).isEqualTo(Optional.empty());
        assertThat(q.count()).isGreaterThanOrEqualTo(1);
        assertThatThrownBy(() -> q.nameOfSingle(-1)).isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @Test void sb0709_spring_data_jdbc_agregado() {
        Order saved = repo.save(new Order(null, "ana", BigDecimal.TEN,
            List.of(new OrderItem("a", 1), new OrderItem("b", 2))));
        assertThat(saved.id()).isNotNull();
        assertThat(repo.findById(saved.id()).get().items()).hasSize(2);
        assertThat(repo.findByCustomer("ana")).hasSize(1);
    }
}
