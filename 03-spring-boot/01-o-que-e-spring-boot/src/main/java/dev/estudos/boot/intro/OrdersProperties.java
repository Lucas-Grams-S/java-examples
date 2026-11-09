package dev.estudos.boot.intro;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Propriedades mínimas usadas pelo {@link StartupLogger}. */
@ConfigurationProperties("orders")
public record OrdersProperties(@DefaultValue("BRL") String currency) {}
