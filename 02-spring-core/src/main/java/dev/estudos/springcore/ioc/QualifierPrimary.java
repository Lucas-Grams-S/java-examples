package dev.estudos.springcore.ioc;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * SC02-03 · {@code @Qualifier} e {@code @Primary}; SC02-02 (ordem de desempate) também é demonstrada aqui:
 * sem desempate falha; o <em>nome do parâmetro</em> serve de último recurso (o projeto compila com
 * {@code -parameters}); {@code @Qualifier} vence {@code @Primary}.
 * Documento: 02-spring-core/02-ioc-e-injecao-de-dependencia.md §2.2 e §2.3.
 *
 * <p>Saída real:
 * <pre>
 * &#64;Primary -&gt; card | &#64;Qualifier("boleto") -&gt; boleto
 * nome padrão do bean escaneado -&gt; cardGateway
 * sem desempate -&gt; NoUniqueBeanDefinitionException
 * nome do parâmetro 'pix' desempata -&gt; pix
 * </pre>
 */
public class QualifierPrimary {
    interface PaymentGateway { String name(); }

    @Component("pixGateway") static class PixGateway implements PaymentGateway { public String name() { return "pix"; } }
    @Component("cardGateway") @Primary static class CardGateway implements PaymentGateway { public String name() { return "card"; } }
    @Component("boleto") static class BoletoGateway implements PaymentGateway { public String name() { return "boleto"; } }

    @Component
    static class CheckoutService {
        final PaymentGateway padrao, alt;
        CheckoutService(PaymentGateway padrao,                       // → CardGateway (@Primary)
                        @Qualifier("boleto") PaymentGateway alt) {   // → BoletoGateway
            this.padrao = padrao; this.alt = alt;
        }
    }

    /** Sem @Primary: só o nome do parâmetro ('pixGateway') desempata. */
    @Component static class PorNome { final PaymentGateway pixGateway; PorNome(PaymentGateway pixGateway) { this.pixGateway = pixGateway; } }
    @Component static class SemDesempate { SemDesempate(PaymentGateway qualquer) {} }
    @Component("sem") static class PixSemPrimary implements PaymentGateway { public String name() { return "pix"; } }
    @Component("sem2") static class BoletoSemPrimary implements PaymentGateway { public String name() { return "boleto"; } }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(PixGateway.class, CardGateway.class, BoletoGateway.class, CheckoutService.class)) {
            var c = ctx.getBean(CheckoutService.class);
            out.add("@Primary -> " + c.padrao.name() + " | @Qualifier(\"boleto\") -> " + c.alt.name());
            out.add("nome padrão do bean escaneado -> " + ctx.getBeanNamesForType(CardGateway.class)[0]);
        }
        try (var ctx = new AnnotationConfigApplicationContext(PixSemPrimary.class, BoletoSemPrimary.class, SemDesempate.class)) {
            out.add("sem desempate -> subiu?!");
        } catch (Exception e) {
            Throwable r = e; while (r.getCause() != null) r = r.getCause();
            out.add("sem desempate -> " + (r instanceof NoUniqueBeanDefinitionException ? "NoUniqueBeanDefinitionException" : r.toString()));
        }
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean("pixGateway", PixGateway.class);
            ctx.registerBean("boleto", BoletoGateway.class);
            ctx.register(PorNome.class);
            ctx.refresh();
            out.add("nome do parâmetro 'pix' desempata -> " + ctx.getBean(PorNome.class).pixGateway.name());
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
