package dev.estudos.outros.restdocs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** OP09-02. */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/snippets")
class OrderDocsTest {
    @Autowired MockMvc mvc;

    @Test void op0902_getOrder_geraSnippets() throws Exception {
        mvc.perform(get("/orders/9")).andExpect(status().isOk()).andDo(document("get-order"));
        try (Stream<Path> s = Files.list(Path.of("target/snippets/get-order"))) {
            assertThat(s.map(p -> p.getFileName().toString()).toList()).containsExactlyInAnyOrder(
                    "curl-request.adoc", "http-request.adoc", "http-response.adoc",
                    "httpie-request.adoc", "request-body.adoc", "response-body.adoc");
        }
        assertThat(Files.readString(Path.of("target/snippets/get-order/http-response.adoc"))).contains("\"status\":\"PAID\"");
    }

    @Test void op0902_documentandoCampos() throws Exception {
        mvc.perform(get("/orders/9")).andExpect(status().isOk())
           .andDo(document("get-order-fields", responseFields(
                   fieldWithPath("id").description("Identificador do pedido"),
                   fieldWithPath("status").description("PAID, SHIPPED..."))));
        assertThat(Files.readString(Path.of("target/snippets/get-order-fields/response-fields.adoc")))
                .contains("Identificador do pedido");
    }

    /** Campo da resposta sem documentação => a documentação falha (protege contra drift). */
    @Test void op0902_campoNaoDocumentado_falha() {
        assertThatThrownBy(() -> mvc.perform(get("/orders/9"))
                .andDo(document("get-order-incompleto", responseFields(fieldWithPath("id").description("id")))))
                .hasMessageContaining("status");
    }
}
