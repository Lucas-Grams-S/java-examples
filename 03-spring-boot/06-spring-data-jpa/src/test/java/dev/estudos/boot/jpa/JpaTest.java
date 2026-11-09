package dev.estudos.boot.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@SpringBootTest(properties = "spring.main.web-application-type=none")
class JpaTest {
    @Autowired OrderRepository repo;
    @Autowired OrderService service;

    @BeforeEach void limpa() { repo.deleteAll(); service.seed(5); }

    @Test void sb0601_0605_entidade_e_repositorio() {
        Order o = repo.findAll().get(0);
        assertThat(o.getId()).isNotNull();
        assertThat(o.getVersion()).isEqualTo(0L);
        assertThat(repo.count()).isEqualTo(5);
        // entidade lazy fora da transação: lazy loading falha
        assertThatThrownBy(() -> o.getCustomer().getName()).isInstanceOf(LazyInitializationException.class);
    }

    @Test void sb0606_query_methods_paginacao() {
        List<Order> r = repo.findByCustomerNameOrderByTotalDesc("c1");
        assertThat(r).hasSize(1);
        Page<Order> p = repo.findByTotalGreaterThan(BigDecimal.ZERO,
            PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "total")));
        assertThat(p.getTotalElements()).isEqualTo(5);
        assertThat(p.getTotalPages()).isEqualTo(3);
        assertThat(p.getContent().get(0).getTotal()).isEqualByComparingTo("14");
        assertThat(service.zero(BigDecimal.valueOf(12))).isEqualTo(2);
    }

    @Test void sb0607_projecoes() {
        assertThat(repo.findSummaryBy()).hasSize(5);
        assertThat(repo.findSummaryBy().get(0).getTotal()).isNotNull();
        List<OrderRow> rows = repo.rows(BigDecimal.valueOf(12));
        assertThat(rows).hasSize(3);
        assertThat(rows.get(0).customerName()).isEqualTo("c2");
    }

    @Test void sb0612_config_minima() {
        assertThat(repo.count()).isEqualTo(5); // H2 + create-drop auto-configurados
    }

    @Test void sb0613_n_mais_1() {
        assertThat(service.nPlusOne()).isEqualTo("N+1: queries=6");
        assertThat(service.joinFetch()).isEqualTo("JOIN FETCH: queries=1");
        assertThat(service.entityGraph()).isEqualTo("EntityGraph items: queries=1");
    }

    @Test void sb0614_dirty_checking() {
        Long id = repo.findAll().get(0).getId();
        service.addItem(id);
        Order o = repo.allWithItems().stream().filter(x -> x.getId().equals(id)).findFirst().get();
        assertThat(o.getItems()).hasSize(2);
        assertThat(o.getVersion()).isEqualTo(1L);
    }

    @Test void sb0615_lock_otimista() {
        Long id = repo.findAll().get(0).getId();
        assertThat(service.optimisticLock(id)).isEqualTo("LOCK=ObjectOptimisticLockingFailureException");
    }

    @Test void sb0616_dto_na_transacao() {
        Long id = repo.findAll().get(0).getId();
        OrderDto dto = service.find(id);
        assertThat(dto.customer()).isEqualTo("c0");
    }
}
