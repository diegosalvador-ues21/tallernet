# Plan de pruebas — TALLERNET (campaña de testing del prototipo TP4)

Autor: Diego Salvador · Fecha: 07/11/2026 · Objeto: prototipo `Tp4/Prototipo/tallernet` (copia de trabajo; la entrega `Tp4/Entrega` no se modificó)

## 1. Objetivo
Encontrar, documentar y corregir defectos del prototipo, con prioridad en la persistencia de las altas («al ingresar un auto, el registro no queda guardado»). Cada defecto debe quedar con una prueba automática que lo reproduzca (rojo) y que pase después de corregirlo (verde).

## 2. Alcance
| Incluido | Excluido |
|---|---|
| Modelo, controladores, DAO, conexión, archivos, vista de consola | Interfaz gráfica (no existe en el TP4) |
| MySQL 8.0 real (Docker, 127.0.0.1:3307) y descartable (Testcontainers) | MySQL nativo de la Mac (3306), que no se toca |
| Concurrencia entre dos sedes, caída del servidor, fin de entrada | Análisis de dependencias con OWASP Dependency-Check (D3: requiere confirmación y clave de API de NVD) |

## 3. Estrategia por nivel y herramienta
| Nivel | Herramienta | Qué se prueba | Criterio de aceptación |
|---|---|---|---|
| Unitario | JUnit 5 + AssertJ | Reglas del modelo, valores límite (CPF03), algoritmos | 100 % en verde |
| Unitario con dobles | Mockito | Controladores (CPC11–CPC13) y vista (CPV01) aislados de la base | 100 % en verde |
| Propiedades | jqwik | Simetría de la superposición, grilla en rango, ordenamientos, búsqueda binaria (CPP01) | 1.000 intentos por propiedad sin contraejemplo |
| Arquitectura | ArchUnit | Capas MVC/DAO, SQL solo en persistencia, sin ciclos, jerarquía de excepciones (CPR01) | 7 reglas en verde |
| Integración | JUnit + MySQL de desarrollo (3307) | DAO y transacciones (CPI01–CPI04) | 100 % en verde |
| Integración aislada | Testcontainers `mysql:8.0` | Altas con oráculo SQL, consultas, inyección, UTF-8, errores (CPI05–CPI07) | 100 % en verde |
| Sistema en proceso | Testcontainers + vista real | Recorrido de las 30 opciones del menú con oráculo SQL y de archivos (CPS03) | Pantalla = base = archivos |
| Punta a punta | `expect` + jar real | Dos sedes simultáneas, Ctrl-D, servidor detenido en un alta (CPE2E-01..03) | APROBADO |
| Regresión | `tools/ejecutar_evidencias.sh` + `regresion_mac.sh` | Sesiones s01–s14 del AP2–AP4 | Sin líneas quitadas; toda línea nueva justificada |
| Cobertura | JaCoCo | Líneas por paquete | ≥ 85 % en modelo, controlador, persistencia, archivos; ≥ 70 % en vista |
| Mutación | PIT | modelo, util, controlador | Puntaje ≥ 75 % |
| Estático | SpotBugs + FindSecBugs, PMD + CPD, Checkstyle | Defectos probables, seguridad, duplicación, estilo | Todo hallazgo corregido o justificado |

## 4. Entorno
MacBook (macOS, Apple Silicon) · OpenJDK 17 (Homebrew) · Maven 3 · Docker Desktop 29 · MySQL 8.0.46 en el contenedor `tallernet-mysql` · `expect` 5.45. Reloj fijo `-Dtallernet.ahora=2026-10-01T08:00` en las sesiones guionadas, salvo s14 (fechas reales).

## 5. Proceso por defecto
1. Reproducir con una prueba automática (rojo) y guardar la evidencia.
2. Analizar la causa raíz y redactar la ficha en `defectos/DEF-NN.md`.
3. Corregir con el cambio mínimo y volver a correr la prueba (verde).
4. Correr la suite completa y la regresión de sesiones.
5. Registrar la fila en `docs/defectos.md`.

## 6. Criterio de finalización
Suite completa en verde, umbrales de cobertura y mutación cumplidos, ningún hallazgo estático sin clasificar, regresión sin diferencias inesperadas, revisión independiente atendida y base de datos devuelta a la carga base (scripts 01 + 02).
