#!/usr/bin/env bash
# SC10-10: compila o app do módulo 03-spring-boot/13 para executável nativo (GraalVM 25) e mede o startup.
# Uso: scripts/native-image.sh   (GRAALVM_HOME opcional; padrão ~/tools/graalvm-jdk-25*)
set -euo pipefail
cd "$(dirname "$0")/.."
GRAAL="${GRAALVM_HOME:-$(ls -d "$HOME"/tools/graalvm-jdk-25* 2>/dev/null | head -1)}"
[ -x "$GRAAL/bin/native-image" ] || { echo "GraalVM 25 não encontrado (defina GRAALVM_HOME)"; exit 1; }
export JAVA_HOME="$GRAAL"
./mvnw -pl 03-spring-boot/13-empacotamento-e-deploy -Pnative -DskipTests -DskipITs native:compile
BIN=03-spring-boot/13-empacotamento-e-deploy/target/sb-13-deploy
ls -lh "$BIN"
"$BIN" --server.port=18777 > /tmp/native-run.out 2>&1 &
PID=$!
for _ in $(seq 40); do curl -sf localhost:18777/ping && break; sleep 0.3; done; echo
grep -o "Started .* in [0-9.]* seconds" /tmp/native-run.out || true
kill $PID
