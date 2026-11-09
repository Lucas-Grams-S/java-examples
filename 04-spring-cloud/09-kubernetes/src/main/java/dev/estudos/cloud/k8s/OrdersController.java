package dev.estudos.cloud.k8s;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
class OrdersController {
    @Value("${greeting:default-do-codigo}") String greeting;
    @Value("${db.password:sem-senha}") String pwd;
    private final DiscoveryClient dc;

    OrdersController(DiscoveryClient dc) { this.dc = dc; }

    @GetMapping("/cfg")
    Map<String, String> cfg() {
        return Map.of("greeting", greeting, "pwdLength", String.valueOf(pwd.length()),
                      "pod", System.getenv().getOrDefault("HOSTNAME", "?"));
    }

    @GetMapping("/services")
    List<String> services() { return dc.getServices(); }

    @GetMapping("/instances/{name}")
    List<Map<String, Object>> instances(@PathVariable String name) {
        return dc.getInstances(name).stream()
            .map(i -> Map.<String, Object>of("host", i.getHost(), "port", i.getPort(), "metadata", i.getMetadata()))
            .toList();
    }
}
