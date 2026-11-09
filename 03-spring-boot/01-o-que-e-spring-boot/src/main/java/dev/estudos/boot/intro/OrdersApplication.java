package dev.estudos.boot.intro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * SB01-01 / SB01-07 · {@code @SpringBootApplication} e o menor projeto Boot (starter {@code webmvc} +
 * {@code spring-boot-starter-parent}). A classe fica no pacote raiz: o scan começa aqui.
 * Documento: 03-spring-boot/01-o-que-e-spring-boot.md §2.1 e §5.1.
 *
 * <p>Saída real (trecho; {@code ./mvnw -pl 03-spring-boot/01-o-que-e-spring-boot spring-boot:run}):
 * <pre>
 * Tomcat started on port 8080 (http) with context path '/'
 * Started OrdersApplication in 0.8 seconds
 * Moeda padrão: BRL
 * </pre>
 * Variações: rode com {@code --debug} (relatório de condições, SB01-08) ou
 * {@code --orders.currency=USD}; mova esta classe para um subpacote e veja o scan deixar de achar os beans.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class OrdersApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrdersApplication.class, args);
    }
}
