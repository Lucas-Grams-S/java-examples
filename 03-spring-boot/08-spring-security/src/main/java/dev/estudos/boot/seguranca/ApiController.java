package dev.estudos.boot.seguranca;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class ApiController {
    @GetMapping("/public/ping") String ping() { return "pong"; }
    @GetMapping("/orders") String list() { return "orders"; }

    /** SB08-07: segurança por método. */
    @PreAuthorize("hasAuthority('SCOPE_orders:write')")
    @PostMapping("/orders") String create() { return "created"; }

    @DeleteMapping("/orders/{id}") String delete(@PathVariable long id) { return "deleted " + id; }

    @PreAuthorize("#id == authentication.name")
    @GetMapping("/customers/{id}") String me(@PathVariable String id) { return "customer " + id; }

    @GetMapping("/web/home") String home(java.security.Principal p) { return "olá " + p.getName(); }
}
