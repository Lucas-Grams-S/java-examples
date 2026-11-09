package dev.estudos.boot.dados;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

/** SB07-08: JdbcClient. */
@Repository
public class ProductQueries {
    public record Row(long id, String name) {}
    private final JdbcClient jdbc;
    ProductQueries(JdbcClient jdbc) { this.jdbc = jdbc; }

    public long insert(String name, BigDecimal price) {
        var keys = new GeneratedKeyHolder();
        jdbc.sql("insert into p(name, price) values (:name, :price)")
            .param("name", name).param("price", price).update(keys);
        return keys.getKey().longValue();
    }
    public List<Row> cheaperThan(BigDecimal max) {
        return jdbc.sql("select id, name from p where price < ?").param(max).query(Row.class).list();
    }
    public Optional<String> nameOf(long id) {
        return jdbc.sql("select name from p where id = ?").param(id).query(String.class).optional();
    }
    public long count() { return jdbc.sql("select count(*) from p").query(Long.class).single(); }
    public String nameOfSingle(long id) {
        return jdbc.sql("select name from p where id = ?").param(id).query(String.class).single();
    }
}
