package dev.estudos.springcore.ciclovida;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.stereotype.Service;

/**
 * SC04-05 (e SC04-12) · Escopo mais curto dentro de mais longo. Um singleton que injeta um {@code prototype}
 * recebe <b>uma</b> instância (na criação do singleton). Soluções: {@code ObjectProvider<T>} (instância nova
 * a cada {@code getObject()}) ou proxy de escopo ({@code ScopedProxyMode.TARGET_CLASS}); o exemplo usa o
 * escopo {@code thread} no lugar de {@code request}, que exigiria contexto web.
 * Documento: 02-spring-core/04-ciclo-de-vida-e-escopos.md §2.5 e §5.4.
 *
 * <p>Saída real:
 * <pre>
 * injeção direta: duas chamadas usam o mesmo builder -&gt; true
 * ObjectProvider: ids dos builders nas 3 chamadas -&gt; [2, 3, 4]
 * proxy de escopo: o singleton guarda um proxy -&gt; true (classe contém $$SpringCGLIB$$)
 * proxy de escopo: thread principal vs outra thread veem objetos diferentes -&gt; true
 * </pre>
 * (os ids exatos dependem da ordem de criação; os testes só afirmam que são distintos)
 */
public class EscopoCurtoEmLongo {
    static final AtomicInteger SEQ = new AtomicInteger();

    static class ReportBuilder {
        final int id = SEQ.incrementAndGet();
        int build() { return id; }
    }

    @Service
    static class ReportService {
        private final ReportBuilder fixo;                         // injetado UMA vez
        private final ObjectProvider<ReportBuilder> builders;     // nova instância a cada getObject()
        ReportService(ReportBuilder fixo, ObjectProvider<ReportBuilder> builders) { this.fixo = fixo; this.builders = builders; }
        int comFixo() { return fixo.build(); }
        int comProvider() { return builders.getObject().build(); }
    }

    /** Dados "da requisição" (aqui: da thread atual). */
    static class RequestContext {
        final int id = SEQ.incrementAndGet();
        int id() { return id; }
    }

    @Service
    static class Consumidor {
        final RequestContext ctx;
        Consumidor(RequestContext ctx) { this.ctx = ctx; }
    }

    @Configuration(proxyBeanMethods = false)
    static class Cfg {
        @Bean @Scope("prototype") ReportBuilder reportBuilder() { return new ReportBuilder(); }
        @Bean @Scope(value = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS) RequestContext requestContext() { return new RequestContext(); }
    }

    public static List<String> run() throws Exception {
        SEQ.set(0);
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.getBeanFactory().registerScope("thread", new SimpleThreadScope());
            ctx.register(Cfg.class, ReportService.class, Consumidor.class);
            ctx.refresh();
            var svc = ctx.getBean(ReportService.class);
            out.add("injeção direta: duas chamadas usam o mesmo builder -> " + (svc.comFixo() == svc.comFixo()));
            out.add("ObjectProvider: ids dos builders nas 3 chamadas -> " + List.of(svc.comProvider(), svc.comProvider(), svc.comProvider()));

            var consumidor = ctx.getBean(Consumidor.class);
            out.add("proxy de escopo: o singleton guarda um proxy -> " + consumidor.ctx.getClass().getName().contains("$$SpringCGLIB$$")
                + " (classe contém $$SpringCGLIB$$)");
            int principal = consumidor.ctx.id();
            int[] outra = new int[1];
            Thread t = new Thread(() -> outra[0] = consumidor.ctx.id());
            t.start(); t.join();
            out.add("proxy de escopo: thread principal vs outra thread veem objetos diferentes -> " + (principal != outra[0]));
        }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
