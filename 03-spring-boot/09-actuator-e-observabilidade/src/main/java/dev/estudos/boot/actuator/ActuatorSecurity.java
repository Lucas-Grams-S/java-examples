package dev.estudos.boot.actuator;

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SB09-15: proteger o Actuator com {@link EndpointRequest} (Boot 4: pacote
 * {@code org.springframework.boot.security.autoconfigure.actuate.web.servlet}).
 *
 * <p>Perfil {@code seguranca}: {@code health}/{@code info} abertos, o resto exige {@code ROLE_OPS}.
 * Sem o perfil, uma cadeia permissiva mantém o comportamento dos outros exemplos do módulo.
 */
@Configuration(proxyBeanMethods = false)
class ActuatorSecurity {

    @Bean @Profile("seguranca") @Order(1)
    SecurityFilterChain actuator(HttpSecurity http) throws Exception {
        http.securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(a -> a
                .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
                .anyRequest().hasRole("OPS"))
            .httpBasic(Customizer.withDefaults())
            .csrf(c -> c.disable());
        return http.build();
    }

    @Bean @Profile("seguranca") @Order(2)
    SecurityFilterChain resto(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a.anyRequest().permitAll());
        return http.build();
    }

    @Bean @Profile("!seguranca")
    SecurityFilterChain aberta(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a.anyRequest().permitAll()).csrf(c -> c.disable());
        return http.build();
    }

    @Bean @Profile("seguranca")
    UserDetailsService usuarios() {
        return new InMemoryUserDetailsManager(
            User.withUsername("ops").password("{noop}ops").roles("OPS").build(),
            User.withUsername("dev").password("{noop}dev").roles("DEV").build());
    }
}
