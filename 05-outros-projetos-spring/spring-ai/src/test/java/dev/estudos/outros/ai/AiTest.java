package dev.estudos.outros.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** OP06-02 (contra o FakeChatModel: valida a mecânica do ChatClient, não a qualidade de um LLM). */
@SpringBootTest
class AiTest {
    @Autowired Assistant assistant;
    @Autowired FakeChatModel model;

    @Test void op0602_call_content() {
        assertThat(assistant.ask("Olá")).isEqualTo("Resposta simulada");
    }

    @Test void op0602_entity_desserializaParaRecord_eAnexaInstrucoesDeFormato() {
        model.prompts.clear();
        var s = assistant.resumir("2 canetas e 1 caderno para Ana");
        assertThat(s).isEqualTo(new Assistant.OrderSummary("Ana", java.util.List.of("2 canetas", "1 caderno"), 19.5));
        assertThat(model.prompts.get(0)).contains("Resuma este pedido").contains("JSON");   // instruções de formato
    }

    /**
     * A parte Java do @Tool: o Spring AI deriva nome, descrição e JSON Schema do método e o executa a partir do JSON
     * de argumentos que o modelo devolveria. (O ciclo completo com um LLM exige provedor real: ver DIVERGENCIAS.md.)
     */
    @Test void op0602_tool_definicaoSchemaEExecucao() {
        Assistant.OrderTools.CHAMADAS.clear();
        var tool = org.springframework.ai.support.ToolCallbacks.from(new Assistant.OrderTools())[0];
        assertThat(tool.getToolDefinition().name()).isEqualTo("status");
        assertThat(tool.getToolDefinition().description()).isEqualTo("Busca o status de um pedido pelo id");
        assertThat(tool.getToolDefinition().inputSchema()).contains("orderId").contains("string");
        assertThat(tool.call("{\"orderId\":\"42\"}")).contains("PAID");
        assertThat(Assistant.OrderTools.CHAMADAS).containsExactly("42");
    }
}
