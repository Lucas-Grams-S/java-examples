package dev.estudos.boot.testes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

/** SB10-13: H2 embutido (substitui o DataSource), rollback ao fim; Flyway cria o schema. */
@DataJpaTest
class OrderRepositoryTest {
    @Autowired OrderRepository repo;
    @Autowired TestEntityManager em;

    @Test void sb1013_achaPorCliente() {
        em.persistAndFlush(new Order("Ana"));
        assertThat(repo.findByCustomer("Ana")).hasSize(1);
    }
}
