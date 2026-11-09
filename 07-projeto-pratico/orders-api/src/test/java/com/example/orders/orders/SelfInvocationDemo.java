package com.example.orders.orders;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/** Só para teste: mostra a armadilha da autochamada com @PreAuthorize (proxy). */
@Component
class SelfInvocationDemo {
    @PreAuthorize("hasRole('ADMIN')")
    String adminOnly() { return "segredo"; }

    String viaThis() { return this.adminOnly(); }   // chamada interna: não passa pelo proxy => sem checagem
}
