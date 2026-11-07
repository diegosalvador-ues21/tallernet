# Informe de pruebas — TALLERNET (campaña sobre el prototipo TP4)

Autor: Diego Salvador · Fecha: 07/11/2026 · Plan: `PLAN_PRUEBAS.md` · Evidencias: `evidencias/`

## 1. Resumen
- **Caso reportado: «al ingresar un auto, el registro no queda guardado».** Se reprodujo y resultó ser **DEF-26**: el cliente 007 no existía, porque la base se había reiniciado con los clientes 1 a 4, y el rechazo llegaba recién después de pedir los seis datos del vehículo. A eso se sumaba **H-28**: Workbench consultaba otro servidor, el MySQL nativo en el puerto 3306. Con un cliente válido, el alta **sí persiste**. El oráculo SQL lo confirma: `LBS854` con 195.555 km (`cp_2_2_oraculo.log`).
- Se registraron **11 defectos (DEF-26 a DEF-36) y un hallazgo de entorno (H-28)**. Los 11 están corregidos y verificados con una prueba de regresión cada uno.
- La suite pasó de **120 a 193 pruebas**, todas en verde. La cobertura de líneas pasó del **54,4 % al 90,0 %** y el puntaje de mutación del **62 % al 80 %**. PMD, CPD y Checkstyle no tienen observaciones. Los 44 avisos de SpotBugs están clasificados.
- La regresión de las sesiones s01–s14 **no perdió ninguna línea**. Todas las diferencias son líneas informativas nuevas, introducidas a propósito por DEF-26, DEF-27 y DEF-35.

## 2. Pruebas por nivel
| Nivel | Clases (código de caso) | Pruebas |
|---|---|---|
| Unitario: modelo y utilidades | ModeloTest, TurnoTest, AgendaDiariaTest, ReporteSemanalTest, LimitesTest (CPF03), RobustezTest (CPS02), AlgoritmosTest, ArchivosTest | 67 |
| Unitario: controladores | ValidadorTurnoTest (CPF02/CPC03), TurnoControladorTest (CPC12), CatalogoYSesionControladorTest (CPC13), ClienteControladorTest (CPC11) | 64 |
| Unitario: vista (Mockito) | VistaAltasTest (CPV01) | 6 |
| Traductor de errores | TraductorSQLExceptionTest, TraductorCodigosTest (CPI07) | 15 |
| Propiedades (jqwik) | PropiedadesTest (CPP01), 1.000 intentos cada una | 5 |
| Arquitectura (ArchUnit) | ArquitecturaTest (CPR01) | 7 |
| Integración con la base de desarrollo | TurnoDAOIntegracionTest, PersistenciaTp4IntegracionTest | 13 |
| Integración aislada (Testcontainers) | AltasContenedorTest (CPI05), ConsultasContenedorTest (CPI06) | 15 |
| Sistema en proceso (Testcontainers) | RecorridoContenedorTest (CPS03): 30 opciones del menú | 1 |
| **Total Maven** | 27 clases | **193** |
| Punta a punta (`expect`) | CPE2E-01 dos sedes simultáneas · CPE2E-02 Ctrl-D · CPE2E-03 servidor detenido durante un alta | 3 aprobadas |
| Regresión guionada | s01–s14 | 14 sesiones; 0 líneas perdidas |

## 3. Métricas: línea base y final
| Métrica | Línea base (`calidad_iter1.log`) | Final (`calidad_final.log`) | Umbral |
|---|---|---|---|
| Pruebas | 120 | **193** (0 fallas) | 100 % en verde |
| Cobertura de líneas · modelo | 90,9 % | **95,0 %** | ≥ 85 % ✔ |
| · controlador | 18,9 % | **95,0 %** | ≥ 85 % ✔ |
| · persistencia | 64,2 % | **89,3 %** | ≥ 85 % ✔ |
| · archivos | 88,6 % | **92,8 %** | ≥ 85 % ✔ |
| · vista | 32,3 % | **92,3 %** | ≥ 70 % ✔ |
| · conexión | 36,4 % | 61,0 % | (sin umbral) |
| Total: líneas / ramas | 54,4 % / 51,5 % | **90,0 % / 83,4 %** | — |
| Mutación PIT (451 mutantes) | 62 % (278 eliminados) | **80 %** (360 eliminados) | ≥ 75 % ✔ |
| SpotBugs + FindSecBugs | 50 | 44, todos clasificados (§5) | Clasificados ✔ |
| PMD / CPD | 6 / 0 | **0 / 0** | ✔ |
| Checkstyle | 0 | 0 | ✔ |
| ArchUnit | 2 reglas violadas (DEF-31) | 7 de 7 en verde | ✔ |

El paquete raíz (`Main`, 44 líneas) no tiene cobertura medida porque solo arma los objetos. Lo ejercitan las sesiones s01–s14 y los E2E, que corren el jar.

## 4. Defectos
| ID | Severidad | Detectado por | Descripción breve | Prueba de regresión |
|---|---|---|---|---|
| DEF-26 | Media | Reporte del autor + CPV01 | Cliente inexistente detectado después de pedir todos los datos del vehículo (ídem sede, patente, servicio, turno) | VistaAltasTest, cp_2_2 + oráculo SQL |
| DEF-27 | Media | Análisis de DEF-26 | El rechazo no decía que no se había guardado nada | VistaAltasTest, E2E 01 y 03 |
| H-28 | Media (entorno) | `lsof` | Workbench conectado al 3306 (otro servidor) | Documentado en `PUESTA_EN_MARCHA.md` |
| DEF-28 | Baja | Exploración | «La marca es obligatorio» | LimitesTest |
| DEF-29 | Media | Exploración + oráculo | Precio actualizable a $ 0,00 | LimitesTest, CatalogoYSesionControladorTest |
| DEF-30 | Baja | Exploración + E2E 02 | Ctrl-D → «[ERROR INTERNO]» | CPE2E-02 (rojo → verde) |
| DEF-31 | Baja | ArchUnit | Vista → conexión; `java.sql` en `Main` | ArquitecturaTest |
| DEF-32 | Media | SpotBugs | Mayúsculas dependientes del idioma de la JVM (turco: ABİ123) | RobustezTest |
| DEF-33 | Baja | PMD | Clave generada leída sin comprobar `next()` | AltasContenedorTest, ConsultasContenedorTest |
| DEF-34 | Media | Revisión independiente | «No se guardó ningún dato» cuando el cliente nuevo sí quedó registrado (introducido por DEF-26) | VistaAltasTest |
| DEF-35 | Baja | Revisión independiente | Reprogramar/cancelar un turno no confirmado pedía datos antes de rechazarlo | VistaAltasTest |
| DEF-36 | Media | Revisión independiente | CHECK de la base `precio_base >= 0`, desalineado con DEF-29 | ConsultasContenedorTest |

Las fichas completas están en `defectos/` y las filas en `docs/defectos.md`. El parche con todas las correcciones está en `evidencias/diff_correcciones.patch`.

## 5. Clasificación del análisis estático (44 avisos finales de SpotBugs)
| Aviso | Cant. | Clasificación | Justificación |
|---|---|---|---|
| EI_EXPOSE_REP / REP2 | 23 | Aceptado (diseño) | Grafo de objetos del dominio (Turno → Sede, Vehículo, Servicio) compartido a propósito dentro de una aplicación de un solo proceso; las colecciones expuestas son copias (`List.copyOf`) |
| CT_CONSTRUCTOR_THROW | 11 | Aceptado | Los constructores validan y lanzan `DatoInvalidoException` (objetos siempre válidos); el «ataque del finalizador» no aplica a una aplicación de escritorio sin código no confiable |
| SQL_INJECTION_JDBC | 4 | Falso positivo | El SQL se arma con constantes (`SELECT + " ORDER BY…"`) y los datos van por `PreparedStatement`; CPI05 demuestra que `Robert'); DROP TABLE cliente;--` se guarda literal |
| REDOS | 1 | Falso positivo comprobado | `[\w-]+` no incluye el punto, así que la repetición no es ambigua; RobustezTest rechaza un correo malicioso de 119 caracteres en menos de 1 s |
| IMPROPER_UNICODE | 2 | Falso positivo | Tras DEF-32 la conversión usa `Locale.ROOT` y se compara con texto ASCII fijo |
| RV_RETURN_VALUE_IGNORED (×2) | 2 | Falso positivo | `offer`/`poll` sobre `ArrayDeque` sin límite: el resultado es siempre conocido |
| NM_CLASS_NOT_EXCEPTION | 1 | Aceptado | `TraductorSQLException` es un traductor, no una excepción; renombrarlo afectaría a todos los DAO sin beneficio funcional |

Corregidos a partir del análisis: DEF-32 (5 `DM_CONVERT_CASE`), DEF-33 (4 `CheckResultSet` de PMD), `OS_OPEN_STREAM` en `ConexionBD` (lector en try-with-resources), `EmptyCatchBlock` y `CheckResultSet` del diagnóstico TLS.

## 6. Regresión de sesiones guionadas
`evidencias/regresion_diferencias.txt` compara las sesiones s01–s13 contra la referencia del AP4 y la s14 contra la corrida previa en la Mac. Para comparar se normalizan la dirección del servidor (3306 → 3307), la versión del motor, las rutas, las marcas de hora y los milisegundos. Resultado: s05, s07, s08, s11, s12 y s13 salen idénticas. Las demás solo **agregan** líneas: «Vehículo: …», «Titular: …», «Servicio: … precio actual», la ficha del turno antes de reprogramar y «Resultado: … no se guardó ningún dato.». Ninguna línea de resultado de negocio cambió ni desapareció. Los números de turno, los estados y los rechazos coinciden con la referencia.

## 7. Revisión independiente
Un revisor que no escribió el código analizó `diff_correcciones.patch` e informó 5 observaciones:
- **Defectos confirmados y corregidos:** DEF-34, DEF-35 y DEF-36.
- **Riesgos residuales:**
  - Una excepción no prevista **después** de una escritura confirmada mostraría «[ERROR INTERNO]» y «no se guardó». La bitácora no puede causarla, porque `Bitacora.registrar` atrapa sus propios errores de E/S.
  - Si el driver no devolviera la clave generada, el INSERT con autocommit ya estaría confirmado. Con Connector/J y `RETURN_GENERATED_KEYS` no ocurre.
- **Propuesta para CU001, fuera del alcance de la corrección:** que «Nuevo turno» ofrezca registrar el vehículo cuando la patente no existe.

## 8. Riesgos residuales
1. Los riesgos residuales de la revisión independiente (§7).
2. La cola de recepción y la pila de cancelaciones viven en la memoria de cada puesto; ya estaban documentadas en `docs/defectos.md`.
3. La cobertura del paquete `conexion` es del 61 %. Los caminos sin cubrir son errores de TLS y de credenciales, que s13 y CPI04-13 ejercitan fuera de JaCoCo.
4. No se ejecutó OWASP Dependency-Check (D3), porque necesita la confirmación del autor y una clave de API de NVD.
5. Los 42 mutantes que sobreviven se concentran en `Ordenamiento` (7), `ReporteSemanal` (6) y `Vehiculo` (5). Son sobre todo mutaciones de límites `<` → `<=` en comparadores con resultado equivalente.

## 9. Cómo reproducir
```bash
cd "Tp4/Prototipo/tallernet"
export JAVA_HOME=/opt/homebrew/opt/openjdk@17 PATH=$JAVA_HOME/bin:$PATH
export DOCKER_CONFIG=/tmp/tallernet-docker      # evita el credential helper de Docker Desktop
mvn -Pcalidad clean verify                       # 193 pruebas + JaCoCo + PIT + SpotBugs + PMD + Checkstyle (~2,5 min)
zsh docs/pruebas/regresion_mac.sh                # sesiones s01–s14 (deja la base en la carga base)
J=$PWD/target/tallernet.jar; C=$PWD/db.properties   # en rutas con espacios usar un enlace, p. ej. /tmp/tn_repo
expect docs/pruebas/e2e/concurrencia_dos_sedes.exp $J $C 11/11/2026
expect docs/pruebas/e2e/fin_de_entrada.exp $J $C
expect docs/pruebas/e2e/servidor_detenido_en_alta.exp $J $C "../mac_mysql.sh detener" "../mac_mysql.sh iniciar"
```
Reportes: `target/site/jacoco/index.html`, `target/pit-reports/index.html`, `target/spotbugsXml.xml`, `target/pmd.xml`.
