package dev.estudos.springcore.aop;

import java.util.ArrayList;
import java.util.List;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;

/**
 * SC07-10 · JDK vs CGLIB e {@code ClassCastException}: com interface o {@code ProxyFactory} gera proxy JDK
 * (não é um {@code Impl}); {@code setProxyTargetClass(true)} força CGLIB (subclasse de {@code Impl}).
 * Documento: 02-spring-core/07-aop-e-proxies.md §5.2.
 *
 * <p>Saída real:
 * <pre>
 * proxy padrão: jdk=true cglib=false
 * (Impl) proxy JDK -&gt; ClassCastException
 * proxy forçado: jdk=false cglib=true, é Impl: true
 * classe real por trás do proxy JDK -&gt; Impl
 * </pre>
 */
public class JdkVsCglib {
    public interface Greeter { String hi(); }
    public static class Impl implements Greeter { public String hi() { return "hi"; } }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var pf = new ProxyFactory(new Impl());
        pf.addAdvice((MethodInterceptor) i -> i.proceed());
        Object p = pf.getProxy();
        out.add("proxy padrão: jdk=" + AopUtils.isJdkDynamicProxy(p) + " cglib=" + AopUtils.isCglibProxy(p));
        try { Impl i = (Impl) p; out.add("sem erro " + i); }
        catch (ClassCastException e) { out.add("(Impl) proxy JDK -> ClassCastException"); }

        var pf2 = new ProxyFactory(new Impl());
        pf2.setProxyTargetClass(true);
        pf2.addAdvice((MethodInterceptor) i -> i.proceed());
        Object p2 = pf2.getProxy();
        out.add("proxy forçado: jdk=" + AopUtils.isJdkDynamicProxy(p2) + " cglib=" + AopUtils.isCglibProxy(p2)
            + ", é Impl: " + (p2 instanceof Impl));
        out.add("classe real por trás do proxy JDK -> " + AopProxyUtils.ultimateTargetClass(p).getSimpleName());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
