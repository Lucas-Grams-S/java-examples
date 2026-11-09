package dev.estudos.padroes.estruturais;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;

/**
 * DP03-12 · Proxy: escrito à mão (cache, proteção), {@code java.lang.reflect.Proxy} e {@code ProxyFactory} do Spring.
 * Documento: java-docs/06-padroes-de-projeto/03-estruturais.md §5.7.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 *   SELECT ... A
 * proxy de cache: 3 chamadas a findName(A) -&gt; 1 consulta(s) ao banco
 * proxy de proteção -&gt; acesso negado (consultas extras: 0)
 *   SELECT ... B
 *   [proxy JDK] findName interceptado
 * proxy dinâmico JDK -&gt; Produto B, é proxy? true
 *   [advice] antes de findName
 *   SELECT ... C
 * Spring ProxyFactory -&gt; Produto C, JDK dynamic proxy? true
 * </pre>
 *
 * <p>Variações: passe uma classe sem interface ao {@code ProxyFactory} e veja {@code isCglibProxy} virar {@code true}.
 */
public class ProxyExemplo {

    interface ProductCatalog { String findName(String sku); }

    static class DbProductCatalog implements ProductCatalog {
        final List<String> log;
        int queries = 0;
        DbProductCatalog(List<String> log) { this.log = log; }
        public String findName(String sku) { queries++; log.add("  SELECT ... " + sku); return "Produto " + sku; }
    }

    static class CachingCatalogProxy implements ProductCatalog {
        private final ProductCatalog real;
        private final Map<String, String> cache = new HashMap<>();
        CachingCatalogProxy(ProductCatalog real) { this.real = real; }
        public String findName(String sku) { return cache.computeIfAbsent(sku, real::findName); }
    }

    static class SecuredCatalogProxy implements ProductCatalog {
        private final ProductCatalog real; private final boolean admin;
        SecuredCatalogProxy(ProductCatalog real, boolean admin) { this.real = real; this.admin = admin; }
        public String findName(String sku) {
            if (!admin) throw new SecurityException("acesso negado");
            return real.findName(sku);
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var db = new DbProductCatalog(out);

        var caching = new CachingCatalogProxy(db);
        caching.findName("A"); caching.findName("A"); caching.findName("A");
        out.add("proxy de cache: 3 chamadas a findName(A) -> " + db.queries + " consulta(s) ao banco");

        var before = db.queries;
        try {
            new SecuredCatalogProxy(db, false).findName("A");
        } catch (SecurityException e) {
            out.add("proxy de proteção -> " + e.getMessage() + " (consultas extras: " + (db.queries - before) + ")");
        }

        ProductCatalog timed = (ProductCatalog) Proxy.newProxyInstance(
            ProductCatalog.class.getClassLoader(), new Class<?>[] { ProductCatalog.class },
            (proxy, method, args) -> {
                try {
                    return method.invoke(db, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();                              // desembrulha a exceção do alvo
                } finally {
                    out.add("  [proxy JDK] " + method.getName() + " interceptado");
                }
            });
        out.add("proxy dinâmico JDK -> " + timed.findName("B") + ", é proxy? " + Proxy.isProxyClass(timed.getClass()));

        var pf = new ProxyFactory(new DbProductCatalog(out));
        pf.addAdvice((MethodInterceptor) inv -> {
            out.add("  [advice] antes de " + inv.getMethod().getName());
            return inv.proceed();
        });
        var spring = (ProductCatalog) pf.getProxy();
        out.add("Spring ProxyFactory -> " + spring.findName("C") + ", JDK dynamic proxy? " + AopUtils.isJdkDynamicProxy(spring));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
