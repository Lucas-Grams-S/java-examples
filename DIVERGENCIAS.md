# Divergências entre documentos e código

Registre aqui qualquer ponto em que a execução real contradiz ou complementa o documento.
Formato por entrada: **arquivo do doc · seção · o que diz · o que de fato acontece · evidência (saída/erro/link)**.
Nenhum documento é alterado por conta disso; a decisão de corrigir é sua.

_(nenhuma divergência registrada até a sessão E0)_

- **04-orientacao-a-objetos.md · §2.8 · `CountingList extends ArrayList`**: o documento diz que `ArrayList.addAll` "pode chamar `add()` internamente" e contar em dobro "dependendo da implementação". **No JDK 25 não chama**: `addAll` de 3 elementos resulta em `adds=3` (correto). O bug é real com `HashSet` (o `addAll` herdado de `AbstractCollection` chama `add()`): `adds=6`. Evidência: `CompositionVsInheritance` (J04-08).

## E3 · J10-03 — `getDeclaredMethods` e métodos *bridge*
Se uma classe **pública** herda um método público de uma classe base **não pública** (pacote-privada), o `javac` gera na subclasse um método *bridge* sintético com o mesmo nome; assim `getDeclaredMethods()` da subclasse lista também o método "herdado". O documento (§2.3) diz que `getDeclaredMethods` não traz herdados, o que vale no caso comum (base pública). O exemplo usa base pública para mostrar o comportamento descrito.

## E4 · 15-testes-em-java.md · §3.3 e §5.4 — Mockito/ByteBuddy no Java 25
O documento diz que Mockito 5.18/5.24 falharam no Java 25 e que foi preciso declarar `net.bytebuddy:byte-buddy:1.18.17-jdk5`. Com o **Mockito 5.23.0 + ByteBuddy 1.18.15 fixados pelo BOM do Boot 4.1.2**, os testes (`@Mock`, `@InjectMocks`, `spy`, strict stubs) **passam no Java 25 sem nenhuma dependência extra** (só o aviso de auto-attach do agente). Evidência: `OrderServiceMockTest`, `StrictStubsTest`, `DependenciasTest`. A falha citada ocorre com versões fora do BOM; com o BOM, não é preciso declarar ByteBuddy.

## E4 · 15-testes-em-java.md · §2.5 — armadilha de `double`
O exemplo do aviso usa `assertEquals(1.1, 1.0 + 0.1)`. Em IEEE-754, `1.0 + 0.1 == 1.1` é **verdadeiro**, então esse par específico não ilustra a armadilha. O caso clássico é `0.1 + 0.2 != 0.3` (usado em `AssertJTest#double_com_tolerancia`).

## E4 · 12-concorrencia.md · §3.1 — "sem volatile o laço pode nunca terminar"
Confirmado, mas não determinístico: em `JavaMemoryModel` (J12-06) o laço `while (!stop) { }` sem `volatile` **não terminou** no JDK 25/x86 (o JIT içou a leitura do campo para fora do laço); a thread é daemon para não segurar a JVM. O resultado depende do JIT, então o teste não o afirma; afirma só que, com `volatile`, o laço termina.

## E4 · 14-modulos-e-build.md · §2.5 — Gradle
Gradle não está instalado no ambiente; o trecho `build.gradle.kts` continua não compilado (J14-05 ❌).

## E5 · 05-configuracao.md · §2.6 — `${chave}` ausente "erro se ausente"
A tabela do documento diz que `${chave}` é "resolvido no `Environment` (erro se ausente)". Em um `AnnotationConfigApplicationContext` **puro** (sem Boot, sem `PropertySourcesPlaceholderConfigurer`), o resolvedor embutido do contexto é **leniente**: `@Value("${nao.existe}") String v` recebe o texto literal `${nao.existe}` e o contexto sobe normalmente. O erro só aparece quando há um `PropertySourcesPlaceholderConfigurer` (`@Bean static`), que o Spring Boot registra automaticamente; o Spring 7 lança `PlaceholderResolutionException` (`Could not resolve placeholder 'nao.existe'`). Demonstrado em `PropertySourceValue` (SC05-06). O texto de §3.2 ("também por `PropertySourcesPlaceholderConfigurer`") está correto; a frase da tabela vale para o Boot, não para o contexto puro.


## E6 · 10-aot-e-native.md · §5.1 — nomes dos arquivos AOT gerados
O doc lista `sources/aot/Aot__BeanDefinitions.java`. Com o `ClassNameGenerator(ClassName.get("aot","Aot"))` o initializer e o `BeanFactoryRegistrations` ficam em `aot/`, mas os `__BeanDefinitions` da classe do exemplo vão para o pacote dela (ex.: `sources/dev/estudos/springcore/aot/GerarAot__BeanDefinitions.java`). Conteúdo igual ao descrito (construtor direto, `@Bean` por `forFactoryMethod`, `DevOnly` ausente, hint registrado). Demonstrado em `GerarAot` (SC10-08) e `AntesDepois` (SC10-04).

## E6 · 09-transacoes.md · §5.1 — `swallow` precisa de `Audit.fail()` participante
O cenário `swallow` só resulta em `UnexpectedRollbackException` se `audit.fail()` **participar** da transação externa (propagação padrão `REQUIRED`). O doc descreve `Audit` com `logNew` = `REQUIRES_NEW` e deixa `fail` implícito; com `REQUIRES_NEW` em `fail` a transação externa faria commit (testado: `nenhuma | linhas=1`). Em `TxFixture`, `fail()` é `REQUIRED`. Na tabela do doc, "Linhas" de `withAudit` = 1 conta a linha de **auditoria** que sobreviveu (os pedidos foram desfeitos).

## E7 · 04-web-mvc.md · §5.6 — versionamento: versão não declarada dá 400
O documento deixa o comportamento do versionamento como "VERIFICAR". Verificado (Boot 4.1.2, `spring.mvc.apiversion.default=1.0.0` + `use.header=X-Version`, `@GetMapping(version = "1.0"/"1.1")`): sem cabeçalho vale o default (1.0); `X-Version: 1.1` seleciona o mapeamento 1.1; `X-Version: 1.2` (nenhum mapeamento a declara) responde **400** `Invalid API version: '1.2.0'.` — não cai no "1.1 ou superior". O atributo `version` e a propriedade existem e funcionam como descrito. Demonstrado em `VersionedController` (SB04-15).

## E7 · 04-web-mvc.md · §5.2/§5.3 — `ProblemDetail` padrão e `ResponseEntityExceptionHandler` próprio
Com `spring.mvc.problemdetails.enabled=true` o Boot registra o seu `ProblemDetailsExceptionHandler` **apenas se não existir** um `ResponseEntityExceptionHandler` do usuário. Por isso as respostas de §5.2 (sem campos) e o handler de §5.3 não convivem na mesma aplicação: no exemplo, o handler fica no perfil `campos`. Demonstrado em `OrdersTest` (padrão) e `CamposTest` (com campos).

## E7 · 04-web-mvc.md · §2.3 — formato de data e fuso
`SimpleDateFormat("yyyy-MM")` usa o fuso da JVM: `new Date(0)` vira `1969-12` em `America/Sao_Paulo`. O exemplo (`DateConverterConfig`, SB04-03) usa uma data no meio do mês (1970-02-15) para a saída não depender do fuso.

## E7 · 02-auto-configuracao.md · §5.4 — "Gson não casa" só aparece com o starter web
`GsonHttpMessageConvertersConfiguration` entra no relatório de condições (negativa) mesmo numa aplicação sem servidor web, desde que `spring-boot-http-converter` esteja no classpath (vem com o starter `webmvc`). O número "59 itens positivos" do documento é do projeto com `webmvc` + `validation`; no módulo de exemplo o total difere.

## E8 · 07-spring-data-outros.md · §5.3 — MongoDB 8 não inicia em kernel Linux ≥ 6.19
O contêiner `mongo:8` encerra com `MongoDB cannot start: Linux kernel versions 6.19 and newer has a known incompatibility with this version of MongoDB (SERVER-121912)`; o ambiente tem kernel 7.0. O teste (`MongoTest`, SB07-10) usa `mongo:7`, que sobe normalmente. O código Spring Data (`MongoRepository`, `findByItemsSku`) é idêntico. Propriedade de conexão não precisou ser configurada: `@ServiceConnection` preenche tudo (o `spring.mongodb.uri` do doc continua válido para execução fora de teste).

## E8 · 07-spring-data-outros.md · §5.2 — schema compartilhado entre contextos
Ao ter vários contextos Boot no mesmo módulo de teste, o H2 em memória com `DB_CLOSE_DELAY=-1` persiste entre eles; o `schema.sql` usa `create table if not exists` para ser reexecutável.

## E9 · 09-actuator-e-observabilidade.md · §5.4 — chave do contexto em `/actuator/flyway`
O doc mostra `contexts.application...`; o contexto é identificado pelo `spring.application.name` (aqui `orders`), então o JSON é `contexts.orders.flywayBeans.flyway.migrations[...]`. Há também campos extras (`checksum`, `executionTime`, `installedBy`).

## E9 · 11-cache-agendamento-e-async.md · §5.3 — `AsyncConfigurer` não pode declarar `@Bean`
Uma `@Configuration @EnableAsync` que implementa `AsyncConfigurer` e também declara um método `@Bean` falha no Boot 4.1.2 com `Illegal factory instance for factory method ... ApplicationTaskExecutorAsyncConfigurer is not an instance of ...` (o configurer é instanciado cedo). O exemplo (`AsyncConfig`) fica numa classe só para o handler; o estado observável vive em outro componente (`AsyncErrors`). Injetar um tipo concreto não final com `@Lazy` também falha (CGLIB em `AtomicInteger` no JDK 25).

## E9 · 11-cache-agendamento-e-async.md · §2.4/§5.5 — `@Retryable(maxRetries = 3)`
Confirmado (resolve o ⚠️ VERIFICAR): `maxRetries` conta retentativas **além** da chamada original; com falha permanente a execução total é 4 tentativas. `@ConcurrencyLimit(2)` limitou `maxRunning` a 2 com 6 chamadas simultâneas.

## E9 · 12-migracoes-de-banco.md · §3.2/§5.1 — H2 e a tabela de histórico
No H2 a `flyway_schema_history` (e suas colunas) é criada em **minúsculas entre aspas**; consultar `installed_rank`/`flyway_schema_history` sem aspas falha com `Table "FLYWAY_SCHEMA_HISTORY" not found`. No H2 o histórico também ganha uma linha inicial `type=TABLE` (criação do schema/tabela), antes de `V1`. No PostgreSQL (SB10-14) a contagem de linhas após V1+V2 é 2.

## E10 · 01-visao-geral.md · §2.1 — versões dos módulos dentro do BOM 2025.1.4
O doc cita Config 5.0.4 (com Commons 5.0.4). O BOM 2025.1.4 resolvido neste laboratório traz **Commons/Context/LoadBalancer/Gateway 5.0.4, Config 5.0.6 e Netflix/Eureka 5.0.3** (cada módulo tem numeração própria, que é justamente o ponto do release train). Conferido pelos jars dos apps em `ReleaseTrainIT` (CL01-01).

## E10 · 02-config-server.md · §5.6 — `/actuator/env` mascara o valor
No Boot 4.1.2, `GET /actuator/env/catalog.discount` devolve `"value":"******"` (padrão `show-values=never`), não o valor 20 que o doc sugere ler ali. O `PropertySource` que forneceu o valor continua visível (`"source":"configserver:file:///.../catalog-prod.yml"`), então a pergunta "de onde veio?" é respondida; o valor se confere pelo próprio endpoint de negócio. `ConfigServerLabIT#cl0212`.

## E10 · 03-service-discovery.md · §5.3/§5.4 — ids e tempos acelerados no teste
Os testes encurtam os relógios (`lease-renewal-interval-in-seconds=1`, `registry-fetch-interval-seconds=1`, `loadbalancer.cache.ttl=1s` e, no servidor, `response-cache-update-interval-ms=1000` sem cache somente-leitura) para não esperar 30 s; os padrões do doc (heartbeat 30 s, cópia local 30 s) não foram alterados nos apps. O round-robin exato (3 e 3 em seis chamadas) e a armadilha do `@LoadBalanced RestClient.Builder` (`BeanCurrentlyInCreationException ... scopedTarget.eurekaClient`, `orders` não se registra) reproduziram como descrito.

## E10 · 05-comunicacao-entre-servicos.md · §5.3 — `orders` → `catalog` com instância morta
O doc diz que esse caminho não foi exercitado. Exercitado em `ComunicacaoLabIT#cl0509`: com uma das duas instâncias do `catalog` morta por `kill -9` (o Eureka ainda a lista como UP, eviction padrão 90 s), uma parte das chamadas de `orders` cai nela e o `try/catch` do doc degrada a resposta (`product.unavailable=true`); as demais seguem normais. O tratamento do doc funciona como descrito (`ResourceAccessException`).

## E11 · 08-tracing-distribuido.md · §5.1 — propriedade do endpoint do Zipkin
O doc usa `management.zipkin.tracing.export.endpoint`, que **não existe** no Boot 4.1.2. Pelo metadado de `spring-boot-zipkin-4.1.2.jar` os nomes são `management.tracing.export.zipkin.endpoint` (o usado aqui) e `management.zipkin.tracing.endpoint`. Com o nome do doc o exportador ignora o valor e envia para `localhost:9411`: a chamada funciona, mas o Zipkin do teste (porta mapeada aleatória) fica sem spans, sem nenhum erro de configuração. `TracingTest`.

## E11 · 08-tracing-distribuido.md · §5.1 — `RestClient.Builder` exige `spring-boot-starter-restclient`
No Boot 4 o auto-configure do `RestClient.Builder` (e sua instrumentação de tracing) saiu do `spring-boot-starter-webmvc`: sem `spring-boot-starter-restclient` o controller do doc falha na partida com `NoSuchBeanDefinitionException: RestClient$Builder`. Versão gerenciada pelo BOM.

## E11 · 08-tracing-distribuido.md · §5.1 — Jackson 3 nos testes
Para ler a API do Zipkin no teste, o pacote é `tools.jackson.databind` (Jackson 3, padrão do Boot 4), não `com.fasterxml.jackson.databind`.

## E11 · 09-spring-cloud-e-kubernetes.md · §5.2 — rodar fora do cluster
Fora de um Pod o Boot não detecta a plataforma, e `config.import: "optional:kubernetes:"` é ignorado **em silêncio** (o `/cfg` devolve `default-do-codigo`). É preciso `spring.main.cloud-platform=kubernetes`. Além disso, o informer do discovery (`kubernetesClientInformerDiscoveryClient`) usa o cliente do kubeconfig padrão, **não** `spring.cloud.kubernetes.client.*` (tentou `localhost:8080` e estourou `Timeout waiting for informers cache to be ready`); por isso o teste sobe o app em subprocesso com a variável `KUBECONFIG`. Dentro do cluster nada disso é necessário.

## E11 · 07-mensageria.md · §5.2/§5.3 — Kafka, não Rabbit
Exercitado só o binder Kafka (`apache/kafka` via Testcontainers). A troca para Rabbit (§5.4) **não** foi executada (sem imagem do RabbitMQ no ambiente). Retry/DLQ confirmados: 3 tentativas, mensagem do pedido 2 em `orders-billing-dlq`, e o fluxo seguinte (pedido 3) segue normal. A exceção final aparece no log como `RetryException ... exhausted` (Spring Framework 7 `core.retry`).

## E12 · 03-spring-modulith.md · §5 — precisa de um `TransactionManager`
O exemplo com `@Transactional` em `place(...)` só entrega o evento ao `@ApplicationModuleListener` se houver gerenciador de transação no contexto. Com `spring-boot-starter` + Modulith apenas, `@Transactional` é inócuo e o listener **não roda** (o teste `op0302_comTransacao_listenerRoda` expirou). Foi adicionado `spring-boot-starter-jdbc` + H2 ao módulo.

## E12 · 04-spring-authorization-server.md · §5 — configuração no Security 7.1
- Os configurers saíram do jar `spring-security-oauth2-authorization-server` e estão em `spring-security-config`: `org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer` (construtor público; não existe `authorizationServer()` estático).
- Configuração funcional: `http.securityMatcher(as.getEndpointsMatcher()).with(as, c -> c.oidc(...))` + uma segunda `SecurityFilterChain` com `formLogin`. Com isso o `/.well-known/openid-configuration` responde **200** (o 401 do doc era a falta dessa cadeia).
- Fluxo executado em teste: login → `/oauth2/authorize` (PKCE S256) → `code` → `/oauth2/token` → `access_token` JWT + `id_token`; verifier errado → `invalid_grant`.
- `MockMvc.param(...)` **não** preenche a query string e o conversor do `/oauth2/authorize` lê dela (`invalid_request: response_type`); monte a URL com a query. O `id_token` exige `authenticationTime`, então o teste faz login real pelo formulário em vez de `user(...)`.

## E12 · 01-spring-batch.md · §5 — confirmado
Job de 5 itens com `chunk(2)`: 3 commits, `[order-10, order-20]`, `[order-30, order-40]`, `[order-50]`. Pacotes do 6.0 conforme o doc (`...core.job.Job`, `...infrastructure.item.support.ListItemReader`, `...core.job.parameters.JobParametersBuilder`, `...core.launch.JobOperator`). Repetir os mesmos parâmetros falha ("already complete").

## E12 · 02-spring-integration.md · §8 — mensagem filtrada trava o gateway
Sem `replyTimeout`, `pricing.price("  ")` (descartada pelo `filter`) **bloqueia para sempre**: não há resposta. Reproduzido (o teste ficou preso até o timeout) e coberto com `MessagingTemplate.setReceiveTimeout`.

## E12 · 06-spring-ai.md · §5 — sem LLM, parte do ciclo de tools não reproduzida
Sem provedor/chave, usou-se um `ChatModel` simulado (`FakeChatModel`). Confirmado: `ChatClient.builder(model)`, `.call().content()`, `.call().entity(Record.class)` (as instruções de formato com "JSON" são anexadas ao prompt) e `@Tool` → `ToolCallbacks.from(obj)` (nome, descrição, JSON Schema, execução). **Não reproduzido**: o ciclo `.tools(obj)` do `ChatClient` com o modelo pedindo a função. No 2.0 o laço de tools vive no `ToolCallingAdvisor` (registrado automaticamente no `ChatClient`), e com o modelo simulado as tools não chegaram ao `Prompt` (opções `DefaultChatOptions`); isso depende das opções de um provedor real. Dependência: `spring-ai-client-chat` 2.0.2 via `spring-ai-bom` (sem starter de provedor, para não exigir chave). Memória (`ChatMemory`/`CONVERSATION_ID`) e RAG não executados.

## E12 · 09-spring-rest-docs.md · §5 — confirmado
`spring-boot-starter-restdocs` + `spring-restdocs-mockmvc` (explícito). Gera os seis snippets; `responseFields` com campo não documentado **falha** (mensagem cita `status`). Montagem do HTML com Asciidoctor não executada.

## E15 · 07-projeto-pratico/README.md — verificações respondidas por teste
- **`@TransactionalEventListener(AFTER_COMMIT)` que lança:** o roteiro pergunta o que acontece. No Spring 7 a exceção **não chega ao chamador**: é registrada em log e o pedido continua gravado (`Pp08CacheEventsTest`). Rollback ⇒ o ouvinte nunca roda.
- **Exceção checada:** por padrão **não** desfaz a transação (o pedido é gravado); `rollbackFor` corrige (`Pp03PersistenceTest#matrizDeRollback`).
- **H2 em modo PostgreSQL** mantém nomes do Flyway (`flyway_schema_history`, colunas) em minúsculas: consultas diretas precisam de aspas, ao contrário do Postgres real.
- **`@EnableCaching` na classe principal** quebra os testes de fatia (ver PROGRESSO E15).


## E17 · resiliência, gateway e Kubernetes
- **Resilience4j 2.4.0 × 2.3.0:** `resilience4j-spring-boot4` só existe na 2.4.0; o BOM do Spring Cloud 2025.1.4 traz a 2.3.0. A 2.4.0 muda a exceção do timeout (`ExecutionException` em vez de `TimeoutException`/`RuntimeException`), por isso CL06-14 ficou no módulo `06b-resiliencia-anotacoes`. Juntos, `resilience4j-spring-boot3` (do starter do Spring Cloud) e `-boot4` duplicam auto-configurações.
- **Gateway 5.0 + CircuitBreaker:** sem `statusCodes`, uma resposta 5xx do upstream também aciona o `fallbackUri` (o doc só fala de falha/timeout).
- **Boot 4 OTLP/Zipkin:** propriedades reais: `management.opentelemetry.tracing.export.otlp.endpoint` e `management.otlp.metrics.export.url`; `EndpointRequest` está em `org.springframework.boot.security.autoconfigure.actuate.web.servlet`. O span HTTP chama-se `http get /rota`.
- **Beans proxiados:** `@Retryable`/`@CircuitBreaker` geram proxy CGLIB; campos lidos/escritos de fora vão ao proxy, não ao alvo.

## E17 · Spring AI + Ollama, Gradle e native
- **deepseek-r1:14b e tools:** o Ollama lista `tools` entre as capacidades, mas o modelo **não chama a ferramenta** (nem pela API crua `/api/chat`, em português ou inglês, com `think` ligado ou não). `llama3.1:8b` chama na primeira tentativa e o Spring AI executa o método Java. Os demais testes (chat, `entity()`, memória, RAG) funcionam com o deepseek. O raciocínio vem em `<think>…</think>` no texto; os testes o removem antes de comparar.
- **Artefato do advisor de RAG:** o `QuestionAnswerAdvisor` está em `spring-ai-vector-store-advisor` (pacote `...chat.client.advisor.vectorstore`), não em `spring-ai-advisors-vector-store`.
- **Gradle (J14-05):** o `build.gradle.kts` do doc, com `spring-boot-starter-web`, compila e gera `orders.jar` (bootJar) e `orders-plain.jar` no Boot 4.1.2 / Gradle 9.8.2.
- **Native (SC10-10):** `-Pnative native:compile` exige GraalVM 25; a compilação precisa de ~8 GB livres. Sem eles o `native-image` encerra na fase de *inlining* sem mensagem de erro.
