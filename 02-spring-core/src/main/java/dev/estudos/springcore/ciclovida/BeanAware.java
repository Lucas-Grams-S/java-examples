package dev.estudos.springcore.ciclovida;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * SC04-10 · Bean {@code Aware}: recebe do contêiner o próprio nome e o {@code ApplicationContext}. Use só em
 * infraestrutura; em código de negócio, injete o que precisa.
 * Documento: 02-spring-core/04-ciclo-de-vida-e-escopos.md §5.2.
 *
 * <p>Saída real:
 * <pre>
 * nome do bean -&gt; diagnostico
 * contexto recebido é o mesmo que criou o bean -&gt; true
 * contexto vê o próprio bean -&gt; true
 * </pre>
 */
public class BeanAware {
    @Component("diagnostico")
    static class DiagnosticsBean implements BeanNameAware, ApplicationContextAware {
        String name;
        ApplicationContext ctx;
        public void setBeanName(String name) { this.name = name; }
        public void setApplicationContext(ApplicationContext ctx) { this.ctx = ctx; }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(DiagnosticsBean.class)) {
            var d = ctx.getBean(DiagnosticsBean.class);
            out.add("nome do bean -> " + d.name);
            out.add("contexto recebido é o mesmo que criou o bean -> " + (d.ctx == ctx));
            out.add("contexto vê o próprio bean -> " + (d.ctx.getBean("diagnostico") == d));
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
