package dev.estudos.springcore.contexto;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * SC03-07 · Anatomia da {@code BeanDefinition}: a "receita" existe antes de qualquer instância. Mostra os
 * atributos principais e conta as definições de infraestrutura que o próprio contexto registra.
 * Documento: 02-spring-core/03-application-context-e-beans.md §3.2.
 *
 * <p>Saída real:
 * <pre>
 * orderSvc: classe=...AnatomiaBeanDefinition$OrderSvc scope=singleton lazy=false primary=false dependsOn=[orderRepo]
 * orderRepo: classe=...AnatomiaBeanDefinition$OrderRepo scope=singleton lazy=true primary=false dependsOn=[]
 * relatorio: factoryMethod=relatorio scope=prototype primary=true initMethod=abrir
 * definições de infraestrutura registradas pelo contexto (incluem ConfigurationClassPostProcessor) -&gt; true
 * </pre>
 */
public class AnatomiaBeanDefinition {
    @Service("orderSvc") @DependsOn("orderRepo") static class OrderSvc {}
    @Component("orderRepo") @Lazy static class OrderRepo {}
    static class Relatorio { void abrir() {} }

    @Configuration(proxyBeanMethods = false)
    static class Cfg {
        @Bean(initMethod = "abrir") @Primary @Scope("prototype") Relatorio relatorio() { return new Relatorio(); }
    }

    private static String curta(String c) { return c == null ? "null" : "..." + c.substring(c.lastIndexOf('.') + 1); }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(OrderSvc.class, OrderRepo.class, Cfg.class)) {
            for (String n : List.of("orderSvc", "orderRepo")) {
                BeanDefinition bd = ctx.getBeanDefinition(n);
                out.add(n + ": classe=" + curta(bd.getBeanClassName()) + " scope=" + (bd.getScope().isEmpty() ? "singleton" : bd.getScope())
                    + " lazy=" + bd.isLazyInit() + " primary=" + bd.isPrimary() + " dependsOn=" + Arrays.toString(bd.getDependsOn() == null ? new String[0] : bd.getDependsOn()));
            }
            BeanDefinition r = ctx.getBeanDefinition("relatorio");
            out.add("relatorio: factoryMethod=" + r.getFactoryMethodName() + " scope=" + r.getScope() + " primary=" + r.isPrimary() + " initMethod=" + r.getInitMethodName());
            out.add("definições de infraestrutura registradas pelo contexto (incluem ConfigurationClassPostProcessor) -> "
                + List.of(ctx.getBeanDefinitionNames()).contains("org.springframework.context.annotation.internalConfigurationAnnotationProcessor"));
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
