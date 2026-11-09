package dev.estudos.padroes.microsservicos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPatternParser;
import org.yaml.snakeyaml.Yaml;

/**
 * DP07-16 · Strangler Fig: as rotas do doc (YAML do Spring Cloud Gateway) lidas e avaliadas na ordem,
 * a primeira que casa vence. É a <b>semântica de ordem</b> do padrão; o Gateway real subindo
 * (rotas reais, {@code lb://}) está em 04-spring-cloud/04-api-gateway (CL04).
 * Documento: java-docs/06-padroes-de-projeto/07-padroes-de-microsservicos.md §5.4.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * rotas = [orders-new, legacy]
 * /orders/42 -&gt; orders-new (lb://orders)
 * /customers/7 -&gt; legacy (http://legacy-monolith:8080)
 * /orders -&gt; orders-new (lb://orders)
 * ordem invertida: /orders/42 -&gt; legacy (o legado engole tudo)
 * </pre>
 */
public class StranglerFigRotas {
    static final String YAML = """
        spring:
          cloud:
            gateway:
              server:
                webflux:
                  routes:
                    - id: orders-new
                      uri: lb://orders
                      predicates: [ Path=/orders/** ]
                    - id: legacy
                      uri: http://legacy-monolith:8080
                      predicates: [ Path=/** ]
        """;

    record Route(String id, String uri, String pathPattern) {}

    @SuppressWarnings("unchecked")
    static List<Route> parse(String yaml) {
        Map<String, Object> m = new Yaml().load(yaml);
        var cur = (Map<String, Object>) m.get("spring");
        for (String k : List.of("cloud", "gateway", "server", "webflux")) cur = (Map<String, Object>) cur.get(k);
        List<Route> rs = new ArrayList<>();
        for (var r : (List<Map<String, Object>>) cur.get("routes")) {
            String p = ((List<String>) r.get("predicates")).getFirst().replaceFirst("^Path=", "");
            rs.add(new Route((String) r.get("id"), (String) r.get("uri"), p));
        }
        return rs;
    }

    static Route match(List<Route> routes, String path) {
        var pc = PathContainer.parsePath(path);
        return routes.stream().filter(r -> new PathPatternParser().parse(r.pathPattern()).matches(pc)).findFirst().orElseThrow();
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var routes = parse(YAML);
        out.add("rotas = " + routes.stream().map(Route::id).toList());
        for (String p : List.of("/orders/42", "/customers/7", "/orders")) {
            var r = match(routes, p);
            out.add(p + " -> " + r.id() + " (" + r.uri() + ")");
        }
        var invertida = List.of(routes.get(1), routes.get(0));
        out.add("ordem invertida: /orders/42 -> " + match(invertida, "/orders/42").id() + " (o legado engole tudo)");
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
