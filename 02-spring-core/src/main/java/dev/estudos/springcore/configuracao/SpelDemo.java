package dev.estudos.springcore.configuracao;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * SC05-07 · SpEL em poucas linhas: literais, tipos estáticos ({@code T(...)}), Elvis, navegação segura,
 * seleção ({@code .?[]}), projeção ({@code .![]}), referência a bean em {@code @Value("#{bean.prop}")} e a
 * opção restrita {@code SimpleEvaluationContext} (sem {@code T()}, para entrada menos confiável).
 * Documento: 02-spring-core/05-configuracao.md §2.7.
 *
 * <p>Saída real:
 * <pre>
 * 'pedido-' + 42 -&gt; pedido-42
 * T(java.lang.Math).max(2, 5) * 10 -&gt; 50
 * 3 &gt; 2 ? 'a' : 'b' -&gt; a
 * nome ?: 'x' (nome nulo) -&gt; x
 * obj?.prop (obj nulo) -&gt; null
 * itens.?[preco &gt; 10] -&gt; [caro1, caro2]
 * itens.![nome] -&gt; [barato, caro1, caro2]
 * &#64;Value("#{config.moeda}") -&gt; BRL
 * SimpleEvaluationContext bloqueia T() -&gt; SpelEvaluationException
 * </pre>
 */
public class SpelDemo {
    public record Item(String nome, int preco) {}
    public static class Dados {
        public String nome = null;
        public Object obj = null;
        public List<Item> itens = List.of(new Item("barato", 5), new Item("caro1", 20), new Item("caro2", 30));
    }
    public static class Config { public String getMoeda() { return "BRL"; } }

    @Configuration(proxyBeanMethods = false)
    static class Cfg {
        @Bean Config config() { return new Config(); }
        @Bean String moedaDoBean(@Value("#{config.moeda}") String moeda) { return moeda; }   // referência a bean
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var parser = new SpelExpressionParser();
        out.add("'pedido-' + 42 -> " + parser.parseExpression("'pedido-' + 42").getValue());
        out.add("T(java.lang.Math).max(2, 5) * 10 -> " + parser.parseExpression("T(java.lang.Math).max(2, 5) * 10").getValue());
        out.add("3 > 2 ? 'a' : 'b' -> " + parser.parseExpression("3 > 2 ? 'a' : 'b'").getValue());
        var dados = new StandardEvaluationContext(new Dados());
        out.add("nome ?: 'x' (nome nulo) -> " + parser.parseExpression("nome ?: 'x'").getValue(dados));
        out.add("obj?.prop (obj nulo) -> " + parser.parseExpression("obj?.prop").getValue(dados));
        out.add("itens.?[preco > 10] -> " + ((List<?>) parser.parseExpression("itens.?[preco > 10]").getValue(dados)).stream().map(i -> ((Item) i).nome()).toList());
        out.add("itens.![nome] -> " + parser.parseExpression("itens.![nome]").getValue(dados));
        try (var ctx = new AnnotationConfigApplicationContext(Cfg.class)) {
            out.add("@Value(\"#{config.moeda}\") -> " + ctx.getBean("moedaDoBean"));
        }
        try {
            parser.parseExpression("T(java.lang.Math).max(2, 5)").getValue(SimpleEvaluationContext.forReadOnlyDataBinding().build());
            out.add("SimpleEvaluationContext bloqueia T() -> (não bloqueou?!)");
        } catch (org.springframework.expression.spel.SpelEvaluationException e) {
            out.add("SimpleEvaluationContext bloqueia T() -> SpelEvaluationException");
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
