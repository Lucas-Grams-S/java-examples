package dev.estudos.java.io;

import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;

/**
 * J13-10 · POST com JSON e tratamento de erro por status (o HttpClient não lança exceção para 4xx/5xx).
 * Documento: java-docs/01-java/13-io-nio-e-http.md §5.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * sucesso -&gt; 201 &#123;"created":true,"ct":"application/json"&#125;
 * erro de negócio -&gt; Falha 400: &#123;"erro":"customerId obrigatório"&#125;
 * </pre>
 */
public class HttpPost {
    static String enviar(HttpClient client, String url, String json) throws Exception {
        HttpRequest post = HttpRequest.newBuilder(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> r = client.send(post, HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() / 100 != 2) {
            throw new IllegalStateException("Falha " + r.statusCode() + ": " + r.body());
        }
        return r.statusCode() + " " + r.body();
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        try (HttpLocal srv = new HttpLocal(); HttpClient client = HttpClient.newHttpClient()) {
            out.add("sucesso -> " + enviar(client, srv.url("/orders"), """
                {"customerId": 7, "items": [{"productId": 1, "qty": 2}]}
                """));
            try { enviar(client, srv.url("/orders"), "{}"); }
            catch (IllegalStateException e) { out.add("erro de negócio -> " + e.getMessage()); }
        }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
