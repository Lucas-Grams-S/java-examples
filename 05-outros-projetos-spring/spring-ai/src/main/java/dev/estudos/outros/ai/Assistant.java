package dev.estudos.outros.ai;

import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class Assistant {
    public record OrderSummary(String customer, List<String> items, double total) {}

    /** Ferramenta exposta ao modelo (o Spring executa o método quando o modelo pede). */
    public static class OrderTools {
        public static final List<String> CHAMADAS = new java.util.concurrent.CopyOnWriteArrayList<>();

        @Tool(description = "Busca o status de um pedido pelo id")
        public String status(String orderId) {
            CHAMADAS.add(orderId);
            return "PAID";
        }
    }

    private final ChatClient chatClient;

    public Assistant(ChatModel model) { this.chatClient = ChatClient.builder(model).build(); }

    public String ask(String question) {
        return chatClient.prompt().user(question).call().content();
    }

    public OrderSummary resumir(String text) {
        return chatClient.prompt().user("Resuma este pedido: " + text).call().entity(OrderSummary.class);
    }
}
