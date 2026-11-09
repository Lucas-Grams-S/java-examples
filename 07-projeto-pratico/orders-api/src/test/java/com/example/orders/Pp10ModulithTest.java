package com.example.orders;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/** PP10-01: fronteiras do monólito modular verificadas por teste (sem ciclos nem acesso a internos). */
class Pp10ModulithTest {
    static final ApplicationModules modules = ApplicationModules.of(OrdersApplication.class);

    @Test void modulos_customers_catalog_orders() {
        var ids = modules.stream().map(m -> m.getIdentifier().toString()).toList();
        assertThat(ids).contains("customers", "catalog", "orders");
    }

    @Test void fronteirasValidas() { modules.verify(); }

    @Test void ordersDependeDeCatalogECustomers_nuncaOContrario() {
        var deps = (java.util.function.Function<String, java.util.List<String>>) name ->
                modules.getModuleByName(name).orElseThrow().getDirectDependencies(modules).stream()
                        .map(d -> d.getTargetModule().getIdentifier().toString()).distinct().toList();
        assertThat(deps.apply("orders")).contains("catalog", "customers");
        assertThat(deps.apply("catalog")).doesNotContain("orders", "customers");
        assertThat(deps.apply("customers")).doesNotContain("orders", "catalog");
    }
}
