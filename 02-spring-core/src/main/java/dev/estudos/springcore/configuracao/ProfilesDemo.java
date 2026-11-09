package dev.estudos.springcore.configuracao;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * SC05-04 (e SC05-11) · Profiles: {@code @Profile("dev")}, negação {@code "!dev"}, expressões
 * ({@code "prod & !eu"}) e o perfil {@code default}. {@code @Profile} é um {@code @Conditional}: a
 * definição fora do perfil <b>nem é registrada</b>. O perfil precisa ser definido <b>antes</b> do refresh.
 * Documento: 02-spring-core/05-configuracao.md §2.4 e §3.3.
 *
 * <p>Saída real:
 * <pre>
 * perfil dev -&gt; console
 * perfil prod -&gt; mail
 * nenhum perfil (default) -&gt; mail
 * perfil prod, 'MailNotifier' registrado? true | 'ConsoleNotifier' registrado? false
 * &#64;Profile("prod &amp; !eu") com prod -&gt; ativo; com prod+eu -&gt; inativo
 * </pre>
 */
public class ProfilesDemo {
    interface Notifier { String go(); }
    @Component("console") @Profile("dev") static class ConsoleNotifier implements Notifier { public String go() { return "console"; } }
    @Component("mail") @Profile("!dev") static class MailNotifier implements Notifier { public String go() { return "mail"; } }
    @Component("soProdForaEu") @Profile("prod & !eu") static class SoProd {}

    private static AnnotationConfigApplicationContext ctx(String... perfis) {
        var c = new AnnotationConfigApplicationContext();
        if (perfis.length > 0) c.getEnvironment().setActiveProfiles(perfis);   // antes do refresh!
        c.register(ConsoleNotifier.class, MailNotifier.class, SoProd.class);
        c.refresh();
        return c;
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var c = ctx("dev")) { out.add("perfil dev -> " + c.getBean(Notifier.class).go()); }
        try (var c = ctx("prod")) {
            out.add("perfil prod -> " + c.getBean(Notifier.class).go());
        }
        try (var c = ctx()) { out.add("nenhum perfil (default) -> " + c.getBean(Notifier.class).go()); }
        try (var c = ctx("prod")) {
            out.add("perfil prod, 'MailNotifier' registrado? " + c.containsBeanDefinition("mail")
                + " | 'ConsoleNotifier' registrado? " + c.containsBeanDefinition("console"));
        }
        boolean ativoProd, ativoProdEu;
        try (var c = ctx("prod")) { ativoProd = c.containsBean("soProdForaEu"); }
        try (var c = ctx("prod", "eu")) { ativoProdEu = c.containsBean("soProdForaEu"); }
        out.add("@Profile(\"prod & !eu\") com prod -> " + (ativoProd ? "ativo" : "inativo") + "; com prod+eu -> " + (ativoProdEu ? "ativo" : "inativo"));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
