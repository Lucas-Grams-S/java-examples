package com.example.orders.customers;

import com.example.orders.shared.NotFoundException;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/** PP02: controller fino. (Sem camada de serviço: não há regra além de unicidade, garantida pelo banco.) */
@RestController
@RequestMapping("/customers")
class CustomerController {
    private final CustomerRepository repo;
    CustomerController(CustomerRepository repo) { this.repo = repo; }

    @PostMapping
    @Transactional
    ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest req) {
        var saved = repo.saveAndFlush(new Customer(req.name(), req.email()));   // flush: viola unique aqui, não no commit
        return ResponseEntity.created(URI.create("/customers/" + saved.getId())).body(CustomerResponse.of(saved));
    }

    @GetMapping("/{id}")
    CustomerResponse get(@PathVariable Long id) {
        return repo.findById(id).map(CustomerResponse::of).orElseThrow(() -> new NotFoundException("customer " + id));
    }
}
