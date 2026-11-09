package dev.estudos.outros.aiollama;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OP06-02 com provedor REAL: Spring AI 2.0 + Ollama local (contêiner {@code ollama}, porta 11434).
 * Cobre {@code ChatClient}, saída estruturada, tools ({@code @Tool}), memória de conversa e RAG com {@code SimpleVectorStore}.
 */
@SpringBootApplication
public class AiOllamaApplication {
    public static void main(String[] args) { SpringApplication.run(AiOllamaApplication.class, args); }
}
