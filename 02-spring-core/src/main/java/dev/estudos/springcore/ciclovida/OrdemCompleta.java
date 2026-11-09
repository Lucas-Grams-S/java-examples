package dev.estudos.springcore.ciclovida;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SC04-01 (e SC04-02) · Ordem completa das etapas do ciclo de vida: um bean que usa <b>todos</b> os
 * ganchos de inicialização/destruição. Mostra também por que {@code @PostConstruct} aparece entre
 * "BPP before" e {@code afterPropertiesSet} (quem o processa é um BPP).
 * Documento: 02-spring-core/04-ciclo-de-vida-e-escopos.md §2.1 e §2.2.
 *
 * <p>Saída real:
 * <pre>
 * 0 BFPP: definições registradas, nenhuma instância de 'stock' ainda
 * 1 construtor
 * 2 BeanNameAware: stock
 * 3 BPP before
 * 4 &#64;PostConstruct
 * 5 afterPropertiesSet
 * 6 initMethod
 * 7 BPP after
 *    (bean em uso)
 * 8 &#64;PreDestroy
 * 9 destroy()
 * 10 destroyMethod
 * </pre>
 */
public class OrdemCompleta {
    static final List<String> LOG = new ArrayList<>();

    static class Stock implements BeanNameAware, InitializingBean, DisposableBean {
        Stock() { LOG.add("1 construtor"); }
        @Override public void setBeanName(String name) { LOG.add("2 BeanNameAware: " + name); }
        @PostConstruct void postConstruct() { LOG.add("4 @PostConstruct"); }
        @Override public void afterPropertiesSet() { LOG.add("5 afterPropertiesSet"); }
        void initMethod() { LOG.add("6 initMethod"); }
        @PreDestroy void preDestroy() { LOG.add("8 @PreDestroy"); }
        @Override public void destroy() { LOG.add("9 destroy()"); }
        void destroyMethod() { LOG.add("10 destroyMethod"); }
    }

    static class Tracer implements BeanPostProcessor {
        @Override public Object postProcessBeforeInitialization(Object bean, String name) {
            if (bean instanceof Stock) LOG.add("3 BPP before");
            return bean;
        }
        @Override public Object postProcessAfterInitialization(Object bean, String name) {
            if (bean instanceof Stock) LOG.add("7 BPP after");
            return bean;
        }
    }

    static class Bfpp implements BeanFactoryPostProcessor {
        @Override public void postProcessBeanFactory(ConfigurableListableBeanFactory bf) {
            LOG.add("0 BFPP: definições registradas, nenhuma instância de 'stock' ainda" + (bf.containsSingleton("stock") ? " (ERRO)" : ""));
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class Cfg {
        @Bean static Bfpp bfpp() { return new Bfpp(); }       // static: post-processors precisam existir cedo
        @Bean static Tracer tracer() { return new Tracer(); }
        @Bean(initMethod = "initMethod", destroyMethod = "destroyMethod") Stock stock() { return new Stock(); }
    }

    public static List<String> run() {
        LOG.clear();
        try (var ctx = new AnnotationConfigApplicationContext(Cfg.class)) {
            LOG.add("   (bean em uso)");
        }
        return new ArrayList<>(LOG);
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
