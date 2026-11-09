package dev.estudos.outros.ai;

import java.util.List;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

/**
 * "Modelo" de mentira: responde por regras (texto simples ou JSON quando o prompt traz instruções de formato).
 * O ciclo de tools do {@code ChatClient} (2.0: {@code ToolCallingAdvisor}) depende das opções do provedor real e
 * NÃO foi reproduzido com este fake; ver {@link Assistant.OrderTools} e DIVERGENCIAS.md.
 */
@Component
public class FakeChatModel implements ChatModel {
    /** Prompts recebidos (texto das mensagens), para os testes inspecionarem. */
    public final List<String> prompts = new java.util.concurrent.CopyOnWriteArrayList<>();

    @Override
    public ChatResponse call(Prompt prompt) {
        String text = prompt.getContents();
        prompts.add(text);

        if (text.contains("JSON")) {
            return resposta("{\"customer\":\"Ana\",\"items\":[\"2 canetas\",\"1 caderno\"],\"total\":19.5}");
        }
        return resposta("Resposta simulada");
    }

    private static ChatResponse resposta(String s) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(s))));
    }
}
