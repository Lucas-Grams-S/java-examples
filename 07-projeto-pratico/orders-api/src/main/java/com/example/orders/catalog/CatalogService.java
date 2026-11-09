package com.example.orders.catalog;

import com.example.orders.shared.DomainException;
import com.example.orders.shared.NotFoundException;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** API pública do módulo catalog (PP8: leitura de produto em cache; PP10: único ponto de entrada). */
@Service
public class CatalogService {
    private final ProductRepository repo;
    private final AtomicInteger loads = new AtomicInteger();
    CatalogService(ProductRepository repo) { this.repo = repo; }

    @Cacheable("products")
    @Transactional(readOnly = true)
    public ProductView find(Long id) {
        loads.incrementAndGet();
        return repo.findById(id).map(p -> new ProductView(p.getId(), p.getName(), p.getPrice()))
                .orElseThrow(() -> new NotFoundException("product " + id));
    }

    /** Baixa de estoque; participa da transação do chamador (REQUIRED). */
    @Transactional
    public void reserve(Long id, int quantity) {
        if (repo.decrementStock(id, quantity) == 0) throw new DomainException("insufficient stock for product " + id);
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    @Transactional
    public ProductView create(String name, BigDecimal price, int stock) {
        var p = repo.save(new Product(name, price, stock));
        return new ProductView(p.getId(), p.getName(), p.getPrice());
    }

    /** Quantas vezes o banco foi realmente consultado (para provar o cache). */
    public int databaseLoads() { return loads.get(); }
}
