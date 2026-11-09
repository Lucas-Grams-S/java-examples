package dev.estudos.cloud.orders;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/** CL05-07: forma manual. O builder comum e primario e o que o proprio cliente Eureka obtem (CL03-12). */
@Configuration
@Profile("manual")
class ManualConfig {
    @Bean @Primary
    RestClient.Builder plainBuilder() { return RestClient.builder(); }

    @Bean @LoadBalanced
    RestClient.Builder lbBuilder() { return RestClient.builder(); }

    @Bean
    CatalogClient catalogClient(@LoadBalanced RestClient.Builder builder) {
        RestClient rc = builder.baseUrl("http://catalog").build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(rc)).build().createClient(CatalogClient.class);
    }
}
