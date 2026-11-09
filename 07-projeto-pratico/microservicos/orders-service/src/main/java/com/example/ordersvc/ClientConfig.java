package com.example.ordersvc;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration(proxyBeanMethods = false)
class ClientConfig {
    /** Builder comum é o primário (o cliente Eureka usa ele, ver CL03-12); o balanceado é pedido por qualificador. */
    /** Os customizers do Boot (observação/tracing) precisam ser aplicados: o builder cru não propaga o traceparent. */
    @Bean @Primary RestClient.Builder plainBuilder(org.springframework.beans.factory.ObjectProvider<org.springframework.boot.restclient.RestClientCustomizer> c) {
        var b = RestClient.builder(); c.orderedStream().forEach(x -> x.customize(b)); return b;
    }

    @Bean @LoadBalanced RestClient.Builder lbBuilder(org.springframework.beans.factory.ObjectProvider<org.springframework.boot.restclient.RestClientCustomizer> c) {
        var b = RestClient.builder(); c.orderedStream().forEach(x -> x.customize(b)); return b;
    }

    @Bean
    CatalogClient catalogClient(@LoadBalanced RestClient.Builder lb, RestClient.Builder plain,
                                @Value("${catalog.url}") String url, @Value("${catalog.balanced}") boolean balanced,
                                @Value("${catalog.timeout}") Duration timeout) {
        var settings = HttpClientSettings.defaults().withReadTimeout(timeout).withConnectTimeout(timeout);   // TIMEOUT
        var factory = ClientHttpRequestFactoryBuilder.detect().build(settings);
        RestClient rc = (balanced ? lb : plain).baseUrl(url).requestFactory(factory).build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(rc)).build().createClient(CatalogClient.class);
    }
}
