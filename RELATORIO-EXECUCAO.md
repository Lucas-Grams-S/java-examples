# Relatório de execução

- Início: 2026-11-05 21:14:40 · Fim: 2026-11-05 21:41:28
- JDK: openjdk version "25.0.4.1" 2026-08-18
- Resultado do Maven: BUILD SUCCESS
- Testes: 490 · falhas: 0 · erros: 0 · ignorados: 1

## Exemplos no catálogo
| Status | Quantidade |
|---|---|
| ✅ pronto | 404|
| ❌ não executável | 0|
| — conceitual | 736|

## Módulos (resumo do reator)
```
[INFO] Reactor Summary for java-examples 1.0.0-SNAPSHOT:
[INFO] 
[INFO] java-examples ...................................... SUCCESS [  0.001 s]
[INFO] 01 - Java .......................................... SUCCESS [ 42.429 s]
[INFO] 02 - Spring Core (Framework puro, sem Boot) ........ SUCCESS [  2.461 s]
[INFO] 03 - Spring Boot (um app por documento) ............ SUCCESS [  0.001 s]
[INFO] 03.01 - O que é Spring Boot ........................ SUCCESS [  2.269 s]
[INFO] 03.02 - Auto-configuração .......................... SUCCESS [  1.518 s]
[INFO] 03.03 - Configuração externalizada ................. SUCCESS [  8.595 s]
[INFO] 03.04 - Web MVC .................................... SUCCESS [  2.906 s]
[INFO] 03.05 - WebFlux e programação reativa .............. SUCCESS [  5.104 s]
[INFO] 03.06 - Spring Data JPA ............................ SUCCESS [  3.622 s]
[INFO] 03.07 - Spring Data: JDBC, Mongo, Redis ............ SUCCESS [  5.355 s]
[INFO] 03.08 - Spring Security ............................ SUCCESS [  2.852 s]
[INFO] 03.09 - Actuator e observabilidade ................. SUCCESS [  5.435 s]
[INFO] 03.10 - Testes no Spring Boot ...................... SUCCESS [  7.333 s]
[INFO] 03.11 - Cache, agendamento e async ................. SUCCESS [  2.555 s]
[INFO] 03.12 - Migrações de banco (Flyway) ................ SUCCESS [  2.245 s]
[INFO] 03.13 - Empacotamento e deploy ..................... SUCCESS [ 58.053 s]
[INFO] 04 - Spring Cloud (laboratorio: config, eureka, catalog, orders, gateways) SUCCESS [  0.000 s]
[INFO] 04 - Config Server ................................. SUCCESS [  0.149 s]
[INFO] 04 - Eureka Server ................................. SUCCESS [  0.207 s]
[INFO] 04 - catalog (Config Client + Eureka Client) ....... SUCCESS [  0.105 s]
[INFO] 04 - orders (HTTP Service Client + LoadBalancer) ... SUCCESS [  0.106 s]
[INFO] 04 - Gateway (WebFlux) ............................. SUCCESS [ 12.707 s]
[INFO] 04 - Gateway (Web MVC) ............................. SUCCESS [  0.100 s]
[INFO] 04 - resiliencia (Circuit Breaker + @Retryable/@ConcurrencyLimit) SUCCESS [  5.776 s]
[INFO] 04 - resiliencia (Resilience4j por anotação e propriedades) SUCCESS [  2.283 s]
[INFO] 04 - mensageria (Spring Cloud Stream + binder Kafka) SUCCESS [  9.455 s]
[INFO] 04 - tracing distribuido (Micrometer Tracing + Brave + Zipkin) SUCCESS [ 10.076 s]
[INFO] 04 - tracing distribuido (OpenTelemetry + OTLP) .... SUCCESS [  3.401 s]
[INFO] 04 - Spring Cloud Kubernetes (config, reload, discovery pela API) SUCCESS [ 48.295 s]
[INFO] 04 - Laboratorio (testes de integracao com subprocessos) SUCCESS [01:49 min]
[INFO] 05 - Outros projetos Spring ........................ SUCCESS [  0.000 s]
[INFO] 05.01 - Spring Batch ............................... SUCCESS [  1.721 s]
[INFO] 05.02 - Spring Integration ......................... SUCCESS [  2.252 s]
[INFO] 05.03 - Spring Modulith ............................ SUCCESS [  3.152 s]
[INFO] 05.04 - Spring Authorization Server ................ SUCCESS [  2.653 s]
[INFO] 05.05 - Spring for GraphQL ......................... SUCCESS [  2.981 s]
[INFO] 05.06 - Spring AI .................................. SUCCESS [  1.810 s]
[INFO] 05.06b - Spring AI com Ollama real ................. SUCCESS [18:37 min]
[INFO] 05.07 - Spring Session ............................. SUCCESS [  2.816 s]
[INFO] 05.08 - Spring HATEOAS ............................. SUCCESS [  2.192 s]
[INFO] 05.09 - Spring REST Docs ........................... SUCCESS [  2.054 s]
[INFO] 06 - Padrões de projeto ............................ SUCCESS [  3.275 s]
[INFO] 07 - Projeto prático ............................... SUCCESS [  0.000 s]
[INFO] 07 - API de pedidos (checkpoints 0 a 10) ........... SUCCESS [ 13.806 s]
[INFO] 07 - Projeto pratico: extensoes 11-13 (dois servicos, resiliencia/mensageria, Kubernetes) SUCCESS [  0.000 s]
[INFO] 07 - PP11: catalog-service (banco proprio, Config Server, Eureka, consumidor Kafka) SUCCESS [  0.216 s]
[INFO] 07 - PP11/PP12: orders-service (banco proprio, HTTP Service Client, resiliencia, Outbox, tracing) SUCCESS [  0.141 s]
[INFO] 07 - PP11-13: laboratório (testes de integração com subprocessos, Kafka, Zipkin e k3s) SUCCESS [01:36 min]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

## Fechamento das pendências (E17)
Itens que estavam ❌/parciais e agora têm exemplo e teste: SB09-15/16, SB13-11/12, CL06-14/15, CL07-13, CL08-10/11, CL09-12,
PP11–PP13, J14-05 (Gradle), SC10-10 (native) e OP06-02 com Ollama real. Novos módulos: `04-spring-cloud/06b-resiliencia-anotacoes`,
`04-spring-cloud/08b-tracing-otlp`, `05-outros-projetos-spring/spring-ai-ollama`, `07-projeto-pratico/microservicos/{catalog-service,orders-service,lab-it}`.

Ferramentas e modelos usados nesta execução:
- GraalVM Oracle 25.0.4 e Gradle 9.8.2 em `~/tools` (fora do reator; `scripts/native-image.sh` repete o build nativo: executável de 96 MB, startup 0,073 s).
- Ollama local: `deepseek-r1:14b` (chat/estruturada/memória/RAG), `nomic-embed-text` (embeddings) e `llama3.1:8b` (só tool calling). `OllamaTest` leva ~16 min em CPU; sem Ollama no ar é ignorado.
- Docker: Kafka, Zipkin, k3s, Postgres, Mongo, Redis e Paketo (buildpacks).

Instabilidades corrigidas durante o fechamento (eram dos testes, não do código):
- `ReativoTest.sb0511`: `flatMap` não preserva ordem; o teste agora compara sem ordem.
- `ConfigServerLabIT` (refresh): o fetch do git pode falhar logo após o commit sob carga; o teste repete o refresh.
- `Pp13KubernetesIT`: espera o CoreDNS e aquece o catalog antes do pedido; o `orders` aceita `CATALOG_TIMEOUT` (5 s no ConfigMap).
- `Pp11Pp12IT`: espera o gateway enxergar `orders` no Eureka.
