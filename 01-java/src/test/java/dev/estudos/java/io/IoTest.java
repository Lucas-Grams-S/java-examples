package dev.estudos.java.io;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos J13-02, 03, 08 a 12. */
class IoTest {
    @Test void j1302_path_files() throws Exception {
        assertThat(PathFiles.run()).contains("readString -> o1;10\\no2;20\\n", "soma via Files.lines -> 30", "primeira linha -> o1;10",
            "walk (dir + 2 arquivos) -> 3", "list (um nível) -> [copy.txt, orders.txt]", "size -> 12",
            "copy sem REPLACE_EXISTING -> FileAlreadyExistsException", "APPEND -> [o1;10, o2;20, o3;30]",
            "arquivo ausente -> NoSuchFileException", "resolveSeguro('a.txt') -> a.txt", "path traversal -> SecurityException");
    }
    @Test void j1303_buffers_e_canais() throws Exception {
        assertThat(BuffersCanais.run()).contains("antes de ler -> pos=0 lim=5 cap=5", "depois de read -> pos=5 lim=5",
            "depois de flip -> pos=0 lim=5", "decodificado -> o1;10", "depois de clear -> pos=0 lim=5",
            "sem flip, bytes restantes para ler -> 0", "com flip, bytes restantes para ler -> 4",
            "buffer direto -> isDirect=true, heap -> false", "escrito via canal -> novo",
            "respostas -> [CLIENTE0, CLIENTE1]");
    }
    @Test void j1308_antigo_vs_moderno() throws Exception {
        assertThat(AntigoVsModerno.run()).contains("antigo -> [pedido-1;açaí, pedido-2;pão]",
            "readAllLines -> [pedido-1;açaí, pedido-2;pão]", "bytes de 'ç' -> UTF-8=2, ISO-8859-1=1",
            "UTF-8 lido como ISO-8859-1 -> Ã§", "charset padrão (Java 18+) -> UTF-8",
            "decorators -> pedido-1;açaí", "InputStream.read() devolve byte -> 97");
    }
    @Test void j1309_http_get() throws Exception {
        assertThat(HttpGet.run()).contains("send -> 200 {\"id\":1}", "sendAsync -> {\"id\":1}",
            "503 NÃO lança exceção -> status 503 indisponível", "timeout da requisição -> HttpTimeoutException",
            "falha de rede -> IOException");
    }
    @Test void j1310_http_post() throws Exception {
        assertThat(HttpPost.run()).containsExactly(
            "sucesso -> 201 {\"created\":true,\"ct\":\"application/json\"}",
            "erro de negócio -> Falha 400: {\"erro\":\"customerId obrigatório\"}");
    }
    @Test void j1311_csv_grande() throws Exception {
        assertThat(CsvGrande.run()).contains("linhas importadas (total > 0) -> 180000", "soma dos totais -> 900000");
    }
    @Test void j1312_watch_service() throws Exception {
        assertThat(WatchServiceDemo.run()).contains("ENTRY_CREATE novo.txt", "reset (continua válida) -> true");
    }
}
