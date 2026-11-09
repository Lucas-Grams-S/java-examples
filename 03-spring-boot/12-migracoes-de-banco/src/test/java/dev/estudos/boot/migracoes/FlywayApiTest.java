package dev.estudos.boot.migracoes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** SB12-06: banco legado e baseline; checksum alterado (§8.1). Usa a API do Flyway sobre H2 isolados. */
class FlywayApiTest {
    @TempDir Path dir;

    private String legado(String nome) throws Exception {
        String url = "jdbc:h2:mem:" + nome + ";DB_CLOSE_DELAY=-1";
        try (var c = DriverManager.getConnection(url, "sa", ""); var st = c.createStatement()) {
            st.execute("create table legacy (id int primary key)");
        }
        return url;
    }

    @Test void sb1206_semBaseline_recusaEsquemaNaoVazio() throws Exception {
        String url = legado("legado1");
        assertThatThrownBy(() -> Flyway.configure().dataSource(url, "sa", "")
                .locations("classpath:db/migration").load().migrate())
            .isInstanceOf(FlywayException.class)
            .hasMessageContaining("Found non-empty schema(s)")
            .hasMessageContaining("but no schema history table");
    }

    @Test void sb1206_comBaseline_registraBaselineEAplicaPorCima() throws Exception {
        String url = legado("legado2");
        Flyway f = Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration")
            .baselineOnMigrate(true).baselineVersion("0").load();
        f.migrate();
        List<String> h = java.util.Arrays.stream(f.info().all()).map(i -> i.getVersion() + ":" + i.getType()).toList();
        assertThat(h.get(0)).isEqualTo("0:BASELINE");
        assertThat(h).contains("1:SQL", "2:SQL", "3:JDBC");
    }

    @Test void sb1206_editarMigracaoAplicada_falhaNaValidacao() throws Exception {
        Path v1 = dir.resolve("V1__t.sql");
        Files.writeString(v1, "create table t (id int);");
        String url = "jdbc:h2:mem:checksum;DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url, "sa", "").locations("filesystem:" + dir).load().migrate();
        Files.writeString(v1, "create table t (id int, extra int);");      // editou depois de aplicada
        assertThatThrownBy(() -> Flyway.configure().dataSource(url, "sa", "").locations("filesystem:" + dir).load().migrate())
            .hasMessageContaining("checksum mismatch");
    }

    @Test void sb1201_nomeComUmSublinhado_naoEReconhecido() throws Exception {
        Files.writeString(dir.resolve("V1_create.sql"), "create table x (id int);");
        var f = Flyway.configure().dataSource("jdbc:h2:mem:nome;DB_CLOSE_DELAY=-1", "sa", "")
            .locations("filesystem:" + dir).load();
        assertThat(f.info().all()).isEmpty();                 // ignorado: nada para migrar
    }
}
