package dev.estudos.springcore.contexto;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SC03-05 (e SC03-08) · {@code @Configuration} <b>full</b> (subclasse CGLIB: chamada entre {@code @Bean}
 * devolve o singleton) vs <b>lite</b> ({@code proxyBeanMethods = false}: chamada Java comum cria nova
 * instância; receba a dependência por parâmetro).
 * Documento: 02-spring-core/03-application-context-e-beans.md §2.5 e §3.3.
 *
 * <p>Saída real:
 * <pre>
 * full: holder.dep == dep: true  | classe em runtime: FullVsLite$Full$$SpringCGLIB$$0
 * lite: holder.dep == dep: false | holder2 (parâmetro): true | classe em runtime: Lite
 * </pre>
 */
public class FullVsLite {
    static class Dep {}
    static class Holder { final Dep dep; Holder(Dep dep) { this.dep = dep; } }

    @Configuration                                  // modo FULL (proxyBeanMethods = true é o padrão)
    static class Full {
        @Bean Dep dep() { return new Dep(); }
        @Bean Holder holder() { return new Holder(dep()); }          // dep() interceptado → singleton
    }

    @Configuration(proxyBeanMethods = false)       // modo LITE
    static class Lite {
        @Bean Dep dep() { return new Dep(); }
        @Bean Holder holder() { return new Holder(dep()); }          // chamada Java comum!
        @Bean Holder holder2(Dep dep) { return new Holder(dep); }    // correto
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(Full.class)) {
            out.add("full: holder.dep == dep: " + (ctx.getBean(Holder.class).dep == ctx.getBean(Dep.class))
                + "  | classe em runtime: " + ctx.getBean(Full.class).getClass().getName().replace("dev.estudos.springcore.contexto.", ""));
        }
        try (var ctx = new AnnotationConfigApplicationContext(Lite.class)) {
            Dep dep = ctx.getBean(Dep.class);
            out.add("lite: holder.dep == dep: " + (((Holder) ctx.getBean("holder")).dep == dep)
                + " | holder2 (parâmetro): " + (((Holder) ctx.getBean("holder2")).dep == dep)
                + " | classe em runtime: " + ctx.getBean(Lite.class).getClass().getSimpleName());
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
