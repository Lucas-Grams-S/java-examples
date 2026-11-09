package dev.estudos.outros.authserver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** OP04-02: os endpoints de descoberta respondem; o fluxo exige login/PKCE. */
@SpringBootTest
@AutoConfigureMockMvc
class AuthServerTest {
    @Autowired MockMvc mvc;

    @Test void op0402_descobertaOidc_semAutenticacao() throws Exception {
        mvc.perform(get("/.well-known/openid-configuration"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.issuer").value("http://localhost:9000"))
           .andExpect(jsonPath("$.authorization_endpoint").value("http://localhost:9000/oauth2/authorize"))
           .andExpect(jsonPath("$.token_endpoint").value("http://localhost:9000/oauth2/token"))
           .andExpect(jsonPath("$.jwks_uri").value("http://localhost:9000/oauth2/jwks"))
           .andExpect(jsonPath("$.code_challenge_methods_supported[0]").value("S256"));
    }

    @Test void op0402_jwks_publicaChaveRsa() throws Exception {
        mvc.perform(get("/oauth2/jwks"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
           .andExpect(jsonPath("$.keys[0].d").doesNotExist());   // nunca a parte privada
    }

    private static final String BASE = "/oauth2/authorize?response_type=code&client_id=orders-web&scope=openid"
            + "&redirect_uri=http://127.0.0.1:8080/login/oauth2/code/orders-web";

    // O conversor lê a query string real; no MockMvc, .param() não a preenche, por isso a URL é montada inteira.
    @Test void op0402_authorize_semLogin_redirecionaParaLogin() throws Exception {
        mvc.perform(get(BASE + "&code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM&code_challenge_method=S256")
                .accept("text/html"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/login"));
    }

    @Test void op0402_authorize_semPkce_naoEmiteCode() throws Exception {
        // a validação de PKCE acontece depois do login; sem login o usuário vai para /login e nenhum code é emitido
        mvc.perform(get(BASE).accept("text/html"))
           .andExpect(status().is3xxRedirection())
           .andExpect(header().string("Location", org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("code="))));
    }

    /** Login real pelo formulário (preenche authenticationTime, exigido pelo id_token) e devolve a sessão. */
    private org.springframework.mock.web.MockHttpSession login() throws Exception {
        var r = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/login")
                .param("username", "ana").param("password", "senha")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection()).andReturn();
        return (org.springframework.mock.web.MockHttpSession) r.getRequest().getSession(false);
    }

    /** Fluxo completo: authorize (usuário logado) -> code -> token (JWT) com o verifier do PKCE. */
    @Test void op0402_fluxoAuthorizationCodeComPkce_emiteJwt() throws Exception {
        String verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";   // exemplo da RFC 7636
        String challenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";
        var auth = mvc.perform(get(BASE + "&code_challenge=" + challenge + "&code_challenge_method=S256&state=xyz")
                        .session(login()))
                .andExpect(status().is3xxRedirection()).andReturn();
        String loc = auth.getResponse().getHeader("Location");
        org.assertj.core.api.Assertions.assertThat(loc).startsWith("http://127.0.0.1:8080/login/oauth2/code/orders-web?code=")
                .contains("state=xyz");
        String code = org.springframework.web.util.UriComponentsBuilder.fromUriString(loc).build().getQueryParams().getFirst("code");

        var body = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/oauth2/token")
                        .param("grant_type", "authorization_code").param("code", code)
                        .param("client_id", "orders-web").param("code_verifier", verifier)
                        .param("redirect_uri", "http://127.0.0.1:8080/login/oauth2/code/orders-web"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.id_token").exists())
                .andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(body).contains("access_token");
        // o access token é um JWT (três partes)
        String at = com.jayway.jsonpath.JsonPath.read(body, "$.access_token");
        org.assertj.core.api.Assertions.assertThat(at.split("\\.")).hasSize(3);
    }

    @Test void op0402_token_comVerifierErrado_eRejeitado() throws Exception {
        var auth = mvc.perform(get(BASE + "&code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM&code_challenge_method=S256")
                        .session(login()))
                .andReturn();
        String code = org.springframework.web.util.UriComponentsBuilder.fromUriString(auth.getResponse().getHeader("Location"))
                .build().getQueryParams().getFirst("code");
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/oauth2/token")
                        .param("grant_type", "authorization_code").param("code", code)
                        .param("client_id", "orders-web").param("code_verifier", "errado-errado-errado-errado-errado-errado-1")
                        .param("redirect_uri", "http://127.0.0.1:8080/login/oauth2/code/orders-web"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_grant"));
    }
}
