#!/usr/bin/env bash
# Executa um exemplo pelo ID do CATALOGO.md.
# Uso: scripts/run-example.sh <ID>      (ex.: scripts/run-example.sh J05-03)
# Uso: scripts/run-example.sh --list    (lista IDs prontos)
#
# Como funciona: procura a linha do ID em CATALOGO.md, lê a coluna "Módulo / pacote" e
# "Classe principal" e executa a classe via exec-maven-plugin (módulos Java/Spring puros) ou
# spring-boot:run (aplicações Boot). Exemplos com flags de JVM têm script próprio (ver README do módulo).
set -euo pipefail
cd "$(dirname "$0")/.."

# O JDK padrão da máquina pode ser mais antigo; os exemplos usam Java 25 (release 25).
if [[ -z "${JAVA_HOME:-}" || ! -x "${JAVA_HOME}/bin/javac" ]] || ! "${JAVA_HOME}/bin/javac" -version 2>&1 | grep -q ' 25'; then
  [[ -d /usr/lib/jvm/java-25-openjdk-amd64 ]] && export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
fi

if [[ "${1:-}" == "--list" ]]; then
  grep -E '^\| [A-Z]+[0-9]+-[0-9]+ ' CATALOGO.md | grep '✅' | cut -d'|' -f2 | tr -d ' '
  exit 0
fi
ID="${1:?Informe o ID do exemplo (ex.: J05-03). Use --list para ver os prontos.}"
shift
ARGS="$*"   # argumentos extras para a aplicação (ex.: --spring.profiles.active=prod)

LINHA=$(grep -E "^\| ${ID} \|" CATALOGO.md || true)
[[ -n "$LINHA" ]] || { echo "ID '$ID' não encontrado em CATALOGO.md" >&2; exit 1; }

# Campos da tabela (separador '|'): 6=Módulo/pacote 7=Classe principal 11=Status
IFS='|' read -r _ _ _ _ _ MOD CLS _ _ _ STATUS _ <<<"$LINHA"
MOD=$(echo "$MOD" | sed 's/^ *//;s/ *$//'); CLS=$(echo "$CLS" | tr -d '` '); STATUS=$(echo "$STATUS" | sed 's/^ *//;s/ *$//')

case "$STATUS" in
  ✅*) ;;
  ❌*) echo "Exemplo $ID marcado como não executável: $STATUS" >&2; exit 2 ;;
  —*)  echo "Exemplo $ID é conceitual (sem código): $STATUS" >&2; exit 2 ;;
  *)   echo "Exemplo $ID ainda não foi implementado ($STATUS)." >&2; exit 3 ;;
esac

MODDIR=$(echo "$MOD" | cut -d' ' -f1)
# Exemplos de teste (J15): a "classe principal" é uma classe *Test; roda-se pelo Surefire.
if [[ "$CLS" == *Test ]]; then
  exec ./mvnw -pl "$MODDIR" test -Dtest="${CLS##*.}"
fi
# Testes de integração (Failsafe, rodam depois do package): classe terminada em IT.
if [[ "$CLS" == *IT ]]; then
  # -am constrói antes os módulos dos quais o IT depende (ex.: os apps do laboratório do 04-spring-cloud)
  exec ./mvnw -pl "$MODDIR" -am verify -Dit.test="${CLS##*.}" -Dsurefire.failIfNoSpecifiedTests=false -Dit.failIfNoSpecifiedTests=false
fi
if [[ "$MOD" == *" / "* ]]; then
  # módulo simples: "01-java / dev.estudos.java.x" + classe principal totalmente qualificada na coluna 7
  exec ./mvnw -q -pl "$MODDIR" compile exec:java -Dexec.mainClass="$CLS" ${ARGS:+-Dexec.args="$ARGS"}
else
  exec ./mvnw -q -pl "$MODDIR" spring-boot:run
fi
