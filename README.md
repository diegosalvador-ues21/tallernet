# TALLERNET — Proyecto integrador (AP4)

Sistema de gestión multi-sede para **Talleres Ruiz Díaz** (Echeandía y Flores, CABA).
Seminario de Práctica de Informática — Licenciatura en Informática, Universidad Siglo 21.
Autor: Diego Salvador. Repositorio: https://github.com/diegosalvador-ues21/tallernet

Este prototipo implementa el **CU001 Gestionar turno** y sus precondiciones (clientes, vehículos, catálogo
de servicios), con arquitectura MVC y patrón **DAO** (con método plantilla en `DaoJdbc`, Singleton en
`ConexionBD` y método de fábrica en `Servicio.crear`) sobre MySQL 8.0.

Novedades del AP4: actualización de clientes y precios (UPDATE), traducción de los errores de MySQL a
mensajes para la sede (`TraductorSQLException`), grilla de disponibilidad con arreglos (`AgendaDiaria`),
reporte semanal con arreglos y matriz (`ReporteSemanal`), exportación CSV/TXT, importación de clientes
desde CSV y bitácora de operaciones.

## Requisitos

| Componente | Versión |
|---|---|
| JDK | 17 LTS |
| Maven | 3.8 o superior (o Apache NetBeans 21, que lo incluye) |
| MySQL Community Server | 8.0 |
| Driver JDBC | MySQL Connector/J 8.4.0 (lo descarga Maven) |

## Puesta en marcha (≈ 10 minutos)

```bash
# 1. Crear la base, cargar los datos base y crear el usuario de la aplicación
mysql -u root -p < db/01_ddl_creacion.sql
mysql -u root -p < db/02_insercion.sql
mysql -u root -p < db/06_usuario_aplicacion.sql
# (opcional) carga de volumen: 50 clientes, 70 vehículos, 133 turnos
mysql -u root -p < db/03_insercion_masiva.sql

# 2. Configurar la conexión
cp src/main/resources/db.properties.example db.properties
#    editar db.properties si el servidor o la contraseña difieren

# 3. Compilar, probar y empaquetar
mvn clean package            # incluye mvn test

# 4. Ejecutar
java -jar target/tallernet.jar
```

En Windows, si la consola muestra caracteres extraños, ejecutar antes `chcp 65001`.

### Verificación completa (versión 2)

La suite tiene 193 pruebas en nueve niveles (unitarias, dobles de prueba con Mockito, propiedades con jqwik,
arquitectura con ArchUnit, integración con MySQL real y con Testcontainers, sistema en proceso y punta a punta).
El perfil `calidad` agrega cobertura (JaCoCo), mutación (PIT) y análisis estático (SpotBugs + FindSecBugs, PMD, Checkstyle):

```bash
mvn -Pcalidad clean verify          # informes en target/site/jacoco, target/pit-reports, target/*.xml
expect docs/pruebas/e2e/concurrencia_dos_sedes.exp target/tallernet.jar db.properties 11/11/2026
expect docs/pruebas/e2e/fin_de_entrada.exp target/tallernet.jar db.properties
```

Las pruebas con Testcontainers (CPI05, CPI06, CPS03) crean un MySQL 8.0 descartable y se omiten si no hay Docker.
Plan, informe, fichas de defectos (DEF-25 a DEF-36) y evidencias: `docs/pruebas/`.

### Pruebas de integración (CPI04)

Las pruebas contra MySQL recrean la base con los scripts 01 y 02. Requieren el usuario de pruebas:

```bash
mysql -u root -p < db/07_usuario_pruebas.sql
```

Si MySQL no está disponible, esas cinco pruebas se informan como omitidas y el resto corre igual.

### Reproducir las evidencias del informe

Los escenarios del AP2 usan fechas de octubre de 2026. La propiedad `tallernet.ahora` fija la fecha actual:

```bash
java -Dtallernet.ahora=2026-10-01T08:00 -jar target/tallernet.jar < docs/evidencias/entradas/s01_cps05_E1_E4.txt
# o todas las sesiones, en orden (usa el cliente mysql con acceso administrativo):
tools/ejecutar_evidencias.sh target/tallernet.jar db.properties
```

### Sin Maven (entorno sin acceso a Maven Central)

```bash
LIB=<carpeta con mysql-connector-j-8.4.0.jar y junit-platform-console-standalone-*.jar> tools/compilar_y_probar.sh
java -jar build/tallernet.jar
```

### Archivos generados

La propiedad `-Dtallernet.datos=<carpeta>` (por omisión, el directorio actual) indica dónde se crean
`exportaciones/` (agenda CSV, reporte TXT) y `logs/tallernet.log` (bitácora). Para probar la importación
se incluye `ejemplos/clientes_ejemplo.csv`.

### Evidencias

`tools/ejecutar_evidencias.sh` reproduce las 13 sesiones (s01–s13; la s14, con fechas reales, está en `docs/pruebas/evidencias/regresion/`) y `tools/render_capturas_ap4.py`
genera las capturas. s12 detiene MySQL durante la sesión: se ejecuta solo si se definen
`DETENER_MYSQL` e `INICIAR_MYSQL`.

## Estructura

```
db/                      scripts SQL 01–08 (DDL, datos, consultas, borrado, usuarios; 08: usuarios para MySQL en Docker)
src/main/java/tallernet/
  Main.java              arma las capas y abre la vista
  vista/                 VistaConsola, LectorEntrada
  controlador/           TurnoControlador, ValidadorTurno, ClienteControlador, CatalogoControlador,
                         SesionControlador, ArchivoControlador, SistemaControlador
  modelo/                Persona, Cliente, Empleado, Sede, Vehiculo, Servicio (+2 subclases), Turno, enums,
                         AgendaDiaria (Turno[]), ReporteSemanal (int[], BigDecimal[], int[][])
  persistencia/          Dao<T,K>, repositorios, DaoJdbc (método plantilla), TraductorSQLException, DAOs
  archivos/              Exportador<T>, ExportadorCsv<T>, AgendaCsv, ReporteSemanalTxt,
                         ImportadorClientesCsv, RegistroOperaciones, Bitacora
  conexion/              ConexionBD (Singleton)
  excepciones/           TallerNetException y 8 subclases
  util/                  Ordenamiento, Busqueda, Formatos
src/test/java/           193 pruebas: CPC01..CPC13, CPF02, CPF03, CPI04..CPI07, CPV01, CPP01, CPR01, CPS02, CPS03
calidad/                 configuración de Checkstyle del perfil calidad
docs/                    decisiones, defectos, diagramas, evidencias, video, pruebas (versión 2)
tools/                   compilación sin Maven, generador del script 03, evidencias y capturas
ejemplos/                CSV de ejemplo para la importación
```

## Menú

```
1. Turnos                       nuevo (con sugerencia de horario en S5) · reprogramar · cancelar · deshacer ·
                                agenda · buscar · registrar llegada · atender · ver cola · disponibilidad
2. Clientes y vehículos         alta de cliente · alta de vehículo · listar · buscar por patente · historial ·
                                modificar datos de contacto
3. Sedes y catálogo             sedes · servicios por precio · por duración · próximo service · actualizar precio
4. Reporte de carga por sede
5. Reportes semanales y archivos reporte semanal · exportar agenda CSV · exportar reporte TXT · importar clientes
6. Sistema                      probar conexión · últimas operaciones (bitácora)
0. Salir
```

## Fuera de alcance de esta iteración

CU002 (presupuestos), CU003 (asistencia), repuestos, autenticación con contraseña (se elige el empleado
en sesión), interfaz gráfica JavaFX, facturación, portal de clientes y modo sin conexión (RNF-TN07).
