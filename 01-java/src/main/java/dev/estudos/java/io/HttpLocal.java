package dev.estudos.java.io;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/** Servidor HTTP local (JDK) usado por J13-09 e J13-10, sem internet. Não é exemplo. */
final class HttpLocal implements AutoCloseable {
    private final HttpServer server;

    HttpLocal() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/orders/1", ex -> reply(ex, 200, "{\"id\":1}"));
        server.createContext("/orders/lento", ex -> {
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            reply(ex, 200, "{}");
        });
        server.createContext("/orders/503", ex -> reply(ex, 503, "indisponível"));
        server.createContext("/orders", ex -> {
            if ("POST".equals(ex.getRequestMethod())) {
                String corpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String ct = ex.getRequestHeaders().getFirst("Content-Type");
                if (corpo.contains("\"customerId\"")) reply(ex, 201, "{\"created\":true,\"ct\":\"" + ct + "\"}");
                else reply(ex, 400, "{\"erro\":\"customerId obrigatório\"}");
            } else reply(ex, 405, "método não permitido");
        });
        server.start();
    }

    String url(String path) { return "http://127.0.0.1:" + server.getAddress().getPort() + path; }

    private static void reply(com.sun.net.httpserver.HttpExchange ex, int status, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(status, b.length);
        try (var os = ex.getResponseBody()) { os.write(b); }
    }

    @Override public void close() { server.stop(0); }
}
