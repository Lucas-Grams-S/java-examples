package dev.estudos.springcore.configuracao.camadas;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class PaymentConfig {
    public record Gateway(String url, Clock clock) {}
    @Bean public Gateway gateway(@Value("${payments.url}") String url, Clock clock) { return new Gateway(url, clock); }
}
