package dev.estudos.padroes.antipadroes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DP08-11 · Field injection vs. construtor: sem contêiner a primeira dá NPE; a segunda falha cedo.
 * Documento: java-docs/06-padroes-de-projeto/08-anti-padroes.md §5.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * field injection sem container: NPE
 * mail: oi
 * construtor: falha cedo (NPE no new)
 * mail: oi
 * com o contêiner, a field injection funciona (e esconde a dependência)
 * </pre>
 */
public class FieldInjectionVsConstrutor {
    static class Mailer { void send(String s, List<String> out) { out.add("mail: " + s); } }

    static class FieldInjected {
        @Autowired private Mailer mailer;               // sem container: null
        void notifyUser(List<String> out) { mailer.send("oi", out); }
    }

    static class CtorInjected {
        private final Mailer mailer;                    // obrigatória e imutável
        CtorInjected(Mailer m) { mailer = Objects.requireNonNull(m); }
        void notifyUser(List<String> out) { mailer.send("oi", out); }
    }

    @Configuration
    static class Cfg {
        @Bean Mailer mailer() { return new Mailer(); }
        @Bean FieldInjected fieldInjected() { return new FieldInjected(); }
        @Bean CtorInjected ctorInjected(Mailer m) { return new CtorInjected(m); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try { new FieldInjected().notifyUser(out); }
        catch (NullPointerException e) { out.add("field injection sem container: NPE"); }
        new CtorInjected(new Mailer()).notifyUser(out);
        try { new CtorInjected(null); }
        catch (NullPointerException e) { out.add("construtor: falha cedo (NPE no new)"); }
        try (var ctx = new AnnotationConfigApplicationContext(Cfg.class)) {
            ctx.getBean(FieldInjected.class).notifyUser(out);
            out.add("com o contêiner, a field injection funciona (e esconde a dependência)");
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
