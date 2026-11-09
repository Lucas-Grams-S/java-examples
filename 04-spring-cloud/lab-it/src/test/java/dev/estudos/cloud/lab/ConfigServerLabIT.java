package dev.estudos.cloud.lab;

import static dev.estudos.cloud.lab.Lab.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/** Doc 02 (CL02-02, 08, 09, 10, 11, 12): Config Server + Config Client com @RefreshScope. */
@TestMethodOrder(MethodOrderer.MethodName.class)
class ConfigServerLabIT {
    @TempDir static Path tmp;
    static Lab lab;
    static Path repo;
    static int server;

    @BeforeAll static void up() throws Exception {
        lab = new Lab(tmp);
        repo = newGitRepo(tmp.resolve("config-repo"), Map.of(
            "application.yml", "shop.currency: BRL\n",
            "catalog.yml", "catalog.discount: 5\n",
            "catalog-prod.yml", "catalog.discount: 20\n"));
        server = freePort();
        lab.start("config-server", "config-server", Map.of(), "--server.port=" + server, "--CONFIG_REPO=file://" + repo);
        await("config server", 90, () -> ok(server, "/catalog/default"));
    }

    @AfterAll static void down() { lab.close(); }

    Process catalog(String id, int port, String... extra) throws Exception {
        var args = new java.util.ArrayList<>(java.util.List.of("--server.port=" + port, "--eureka.client.enabled=false",
            "--spring.config.import=optional:configserver:http://localhost:" + server));
        args.addAll(java.util.List.of(extra));
        return lab.start(id, "catalog", Map.of(), args.toArray(String[]::new));
    }

    @Test void cl0202_ordemDeResolucao() throws Exception {
        String sha = git(repo, "rev-parse", "HEAD").trim();
        String json = get(server, "/catalog/prod").body();
        // catalog-prod.yml vence catalog.yml, que vence application.yml (global); "version" e o SHA do commit
        int prod = json.indexOf("catalog-prod.yml"), cat = json.indexOf("/catalog.yml"), app = json.indexOf("/application.yml");
        assertThat(prod).isPositive();
        assertThat(prod).isLessThan(cat);
        assertThat(cat).isLessThan(app);
        assertThat(json).contains("\"name\":\"catalog\"", "\"profiles\":[\"prod\"]", "\"catalog.discount\":20", "\"shop.currency\":\"BRL\"");
        assertThat(json).containsPattern("\"version\":\"[0-9a-f]{40}\"");
        assertThat(json).contains(sha.substring(0, 7));   // o SHA servido e o do commit atual do repositorio
    }

    @Test void cl0209_cl0210_refreshAfetaUmaInstanciaPorVez() throws Exception {
        int a = freePort(), b = freePort();
        catalog("catalog-a", a); catalog("catalog-b", b);
        await("catalog a", 90, () -> ok(a, "/products/1"));
        await("catalog b", 90, () -> ok(b, "/products/1"));
        String antes = get(a, "/products/1").body();
        assertThat(antes).contains("\"discount\":5", "\"currency\":\"BRL\"", "\"instancePort\":\"" + a + "\"");

        // novo commit no repositorio: 5 -> 7
        java.nio.file.Files.writeString(repo.resolve("catalog.yml"), "catalog.discount: 7\n");
        commit(repo, "desconto 7");

        // sob carga o Config Server pode falhar o fetch do git logo após o commit (500); um refresh que falha não muda estado: repete
        var r = post(b, "/actuator/refresh");
        for (int i = 0; i < 5 && r.status() != 200; i++) { Thread.sleep(1000); r = post(b, "/actuator/refresh"); }
        assertThat(r.status()).isEqualTo(200);
        assertThat(r.body()).contains("catalog.discount");           // a resposta lista a chave alterada
        assertThat(get(b, "/products/1").body()).contains("\"discount\":7");
        assertThat(get(a, "/products/1").body()).contains("\"discount\":5");   // refresh e por instancia
    }

    @Test void cl0212_deOndeVeioEsteValor() throws Exception {
        int p = freePort();
        catalog("catalog-env", p, "--spring.profiles.active=prod");
        await("catalog env", 90, () -> ok(p, "/products/1"));
        String env = get(p, "/actuator/env/catalog.discount").body();
        // o PropertySource que forneceu o valor aparece; o valor vem mascarado (Boot 4: padrao show-values=never)
        assertThat(env).contains("\"source\":\"configserver:").contains("catalog-prod.yml").contains("\"value\":\"******\"");
        assertThat(get(p, "/products/1").body()).contains("\"discount\":20");
    }

    @Test void cl0211_servidorForaDoAr() throws Exception {
        int dead = freePort();
        // com optional: ignora o servidor, mas o @Value falha ao criar o bean (erro so no primeiro uso, mensagem sem apontar para o servidor)
        Process opt = lab.start("sem-servidor-optional", "catalog", Map.of(), "--server.port=" + freePort(), "--eureka.client.enabled=false",
            "--spring.config.import=optional:configserver:http://localhost:" + dead);
        assertThat(opt.waitFor(90, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
        assertThat(opt.exitValue()).isNotZero();
        assertThat(lab.log("sem-servidor-optional")).contains("Could not resolve placeholder 'catalog.discount'");

        // sem optional: falha rapida e clara
        Process req = lab.start("sem-servidor", "catalog", Map.of(), "--server.port=" + freePort(), "--eureka.client.enabled=false",
            "--spring.config.import=configserver:http://localhost:" + dead);
        assertThat(req.waitFor(90, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
        assertThat(req.exitValue()).isNotZero();
        assertThat(lab.log("sem-servidor")).contains("ConfigClientFailFastException");
    }
}
