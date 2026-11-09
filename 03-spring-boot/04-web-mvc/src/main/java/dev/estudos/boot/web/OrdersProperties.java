package dev.estudos.boot.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("orders")
public record OrdersProperties(String currency) {}
