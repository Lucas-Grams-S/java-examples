package dev.estudos.springcore.aop;

import dev.estudos.springcore.transacoes.TxFixture;
import java.util.ArrayList;
import java.util.List;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * J11-10 · Realista: ver o proxy do Spring de fora (movido do módulo 01-java por precisar do Spring). O bean
 * com {@code @Transactional} é uma subclasse gerada ({@code $$SpringCGLIB$$0}); {@code getClass()} NÃO é
 * {@code OrderService.class}; o alvo real fica por trás (diferente do proxy escrito à mão em J11-02).
 * Documento: 01-java/11-proxies-dinamicos-e-bytecode.md §5.4.
 *
 * <p>Saída real:
 * <pre>
 * service.getClass() -&gt; ...ProxyDeFora$OrderService$$SpringCGLIB$$0
 * AopUtils.isCglibProxy -&gt; true
 * getClass() == OrderService.class -&gt; false
 * ultimateTargetClass -&gt; OrderService
 * alvo é outro objeto (não o próprio proxy) -&gt; true
 * </pre>
 */
public class ProxyDeFora {
    @Service
    public static class OrderService { @Transactional public void place() {} }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(TxFixture.Cfg.class, OrderService.class)) {
            var service = ctx.getBean(OrderService.class);
            out.add("service.getClass() -> " + service.getClass().getName());
            out.add("AopUtils.isCglibProxy -> " + AopUtils.isCglibProxy(service));
            out.add("getClass() == OrderService.class -> " + (service.getClass() == OrderService.class));
            out.add("ultimateTargetClass -> " + AopProxyUtils.ultimateTargetClass(service).getSimpleName());
            out.add("alvo é outro objeto (não o próprio proxy) -> "
                + (service instanceof Advised a && a.getTargetSource().getTarget() != service ? "true"
                   : "false"));
        } catch (Exception e) { throw new IllegalStateException(e); }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
