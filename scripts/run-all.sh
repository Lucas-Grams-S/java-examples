#!/usr/bin/env bash
# Roda o reator completo em `verify` (testes + ITs) e gera RELATORIO-EXECUCAO.md.
# Uso: scripts/run-all.sh [argumentos extras do Maven, ex.: -pl 01-java]
# Requisitos: JDK 25, Docker (Testcontainers/k3s), git, portas livres. Tempo: dezenas de minutos.
set -uo pipefail
cd "$(dirname "$0")/.."

if [[ -z "${JAVA_HOME:-}" ]] || ! "${JAVA_HOME}/bin/javac" -version 2>&1 | grep -q ' 25'; then
  [[ -d /usr/lib/jvm/java-25-openjdk-amd64 ]] && export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
fi

LOG=target/run-all.log
mkdir -p target
INICIO=$(date '+%F %T')
./mvnw verify -fae "$@" 2>&1 | tee "$LOG"
RC=${PIPESTATUS[0]}
FIM=$(date '+%F %T')

# Soma dos relatórios do Surefire/Failsafe
read -r T F E S < <(find . -path '*/target/*-reports/TEST-*.xml' -print0 | xargs -0 awk '
  /<testsuite /{ match($0,/ tests="[0-9]+"/); t+=substr($0,RSTART+8,RLENGTH-9);
                 match($0,/ failures="[0-9]+"/); f+=substr($0,RSTART+11,RLENGTH-12);
                 match($0,/ errors="[0-9]+"/); e+=substr($0,RSTART+9,RLENGTH-10);
                 match($0,/ skipped="[0-9]+"/); s+=substr($0,RSTART+10,RLENGTH-11) }
  END{print t+0,f+0,e+0,s+0}')

{
  echo "# Relatório de execução"
  echo
  echo "- Início: $INICIO · Fim: $FIM"
  echo "- JDK: $("$JAVA_HOME/bin/java" -version 2>&1 | head -1)"
  echo "- Resultado do Maven: $([[ $RC -eq 0 ]] && echo 'BUILD SUCCESS' || echo "FALHOU (código $RC)")"
  echo "- Testes: $T · falhas: $F · erros: $E · ignorados: $S"
  echo
  echo "## Exemplos no catálogo"
  echo "| Status | Quantidade |"; echo "|---|---|"
  echo "| ✅ pronto | $(grep -E '^\| [A-Z]+[0-9]+-[0-9]+ ' CATALOGO.md | grep -c '| ✅')|"
  echo "| ❌ não executável | $(grep -E '^\| [A-Z]+[0-9]+-[0-9]+ ' CATALOGO.md | grep -c '| ❌')|"
  echo "| — conceitual | $(grep -E '^\| [A-Z]+[0-9]+-[0-9]+ ' CATALOGO.md | grep -c '| — ')|"
  echo
  echo "## Módulos (resumo do reator)"
  echo '```'
  sed -n '/Reactor Summary/,/BUILD/p' "$LOG" | sed 's/\x1b\[[0-9;]*m//g'
  echo '```'
} > RELATORIO-EXECUCAO.md
echo "Relatório: RELATORIO-EXECUCAO.md"
exit $RC
