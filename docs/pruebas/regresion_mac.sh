#!/bin/zsh
# Regresión de las sesiones guionadas s01-s14 en la Mac (campaña de pruebas, iteración final).
# Uso: docs/pruebas/regresion_mac.sh   (desde la carpeta tallernet; requiere el contenedor tallernet-mysql)
set -e
REPO="${0:A:h:h:h}"; PROTO="${REPO:h}"
ln -sfn "$PROTO" /tmp/tn_proto
export JAVA_HOME=/opt/homebrew/opt/openjdk@17 PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH
SAL="$PROTO/evidencias_mac/salidas_testing"; mkdir -p "$SAL"
cd "$REPO"
mvn -B -q -DskipTests package
SALIDAS="$SAL" MYSQL='/tmp/tn_proto/mac_mysql.sh cliente' \
  DETENER_MYSQL='/tmp/tn_proto/mac_mysql.sh detener' INICIAR_MYSQL='/tmp/tn_proto/mac_mysql.sh iniciar' \
  tools/ejecutar_evidencias.sh target/tallernet.jar db.properties
# s14: fechas reales (sin reloj fijo), sobre la carga base.
for f in 01_ddl_creacion.sql 02_insercion.sql; do /tmp/tn_proto/mac_mysql.sh cliente < db/$f > /dev/null; done
rm -rf "$SAL/archivos_s14"
java -Dfile.encoding=UTF-8 -Duser.timezone=America/Argentina/Buenos_Aires -Dtallernet.config=db.properties \
     -Dtallernet.datos="$SAL/archivos_s14" -jar target/tallernet.jar \
     < "$PROTO/evidencias_mac/s14_fechas_reales.txt" > "$SAL/s14_fechas_reales.log" 2>&1 || true
for f in 01_ddl_creacion.sql 02_insercion.sql; do /tmp/tn_proto/mac_mysql.sh cliente < db/$f > /dev/null; done
echo "REGRESION OK -> $SAL"
