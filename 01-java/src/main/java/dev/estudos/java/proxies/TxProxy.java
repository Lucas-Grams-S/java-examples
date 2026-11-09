package dev.estudos.java.proxies;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * J11-08 · Proxy guiado por anotação: o esqueleto do @Transactional (BEGIN/COMMIT/ROLLBACK).
 * Documento: java-docs/01-java/11-proxies-dinamicos-e-bytecode.md §5.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * BEGIN
 * COMMIT
 * depositado 10
 * BEGIN
 * ROLLBACK
 * erro saldo inválido
 * lido
 * </pre>
 *
 * <p>Variações: mova @Tx para a interface e veja que o handler (que lê a classe concreta) deixa de transacionar.
 */
public class TxProxy {
    @Retention(RetentionPolicy.RUNTIME) @interface Tx {}

    public interface AccountService {
        String deposit(int v);
        String broken();
        String readOnly();
    }

    static class AccountServiceImpl implements AccountService {
        @Tx public String deposit(int v) { return "depositado " + v; }
        @Tx public String broken() { throw new IllegalStateException("saldo inválido"); }
        public String readOnly() { return "lido"; }
    }

    static AccountService withTx(AccountService target, List<String> log) {
        InvocationHandler handler = (proxy, m, args) -> {
            Method concrete = target.getClass().getMethod(m.getName(), m.getParameterTypes());
            if (!concrete.isAnnotationPresent(Tx.class)) return concrete.invoke(target, args);
            log.add("BEGIN");
            try {
                Object r = concrete.invoke(target, args);
                log.add("COMMIT");
                return r;
            } catch (InvocationTargetException e) {
                log.add("ROLLBACK");
                throw e.getCause();
            }
        };
        return (AccountService) Proxy.newProxyInstance(AccountService.class.getClassLoader(), new Class<?>[]{AccountService.class}, handler);
    }

    public static List<String> run() {
        List<String> log = new ArrayList<>();
        AccountService s = withTx(new AccountServiceImpl(), log);
        log.add(s.deposit(10));
        try { s.broken(); } catch (IllegalStateException e) { log.add("erro " + e.getMessage()); }
        log.add(s.readOnly());
        return log;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
