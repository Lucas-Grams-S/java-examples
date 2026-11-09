package dev.estudos.boot.migracoes;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

@SpringBootTest
class MigracoesTest {
    @Autowired JdbcClient jdbc;

    @Test void sb1208_1010_1011_historicoDasMigracoes() {
        List<String> h = jdbc.sql("select coalesce(\"version\",'null')||':'||\"type\" from \"flyway_schema_history\" order by \"installed_rank\"")
            .query(String.class).list();
        // versionadas em ordem (V1, V2, V3 Java) e a repetível (versão null) por último
        assertThat(h).filteredOn(x -> !x.endsWith(":TABLE")).containsExactly("1:SQL", "2:SQL", "3:JDBC", "null:SQL");
        assertThat(jdbc.sql("select customer from v_orders where id = 1").query(String.class).single())
            .isEqualTo("Ana");            // V3 (Java) aparou os espaços
    }
}
