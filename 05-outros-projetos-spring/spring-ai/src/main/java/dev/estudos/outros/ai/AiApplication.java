package dev.estudos.outros.ai;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * OP06-02: {@code ChatClient} do Spring AI 2.0 contra um {@link FakeChatModel} (determinístico, sem rede nem chave).
 * Troque o {@code ChatModel} pelo de um provedor (starter {@code spring-ai-starter-model-*}) e o código do cliente não muda.
 */
@SpringBootApplication
public class AiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }

    @Bean CommandLineRunner demo(Assistant assistant) {
        return args -> {
            System.out.println("ask      = " + assistant.ask("Olá"));
            System.out.println("resumo   = " + assistant.resumir("2 canetas e 1 caderno para Ana"));
            var tool = org.springframework.ai.support.ToolCallbacks.from(new Assistant.OrderTools())[0];
            System.out.println("tool     = " + tool.getToolDefinition().name() + " -> " + tool.call("{\"orderId\":\"42\"}"));
        };
    }
}
