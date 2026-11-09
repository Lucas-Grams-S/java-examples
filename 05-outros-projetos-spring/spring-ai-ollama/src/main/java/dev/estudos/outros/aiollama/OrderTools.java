package dev.estudos.outros.aiollama;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/** Ferramenta exposta ao modelo; o Spring executa o método quando o modelo pede. */
public class OrderTools {
    public final List<String> chamadas = new CopyOnWriteArrayList<>();

    @Tool(description = "Busca o status de um pedido pelo seu id numérico")
    public String orderStatus(@ToolParam(description = "id numérico do pedido") String orderId) {
        chamadas.add(orderId);
        return "PEDIDO-" + orderId + "-STATUS-ENVIADO-CODIGO-ZX81";
    }
}
