package dev.estudos.boot.seguranca;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SegurancaTest {
    @Autowired MockMvc mvc;
    @Autowired JwtEncoder encoder;

    String token(String sub, String scope, List<String> roles) {
        var b = JwtClaimsSet.builder().subject(sub).issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300));
        if (scope != null) b.claim("scope", scope);
        if (roles != null) b.claim("roles", roles);
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), b.build())).getTokenValue();
    }
    String bearer(String t) { return "Bearer " + t; }

    @Test void sb0811_dependencias_protegem_tudo() throws Exception {
        mvc.perform(get("/qualquer")).andExpect(status().isUnauthorized());
    }

    @Test void sb0812_tabela_de_status_jwt() throws Exception {
        mvc.perform(get("/public/ping")).andExpect(status().isOk());
        mvc.perform(get("/orders")).andExpect(status().isUnauthorized());
        String ok = token("ana", "orders:read", null);
        mvc.perform(get("/orders").header(HttpHeaders.AUTHORIZATION, bearer(ok))).andExpect(status().isOk());
        mvc.perform(get("/orders").header(HttpHeaders.AUTHORIZATION, "Bearer abc.def.ghi")).andExpect(status().isUnauthorized());
        mvc.perform(post("/orders").header(HttpHeaders.AUTHORIZATION, bearer(ok))).andExpect(status().isForbidden());
        String w = token("ana", "orders:read orders:write", null);
        mvc.perform(post("/orders").header(HttpHeaders.AUTHORIZATION, bearer(w))).andExpect(status().isOk());
        mvc.perform(delete("/orders/1").header(HttpHeaders.AUTHORIZATION, bearer(w))).andExpect(status().isForbidden());
    }

    @Test void sb0813_roles_de_claim_propria() throws Exception {
        String admin = token("root", "orders:read", List.of("ADMIN"));
        mvc.perform(delete("/orders/1").header(HttpHeaders.AUTHORIZATION, bearer(admin))).andExpect(status().isOk());
    }

    @Test void sb0807_seguranca_por_metodo() throws Exception {
        String ana = token("ana", "orders:read", null);
        mvc.perform(get("/customers/ana").header(HttpHeaders.AUTHORIZATION, bearer(ana))).andExpect(status().isOk());
        mvc.perform(get("/customers/bob").header(HttpHeaders.AUTHORIZATION, bearer(ana))).andExpect(status().isForbidden());
    }

    @Test void sb0814_testando_seguranca() throws Exception {
        mvc.perform(delete("/orders/2").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
           .andExpect(status().isOk());
        mvc.perform(delete("/orders/2").with(jwt())).andExpect(status().isForbidden());
    }

    @Test void sb0815_form_login_e_sessao() throws Exception {
        mvc.perform(get("/web/home")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        // POST sem CSRF é recusado; com CSRF e credenciais válidas redireciona e abre sessão
        mvc.perform(post("/login").param("username", "ana").param("password", "secret")).andExpect(status().isForbidden());
        var res = mvc.perform(post("/login").param("username", "ana").param("password", "secret").with(csrf()))
            .andExpect(status().is3xxRedirection()).andReturn();
        var session = res.getRequest().getSession(false);
        mvc.perform(get("/web/home").session((org.springframework.mock.web.MockHttpSession) session))
           .andExpect(status().isOk()).andExpect(content().string("olá ana"));
        mvc.perform(post("/login").param("username", "ana").param("password", "errada").with(csrf()))
           .andExpect(redirectedUrl("/login?error"));
    }

    @Test void sb0805_usuarios_em_memoria() throws Exception {
        mvc.perform(get("/web/home").with(httpBasic("root", "s3cret"))).andExpect(status().is3xxRedirection());
    }
}
