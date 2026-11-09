package dev.estudos.outros.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

/** OP05-02. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureHttpGraphQlTester
class OrderGraphQlTest {
    @Autowired HttpGraphQlTester gql;

    @Test void op0502_orderById() {
        gql.document("{ orderById(id: \"7\") { id status } }").execute()
           .path("orderById.status").entity(String.class).isEqualTo("PAID");
    }

    @Test void op0502_batchMapping_umaChamadaParaTodosOsPedidos() {
        OrderGraphQl.LOTES.clear();
        List<String> nomes = gql.document("{ orders { id customer { name } } }").execute()
                .path("orders[*].customer.name").entityList(String.class).get();
        assertThat(nomes).containsExactly("Cliente 1", "Cliente 2", "Cliente 3");
        assertThat(OrderGraphQl.LOTES).containsExactly(3);   // 1 lote de 3, não 3 chamadas (N+1)
    }

    @Test void op0502_campoInexistente_viraErroDeValidacao() {
        gql.document("{ orderById(id: \"7\") { naoExiste } }").execute()
           .errors().satisfy(errs -> assertThat(errs).isNotEmpty());
    }
}
