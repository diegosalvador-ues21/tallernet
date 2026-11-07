#!/bin/bash
# TALLERNET - Compila, empaqueta y prueba SIN Maven (equivalente a "mvn clean package").
# Se usó en el entorno de construcción del AP4, que no tenía acceso a Maven Central.
# Uso: LIB=<carpeta con mysql-connector-j-8.4.0.jar y junit-platform-console-standalone-*.jar> tools/compilar_y_probar.sh
# En el equipo del autor alcanza con: mvn clean package
# Versión 2: las pruebas que usan AssertJ, Mockito, jqwik, ArchUnit o Testcontainers necesitan las
# dependencias que resuelve Maven; sin Maven se compila y ejecuta solo la suite base (sin esas bibliotecas).
set -e
cd "$(dirname "$0")/.."
LIB="$(cd "${LIB:-../lib}" && pwd)"
set -o pipefail
CP_MAIN="$LIB/mysql-connector-j-8.4.0.jar"
JUNIT=$(ls "$LIB"/junit-platform-console-standalone-*.jar | head -1)
rm -rf build && mkdir -p build/classes build/test-classes
javac --release 17 -Xlint:all -encoding UTF-8 -d build/classes $(find src/main/java -name '*.java') 2>&1 | tee build/javac_xlint.log
cp -r src/main/resources/. build/classes/ 2>/dev/null || true
PRUEBAS=$(grep -L -E 'org\.(assertj|mockito|testcontainers)|net\.jqwik|com\.tngtech' $(find src/test/java -name '*.java'))
echo "Sin Maven: $(echo "$PRUEBAS" | wc -l | tr -d ' ') archivos de prueba de la suite base (la suite completa requiere mvn)"
javac --release 17 -encoding UTF-8 -cp "build/classes:$JUNIT:$CP_MAIN" -d build/test-classes $PRUEBAS
cp -r src/test/resources/. build/test-classes/
# Jar ejecutable equivalente al de mvn package (driver incluido)
rm -rf build/fat && mkdir -p build/fat && cp -r build/classes/. build/fat/
(cd build/fat && jar xf "$CP_MAIN" com META-INF/services 2>/dev/null; rm -f META-INF/*.SF META-INF/*.RSA)
printf 'Main-Class: tallernet.Main\n' > build/manifest.txt
jar cfm build/tallernet.jar build/manifest.txt -C build/fat .
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar $JUNIT -cp "build/classes:build/test-classes:$CP_MAIN" --scan-classpath --disable-banner --details=tree "$@"
