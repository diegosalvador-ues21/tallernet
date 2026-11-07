# Verificación del AP3

| Verificación | Resultado | Evidencia |
|---|---|---|
| Compilación `javac --release 17 -Xlint:all` | 0 advertencias | `docs/evidencias/salidas/javac_xlint.log` (vacío) |
| `mvn clean test` contra MySQL 8.0.46 | 37 pruebas, 0 fallas, 0 omitidas | `docs/evidencias/salidas/mvn_clean_test.log` |
| Estabilidad de la prueba de concurrencia CPI04-3 | 13 ejecuciones consecutivas sin fallas | `docs/evidencias/salidas/cpi04_3_estabilidad.log` |
| Control negativo: CPI04-3 sin `SELECT ... FOR UPDATE` | 3 de 3 ejecuciones fallan (doble reserva) | `docs/evidencias/salidas/control_negativo_sin_bloqueo.log` |
| Sesiones de evidencia (entrada guionada) | 8 sesiones, E1–E15 con el resultado esperado | `docs/evidencias/entradas`, `docs/evidencias/salidas` |
| Revisión independiente del código (2 pasadas) | 14/14 ítems de la consigna CUMPLE; 13 defectos corregidos (DEF-03 a DEF-15) | `docs/defectos.md` |

## Entorno de verificación

El entorno de construcción no tenía acceso a Maven Central. Por eso la verificación usó una copia del `pom.xml`
que solo cambia coordenadas de artefactos por las versiones empaquetadas por Ubuntu 24.04:

| Artefacto | `pom.xml` entregado | Verificación |
|---|---|---|
| Driver JDBC | com.mysql:mysql-connector-j:8.4.0 | org.mariadb.jdbc:mariadb-java-client:2.7.6 (URL con `useSsl=true`) |
| JUnit | 5.10.2 | 5.10.1 |
| maven-compiler / surefire / shade | 3.13.0 / 3.2.5 / 3.5.3 | 3.10.1 / 2.22.3 / 3.4.1 |
| JDK | 17 (`release 17`) | OpenJDK 21 compilando con `--release 17` |
| Servidor | MySQL 8.0 | MySQL 8.0.46 |

El código fuente usa solo `java.sql` y no contiene clases de ningún driver. Paso pendiente en el equipo del autor:
`mvn clean test` con el `pom.xml` entregado (descarga Connector/J 8.4.0) y una ejecución interactiva en la
consola de Windows para confirmar la corrección DEF-04 (acentos).

## Verificación final (18/10/2026)

Se repitió la verificación completa desde una base recreada: `mvn clean test` (37/37), `mvn package` y las 8 sesiones de evidencia, cuyas salidas coinciden línea a línea con las conservadas en `docs/evidencias/salidas`. El informe se revisó con una evaluación independiente contra la consigna y la rúbrica; las correcciones de texto y formato se aplicaron en el `.docx`.
