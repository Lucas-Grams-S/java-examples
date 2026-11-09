package dev.estudos.outros.authserver;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
public class AuthServerConfig {

    /** Cadeia própria do servidor de autorização (endpoints /oauth2/**, /.well-known/**, OIDC). */
    @Bean @Order(1)
    SecurityFilterChain authorizationServer(HttpSecurity http) throws Exception {
        var as = new OAuth2AuthorizationServerConfigurer();
        http.securityMatcher(as.getEndpointsMatcher())          // só os endpoints do servidor de autorização
            .with(as, c -> c.oidc(Customizer.withDefaults()))
            .authorizeHttpRequests(a -> a.anyRequest().authenticated());
        http.exceptionHandling(e -> e.defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint("/login"), new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));
        return http.build();
    }

    /** Cadeia padrão: tudo exige login (formulário), é onde o usuário se autentica. */
    @Bean @Order(2)
    SecurityFilterChain padrao(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a.anyRequest().authenticated()).formLogin(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    RegisteredClientRepository clients() {
        RegisteredClient web = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("orders-web")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)      // cliente público + PKCE
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://127.0.0.1:8080/login/oauth2/code/orders-web")
                .scope(OidcScopes.OPENID).scope("orders.read")
                .clientSettings(ClientSettings.builder().requireProofKey(true).build())
                .build();
        return new InMemoryRegisteredClientRepository(web);
    }

    @Bean
    UserDetailsService users() {
        return new InMemoryUserDetailsManager(User.withUsername("ana").password("{noop}senha").roles("USER").build());
    }

    @Bean
    AuthorizationServerSettings settings() {
        return AuthorizationServerSettings.builder().issuer("http://localhost:9000").build();
    }

    /** Chave RSA gerada na inicialização (só para estudo: em produção, chave persistente e rotacionável). */
    @Bean
    JWKSource<SecurityContext> jwkSource() throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
        g.initialize(2048);
        KeyPair kp = g.generateKeyPair();
        RSAKey rsa = new RSAKey.Builder((RSAPublicKey) kp.getPublic()).privateKey((RSAPrivateKey) kp.getPrivate())
                .keyID(UUID.randomUUID().toString()).build();
        return new ImmutableJWKSet<>(new JWKSet(rsa));
    }
}
