package dev.estudos.boot.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * SB03-10 · Propriedades tipadas com record ({@code @ConfigurationProperties} + {@code @DefaultValue} + validação).
 * Documento: 03-configuracao-externalizada.md §5.1.
 *
 * <p>Saída real ({@code ./scripts/run-example.sh SB03-10}, com {@code --spring.profiles.active=prod}):
 * <pre>
 * OrdersProperties[currency=BRL, paymentTimeout=PT45S, limits=Limits[maxItems=10], paymentMethods=[CARD]]
 * </pre>
 * Sem o perfil {@code prod}: {@code paymentTimeout=PT30S} e {@code maxItems=50} (os {@code @DefaultValue}).
 */
@Validated
@ConfigurationProperties("orders")
public record OrdersProperties(
        @NotBlank String currency,
        @DefaultValue("30s") Duration paymentTimeout,
        @DefaultValue Limits limits,                       // objeto sempre não nulo
        @DefaultValue("CARD") List<String> paymentMethods) {

    public record Limits(@Min(1) @DefaultValue("50") int maxItems) {}
}
