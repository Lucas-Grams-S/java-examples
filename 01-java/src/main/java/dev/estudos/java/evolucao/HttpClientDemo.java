package dev.estudos.java.evolucao;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * J00-06 · Java 11: HttpClient.
 * Documento: java-docs/01-java/00-evolucao-do-java.md §4.6.
 *
 * <p>Sobe um servidor HTTP local (JDK {@code com.sun.net.httpserver}) para o exemplo não depender de internet.
 *
 * <p>Saída real (JDK 25; a porta varia):
 * <pre>
 * status -&gt; 200
 * versão HTTP -&gt; HTTP_1_1
 * corpo -&gt; [{"id":1}]
 * sendAsync -&gt; [{"id":1}]
 * </pre>
 *
 * <p>Variações: troque {@code send} por {@code sendAsync(...).thenApply(HttpResponse::body).join()}.
 */
public class HttpClientDemo {
    public static List<String> run() throws IOException, InterruptedException {
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/orders", ex -> {
            byte[] body = "[{\"id\":1}]".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, body.length);
            try (var os = ex.getResponseBody()) { os.write(body); }
        });
        server.start();
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI uri = URI.create("http://localhost:" + server.getAddress().getPort() + "/orders");
            HttpRequest req = HttpRequest.newBuilder(uri).GET().build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            String async = client.sendAsync(req, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).join();
            return List.of("status -> " + res.statusCode(), "versão HTTP -> " + res.version(),
                    "corpo -> " + res.body(), "sendAsync -> " + async);
        } finally {
            server.stop(0);
        }
    }

    public static void main(String[] args) throws Exception {
        run().forEach(System.out::println);
    }
}
