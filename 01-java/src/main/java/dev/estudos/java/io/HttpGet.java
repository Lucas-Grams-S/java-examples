package dev.estudos.java.io;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * J13-09 · HttpClient: GET síncrono e assíncrono, timeouts, versão HTTP, 5xx sem exceção.
 * Documento: java-docs/01-java/13-io-nio-e-http.md §5.2 (e §3.3). Servidor: HttpServer local do JDK.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * send -&gt; 200 &#123;"id":1&#125;
 * versão negociada em HTTP puro -&gt; HTTP_1_1
 * sendAsync -&gt; &#123;"id":1&#125;
 * 503 NÃO lança exceção -&gt; status 503 indisponível
 * timeout da requisição -&gt; HttpTimeoutException
 * falha de rede -&gt; IOException
 * </pre>
 */
public class HttpGet {
    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        try (HttpLocal srv = new HttpLocal();
             HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()) {
            HttpRequest req = HttpRequest.newBuilder(URI.create(srv.url("/orders/1")))
                .timeout(Duration.ofSeconds(2)).header("Accept", "application/json").GET().build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            out.add("send -> " + res.statusCode() + " " + res.body());
            out.add("versão negociada em HTTP puro -> " + res.version());
            out.add("sendAsync -> " + client.sendAsync(req, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).get());

            HttpResponse<String> r503 = client.send(HttpRequest.newBuilder(URI.create(srv.url("/orders/503"))).build(),
                HttpResponse.BodyHandlers.ofString());
            out.add("503 NÃO lança exceção -> status " + r503.statusCode() + " " + r503.body());

            HttpRequest lento = HttpRequest.newBuilder(URI.create(srv.url("/orders/lento"))).timeout(Duration.ofMillis(100)).build();
            try { client.send(lento, HttpResponse.BodyHandlers.ofString()); }
            catch (HttpTimeoutException e) { out.add("timeout da requisição -> HttpTimeoutException"); }
        }
        try (HttpClient c = HttpClient.newHttpClient()) {
            // porta fechada: falha de rede lança IOException
            try { c.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:1/")).build(), HttpResponse.BodyHandlers.discarding()); }
            catch (java.io.IOException e) { out.add("falha de rede -> IOException"); }
        }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
