package com.example.orders.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.orders.orders.OrderRequests.CreateOrderRequest;
import com.example.orders.orders.OrderRequests.ItemRequest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** PP05-01: ADMIN cancela qualquer pedido, USER só os próprios; token real assinado; armadilha da autochamada. */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestData.class, SelfInvocationDemo.class})
class Pp05SecurityTest {
    @Autowired MockMvc mvc;
    @Autowired OrderService service;
    @Autowired OrderRepository orders;
    @Autowired TestData data;
    @Autowired SelfInvocationDemo demo;

    static RequestPostProcessor as(String email, String role) {
        return jwt().jwt(j -> j.subject(email)).authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    /** Cria um pedido de {@code owner} e devolve o id (o serviço é chamado sem contexto de segurança: create não é protegido). */
    long orderOf(com.example.orders.customers.Customer owner) {
        var p = data.product("3.00", 100);
        return service.create(new CreateOrderRequest(owner.getId(), List.of(new ItemRequest(p.id(), 1)))).id();
    }

    @Test void usuarioCancelaOProprio_naoOAlheio_adminCancelaQualquer() throws Exception {
        var ana = data.customer("ana"); var bia = data.customer("bia");
        long deAna = orderOf(ana), deBia = orderOf(bia), outro = orderOf(bia);

        mvc.perform(post("/orders/" + deAna + "/cancel").with(as(ana.getEmail(), "USER"))).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELED"));
        mvc.perform(post("/orders/" + deBia + "/cancel").with(as(ana.getEmail(), "USER"))).andExpect(status().isForbidden());
        mvc.perform(get("/orders/" + deBia).with(as(ana.getEmail(), "USER"))).andExpect(status().isForbidden());
        mvc.perform(post("/orders/" + deBia + "/cancel").with(as("root@x.com", "ADMIN"))).andExpect(status().isOk());
        mvc.perform(post("/orders/" + outro + "/cancel")).andExpect(status().isUnauthorized());
    }

    @Test void cancelarDuasVezes_ou_enviado_409() throws Exception {
        var ana = data.customer("ana2");
        long id = orderOf(ana);
        mvc.perform(post("/orders/" + id + "/cancel").with(as(ana.getEmail(), "USER"))).andExpect(status().isOk());
        mvc.perform(post("/orders/" + id + "/cancel").with(as(ana.getEmail(), "USER"))).andExpect(status().isConflict());

        long enviado = orderOf(ana);
        var e = orders.findById(enviado).orElseThrow();
        e.setStatus(OrderStatus.SHIPPED);
        orders.save(e);
        mvc.perform(post("/orders/" + enviado + "/cancel").with(as(ana.getEmail(), "USER")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.detail").value("shipped order cannot be canceled"));
    }

    /** Token realmente assinado (HS256) e decodificado pelo resource server; a claim "roles" vira ROLE_x. */
    @Test void tokenAssinado_valido_invalido_eRoles() throws Exception {
        var ana = data.customer("ana3");
        long id = orderOf(ana);
        mvc.perform(get("/orders/" + id).header("Authorization", "Bearer " + token("uma-chave-simetrica-de-32-bytes!!", ana.getEmail(), "USER")))
                .andExpect(status().isOk());
        mvc.perform(get("/orders/" + id).header("Authorization", "Bearer " + token("outra-chave-simetrica-de-32-byte!", ana.getEmail(), "USER")))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/orders/" + id).header("Authorization", "Bearer " + token("uma-chave-simetrica-de-32-bytes!!", "root@x.com", "ADMIN")))
                .andExpect(status().isOk());
    }

    static String token(String secret, String subject, String role) {
        var enc = new NimbusJwtEncoder(new com.nimbusds.jose.jwk.source.ImmutableSecret<>(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")));
        var claims = JwtClaimsSet.builder().subject(subject).issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300)).claim("roles", List.of(role)).build();
        return enc.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    /** Verificação do roteiro: método @PreAuthorize chamado por outro método da mesma classe não é protegido. */
    @Test @WithMockUser(roles = "USER")
    void autochamada_naoAplicaOPreAuthorize() {
        assertThatThrownBy(() -> demo.adminOnly()).isInstanceOf(AccessDeniedException.class);   // de fora: proxy aplica
        assertThat(demo.viaThis()).isEqualTo("segredo");                                          // por this: passa direto
        SecurityContextHolder.clearContext();
    }
}
