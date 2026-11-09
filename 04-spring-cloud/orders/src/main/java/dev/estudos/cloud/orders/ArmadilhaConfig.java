package dev.estudos.cloud.orders;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/** CL03-12: SO o builder balanceado. O cliente Eureka o recebe tambem e tenta resolver "localhost" como servico. */
@Configuration
@Profile("armadilha")
class ArmadilhaConfig {
    @Bean @LoadBalanced
    RestClient.Builder lbBuilder() { return RestClient.builder(); }

    @Bean
    CatalogClient catalogClient(@LoadBalanced RestClient.Builder builder) {
        RestClient rc = builder.baseUrl("http://catalog").build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(rc)).build().createClient(CatalogClient.class);
    }
}
