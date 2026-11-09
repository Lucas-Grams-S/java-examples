package com.example.orders.orders;

import com.example.orders.customers.Customer;
import com.example.orders.customers.CustomerRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Sonda da matriz de rollback do 09-transacoes: grava e depois lança. */
@Component
class TxProbe {
    static class CheckedFailure extends Exception {}
    private final CustomerRepository customers;
    TxProbe(CustomerRepository customers) { this.customers = customers; }

    @Transactional
    void runtimeFailure(String email) { customers.saveAndFlush(new Customer("rt", email)); throw new IllegalStateException("boom"); }

    @Transactional
    void checkedFailure(String email) throws CheckedFailure { customers.saveAndFlush(new Customer("ck", email)); throw new CheckedFailure(); }

    @Transactional(rollbackFor = CheckedFailure.class)
    void checkedWithRollbackFor(String email) throws CheckedFailure { customers.saveAndFlush(new Customer("ckrf", email)); throw new CheckedFailure(); }
}
