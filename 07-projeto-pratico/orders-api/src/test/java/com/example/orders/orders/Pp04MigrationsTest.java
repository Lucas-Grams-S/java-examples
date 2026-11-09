package com.example.orders.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

/** PP04-01: Flyway aplica V1/V2 e o Hibernate só valida (ddl-auto=validate). */
@SpringBootTest
class Pp04MigrationsTest {
    @Autowired JdbcClient jdbc;

    @Test void historico_temV1eV2_aplicadasComSucesso() {
        var rows = jdbc.sql("select \"version\", \"success\" from \"flyway_schema_history\" where \"version\" is not null order by \"installed_rank\"")
                .query((rs, i) -> rs.getString(1) + ":" + rs.getBoolean(2)).list();
        assertThat(rows).containsExactly("1:true", "2:true");
    }

    @Test void colunaStatusVeioDaV2_comPadraoNew() {
        assertThat(jdbc.sql("select count(*) from information_schema.columns where lower(table_name)='orders' and lower(column_name)='status'")
                .query(Long.class).single()).isEqualTo(1);
    }

    /** Verificação do roteiro: editar migração já aplicada muda o checksum e o Flyway recusa (validate). */
    @Test void editarMigracaoAplicada_quebraOChecksum(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("V1__t.sql"), "create table t (id int);");
        Flyway.configure().dataSource("jdbc:h2:mem:pp04;DB_CLOSE_DELAY=-1", "sa", "").locations("filesystem:" + dir).load().migrate();
        Files.writeString(dir.resolve("V1__t.sql"), "create table t (id int, extra int);");   // "só um ajuste"
        var flyway = Flyway.configure().dataSource("jdbc:h2:mem:pp04;DB_CLOSE_DELAY=-1", "sa", "").locations("filesystem:" + dir).load();
        assertThatThrownBy(flyway::validate).hasMessageContaining("checksum");
    }
}
