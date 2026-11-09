# java-examples — códigos executáveis da base de estudo Java/Spring

Cada item explicado em [`../java-docs`](../java-docs/00-README.md) ganha um exemplo que compila, roda e tem teste automatizado.
O mapa completo está em [`CATALOGO.md`](CATALOGO.md); o estado do trabalho, em [`PROGRESSO-EXEMPLOS.md`](PROGRESSO-EXEMPLOS.md);
discrepâncias entre documento e código, em [`DIVERGENCIAS.md`](DIVERGENCIAS.md).

## Pré-requisitos
- **JDK 25** (versão de referência dos documentos). Se o `java` padrão da máquina for mais antigo, aponte `JAVA_HOME`:
  `export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64` (o `scripts/run-example.sh` já tenta isso sozinho).
- Maven **não** precisa estar instalado: use o wrapper `./mvnw` (Maven 3.9.12).
- **Docker**: só para exemplos que o exigem (Testcontainers, Kafka/RabbitMQ/Zipkin, k3s). A coluna "Requisitos" do catálogo indica quais.
- Acesso à internet na primeira build (Maven Central).
- **Opcionais** (os testes correspondentes são ignorados ou o script avisa se faltarem):
  - **Gradle** (J14-05): `$GRADLE_HOME`, `~/tools/gradle-*` ou no `PATH`.
  - **GraalVM 25** (SC10-10): `scripts/native-image.sh` (`GRAALVM_HOME` ou `~/tools/graalvm-jdk-25*`); o build nativo leva ~3 min e precisa de ~8 GB de RAM livres.
  - **Ollama** em `localhost:11434` com `deepseek-r1:14b`, `nomic-embed-text` e `llama3.1:8b` (módulo `spring-ai-ollama`; ~16 min em CPU).
- Não rode o reator completo junto com modelo do Ollama carregado ou com o build nativo: a memória acaba.

## Estrutura
| Pasta | Conteúdo |
|---|---|
| `01-java` | Java puro, um pacote por documento (`dev.estudos.java.<topico>`) |
| `02-spring-core` | Spring Framework **sem Boot** (`AnnotationConfigApplicationContext`) |
| `03-spring-boot` | uma aplicação Boot por documento |
| `04-spring-cloud` | um tópico por pasta (inclui `06b-resiliencia-anotacoes` e `08b-tracing-otlp`) |
| `05-outros-projetos-spring` | um projeto por pasta (`spring-ai` com modelo simulado e `spring-ai-ollama` com Ollama real) |
| `06-padroes-de-projeto` | um pacote por padrão (Java puro + versão Spring) |
| `07-projeto-pratico` | `orders-api` (checkpoints 0–10, um teste `PpNN...Test` por checkpoint) e `microservicos/` (checkpoints 11–13: `catalog-service`, `orders-service` e `lab-it` com Kafka, Zipkin e k3s) |

Versões (todas vindas dos BOMs): Java 25 · Spring Boot 4.1.2 · Spring Framework 7.0.10 · Spring Cloud 2025.1.4.

## Compilar e testar
```bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
./mvnw -q verify                      # build + testes de tudo
scripts/run-all.sh                    # verify do reator + gera RELATORIO-EXECUCAO.md (dezenas de minutos)
scripts/native-image.sh               # SC10-10: executável nativo do módulo 03-spring-boot/13 (GraalVM)
./mvnw -q -pl 01-java test            # só um módulo
./mvnw -q -pl 01-java test -Dtest=NomeDaClasseTest
```

## Rodar um exemplo pelo ID
```bash
scripts/run-example.sh J05-03         # lê a linha do ID em CATALOGO.md e executa a classe principal
scripts/run-example.sh --list         # lista os IDs já prontos (✅)
```
Apps Spring Boot também sobem com `./mvnw -pl 03-spring-boot/<modulo> spring-boot:run`.
IDs: `J` java · `SC` spring-core · `SB` spring-boot · `CL` spring-cloud · `OP` outros projetos · `DP` padrões · `PP` projeto prático.

## Convenções
- Classes e métodos em inglês; comentários e saídas em português.
- Todo exemplo tem cabeçalho Javadoc com ID, link do documento, saída real e variações para testar.
- Saídas documentadas são copiadas de execuções reais. Comportamento não determinístico vem explicado no comentário.
