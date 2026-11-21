#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    SG_JAVA="$JAVA_HOME/bin/java"
else
    SG_JAVA=java
fi
if [ ! -f dist/sabor-gourmet.jar ]; then
    sh maven/bin/mvn clean package
    mkdir -p dist
    cp target/sabor-gourmet.jar dist/sabor-gourmet.jar
fi
echo 'Sabor Gourmet: http://localhost:8080 (espere el mensaje Started). Ctrl+C para detener.'
exec "$SG_JAVA" -Xms64m -Xmx512m -jar dist/sabor-gourmet.jar "$@"
