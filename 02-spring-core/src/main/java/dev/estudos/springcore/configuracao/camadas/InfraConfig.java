package dev.estudos.springcore.configuracao.camadas;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class InfraConfig {
    @Bean public Clock clock() { return Clock.systemUTC(); }
}
