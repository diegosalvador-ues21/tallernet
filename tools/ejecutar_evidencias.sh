#!/bin/bash
# TALLERNET - Reproduce las sesiones de evidencia del AP3 con entrada guionada.
# Uso: tools/ejecutar_evidencias.sh [ruta/tallernet.jar] [ruta/db.properties]
# Requiere el cliente mysql con acceso administrativo (por socket o ~/.my.cnf).
# Variables opcionales: MYSQL (comando del cliente), DETENER_MYSQL, INICIAR_MYSQL (s12 y s13),
# SALIDAS (carpeta de salida; por omisión docs/evidencias/salidas). Admiten rutas con espacios
# si se citan dentro de la variable, por ejemplo: MYSQL='"/ruta con espacios/cliente.sh" cliente'.
set -e
RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
JAR="${1:-$RAIZ/target/tallernet.jar}"
JAR="$(cd "$(dirname "$JAR")" && pwd)/$(basename "$JAR")"
CONFIG_IN="${2:-$RAIZ/db.properties}"
CONFIG_IN="$(cd "$(dirname "$CONFIG_IN")" && pwd)/$(basename "$CONFIG_IN")"
CONFIG="$CONFIG_IN"
ENT="$RAIZ/docs/evidencias/entradas"
SAL="${SALIDAS:-$RAIZ/docs/evidencias/salidas}"
mkdir -p "$SAL"
cd "$RAIZ"
# Función y no variable: así las rutas con espacios llegan enteras a java (DEF-25).
java_sesion() {
  java -Dfile.encoding=UTF-8 -Duser.timezone=America/Argentina/Buenos_Aires -Dtallernet.ahora=2026-10-01T08:00 \
       -Dtallernet.config="$CONFIG" -Dtallernet.datos=docs/evidencias/archivos -jar "$JAR"
}

MYSQL="${MYSQL:-mysql}"
cargar() { for f in "$@"; do eval "$MYSQL --default-character-set=utf8mb4" < "$RAIZ/db/$f" > /dev/null; done; }
correr() { echo "== $1" ; java_sesion < "$ENT/$1.txt" > "$SAL/$1.log" 2>&1; }

# Sesiones sobre la carga base (scripts 01 + 02), en orden: comparten el estado de la base.
cargar 01_ddl_creacion.sql 02_insercion.sql
for s in s01_cps05_E1_E4 s02_cps05_E5 s03_cps05_E6_E7 s04_robustez s05_ordenamiento_busqueda \
         s06_cola_pila_reprogramacion s07_catalogo_reportes; do correr $s; done

# Sesiones nuevas del AP4 sobre la carga base: UPDATE, excepciones, arreglos y archivos.
rm -rf "$RAIZ/docs/evidencias/archivos"
cargar 01_ddl_creacion.sql 02_insercion.sql
correr s09_actualizacion_excepciones
cargar 01_ddl_creacion.sql 02_insercion.sql
correr s10_arreglos_archivos

# Sesiones con volumen (scripts 01 + 02 + 03).
cargar 01_ddl_creacion.sql 02_insercion.sql 03_insercion_masiva.sql
correr s08_volumen
correr s11_volumen_arreglos

# s12: el servidor se detiene en medio de la sesión (requiere permiso para detener MySQL).
if [ -n "$DETENER_MYSQL" ]; then
  cargar 01_ddl_creacion.sql 02_insercion.sql
  echo "== s12_servidor_detenido"
  ( { cat "$ENT/s12a.txt"; sleep 6; cat "$ENT/s12b.txt"; } | java_sesion > "$SAL/s12_servidor_detenido.log" 2>&1 ) &
  sleep 3; eval "$DETENER_MYSQL"; wait; eval "$INICIAR_MYSQL"; sleep 3
  echo "== s13_credenciales_invalidas"
  sed 's/^db.clave=.*/db.clave=clave-incorrecta/' "$CONFIG" > /tmp/db-invalida.properties
  java -Dfile.encoding=UTF-8 -Dtallernet.config=/tmp/db-invalida.properties -jar "$JAR" < /dev/null > "$SAL/s13_credenciales_invalidas.log" 2>&1 || true
fi

# Se deja la base en el estado de la carga base.
cargar 01_ddl_creacion.sql 02_insercion.sql
echo "Salidas en $SAL"
