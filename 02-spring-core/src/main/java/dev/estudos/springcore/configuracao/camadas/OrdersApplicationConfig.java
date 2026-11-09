package dev.estudos.springcore.configuracao.camadas;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/** Configuração raiz por camadas: importa configs de infra/pagamento, varre o pacote e lê {@code orders.properties}. */
@Configuration
@Import({PaymentConfig.class, InfraConfig.class})
@ComponentScan(basePackageClasses = OrdersApplicationConfig.class)
@PropertySource("classpath:orders.properties")
public class OrdersApplicationConfig {}
