package dev.estudos.springcore.configuracao;

import dev.estudos.springcore.configuracao.camadas.OrdersApplicationConfig;
import dev.estudos.springcore.configuracao.camadas.PaymentConfig;
import dev.estudos.springcore.configuracao.camadas.PricingService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * SC05-12 · Configuração por camadas (domínio de pedidos): uma raiz com {@code @Import} (infra e pagamento),
 * {@code @ComponentScan} e {@code @PropertySource}. O {@code Gateway} recebe a URL por {@code @Value} e o
 * {@code Clock} por parâmetro; o {@code PricingService} (escaneado) lê o {@code Environment}.
 * Documento: 02-spring-core/05-configuracao.md §5.1.
 *
 * <p>Saída real:
 * <pre>
 * gateway.url -&gt; http://pagamentos.local/api
 * gateway.clock é o Clock da InfraConfig -&gt; true
 * PricingService (escaneado) -&gt; maxItems=5 currency=BRL
 * </pre>
 */
public class ConfigPorCamadas {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(OrdersApplicationConfig.class)) {
            var gw = ctx.getBean(PaymentConfig.Gateway.class);
            out.add("gateway.url -> " + gw.url());
            out.add("gateway.clock é o Clock da InfraConfig -> " + (gw.clock() == ctx.getBean(java.time.Clock.class)));
            var p = ctx.getBean(PricingService.class);
            out.add("PricingService (escaneado) -> maxItems=" + p.maxItems() + " currency=" + p.currency());
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
