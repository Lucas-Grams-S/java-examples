package dev.estudos.springcore.contexto;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * SC03-04 · {@code @Bean}: nome (do método ou {@code name={...}} com apelidos), dependências por parâmetro,
 * {@code initMethod}, {@code destroyMethod} inferido ({@code close}) e escopo.
 * Documento: 02-spring-core/03-application-context-e-beans.md §2.4.
 *
 * <p>Saída real:
 * <pre>
 * nome do bean pelo método -&gt; [cardGateway]
 * apelidos de 'pix' -&gt; [pixGateway]
 * card recebeu o Clock por parâmetro -&gt; true
 * pix: initMethod chamado -&gt; true
 * prototype: duas instâncias diferentes -&gt; true
 * após close(): pix.close() inferido chamado -&gt; true
 * </pre>
 */
public class BeanMetodo {
    interface PaymentGateway { String name(); }
    static class CardGateway implements PaymentGateway {
        final Clock clock; CardGateway(Clock clock) { this.clock = clock; }
        public String name() { return "card"; }
    }
    static class PixGateway implements PaymentGateway, AutoCloseable {
        boolean connected, closed;
        void connect() { connected = true; }
        public void close() { closed = true; }          // destroyMethod "close" é inferido em @Bean
        public String name() { return "pix"; }
    }
    static class Ticket {}

    @Configuration(proxyBeanMethods = false)
    static class PaymentConfig {
        @Bean Clock clock() { return Clock.systemUTC(); }
        @Bean PaymentGateway cardGateway(Clock clock) { return new CardGateway(clock); }   // dependência = parâmetro
        @Bean(name = {"pix", "pixGateway"}, initMethod = "connect")
        PaymentGateway pixGateway() { return new PixGateway(); }
        @Bean @Scope("prototype") Ticket ticket() { return new Ticket(); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        PixGateway pix;
        try (var ctx = new AnnotationConfigApplicationContext(PaymentConfig.class)) {
            out.add("nome do bean pelo método -> " + Arrays.toString(ctx.getBeanNamesForType(CardGateway.class)));
            out.add("apelidos de 'pix' -> " + Arrays.toString(ctx.getAliases("pix")));
            out.add("card recebeu o Clock por parâmetro -> " + (((CardGateway) ctx.getBean("cardGateway")).clock == ctx.getBean(Clock.class)));
            pix = (PixGateway) ctx.getBean("pix");
            out.add("pix: initMethod chamado -> " + pix.connected);
            out.add("prototype: duas instâncias diferentes -> " + (ctx.getBean("ticket") != ctx.getBean("ticket")));
        }
        out.add("após close(): pix.close() inferido chamado -> " + pix.closed);
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
