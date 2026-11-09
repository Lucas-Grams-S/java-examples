package dev.estudos.boot.seguranca;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/** SB08-03 (lambda DSL), SB08-05 (UserDetailsService), SB08-12/13 (JWT), SB08-15 (form login). */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    static final byte[] KEY = "uma-chave-simetrica-de-32-bytes!!".getBytes(StandardCharsets.UTF_8);

    /** Aplicação web clássica: sessão + formulário, só sob /web/**. */
    @Bean @Order(1)
    SecurityFilterChain web(HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/web/**", "/login", "/logout")
            .authorizeHttpRequests(a -> a.requestMatchers("/login").permitAll().anyRequest().authenticated())
            .formLogin(Customizer.withDefaults())
            .logout(Customizer.withDefaults())
            .build();   // CSRF continua habilitado
    }

    /** API stateless com JWT. */
    @Bean @Order(2)
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/public/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/orders/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(jwtAuthConverter())))
            .build();
    }

    @Bean PasswordEncoder passwordEncoder() { return PasswordEncoderFactories.createDelegatingPasswordEncoder(); }

    @Bean
    UserDetailsService users(PasswordEncoder pe) {
        return new InMemoryUserDetailsManager(
            User.withUsername("ana").password(pe.encode("secret")).roles("USER").build(),
            User.withUsername("root").password(pe.encode("s3cret")).roles("ADMIN").build());
    }

    @Bean JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(KEY, "HmacSHA256")).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean JwtEncoder jwtEncoder() {
        return NimbusJwtEncoder.withSecretKey(new SecretKeySpec(KEY, "HmacSHA256")).build();
    }

    /** SB08-13: scope -> SCOPE_x (padrão) + claim "roles" -> ROLE_x. */
    @Bean
    JwtAuthenticationConverter jwtAuthConverter() {
        var scopes = new JwtGrantedAuthoritiesConverter();
        var roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");
        var conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(jwt -> {
            var all = new ArrayList<GrantedAuthority>(scopes.convert(jwt));
            all.addAll(roles.convert(jwt));
            return all;
        });
        return conv;
    }
}
