package dev.estudos.outros.aiollama;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * OP06-02 com o Ollama REAL (modelo {@code deepseek-r1:14b}, embeddings {@code nomic-embed-text}).
 * Respostas de LLM variam: as asserções verificam o INVARIANTE (a ferramenta foi chamada, o fato recuperado aparece),
 * não o texto exato. Se o Ollama não estiver em {@code localhost:11434}, os testes são ignorados.
 */
@SpringBootTest
@Timeout(600)
class OllamaTest {
    @Autowired ChatModel model;
    @Autowired EmbeddingModel embeddings;

    @BeforeAll static void ollamaNoAr() {
        boolean ok;
        try {
            ok = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://localhost:11434/api/tags")).build(),
                HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { ok = false; }
        assumeTrue(ok, "Ollama não está em localhost:11434");
    }

    /** O deepseek-r1 devolve o raciocínio entre <think>…</think>; o que interessa é o texto depois dele. */
    static String resposta(String s) { return s.replaceAll("(?s)<think>.*?</think>", "").trim(); }

    record Resumo(String cliente, List<String> itens) {}

    @Test void op0602_chatClientResponde() {
        String r = resposta(ChatClient.builder(model).build().prompt()
            .user("Responda somente com a palavra ABACAXI, em maiúsculas, sem mais nada.").call().content());
        assertThat(r.toUpperCase()).contains("ABACAXI");
    }

    @Test void op0602_saidaEstruturadaViraRecord() {
        Resumo r = ChatClient.builder(model).build().prompt()
            .user("Extraia do pedido: 'Ana comprou 2 canetas e 1 caderno'. O campo cliente é o nome e itens a lista de itens comprados.")
            .call().entity(Resumo.class);
        assertThat(r.cliente()).containsIgnoringCase("Ana");
        assertThat(r.itens()).hasSize(2);
    }

    @Test void op0602_toolCallingExecutaOMetodoJava() {
        var tools = new OrderTools();
        // deepseek-r1:14b anuncia "tools" mas, na prática, não chama a ferramenta (nem pela API crua do Ollama; ver DIVERGENCIAS.md);
        // só este teste troca o modelo para um com tool calling confiável.
        String r = resposta(ChatClient.builder(model).defaultOptions(
                org.springframework.ai.ollama.api.OllamaChatOptions.builder().model(System.getProperty("ollama.tools.model", "llama3.1:8b"))).build().prompt()
            .user("Qual o status do pedido 42? Use a ferramenta e responda citando o código que ela devolveu.")
            .tools(tools).call().content());
        assertThat(tools.chamadas).as("o modelo pediu a ferramenta e o Spring executou o método").contains("42");
        assertThat(r).contains("ZX81");       // a resposta final usa o resultado da ferramenta
    }

    @Test void op0602_memoriaDeConversa() {
        var memory = MessageWindowChatMemory.builder().maxMessages(10).build();
        var client = ChatClient.builder(model).defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build()).build();
        client.prompt().user("Meu nome é Rodolfo. Apenas confirme com OK.")
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "c1")).call().content();
        String r = resposta(client.prompt().user("Qual é o meu nome? Responda só o nome.")
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "c1")).call().content());
        assertThat(r).containsIgnoringCase("Rodolfo");
        // outra conversa não enxerga a primeira
        String outra = resposta(client.prompt().user("Qual é o meu nome? Se não souber, diga NAO SEI.")
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "c2")).call().content());
        assertThat(outra).doesNotContainIgnoringCase("Rodolfo");
    }

    @Test void op0602_ragRecuperaFatoDoVectorStore() {
        var store = SimpleVectorStore.builder(embeddings).build();
        store.add(List.of(
            new Document("A política de troca da loja Zeta permite devolução em até 17 dias corridos."),
            new Document("O horário de atendimento da loja Zeta é das 9h às 18h, de segunda a sábado."),
            new Document("O frete para o Nordeste na loja Zeta custa R$ 29,90.")));
        String r = resposta(ChatClient.builder(model).defaultAdvisors(QuestionAnswerAdvisor.builder(store).build()).build()
            .prompt().user("Em quantos dias posso devolver um produto na loja Zeta? Responda com o número.").call().content());
        assertThat(r).contains("17");
    }
}
