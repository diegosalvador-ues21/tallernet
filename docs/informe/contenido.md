PORTADA

[[INDICE]]

# Presentación

Este documento integra en una sola entrega el proyecto TALLERNET, desarrollado a lo largo de la asignatura Seminario de Práctica de Informática para Talleres Ruiz Díaz, una red de dos talleres mecánicos de la Ciudad Autónoma de Buenos Aires. Reúne, corregidas y condensadas, las entregas AP1 (modelo de negocio y requerimientos), AP2 (análisis, diseño, base de datos y comunicaciones) y AP3 (implementación orientada a objetos), y agrega el incremento del AP4: la selección y justificación del patrón de diseño, la persistencia completa en MySQL con actualización de registros, el tratamiento de las excepciones de la base de datos, el uso complementario de arreglos y de la clase ArrayList, y el manejo de archivos.

Esta es la **versión 3** del documento. La versión 2 incorporó la campaña de verificación realizada sobre la versión 1 del prototipo: métodos, evaluaciones y correcciones (sección 6.6). La versión 3 aplica las correcciones surgidas de una revisión de la versión 2 contra la consigna y la rúbrica del TP4, listadas con la entrega «AP4 v2» en la Tabla 3. El historial de cambios figura en la Tabla 2.

El resultado es un prototipo operacional, entendido como «la creación de un modelo operacional que incluye solo algunas características del sistema final» (Kendall y Kendall, 2011, como se citó en Universidad Siglo 21, 2026b): no cubre todo el sistema, pero lo que cubre funciona contra la base de datos real y se integra a la versión definitiva. El prototipo implementa el caso de uso crítico CU001 Gestionar turno con sus precondiciones (clientes, vehículos, catálogo y sedes), los reportes para el titular y las herramientas de soporte de cada sede.

TABLA: Datos de la entrega
WIDTHS: 30,70
| Elemento | Detalle |
| Repositorio del proyecto completo | https://github.com/diegosalvador-ues21/tallernet (código, scripts SQL, pruebas, evidencias y diagramas editables) |
| Video de presentación (≈ 3 min) | [enlace al video] |
| Lenguaje y plataforma | Java SE, JDK 17 LTS; proyecto Maven compatible con Apache NetBeans 21 |
| Base de datos | MySQL Community Server 8.0, base tallernet (InnoDB, utf8mb4) |
| Prototipo | 59 clases Java en 8 paquetes; 193 pruebas automatizadas en 27 clases; 14 sesiones de ejecución reproducibles y 3 pruebas de punta a punta |
| Versión del documento | 3, del 7 de noviembre de 2026 (reemplaza a la versión 2) |

TABLA: Control de versiones del documento
WIDTHS: 9,13,78
| Versión | Fecha | Cambios |
| 1 | 07/11/2026 | Entrega integradora del AP4: AP1 a AP3 corregidos e incremento del AP4 (patrón de diseño, persistencia, excepciones, arreglos y archivos). 64 pruebas automatizadas y 13 sesiones de evidencia. |
| 2 | 07/11/2026 | Campaña de verificación sobre la versión 1: 11 defectos corregidos con su prueba de regresión (DEF-26 a DEF-36), DEF-25 en el script de evidencias y un hallazgo de entorno (H-28); 129 pruebas automatizadas nuevas (193 en total); medición de cobertura y de mutación; análisis estático; regresión de 14 sesiones y 3 pruebas de punta a punta; revisión independiente. Secciones nuevas: 6.6 a 6.9. Secciones actualizadas: Presentación, Correcciones, 5.7, 6.1 a 6.5, Conclusiones, Referencias y Declaración. |
| 3 | 07/11/2026 | Correcciones de una revisión contra la consigna y la rúbrica: enlace al repositorio publicado; código explicado de la conexión, de una consulta y de 5.4; citas APA completas; Parte VI sintetizada (6.6 a 6.9 en una sección); figuras legibles y diagrama de paquetes corregido; PDF con Calibri; guion único del video; prototipo 0.4.2. |

El documento se organiza en seis partes. La Parte I presenta el modelo de negocio y la definición del proyecto; la Parte II, el análisis y el diseño según el proceso unificado de desarrollo (PUD); la Parte III, la base de datos y la arquitectura de comunicaciones; la Parte IV, la implementación orientada a objetos; la Parte V, el proyecto integrador del AP4, y la Parte VI, las pruebas, la trazabilidad de punta a punta y la campaña de verificación de la versión 2. Antes se listan las correcciones realizadas sobre las entregas anteriores.

# Correcciones respecto de las entregas anteriores

La entrega es incremental: cada corrección se aplicó sobre el contenido integrado y se indica dónde puede verificarse. Las correcciones surgen de una revisión propia de los tres informes y del código entregado; no se recibieron devoluciones escritas con observaciones adicionales a las que aquí se listan. La versión 2 agrega, con la entrega «AP4 v1», las correcciones que surgieron de la campaña de verificación sobre la propia versión 1 (sección 6.6). La versión 3 suma, con la entrega «AP4 v2», las observaciones de una revisión de la versión 2 contra la consigna y la rúbrica del TP4.

TABLA: Correcciones aplicadas
WIDTHS: 10,38,38,14
| Entrega | Observación | Corrección | Sección |
| AP1 | El sistema aparecía como «TALLER-NET» en la introducción y como «TALLERPLUS» en el borrador. | Nombre unificado como TALLERNET en todo el documento, el código y la base de datos. | Todas |
| AP1 | «que de soporte a estas necesidades». | «que dé soporte», con tilde diacrítica. | 1.1 |
| AP1 | Las figuras no seguían el orden de aparición (la Figura 4 precedía a la Figura 1). | Numeración única y continua de figuras y tablas en todo el documento. | Todas |
| AP1 | El actor «Empleado» se usaba en CU003 sin estar definido. | Se lo define como generalización de recepcionista, mecánico y encargado de repuestos. | 1.6 |
| AP1 | Requerimientos sin las ampliaciones posteriores. | Tabla única vigente: 22 RF y RNF-TN01 a RNF-TN09, con las ampliaciones de RF-TN03 y RF-TN17 señaladas. | 1.5 |
| AP2 | Consultas nombradas de forma inconsistente («Qquery1», «Qquery7», «Query2», «Q4»). | Nomenclatura única Q1 a Q7. | 3.3 |
| AP2 | «impide crear turnos superpuestos en una misma.» | «en una misma sede». | 3.3 |
| AP2 | «Los diagramas de este informe generaron»; «via Internet». | «se generaron»; «vía Internet». | 3.4 y Declaración |
| AP2 | Mezcla de primera persona («Preparé», «Probé», «Consideré», «Elegí») y registro impersonal. | Registro impersonal en todo el documento. | Todas |
| AP2 | Faltaba la Tabla 7 (la tabla de requerimientos derivados de CPF02 no tenía número) y el índice tenía páginas desalineadas. | Todas las tablas numeradas; índice regenerado a partir del documento final. | Índice |
| AP2 | CPS05-E5 informa «Cliente N.° 5» para Paula Herrera; con la carga masiva el número 5 corresponde a otra clienta. | Se aclara que CPS05 se ejecuta sobre la carga base (scripts 01 y 02). | 6.3 |
| AP3 | La numeración de tablas saltaba de la Tabla 1 a la Tabla 3. | Numeración continua. | Todas |
| AP3 | La declaración de herramientas mencionaba DrawIO, mientras que los diagramas del AP3 se generaron con Graphviz; «correccion» sin tilde. | Declaración corregida y redactada según lo efectivamente utilizado. | Declaración |
| AP3 | No incluía el enlace al repositorio GitHub que exige la consigna ni una sección de correcciones. | Enlace en la presentación y esta sección. | Presentación |
| AP3 | Las carpetas de entrega contenían copias incompletas del proyecto (sin pom.xml ni README). | Una única fuente: el repositorio del AP4, con todos los archivos. | 5.7 |
| AP3 | Limitación: el inicio con el servidor caído o con credenciales inválidas se informaba igual que un error de sentencia. | Se distingue «[SIN CONEXIÓN]» de «[ERROR DE BASE DE DATOS]» (DEF-18). | 5.3 |
| AP4 v1 | Con un cliente inexistente, el alta de vehículo pedía todos los datos antes de rechazarlo y no aclaraba que no se había guardado nada; en la prueba local se interpretó como «el auto no queda guardado». | Validación temprana de cada dato y resultado explícito de cada operación de escritura (DEF-26, DEF-27, DEF-34 y DEF-35). | 6.6 |
| AP4 v1 | «La marca es obligatorio»; precio de servicio actualizable a $ 0; Ctrl-D informado como error interno. | DEF-28; DEF-29 con el CHECK de la base alineado (DEF-36); DEF-30. | 6.6 |
| AP4 v1 | Dependencias que violaban las capas MVC/DAO; conversión de mayúsculas dependiente del idioma de la JVM; clave generada leída sin comprobación. | DEF-31 (ArchUnit), DEF-32 (SpotBugs) y DEF-33 (PMD). | 6.6 |
| AP4 v1 | El script de evidencias fallaba si la ruta del proyecto tenía espacios. | Comandos citados y carpeta de salida configurable (DEF-25). | 6.6 |
| AP4 v1 | No se medía la cobertura de las pruebas; los controladores y la vista casi no tenían pruebas automatizadas. | 129 pruebas nuevas en nueve niveles; cobertura de líneas del 90 % y puntaje de mutación del 80 %. | 6.6 |
| AP4 v2 | El enlace al repositorio GitHub seguía como marcador, aunque la corrección del AP3 lo daba por incorporado. | Repositorio publicado; enlace en la Tabla 1, 4.1 y el README. | Presentación |
| AP4 v2 | El documento no explicaba con código la conexión ni una consulta, y 5.4 no tenía fragmento. | Fragmentos de ConexionBD.getConexion, TurnoDAO.listarAgenda con su presentación, Persona y Dao<T, K>. | 5.2 y 5.4 |
| AP4 v2 | Ocho referencias no se citaban en el texto y la cita textual de Kendall y Kendall no indicaba de dónde se tomó. | Cada obra se cita donde se usa; la cita se atribuye a la consigna que la reproduce. | Todas |
| AP4 v2 | El PDF incrustaba Carlito, una fuente equivalente, en lugar de Calibri. | PDF exportado desde Microsoft Word con Calibri. | Formato |
| AP4 v2 | La campaña de pruebas ocupaba cuatro secciones de la Parte VI. | Síntesis en la sección 6.6; el detalle queda en el repositorio. | 6.6 |
| AP4 v2 | Figuras 18 y 20 ilegibles al imprimir; el diagrama de paquetes omitía la dependencia vista → modelo; afirmaciones inexactas en 4.1 y en las tablas de entorno y de operaciones; prefijo CPA repetido en los casos de prueba. | Figuras en página apaisada; diagrama corregido; frases precisadas; CPR01 para las reglas de arquitectura. | 4.1, 4.2, 5.1, 5.2 y 6.1 |
| AP4 v2 | Dos guiones distintos para el video y un kit de borradores de participación entre los entregables. | Guion único de 3 minutos alineado con las diapositivas; el kit se retiró de la entrega. | Video |

# Parte I. Modelo de negocio y definición del proyecto

## 1.1 La organización y el problema

Talleres Ruiz Díaz está compuesta por dos talleres mecánicos de la Ciudad Autónoma de Buenos Aires, propiedad del Sr. Mario Ruiz Díaz, dedicados al mantenimiento, diagnóstico y reparación de automóviles particulares. La sede principal funciona en Echeandía 3042 y la sucursal, en Av. Rivadavia 8450 (Flores). Ambas tienen una estructura similar —recepción, mecánicos y encargado de repuestos—, pero operan de manera prácticamente independiente. Con el crecimiento de la segunda sede, el titular necesita un sistema que dé soporte a la coordinación de ambos talleres y le brinde una visión consolidada del negocio.

Cada sede coordina sus turnos por teléfono o WhatsApp y los anota en una agenda de papel propia, sin posibilidad de consultar la disponibilidad de la otra. Esto produce turnos superpuestos dentro de una misma sede y una distribución despareja de la carga de trabajo. Los presupuestos se calculan a mano, con errores y demoras en su aprobación; el stock de repuestos no se comparte, lo que genera compras duplicadas o faltantes de piezas disponibles en la sede vecina; y la asistencia del personal se registra en planillas de papel independientes, que el titular no puede consolidar para la liquidación de haberes.

Bajo el enfoque entrada-proceso-salida (Saroka, 2002), el sistema recibe el vehículo con su turno y el diagnóstico inicial, procesa el diagnóstico técnico, el presupuesto y la reparación, y entrega el vehículo reparado junto con su historial actualizado. La Figura 1 presenta el modelo de dominio que surge de ese análisis.

FIG: dominio.png | Diagrama de dominio del sistema TALLERNET | 15

## 1.2 Objetivos

**Objetivo general del proyecto.** Desarrollar un sistema de gestión que permita coordinar los turnos, presupuestos, asistencia del personal, clientes, vehículos, repuestos y servicios de las dos sedes de Talleres Ruiz Díaz, unificando la operación de ambas sucursales durante el próximo año.

**Objetivos específicos.**

- Implementar la gestión de turnos en cualquiera de las dos sedes, evitando superposiciones horarias.
- Calcular automáticamente el presupuesto de cada orden de trabajo a partir de repuestos y mano de obra, y registrar su aprobación.
- Registrar el fichaje del personal en cada sede y consolidar reportes de asistencia para la liquidación de haberes.
- Construir un registro compartido de clientes, vehículos y repuestos que evite duplicaciones y brinde visibilidad cruzada del stock.
- Administrar las sedes y el catálogo de servicios para consolidar la información operativa de ambas ubicaciones.

**Objetivo del sistema.** Registrar y centralizar la operación de turnos, presupuestos, asistencia, clientes, vehículos, repuestos, sedes y servicios de las dos sucursales, facilitando al titular una visión unificada del negocio y agilizando la coordinación entre ambas.

## 1.3 Límites, alcance y restricciones

Los límites de un sistema distinguen los elementos que están bajo su control de los que forman parte de su ambiente (Saroka, 2002). Quedan dentro del sistema la creación y el seguimiento de turnos por sede, las órdenes de trabajo y presupuestos, el registro de repuestos y mano de obra, el control de stock por sede, el fichaje del personal y la administración del catálogo y de las sedes. Para que el desarrollo resulte abordable como prototipo, quedan en el ambiente, como requerimientos candidatos: la liquidación de sueldos (el sistema solo consolida horas), la integración con relojes biométricos, la facturación electrónica, la logística de traslado de repuestos entre sedes y un portal de autogestión para clientes. Como restricción tecnológica, el sistema se desarrolla en Java y persiste sus datos en MySQL.

## 1.4 Elicitación, procesos actuales y diagnóstico

Se combinaron tres técnicas: una entrevista semiestructurada al titular en la sede de Echeandía, la observación directa de una jornada en ambas sedes y una encuesta breve a recepcionistas y mecánicos. Cada sede cuenta con una computadora en la recepción, usada para planillas de cálculo y para comunicarse con los clientes, y con una conexión a Internet de uso administrativo. No existe ningún sistema que centralice la información. Los dos talleres de la zona que operan con más de una sucursal tampoco lo tienen, lo que representa una oportunidad de diferenciación.

Se relevaron tres procesos de negocio. En la coordinación de turnos (Figura 2), el cliente solicita el turno y el recepcionista verifica la disponibilidad en la agenda de papel de su sede, lo agenda y, el día del turno, confirma la llegada y genera la orden de trabajo. En el proceso de diagnóstico, presupuesto y reparación (Figura 3), el mecánico registra el diagnóstico, solicita los repuestos, se calcula el presupuesto y el recepcionista registra la aprobación o el rechazo del cliente. En el registro de asistencia (Figura 4), cada empleado ficha la entrada y la salida y el administrador consolida las horas.

FIG: proc_turnos.png | Proceso actual: coordinación de turnos y recepción del vehículo | 7
FIG: proc_presupuesto.png | Proceso actual: diagnóstico, presupuesto y reparación | 12
FIG: proc_asistencia.png | Proceso actual: registro y control de asistencia del personal | 8

TABLA: Diagnóstico de los procesos relevados
WIDTHS: 24,38,38
| Proceso | Problemas | Causas |
| Coordinación de turnos y recepción | Turnos superpuestos en una misma sede y carga de trabajo despareja entre sucursales. | Agendas de papel independientes, sin visibilidad de la disponibilidad de la otra sede. |
| Diagnóstico, presupuesto y reparación | Presupuestos con errores y demoras; compras de repuestos duplicadas. | Cálculo manual de costos; stock no compartido. |
| Registro de asistencia | Diferencias entre las horas informadas y las trabajadas; sin consolidación para el titular. | Planillas de papel por sede. |

## 1.5 Propuesta de solución y requerimientos

TALLERNET centraliza en una única base de datos MySQL los turnos, clientes, vehículos, mecánicos, repuestos y servicios de ambas sedes; verifica la disponibilidad de cada turno, calcula los presupuestos y alerta cuando el stock de una sede baja del mínimo. La aplicación se ejecuta en el puesto de recepción de cada sede y se conecta con el servidor central a través de una VPN sobre Internet, modelo que admite sumar una tercera sede (Figura 5).

FIG: arquitectura.png | Arquitectura propuesta del sistema TALLERNET | 13

La Tabla 5 presenta los requerimientos vigentes. Se conserva la numeración del AP1 para preservar la trazabilidad; RF-TN03 y RF-TN17 se ampliaron en el AP2 con datos de kilometraje, y RNF-TN08 y RNF-TN09 surgieron de las decisiones de implementación y comunicación.

TABLA: Requerimientos funcionales y no funcionales vigentes
WIDTHS: 13,72,15
| Req. | Descripción | Estado en el prototipo |
| RF-TN01 | Ingreso con usuario y contraseña, diferenciando roles. | Parcial: se elige el empleado en sesión, sin contraseña |
| RF-TN02 | Registrar un cliente (nombre, teléfono, correo), visible desde ambas sedes. **AP4: modificar sus datos de contacto.** | Implementado |
| RF-TN03 | Registrar un vehículo del cliente (patente, marca, modelo, año y kilometraje al alta — ampliado en AP2). | Implementado |
| RF-TN04 | Crear un turno indicando sede, fecha, hora y servicio. | Implementado |
| RF-TN05 | Impedir turnos superpuestos para una misma sede y horario. | Implementado |
| RF-TN06 | Modificar o cancelar un turno. | Implementado |
| RF-TN07 a RF-TN13 | Orden de trabajo, mecánico, diagnóstico, repuestos, mano de obra, presupuesto y aprobación (CU002). | Próxima iteración |
| RF-TN14 a RF-TN16 | Alta de repuestos, alerta de stock mínimo y consulta cruzada de stock. | Próxima iteración |
| RF-TN17 | Catálogo de servicios con precio, duración y kilometraje recomendado (ampliado en AP2). **AP4: actualizar el precio.** | Implementado |
| RF-TN18, RF-TN19 | Fichaje y reporte de asistencia (CU003). | Próxima iteración |
| RF-TN20 | Alta de sede con nombre, dirección y horario. | Por script SQL; consulta implementada |
| RF-TN21 | Listar las órdenes activas por sede. | Próxima iteración (CU002) |
| RF-TN22 | Historial de un vehículo en cualquiera de las sedes. | Implementado |
| RNF-TN01 a 03 | Java, MySQL y patrón MVC. | Cumplido |
| RNF-TN04 | Disponibilidad en horario comercial (lunes a sábado, 08 a 20 h). | Validado en el prototipo |
| RNF-TN05 | Escalable a nuevas sedes. | Cumplido (datos y red) |
| RNF-TN06 | Validar los datos antes de persistirlos. | Cumplido |
| RNF-TN07 | Requiere conexión; sin modo offline. | Cumplido; la falta de conexión se informa (AP4) |
| RNF-TN08 | JDK 17 LTS en los puestos y MySQL 8.0 en un servidor Linux (AP2). | Cumplido |
| RNF-TN09 | Comunicación cifrada: VPN y TLS en la conexión a la base (AP2). | Cumplido |

Quedan como requerimientos candidatos: autogestión de turnos por parte del cliente, recordatorios automáticos por WhatsApp o correo, liquidación de haberes, logística de repuestos entre sedes y facturación electrónica.

## 1.6 Casos de uso y actores

El sistema se organiza en ocho macrocasos de uso (Figura 6). Los actores son el recepcionista, que coordina turnos, recibe vehículos y comunica presupuestos; el mecánico, que diagnostica y repara; el encargado de repuestos, que gestiona el stock de su sede; el administrador (el titular), que administra sedes y catálogo y consulta reportes consolidados, y el cliente, actor secundario. «Empleado» es la generalización de los tres primeros, necesaria para CU003. Se especificaron en detalle CU001 Gestionar turno, CU002 Generar presupuesto de servicio y CU003 Registrar asistencia de personal.

FIG: casos_uso.png | Diagrama de casos de uso del sistema TALLERNET | 15

TABLA: Especificación del caso de uso crítico CU001 Gestionar turno
WIDTHS: 22,78
| Elemento | Contenido |
| Actores | Recepcionista (principal), cliente (secundario). |
| Referencias | RF-TN04, RF-TN05, RF-TN06; precondiciones RF-TN02, RF-TN03 y RF-TN17. |
| Precondición | El recepcionista inició sesión. El cliente y el vehículo están registrados o se registran en el mismo flujo. |
| Flujo principal | 1. El recepcionista elige «Nuevo turno». 2. Indica sede, vehículo y servicio. 3. Indica fecha y hora. 4. Confirma (S5). 5. El sistema verifica que no exista superposición en la sede. 6. Registra el turno como confirmado con número correlativo. 7. Muestra la confirmación. |
| Flujo alternativo S5 | Superposición detectada: el sistema informa que el horario no está disponible y vuelve al paso 3. **AP4:** además sugiere el primer horario libre del día para ese servicio. |
| Postcondición | Turno confirmado y visible desde ambas sedes. |

CU001 se eligió como caso de uso crítico porque el turno es el primer eslabón de los demás procesos —sin turno no hay orden de trabajo, presupuesto ni reparación—, porque ataca el problema más visible del diagnóstico y porque exige resolver el mayor riesgo técnico: dos recepcionistas de sedes distintas operando a la vez sobre la misma base.

## 1.7 Planificación y cronograma real

La Figura 7 muestra la planificación del proyecto definida en el AP1; la Tabla 7, las fechas reales de las entregas académicas, que corresponden a las iteraciones del PUD.

FIG: gantt.png | Diagrama de Gantt del proyecto TALLERNET (planificación del AP1) | 16

TABLA: Cronograma real de las entregas
WIDTHS: 14,22,64
| Entrega | Fecha | Fase del PUD e incremento |
| AP1 | 07/09/2026 | Inicio: problema, alcance, requerimientos y casos de uso. |
| AP2 | 28/09/2026 | Elaboración: análisis y diseño de CU001, base de datos, comunicaciones y primer prototipo. |
| AP3 | 19/10/2026 | Construcción: implementación orientada a objetos, estructuras de datos y pruebas. |
| AP4 | Noviembre de 2026 | Construcción y transición: patrón de diseño, persistencia completa, archivos y versión integradora. |

# Parte II. Análisis y diseño (PUD)

## 2.1 Iteración dirigida por el caso de uso crítico

El proyecto se desarrolló como un proceso iterativo, dirigido por casos de uso y centrado en la arquitectura (Jacobson et al., 2000). El modelo de casos de uso describe el sistema desde la perspectiva del recepcionista; el modelo de análisis lo describe internamente, con clases y paquetes todavía independientes del lenguaje; el modelo de diseño incorpora Java, MySQL y MVC; la implementación los convierte en componentes ejecutables, y las pruebas verifican cada nivel.

## 2.2 Modelo de análisis

Del flujo de CU001 surgieron ocho clases de análisis con los tres estereotipos de UML (Rumbaugh et al., 2006) (Figura 8): PantallaTurno «boundary» conversa con el recepcionista; GestorTurno «control» coordina el caso de uso, y Turno, Sede, Vehiculo, Cliente, Servicio y Empleado «entity» conservan el conocimiento del dominio. El diagrama de secuencia de análisis (Figura 9) permitió descubrir que la verificación de superposición, que parecía propia del gestor, debía delegarse en Turno, porque la duración surge del servicio que el turno conoce.

FIG: an_clases.png | Diagrama de clases de análisis del CU001 Gestionar turno | 12
FIG: an_secuencia.png | Diagrama de secuencia de análisis del CU001 en su curso normal | 15

Las clases se agruparon en tres paquetes de análisis alineados con los macrocasos de uso: Turnos, Clientes y vehículos, y Sedes y catálogo. CU002 y CU003 necesitarán clientes y sedes, pero no turnos, de modo que esa división permite reutilizar paquetes sin dependencias innecesarias.

## 2.3 Modelo de diseño

En el diseño se incorporaron las restricciones tecnológicas (RNF-TN01 a RNF-TN03). La clase de interfaz se convirtió en la vista del patrón MVC; la de control se dividió en un controlador que orquesta el caso de uso y un validador sin estado que concentra las reglas de fecha y horario. La persistencia se resolvió con el patrón DAO y la configuración de la conexión con un Singleton que entrega una conexión nueva por operación, de modo que un corte de red en una sede no inutilice la conexión de la otra. Los montos se representan con un decimal exacto; el kilometraje recomendado es opcional (registrar cero sería un dato falso), y la verificación de superposición y el alta del turno se ejecutan en una misma transacción (Figura 10).

FIG: di_secuencia.png | Diagrama de secuencia de diseño del CU001 | 15

La Figura 11 muestra los seis subsistemas de diseño. Las dependencias siguen una dirección descendente, desde la vista hasta la conexión, sin ciclos: puede reemplazarse la consola por una interfaz gráfica, o MySQL por otro motor, limitando el impacto al subsistema correspondiente. Los diagramas de clases de diseño se trasladaron al código manteniendo los nombres; su versión final, que coincide con la implementación, se presenta en la Parte IV.

FIG: subsistemas.png | Subsistemas de diseño y sus dependencias | 9

## 2.4 Modelo de implementación y despliegue

El ejecutable contiene la vista, los controladores, el modelo y la persistencia, y depende del driver JDBC, del archivo de configuración y de la base de datos (Figura 12). Se adoptó una arquitectura cliente-servidor de dos capas: la aplicación corre en el puesto de recepción de cada sede y accede a la base central mediante JDBC, dentro de un túnel VPN y con TLS (Figura 13). Un servidor de aplicaciones agregaría complejidad sin resolver un problema del alcance actual; se incorporará cuando se habilite el portal de autogestión, para que los clientes externos no accedan directamente a la base.

FIG: componentes.png | Diagrama de componentes del prototipo | 15
FIG: despliegue.png | Diagrama de despliegue de TALLERNET | 13

# Parte III. Base de datos y comunicaciones

## 3.1 Normalización y modelo de datos

La base tallernet reside en el servidor central y la comparten ambas sedes (RNF-TN02), lo que resuelve la falta de visibilidad del diagnóstico (Figura 14). Usa InnoDB, que admite claves foráneas, transacciones y bloqueos por fila, y utf8mb4, que almacena correctamente nombres como «Benítez» o «Echeandía». Se partió de la información que hoy registra la agenda de papel —una línea por turno con cliente, teléfono, vehículo, servicio, sede y hora— y se aplicaron las tres primeras formas normales (Reinosa et al., 2012):

- **1FN:** valores atómicos (nombre y apellido separados, horario de la sede en apertura y cierre) y vehículos en su propia tabla en lugar de columnas repetidas.
- **2FN:** todas las tablas tienen clave simple autonumérica. Se descartó una clave natural (sede, fecha y hora) para el turno porque impediría volver a reservar una franja liberada por una cancelación, que se conserva en el historial.
- **3FN:** el turno no guarda datos del cliente ni la duración o el precio del servicio, que dependen de otras entidades. El kilometraje al alta pertenece al vehículo y el intervalo recomendado al servicio; el kilometraje de cada visita se guardará en la orden de trabajo de CU002.

FIG: der.png | Diagrama entidad-relación de la base de datos tallernet | 14

Todas las relaciones son de uno a muchos, con ON DELETE RESTRICT y ON UPDATE CASCADE: eliminar un cliente no debe borrar sus vehículos ni los turnos atendidos. Cuando una sede cierra o un empleado deja de trabajar, se lo marca como inactivo. El script de creación nombra cada restricción con prefijos (pk_, fk_, uq_, ck_), valida con CHECK el año, el kilometraje (0 a 2.000.000 km), el precio y la duración, y crea un índice compuesto sobre la sede y la fecha-hora del turno, que es el filtro de la consulta de superposición.

## 3.2 Scripts entregados

TABLA: Scripts SQL del repositorio (carpeta db)
WIDTHS: 8,30,62
| Orden | Archivo | Contenido |
| 1 | 01_ddl_creacion.sql | Base y seis tablas con PK, FK, UNIQUE, CHECK e índice. |
| 2 | 02_insercion.sql | Carga base: 2 sedes, 4 clientes, 5 vehículos, 5 servicios, 5 empleados y 6 turnos. |
| 3 | 03_insercion_masiva.sql | Volumen: 50 clientes, 70 vehículos, 8 servicios y 133 turnos, con controles de reglas de negocio. |
| 4 | 04_consultas.sql | Consultas de reporte Q1 a Q7. |
| 5 | 05_borrado.sql | Depuración de turnos cancelados y borrado completo en transacción. |
| 6 | 06_usuario_aplicacion.sql | Usuario de la aplicación con SELECT, INSERT y UPDATE, y REQUIRE SSL. |
| 7 | 07_usuario_pruebas.sql | Usuario para las pruebas de integración (solo desarrollo). |

## 3.3 Consultas

Las siete consultas responden preguntas que hoy el taller no puede contestar sin revisar dos agendas. Q4 implementa en SQL la regla de RF-TN05: dos turnos de una misma sede se superponen cuando el existente empieza antes de que termine el nuevo y termina después de que el nuevo empiece. En el prototipo, cada consulta está encapsulada en un método DAO con parámetros (Parte V).

TABLA: Consultas de reporte
WIDTHS: 8,46,26,20
| Consulta | Pregunta que responde | Cláusulas | Req. |
| Q1 | ¿Qué turnos confirmados tiene cada sede en un día? | INNER JOIN (5 tablas), WHERE, ORDER BY | RF-TN04 |
| Q2 | ¿Qué servicios recibió un vehículo, en cualquier sede? | INNER JOIN, WHERE, ORDER BY DESC | RF-TN22 |
| Q3 | ¿Cuánta carga e ingreso estimado tiene cada sede? | LEFT JOIN, GROUP BY, SUM, COUNT | Visión consolidada |
| Q4 | ¿Choca un turno nuevo con alguno existente en la misma sede? | INNER JOIN, DATE_ADD, WHERE | RF-TN05 |
| Q5 | ¿Qué clientes tienen más de un vehículo? | GROUP BY, HAVING | RF-TN02, RF-TN03 |
| Q6 | ¿Cuánto uso traen, en promedio, los vehículos de cada marca? | GROUP BY, AVG, MAX, HAVING | RF-TN03 |
| Q7 | ¿A qué kilometraje corresponde el próximo service? | INNER JOIN (4 tablas), WHERE, LIMIT | RF-TN03, RF-TN17 |

## 3.4 Arquitectura de comunicaciones

Las sedes acceden vía Internet (Tanenbaum y Wetherall, 2012) al servidor central, alojado en un proveedor de hosting y no en la sede principal: un corte de luz en Echeandía dejaría también a Flores sin sistema, y RNF-TN04 admite menos de un 2 % de inactividad mensual. Los routers de cada local mantienen un túnel WireGuard permanente con el servidor (Donenfeld, 2017), como muestra la Figura 15.

FIG: topologia.png | Topología de red de TALLERNET | 12

TABLA: Decisiones de comunicación por capa
WIDTHS: 22,78
| Capa | Decisión |
| Física y enlace | Puesto de recepción por cable UTP categoría 6 a un switch Gigabit (los compresores y elevadores interfieren con el Wi-Fi); UPS de 600 VA; fibra óptica en cada sede; Wi-Fi del personal (WPA2/WPA3) separado del Wi-Fi de clientes. |
| Red | Subredes 192.168.10.0/24 (Echeandía), 192.168.20.0/24 (Flores) y 10.8.0.0/24 (VPN); servidor en 10.8.0.1; reservada 192.168.30.0/24 para una tercera sede (RNF-TN05). |
| Transporte | Túnel WireGuard sobre UDP 51820, único puerto expuesto; MySQL en TCP 3306 solo dentro de la VPN; SSH con clave pública; NTP y DNS salientes. |
| Aplicación | JDBC mediante MySQL Connector/J; turnos en hora local de Buenos Aires con relojes sincronizados por NTP. |
| Seguridad y disponibilidad | Usuario de aplicación con permisos mínimos, solo desde la VPN y con TLS obligatorio; copia nocturna cifrada con mysqldump; pendiente un router con respaldo 4G para operar ante un corte de fibra. |

# Parte IV. Implementación orientada a objetos

## 4.1 Entorno y estructura del proyecto

TABLA: Entorno de implementación
WIDTHS: 26,44,30
| Elemento | Herramienta y versión | Requerimiento |
| Lenguaje y plataforma | Java SE, JDK 17 LTS (maven.compiler.release=17) | RNF-TN01, RNF-TN08 |
| IDE y construcción | Apache NetBeans 21, proyecto Maven | RNF-TN08 |
| Base de datos | MySQL Community Server 8.0 (InnoDB, utf8mb4) | RNF-TN02 |
| Driver | MySQL Connector/J 8.4.0 (JDBC) | RNF-TN02 |
| Pruebas | JUnit 5; desde la versión 2, además AssertJ, Mockito, jqwik, ArchUnit, Testcontainers, JaCoCo y PIT | — |
| Versionado | Git y GitHub (https://github.com/diegosalvador-ues21/tallernet) | Consigna |
| Diagramas | draw.io (análisis y diseño, AP2) y Graphviz (implementación, AP3 y AP4), con fuentes editables en el repositorio | Consigna |

El prototipo se organiza en ocho paquetes (Figura 16). La vista invoca solo a los controladores y presenta los objetos del modelo que estos devuelven; los controladores dependen de interfaces de persistencia y de archivos, no de sus implementaciones; el modelo no conoce ni la base ni la consola.

FIG: fig_paquetes_ap4.png | Paquetes del prototipo y sus dependencias (versión AP4) | 16

## 4.2 Diagrama de clases de implementación

Las Figuras 17 y 18 muestran el modelo de dominio y las capas de control y persistencia tal como quedaron en el código. Respecto del diseño del AP2, Servicio pasó a ser una clase abstracta con dos especializaciones: ServicioPorKilometraje (trabajos que se repiten según el uso, como el cambio de aceite) y ServicioGeneral (los que no dependen del kilometraje, como el diagnóstico con escáner). La distinción que en el AP2 se resolvía con un valor nulo pasa al modelo y elimina el riesgo del defecto DEF-02. Las clases agregadas en el AP4 se detallan en la Parte V.

FIG: fig_clases_modelo.png | Diagrama de clases del modelo de dominio | 15
FIG: fig_clases_control_persistencia.png | Controladores, persistencia y excepciones (núcleo del AP3) | 25 | apaisada

## 4.3 Pilares de la programación orientada a objetos

Los cuatro pilares se aplican como los presenta el material de la asignatura (Universidad Siglo 21, 2026a).

TABLA: Aplicación de los pilares de la POO
WIDTHS: 16,46,38
| Pilar | Dónde se aplica | Por qué en este dominio |
| Abstracción | Persona y Servicio (clases abstractas con métodos abstractos); Dao<T, K>, TurnoRepositorio y VehiculoRepositorio (interfaces); DaoJdbc y ExportadorCsv<T> (abstractas, AP4). | En el taller no existe «una persona» genérica: es un cliente o un empleado. Los controladores conocen qué se puede hacer con los datos, no cómo se guardan. |
| Encapsulamiento | Atributos private en todas las entidades; setters que validan (Vehiculo.setKmAlta, Cliente.setTelefono, Servicio.setPrecioBase); copias defensivas de colas y arreglos. | Un objeto nunca queda en un estado inválido: la regla del kilometraje, que en el AP2 solo controlaba la base, se aplica antes de llegar a ella. |
| Herencia | Cliente y Empleado extienden Persona; ServicioPorKilometraje y ServicioGeneral extienden Servicio; seis DAO extienden DaoJdbc; jerarquía de nueve excepciones propias. | Reutiliza validaciones, mapeo de filas y manejo de errores; cada jerarquía surge del dominio o del diseño. |
| Polimorfismo | calcularProximoServiceKm(int) en las subclases de Servicio; getDescripcionRol() en las de Persona; controladores que invocan Dao<T, K>; exportadores que implementan Exportador<T>. | Se programa en forma general: el controlador envía el mismo mensaje sin preguntar la clase, y la ligadura dinámica elige la implementación. |

CODE: ServicioPorKilometraje.java y ServicioGeneral.java (fragmentos)
@Override // ServicioPorKilometraje: el próximo service depende del uso
public OptionalInt calcularProximoServiceKm(int kmReferencia) {
    return OptionalInt.of(kmReferencia + kmServicio);
}
@Override // ServicioGeneral: no depende del kilometraje; no se inventa un número
public OptionalInt calcularProximoServiceKm(int kmReferencia) {
    return OptionalInt.empty();
}
END

Las entidades ofrecen dos constructores: uno completo, que usa la persistencia al reconstruir un registro, y otro para un objeto nuevo, que delega en el primero con this(...) porque el número lo asigna la base. Las subclases invocan el constructor de la superclase con super(...). Todos los objetos de las capas se declaran y crean explícitamente en Main, que conecta vista, controladores y DAO en un único punto.

## 4.4 Sintaxis, tipos de datos y estructuras de control

Se usan int y long (contadores de comparaciones, que en un ordenamiento cuadrático pueden superar el rango de int), boolean, String, BigDecimal para montos, LocalDate, LocalTime y LocalDateTime de java.time, enum (EstadoTurno, Rol, criterios de orden), record para resultados inmutables y Optional para expresar la ausencia de valor (Deitel y Deitel, 2016; Oracle, 2021). Los menús usan switch con la sintaxis case -> y do-while, porque deben mostrarse al menos una vez; las lecturas usan while hasta obtener un dato válido; los recorridos, for y for-each; y el control de errores, try/catch/finally y try-with-resources. El compilador no informa advertencias con -Xlint:all.

## 4.5 Menú de selección

TABLA: Opciones del menú (las marcadas como AP4 son nuevas)
WIDTHS: 24,58,18
| Menú | Opciones | Requerimiento |
| 1. Turnos | Nuevo turno (con sugerencia de horario en S5, AP4) · reprogramar · cancelar · deshacer la última cancelación · agenda por fecha · buscar por número · registrar llegada · atender siguiente · ver cola · **disponibilidad de la sede (AP4)** | RF-TN04 a RF-TN06 |
| 2. Clientes y vehículos | Alta de cliente · alta de vehículo · listar ordenados · buscar por patente · historial · **modificar datos de contacto (AP4)** | RF-TN02, RF-TN03, RF-TN22 |
| 3. Sedes y catálogo | Listar sedes · servicios por precio o duración · próximo service · **actualizar precio (AP4)** | RF-TN17, RF-TN20 |
| 4. Reporte de carga por sede | Turnos, minutos e ingreso estimado por sede (Q3) | Visión consolidada |
| **5. Reportes semanales y archivos (AP4)** | Reporte semanal por día · exportar agenda a CSV · exportar reporte a TXT · importar clientes desde CSV | Visión consolidada, RF-TN02 |
| **6. Sistema (AP4)** | Probar la conexión con el servidor central · ver las últimas operaciones (bitácora) | RNF-TN07, RNF-TN09 |
| 0. Salir | Finaliza la sesión | — |

FIG: c01_inicio_menu.png | Inicio de sesión y menú principal | 13

## 4.6 Estructuras de datos, ordenamiento y búsqueda

Cada estructura responde a un comportamiento del dominio. La lista (ArrayList) guarda los resultados de las consultas y permite el acceso por posición que exigen el ordenamiento y la búsqueda binaria. La cola de recepción (Queue sobre ArrayDeque) atiende los vehículos en orden de llegada, y la pila de cancelaciones (Deque) permite deshacer primero la última cancelación. Se implementaron algoritmos propios: inserción, O(n²) pero eficiente con listas casi ordenadas como la agenda del día; quicksort, O(n log n) en promedio, para el listado alfabético de clientes; búsqueda binaria, O(log n), sobre la lista de vehículos que llega ordenada por patente desde la base, y búsqueda lineal, O(n), como referencia. Con la carga de volumen, la búsqueda binaria encuentra una patente entre 75 vehículos con a lo sumo 7 comparaciones.

# Parte V. Proyecto integrador (AP4)

Esta parte responde, ítem por ítem, a las características que exige la consigna del AP4. Cada sección indica el concepto, dónde se aplica en el código, un fragmento mínimo y la evidencia de ejecución. Los fragmentos omiten con «// ...» las líneas que no aportan a la explicación; el código completo está en el repositorio.

## 5.1 Selección y justificación del patrón de diseño

Un patrón de diseño describe una solución probada a un problema que se repite en un contexto (Gamma et al., 1995). El problema central del AP4 es persistir y consultar los datos en MySQL sin que la lógica del taller dependa de cómo se guardan. Por eso se seleccionó el patrón **DAO (Data Access Object)**, que encapsula todo el acceso a una fuente de datos detrás de una interfaz: los controladores piden «insertá este turno» o «listá la agenda» y no saben que existe SQL. La elección mantiene la coherencia con la arquitectura definida: el AP1 fijó el patrón MVC (RNF-TN03), el AP2 definió el DAO en el diseño (sección 2.3) y el subsistema de persistencia, y el AP3 lo implementó.

El DAO se apoya en tres patrones complementarios, cada uno con una razón concreta:

- **Método plantilla (Template Method).** DaoJdbc define el esqueleto de toda operación JDBC —abrir la conexión, preparar la sentencia, asignar parámetros, ejecutar, recorrer el resultado, cerrar los recursos y traducir el error— y deja a cada DAO solo la sentencia SQL, los parámetros y el mapeo de la fila.
- **Singleton.** ConexionBD guarda la única configuración de acceso y entrega una conexión nueva por operación (decisión del AP2).
- **Método de fábrica.** Servicio.crear(...) decide qué subclase instanciar según exista o no un kilometraje recomendado, de modo que la persistencia traduce el NULL de la columna en un tipo concreto.

FIG: fig_patron_dao.png | Estructura del patrón DAO aplicado, con método plantilla y Singleton | 25 | apaisada

TABLA: Alternativas evaluadas para la persistencia
WIDTHS: 22,40,38
| Alternativa | Qué propone | Por qué se descartó |
| Active Record | Cada entidad sabe guardarse (turno.guardar()). | Mezcla las reglas del taller con SQL en el modelo y obliga a que Turno conozca la conexión; contradice la separación MVC del AP1. |
| ORM (JPA/Hibernate) | Mapeo automático de objetos a tablas. | Agrega dependencias y configuración, y oculta las sentencias que la consigna pide mostrar; el control fino del bloqueo de la sede (SELECT ... FOR UPDATE) se vuelve menos explícito. |
| JDBC en los controladores | Cada controlador arma y ejecuta su SQL. | Acopla el CU001 a MySQL, duplica el manejo de conexiones y errores, e impide probar los controladores sin base. |
| **DAO (elegido)** | Interfaz por entidad; implementación JDBC intercambiable. | Bajo acoplamiento, reutilización del esqueleto JDBC, pruebas aisladas y un único lugar para traducir los errores de MySQL. |

CODE: persistencia/DaoJdbc.java (método plantilla)
protected <T> List<T> consultar(String accion, String sql, ParametrosSentencia parametros,
        MapeadorFila<T> mapeador) throws PersistenciaException {
    List<T> resultado = new ArrayList<>();
    try (Connection con = abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
        parametros.asignar(ps);                     // paso variable 1: parámetros
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapeador.mapear(rs)); // paso variable 2: mapeo de la fila
            }
        }
    } catch (SQLException e) {
        throw error(accion, e);                     // traducción única de errores
    }
    return resultado;
}
END

Con la plantilla, un DAO concreto se reduce a lo propio de su tabla. Por ejemplo, ClienteDAO.listar() pasa a ser una sola línea: consultar("listar clientes", SELECT, ParametrosSentencia.NINGUNO, DaoJdbc::mapearCliente). TurnoDAO, que en el AP3 tenía su propio esquema de consulta, se unificó sobre la misma plantilla.

## 5.2 Persistencia y consulta en MySQL

La consigna pide establecer conexiones, realizar consultas, actualizar registros y presentar resultados en la interfaz. La Tabla 15 traza cada operación desde el menú hasta la sentencia SQL. Todas usan PreparedStatement con parámetros: los datos del usuario nunca se concatenan en el SQL, lo que evita la inyección SQL.

TABLA: Operaciones sobre MySQL, del menú a la sentencia
WIDTHS: 16,22,30,32
| Operación | Opción del menú | Clase y método | Sentencia |
| Conectar | 6.1 Probar conexión | ConexionBD.getConexion / diagnosticar | DriverManager.getConnection; SHOW SESSION STATUS LIKE 'Ssl_cipher' |
| Consultar | 1.5, 1.10, 2.5, 4, 5.1 | TurnoDAO.listarAgenda, historialPorPatente, resumenPorSede, listarEntre | SELECT con INNER/LEFT JOIN (Q1 a Q4) |
| Insertar | 1.1, 2.1, 2.2, 5.4 | TurnoDAO.insertarSiDisponible, ClienteDAO.insertar, VehiculoDAO.insertar | INSERT; el alta de turno, dentro de una transacción con bloqueo de la sede |
| **Actualizar** | 2.6 contacto; 3.5 precio; 1.2 reprogramar; 1.3 cancelar | ClienteDAO.actualizar, ServicioDAO.actualizar, TurnoDAO.reprogramarSiDisponible, cambiarEstado | UPDATE ... WHERE id = ? (verifica una fila afectada) |
| Presentar | Todas | VistaConsola (tablas alineadas, grilla de franjas) | — |

La conexión se obtiene siempre de ConexionBD, un Singleton que lee la configuración una sola vez. Cada DAO abre una conexión por operación y la cierra con try-with-resources; el driver Connector/J negocia el cifrado TLS que pide db.url (Oracle, 2024b). Un fallo se traduce a ConexionException con la indicación de qué revisar:

CODE: conexion/ConexionBD.java (establecer la conexión)
public Connection getConexion() throws PersistenciaException {
    try {
        return DriverManager.getConnection(url, usuario, clave);
    } catch (SQLException e) {
        if (e.getErrorCode() == 1045) {          // credenciales rechazadas
            throw new ConexionException("El servidor central rechazó el usuario"
                    + " o la contraseña. Revise db.usuario y db.clave ...", e);
        }
        throw new ConexionException("No se pudo conectar con el servidor central"
                + " (" + servidor() + "). Verifique Internet, la VPN ...", e);
    }
}
END

La agenda de una sede muestra el recorrido completo de una consulta. El DAO arma el SELECT con parámetros; el método plantilla consultar de DaoJdbc (5.1) recorre el ResultSet y lo convierte en una lista de Turno; el controlador la ordena por inserción, y la vista presenta el resultado en columnas:

CODE: persistencia/TurnoDAO.java y vista/VistaConsola.java (consultar y presentar)
// TurnoDAO: SELECT con parámetros; consultar() recorre el ResultSet
public List<Turno> listarAgenda(int sedeId, LocalDate fecha) throws PersistenciaException {
    return consultar(SELECT + " WHERE t.sede_id = ? AND t.fecha_hora >= ?"
            + " AND t.fecha_hora < ? AND t.estado <> 'CANCELADO'", ps -> {
                ps.setInt(1, sedeId);
                ps.setObject(2, fecha.atStartOfDay());
                ps.setObject(3, fecha.plusDays(1).atStartOfDay());
            });
}
// VistaConsola: presenta el resultado en columnas
List<Turno> agenda = turnos.consultarAgenda(sedeId, fecha);
for (Turno t : agenda) {
    out.printf("  %s-%s  N.° %-4d %-8s %-28s %s%n",
            t.getFechaHora().format(Formatos.HORA), t.getFin().format(Formatos.HORA),
            t.getId(), t.getVehiculo().getPatente(),
            recortar(t.getServicio().getDescripcion(), 28), t.getEstado());
}
END

La actualización se incorporó en el AP4 como un método por defecto de la interfaz Dao: las entidades que en el prototipo no admiten modificación (sedes, empleados) heredan un comportamiento que la rechaza, y las que sí la admiten lo sobrescriben. El UPDATE verifica que haya afectado exactamente una fila; cero significa que el registro no existe y se informa con EntidadNoEncontradaException.

CODE: persistencia/Dao.java y persistencia/ClienteDAO.java
default void actualizar(T entidad) throws TallerNetException {        // en Dao<T, K>
    throw new TallerNetException("Esta entidad no admite modificaciones en el prototipo.");
}
@Override
public void actualizar(Cliente cliente) throws TallerNetException {   // en ClienteDAO
    int filas = actualizar("actualizar el cliente",
            "UPDATE cliente SET telefono = ?, email = ? WHERE cliente_id = ?",
            ps -> { ps.setString(1, cliente.getTelefono()); /* ... */ },
            "Ya existe otro cliente registrado con el correo " + cliente.getEmail() + ".");
    if (filas != 1) {
        throw new EntidadNoEncontradaException("No existe el cliente N.° " + cliente.getId());
    }
}
END

Antes de llegar a la base, el controlador valida con los setters del modelo, igual que en el alta (RNF-TN06): un teléfono con menos de 8 caracteres o un precio con más de dos decimales se rechazan sin abrir una conexión. Las Figuras 21 a 23 muestran la ejecución real: la prueba de conexión informa el motor, el driver y el cifrado TLS negociado; la actualización de contacto queda persistida, y el nuevo precio aparece en el listado siguiente. Las capturas de las Partes V y VI corresponden a la versión 1; la versión 2 agrega líneas informativas, como el titular confirmado o el resultado de la operación, sin cambiar los resultados (sección 6.6).

FIG: c12_probar_conexion.png | Prueba de conexión con el servidor central (menú Sistema) | 14
FIG: c13_update_cliente.png | UPDATE de cliente y excepciones de MySQL | 15
FIG: c14_update_precio.png | INSERT con patente duplicada y UPDATE del precio de un servicio | 15

## 5.3 Excepciones en la interacción con MySQL

Hasta el AP3, una SQLException se envolvía en PersistenciaException con el mensaje técnico del driver, y solo la clave duplicada tenía un tratamiento propio. En el AP4 se centralizó la traducción en la clase TraductorSQLException, que clasifica cada error por su código de MySQL y por la clase de SQLState, estándar de SQL e independiente del driver (Oracle, 2024a). La Figura 24 muestra la jerarquía resultante. Se agregaron dos excepciones: ConexionException, subclase de PersistenciaException, y RegistroDuplicadoException, subclase de DatoInvalidoException. Al ser subclases, los bloques catch existentes siguen funcionando y la vista puede, además, distinguirlas.

FIG: fig_excepciones.png | Jerarquía de excepciones del prototipo (en amarillo, las nuevas del AP4) | 14

TABLA: Traducción de los errores de MySQL
WIDTHS: 30,16,26,28
| Situación | Código / SQLState | Excepción | Qué ve el recepcionista |
| Patente, correo o descripción repetidos | 1062 / 23000 | RegistroDuplicadoException | «Ya existe un vehículo con la patente AE123BC.» |
| Dato relacionado inexistente | 1452 | EntidadNoEncontradaException | Qué dato relacionado falta. |
| Registro con historial asociado | 1451 | DatoInvalidoException | Que se conserva para el historial. |
| CHECK violado o texto demasiado largo | 3819, 1406 | DatoInvalidoException | Que un dato está fuera de rango. |
| Servidor caído o VPN cortada | SQLState 08xxx | ConexionException | «[SIN CONEXIÓN] ... Verifique Internet, la VPN de la sede...» |
| Credenciales o TLS rechazados | 1045, 3159 | ConexionException | Que revise db.properties. |
| Interbloqueo o espera agotada | 1213, 1205 / 40001 | Reintento único; luego PersistenciaException | Que vuelva a intentar en unos segundos. |
| Sin privilegio | 1142 | PersistenciaException | Que avise al administrador. |

CODE: persistencia/TraductorSQLException.java (fragmento)
public static TallerNetException traducir(String accion, SQLException e, String duplicado) {
    switch (e.getErrorCode()) {
        case DUPLICADO:
            return new RegistroDuplicadoException(duplicado /* o un mensaje genérico */);
        case FK_SIN_PADRE:
            return new EntidadNoEncontradaException("No se pudo " + accion + ": ...");
        case CHECK_VIOLADO:
        case DATO_DEMASIADO_LARGO:
            return new DatoInvalidoException("No se pudo " + accion + ": ...");
        default:
            return tecnica(accion, e);  // conexión, credenciales, bloqueos y otros
    }
}
END

Las transacciones de turnos conservan el tratamiento del AP3: ante cualquier error se ejecuta rollback, y si el propio rollback falla, su excepción se agrega como suprimida para no ocultar la original (DEF-10). En el AP4 se sumó un reintento único cuando MySQL elige la transacción como víctima de un interbloqueo, situación posible cuando ambas sedes reservan a la vez: la transacción ya se revirtió por completo, de modo que repetirla es seguro.

CODE: persistencia/TurnoDAO.java (reintento ante interbloqueo)
public int insertarSiDisponible(Turno turno) throws TallerNetException {
    try {
        return insertarUnaVez(turno);
    } catch (PersistenciaException e) {
        if (e.getCause() instanceof SQLException sql
                && TraductorSQLException.esReintentable(sql)) {
            return insertarUnaVez(turno);
        }
        throw e;
    }
}
END

Las excepciones se capturan en un único método de la vista, ejecutar, ordenadas de la más específica a la más general: [S5] para la superposición, [!] para datos y reglas, [?] para registros inexistentes, [SIN CONEXIÓN] para la conexión, [ERROR DE BASE DE DATOS] para otras fallas técnicas y [ARCHIVO] para los archivos. Un último bloque captura cualquier RuntimeException imprevista y la informa como [ERROR INTERNO] (DEF-20): el menú nunca termina por una excepción y main no declara throws. Si la conexión falla al iniciar, el programa termina con código de salida 1 para que un script de arranque pueda detectarlo. La Figura 25 muestra la prueba más exigente: se detuvo el servidor MySQL en medio de una sesión; la consulta siguiente informó la falta de conexión, el menú continuó, y un inicio con credenciales inválidas se rechazó con una indicación concreta.

FIG: c20_sin_conexion.png | Servidor detenido en medio de la sesión (s12) y credenciales inválidas (s13) | 15

## 5.4 Clases abstractas e interfaces

TABLA: Clases abstractas e interfaces y lo que reutilizan
WIDTHS: 24,12,30,34
| Clase | Tipo | Implementaciones | Qué se reutiliza |
| Persona | abstracta | Cliente, Empleado | Número, nombre, apellido, validaciones y toString(). |
| Servicio | abstracta | ServicioPorKilometraje, ServicioGeneral | Descripción, precio, duración y validación; cálculo polimórfico del próximo service. |
| DaoJdbc (AP4: plantilla) | abstracta | Seis DAO | Esqueleto JDBC, mapeo de filas y traducción de errores. |
| ExportadorCsv<T> (AP4) | abstracta | AgendaCsv | Escritura UTF-8 con BOM, separador, escape y manejo de IOException. |
| Dao<T, K> | interfaz | Seis DAO; extendida por TurnoRepositorio y VehiculoRepositorio | Contrato común; método por defecto actualizar (AP4). |
| Exportador<T> (AP4) | interfaz | ExportadorCsv<T>, ReporteSemanalTxt | ArchivoControlador exporta sin conocer el formato. |
| RegistroOperaciones (AP4) | interfaz funcional | Bitacora; RegistroOperaciones.NINGUNO | Los controladores registran sin depender del archivo; en las pruebas no se escribe nada. |
| MapeadorFila<T>, ParametrosSentencia (AP4) | interfaces funcionales | Expresiones lambda y referencias a métodos | Pasos variables del método plantilla. |
| RegistroCliente (AP4) | interfaz funcional | ClienteControlador::registrarCliente | El importador reutiliza la validación y el alta del menú y se prueba sin base. |

Los fragmentos siguientes muestran las dos formas. Persona es abstracta: no se instancia, concentra los datos y las validaciones comunes y obliga a cada subclase a definir su rol y su dato de contacto. Dao<T, K> es una interfaz genérica: fija el contrato que usan los controladores y aporta un comportamiento por defecto para la actualización.

CODE: modelo/Persona.java y persistencia/Dao.java (fragmentos)
public abstract class Persona {
    private int id;
    private String nombre;
    private String apellido;
    protected Persona(int id, String nombre, String apellido) throws DatoInvalidoException {
        this.id = id;
        setNombre(nombre);              // validación común a Cliente y Empleado
        setApellido(apellido);
    }
    public abstract String getDescripcionRol();     // cada subclase la define
    public abstract String getDatoDeContacto();
}
public interface Dao<T, K> {
    K insertar(T entidad) throws TallerNetException;
    List<T> listar() throws PersistenciaException;
    Optional<T> buscarPorId(K id) throws PersistenciaException;
    default void actualizar(T entidad) throws TallerNetException {
        throw new TallerNetException("Esta entidad no admite modificaciones en el prototipo.");
    }
}
END

La interfaz RegistroOperaciones muestra el valor de la abstracción: TurnoControlador registra cada alta, reprogramación o cancelación con registro.registrar(...), sin saber si detrás hay un archivo, nada (en las pruebas) o, en una versión futura, una tabla de auditoría.

## 5.5 Arreglos y ArrayList de forma complementaria

La consigna pide declarar, inicializar y manipular arreglos y utilizar la clase ArrayList de manera complementaria. El criterio de diseño fue el siguiente: **se usa un arreglo cuando la cantidad de elementos es fija y la define el negocio, y un ArrayList cuando la cantidad es variable y depende de los datos**. Ambas estructuras se combinan en dos clases nuevas del modelo (Figura 26).

FIG: fig_arreglos_archivos.png | Clases del AP4: arreglos, ArrayList y archivos | 16

**AgendaDiaria.** La jornada de una sede se divide en franjas de 30 minutos: de 08:00 a 20:00 hay exactamente 24, cantidad que se calcula a partir del horario de la sede. Las franjas se guardan en un arreglo Turno[], con acceso directo por índice: la franja de las 10:30 es siempre la posición (10:30 − 08:00) / 30 = 5. Los turnos del día, en cambio, llegan de la base en un ArrayList, porque su cantidad varía. El constructor convierte la lista en arreglo para ordenarla con Arrays.sort y marca en la grilla las posiciones que ocupa cada turno según su duración.

CODE: modelo/AgendaDiaria.java (constructor y ocupación de franjas)
long minutosDeAtencion = Duration.between(sede.getHoraApertura(),
        sede.getHoraCierre()).toMinutes();
this.franjas = new Turno[(int) (minutosDeAtencion / MINUTOS_FRANJA)]; // 24 posiciones
Turno[] ordenados = turnosDelDia.toArray(new Turno[0]);       // ArrayList -> arreglo
Arrays.sort(ordenados, Comparator.comparing(Turno::getFechaHora));
for (Turno t : ordenados) { ocupar(t); }
// ocupar(t): índices de la franja inicial y final, recortados al tamaño del arreglo
for (int i = Math.max(0, desde); i < Math.min(hasta, franjas.length); i++) {
    franjas[i] = turno;
}
END

Sobre la grilla se resuelven dos necesidades del taller. La opción «Disponibilidad de una sede» muestra el mapa de ocupación del día, y en el flujo S5 el sistema ya no se limita a informar el choque: recorre el arreglo buscando tantas franjas libres consecutivas como requiere el servicio y sugiere el primer horario disponible (Figura 27). El método horariosDisponibles devuelve todos los inicios posibles en un ArrayList, porque su cantidad depende de la ocupación. El getter del arreglo devuelve una copia (Arrays.copyOf), de modo que quien lo recibe no puede alterar la grilla.

FIG: c15_grilla_S5.png | Disponibilidad de la sede (arreglo de franjas) y sugerencia de horario en el flujo S5 | 15

**ReporteSemanal.** La semana hábil del taller tiene seis días (RNF-TN04: no se atiende los domingos). El reporte acumula los turnos de la semana, que llegan en un ArrayList, en arreglos de tamaño fijo indexados por día: int[6] de turnos, int[6] de minutos y BigDecimal[6] de ingresos estimados, más una matriz int[sedes][6] que compara las sedes día por día (Figura 28). Los nombres de los días se declaran e inicializan en una línea (String[] DIAS = {"Lunes", ..., "Sábado"}) como constante privada; getDias() devuelve una copia, porque un arreglo público, aun declarado final, podría modificarse elemento por elemento (DEF-19). El día de mayor y de menor carga se obtienen recorriendo el arreglo, y los días sin turnos se devuelven en un ArrayList.

CODE: modelo/ReporteSemanal.java (acumulación en arreglos y matriz)
private static final String[] DIAS =
        {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
private final int[] turnosPorDia = new int[DIAS.length];
private final int[][] turnosPorSedeYDia;          // new int[sedes.length][DIAS.length]
// ...
for (Turno t : turnos) {                                  // turnos: List<Turno> (ArrayList)
    int dia = indiceDelDia(t.getFechaHora().toLocalDate());
    if (dia < 0 || !t.getEstado().ocupaAgenda()) continue; // fuera de semana o cancelado
    turnosPorDia[dia]++;
    minutosPorDia[dia] += t.getServicio().getDuracionMin();
    // ... ingresos y matriz sede × día
}
END

FIG: c16_reporte_semanal.png | Reporte semanal: arreglos por día y matriz sede × día | 15

Con la carga de volumen, la semana del 12 al 17 de octubre de 2026 reúne 26 turnos de ambas sedes, y la grilla del 19 de octubre en Echeandía muestra 11 de 24 franjas ocupadas (Figura 29).

FIG: c19_volumen_arreglos.png | Grilla y reporte semanal con la carga de volumen | 15

Otros usos de arreglos en el código: cada fila de una exportación CSV es un String[] con una posición por columna; la bitácora lee sus últimas líneas con un búfer circular de tamaño fijo, y el importador separa cada línea en un String[] con split(";", -1). La Tabla 18 resume el criterio aplicado.

TABLA: Arreglos y ArrayList en el prototipo
WIDTHS: 30,22,48
| Dato | Estructura | Motivo |
| Franjas de la jornada de una sede | Turno[24] | Cantidad fija (horario de la sede); acceso directo por hora. |
| Totales por día hábil y sede × día | int[6], BigDecimal[6], int[ ][ ] | Seis días hábiles, constante del negocio. |
| Nombres de los días | String[] constante | Declaración e inicialización en una línea; índice compartido con los totales. |
| Columnas de una fila CSV | String[] | Cantidad de columnas fija por tipo de exportación. |
| Últimas líneas de la bitácora | String[n] circular | Memoria acotada aunque el archivo crezca. |
| Turnos, clientes, vehículos y servicios leídos de la base | ArrayList | Cantidad variable; se agregan a medida que se recorre el ResultSet. |
| Horarios disponibles, días sin turnos, rechazos de una importación | ArrayList | El resultado depende de los datos. |

## 5.6 Archivos

La consigna considera opcional el uso de archivos; se implementó porque resuelve necesidades reales del taller y prepara la instancia del EFIP I. Todas las operaciones usan java.nio.file en UTF-8 y se agrupan en el paquete archivos.

- **Exportación de la agenda a CSV.** Una fila por franja de 30 minutos, con separador punto y coma y marca de orden de bytes (BOM), que es lo que Excel espera con la configuración regional de Argentina. Permite imprimir la agenda o compartirla con la otra sede ante un corte de la VPN.
- **Exportación del reporte semanal a TXT**, con el mismo formato que se muestra en la consola.
- **Importación de clientes desde CSV** (nombre;apellido;telefono;email), por ejemplo la planilla de contactos que hoy lleva cada sede. Cada línea se valida y registra por separado: una línea con errores se informa con su número y su motivo y no impide registrar las demás.
- **Bitácora de operaciones**, un archivo de texto en modo agregar con fecha, empleado, operación y detalle de cada alta, modificación o cancelación. Si no puede escribirse, la operación del taller continúa y se muestra un aviso.

CODE: archivos/ExportadorCsv.java (método plantilla de la exportación)
public final Path exportar(T datos, Path carpeta) throws ArchivoException {
    Path destino = carpeta.resolve(nombreArchivo(datos));
    try {
        Files.createDirectories(carpeta);
        try (BufferedWriter w = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            w.write(BOM);
            escribirFila(w, encabezado());                       // String[]
            for (String[] fila : filas(datos)) { escribirFila(w, fila); }
        }
        return destino;
    } catch (IOException e) {
        throw new ArchivoException("No se pudo escribir el archivo " + destino /* ... */, e);
    }
}
END

FIG: c17_archivos.png | Exportación CSV y TXT e importación de clientes con rechazos por línea | 15

En la importación de la Figura 30 se registraron tres clientes y se rechazaron tres líneas: un teléfono demasiado corto, un correo que ya pertenecía a otra clienta (error 1062 traducido) y una línea con una columna faltante. Los clientes registrados recibieron los números 5, 6 y 8: el 7 lo consumió el INSERT rechazado, comportamiento documentado de AUTO_INCREMENT en InnoDB que no afecta la integridad.

FIG: c18_bitacora.png | Bitácora de operaciones | 15

## 5.7 Repositorio y ejecución

El repositorio contiene el proyecto Maven completo, los scripts SQL 01 a 08 (el 08, opcional, crea los usuarios para una base en Docker), las pruebas, las entradas y salidas de las 14 sesiones de evidencia, la carpeta docs/pruebas con el plan, el informe, las fichas de defectos y las evidencias de la campaña de la versión 2, las capturas, los diagramas con sus fuentes editables, un CSV de ejemplo para la importación, el registro de decisiones (docs/decisiones.md) y el de defectos (docs/defectos.md). El archivo README permite a un tercero crear la base y ejecutar el prototipo en menos de diez minutos:

CODE: Puesta en marcha (README.md)
mysql -u root -p < db/01_ddl_creacion.sql
mysql -u root -p < db/02_insercion.sql
mysql -u root -p < db/06_usuario_aplicacion.sql
cp src/main/resources/db.properties.example db.properties
mvn clean package            # compila, ejecuta las pruebas y arma target/tallernet.jar
java -jar target/tallernet.jar
END

Para la versión 2 se agregó al proyecto el perfil Maven calidad, que ejecuta la suite completa y las herramientas de evaluación de la sección 6.6. Las pruebas que usan Testcontainers se omiten automáticamente si el equipo no tiene Docker; las pruebas de punta a punta usan expect sobre el jar generado:

CODE: Verificación completa (versión 2)
mvn -Pcalidad clean verify     # 193 pruebas, JaCoCo, PIT, SpotBugs, PMD y Checkstyle
expect docs/pruebas/e2e/concurrencia_dos_sedes.exp target/tallernet.jar db.properties 11/11/2026
expect docs/pruebas/e2e/fin_de_entrada.exp target/tallernet.jar db.properties
END

# Parte VI. Pruebas, evidencias y trazabilidad

## 6.1 Niveles y plan de pruebas

En la versión 1 se trabajó en los cuatro niveles clásicos de prueba (Sommerville, 2005), con un plan organizado según la norma IEEE 829 (Institute of Electrical and Electronics Engineers, 2008). Las pruebas unitarias verifican las clases del modelo, el validador, los algoritmos, los archivos y la traducción de errores sin base de datos. Las de integración (CPI04) recrean la base con los scripts 01 y 02 antes de cada caso y verifican transacciones, bloqueos, actualizaciones y excepciones contra MySQL real. Las de sistema recorren el prototipo completo desde la consola con entradas guionadas. La aceptación (CPA06) requiere una semana de uso en paralelo con la agenda de papel en ambas sedes y queda pendiente de la puesta en marcha piloto. La versión 2 amplió el plan a nueve niveles (componente con dobles de prueba, propiedades, arquitectura, integración aislada, sistema en proceso y punta a punta); los casos nuevos se marcan como «AP4 v2» y el método se describe en 6.6.

TABLA: Plan de pruebas
WIDTHS: 12,16,40,32
| Código | Nivel | Qué verifica | Origen |
| CPC01 | Componente | Regla de superposición de Turno (5 casos) | AP2 |
| CPF02 | Funcional | Hora de inicio: partición de equivalencias y valores límite (12 casos) | AP2 |
| CPC03 | Componente | Fin después del cierre, domingo, fecha pasada | AP2 |
| CPI04-1 a 7 | Integración | Alta, rechazo y concurrencia entre sedes; reprogramación; cambio de estado; reporte | AP2 y AP3 |
| CPI04-8 a 13 | Integración | UPDATE de cliente y precio; registro inexistente; duplicados 1062; consulta por rango; servidor inaccesible | AP4 |
| CPS05 | Sistema | Escenarios E1 a E7 del CU001 | AP2 |
| CPC07 | Componente | AgendaDiaria: tamaño del arreglo, ocupación, límites del arreglo, primer hueco, copia defensiva | AP4 |
| CPC08 | Componente | ReporteSemanal: arreglos, matriz, exclusiones, copias | AP4 |
| CPC09 | Componente | Exportación CSV y TXT, escape, importación con rechazos, bitácora | AP4 |
| CPC10 | Componente | Traducción de códigos de MySQL a excepciones | AP4 |
| CPC11 a CPC13 | Componente | Controladores aislados con dobles de prueba (Mockito): turnos, clientes, catálogo y sesión | AP4 v2 |
| CPV01 | Componente | Vista: validación temprana y resultado de cada alta (DEF-26, 27, 34 y 35) | AP4 v2 |
| CPF03 | Funcional | Valores límite de año, kilometraje, patente, longitudes, precio, correo y teléfono (32 casos) | AP4 v2 |
| CPP01 | Propiedades | Superposición simétrica, grilla en rango, ordenamientos y búsqueda binaria (jqwik, 1.000 intentos) | AP4 v2 |
| CPR01 | Arquitectura | 7 reglas de capas y dependencias (ArchUnit) | AP4 v2 |
| CPI05 a CPI07 | Integración | Altas, consultas y errores contra un MySQL 8.0 descartable (Testcontainers) con oráculo SQL; traducción de cada código de error | AP4 v2 |
| CPS02 | Componente | Configuración regional turca y expresión regular del correo (ReDoS) | AP4 v2 |
| CPS03 | Sistema | Recorrido en proceso de las 30 opciones del menú, con oráculo SQL y de archivos | AP4 v2 |
| CPE2E-01 a 03 | Punta a punta | Dos sedes simultáneas, fin de la entrada y servidor detenido durante un alta (expect sobre el jar) | AP4 v2 |
| CPA06 | Aceptación | Uso real en ambas sedes | Pendiente |

## 6.2 Resultados de las pruebas automatizadas

En la versión 1 se ejecutaron 64 pruebas automatizadas, todas aprobadas: 37 heredadas del AP3 y 27 nuevas (Figura 32). Las de integración corrieron contra MySQL 8.0.46 con el driver MySQL Connector/J 8.4.0, el mismo que declara el proyecto.

FIG: c21_pruebas.png | Resultado de las pruebas automatizadas (versión 1) | 13

En la versión 2 la suite tiene 193 pruebas en 27 clases, todas aprobadas, y se ejecuta con el perfil calidad, que además mide la cobertura, la mutación y el análisis estático (Figura 33). Las 129 pruebas nuevas y sus resultados se describen en 6.6.

FIG: c23_calidad.png | Resultado del perfil calidad en la versión 2 | 14

## 6.3 Escenarios de sistema

Las sesiones de evidencia se ejecutan redirigiendo un archivo de entrada a la consola, con la fecha actual fijada en el 1 de octubre de 2026 para reproducir los escenarios del AP2. Las sesiones s01 a s08 corresponden a CPS05 y a los escenarios del AP3, y se volvieron a ejecutar como regresión: la comparación automática con las salidas del AP3 (archivo regresion_ap3_vs_ap4.log) no muestra diferencias, salvo la línea nueva de sugerencia de horario en el flujo S5. En la versión 2 las 14 sesiones se repitieron como regresión sobre el prototipo corregido (sección 6.6). CPS05 se ejecuta sobre la carga base, de modo que la clienta Paula Herrera recibe el número 5; con la carga de volumen, ese número corresponde a otra clienta.

FIG: c02_E1_E2.png | CPS05 E1 (flujo S5, ahora con sugerencia de horario) y E2 (alta exitosa) | 15

TABLA: Sesiones de evidencia
WIDTHS: 26,52,22
| Sesión | Contenido | Resultado |
| s01 a s03 | CPS05 E1 a E7: S5, alta, fuera de franja, domingo, alta de clienta y vehículo, cancelación, kilometraje inválido | Coincide con el AP2 |
| s04 a s08 | Robustez de la entrada, ordenamiento y búsqueda, cola y pila, catálogo y reportes, volumen | Coincide con el AP3 |
| s09 | Conexión, UPDATE de cliente y precio, correo y patente duplicados, cliente inexistente, precio inválido, bitácora | Aprobado |
| s10 | Grilla de disponibilidad, sugerencia en S5, reporte semanal, exportación CSV y TXT, importación con rechazos, archivo inexistente | Aprobado |
| s11 | Grilla y reporte semanal con la carga de volumen | Aprobado |
| s12 | Servidor MySQL detenido en medio de la sesión | Aprobado |
| s13 | Inicio con credenciales inválidas | Aprobado |
| s14 (v2) | Fechas reales (lunes 09/11/2026): las seis opciones del menú principal y todos sus rechazos, con verificación en MySQL | Aprobado |

## 6.4 Registro de defectos

El registro continúa la numeración de las entregas anteriores (DEF-01 a DEF-15). En el AP4 se detectaron y corrigieron nueve defectos: DEF-16 en las pruebas automatizadas, DEF-17 y DEF-18 en las sesiones de evidencia, y DEF-19 a DEF-24 en una revisión independiente del código, realizada por un revisor que no participó de su escritura. Esa revisión verificó cada característica de la consigna y no encontró defectos de severidad alta. Después de las correcciones se volvieron a ejecutar todas las pruebas y sesiones.

TABLA: Defectos detectados y corregidos en el AP4
WIDTHS: 9,33,28,30
| ID | Descripción | Causa | Corrección |
| DEF-16 | La exportación de la agenda fallaba en un entorno que no usa UTF-8 para los nombres de archivo. | El nombre del archivo incluía «Echeandía» con tilde. | Nombre normalizado sin tildes (agenda_echeandia_2026-10-05.csv); el contenido sigue en UTF-8. |
| DEF-17 | El promedio diario se mostraba con punto decimal («4.3»). | Formato sin configuración regional. | Formato es-AR («4,3»), igual que montos y kilómetros. |
| DEF-18 | Con el servidor apagado o credenciales inválidas, el inicio mostraba «[ERROR DE BASE DE DATOS]». | Main no distinguía la falta de conexión. | Se captura antes ConexionException y se muestra «[SIN CONEXIÓN]» con qué revisar. |
| DEF-19 | El arreglo DIAS era público y podía modificarse elemento por elemento. | final no impide cambiar el contenido de un arreglo. | Arreglo privado; getDias() devuelve una copia. |
| DEF-20 | Una excepción no verificada imprevista cerraba el programa con la traza. | La vista solo capturaba TallerNetException. | Último catch de RuntimeException con «[ERROR INTERNO]»; el menú continúa. |
| DEF-21 | Si fallaba la escritura del reporte TXT, se informaba igual como exportado. | PrintWriter no lanza IOException. | Se verifica checkError() y se lanza ArchivoException. |
| DEF-22 | El aviso de bitácora no escrita se repetía en cada acción. | El error no se descartaba al informarlo. | Se informa una sola vez. |
| DEF-23 | Una semana sin turnos informaba «Día de mayor carga: Lunes (0 turnos)». | No se contemplaba la semana vacía. | Se informa que la semana no tiene turnos. |
| DEF-24 | Para un turno del día, la sugerencia del S5 podía proponer un horario ya pasado. | Se buscaba desde la apertura sin considerar la hora actual. | Se busca desde la hora actual; deshacer una cancelación también queda en la bitácora. |

La campaña de verificación de la versión 2 continuó la numeración con DEF-25 a DEF-36, que se presentan en la sección 6.6. Quedan como limitaciones conocidas, documentadas en el repositorio: el ingreso estimado de los reportes usa el precio vigente, de modo que actualizar un precio modifica también los ingresos estimados de semanas pasadas (consecuencia de la 3FN; el precio pactado se guardará en la orden de trabajo de CU002); la importación no interpreta campos entre comillas; la cola de recepción vive en la memoria de cada puesto y no verifica que el turno sea del día ni de la sede, y Vehiculo.setAnio usa el año del sistema y no el reloj inyectado.

## 6.5 Trazabilidad de punta a punta

TABLA: Trazabilidad: requerimiento, caso de uso, clases, tabla y prueba
WIDTHS: 11,10,33,13,33
| Req. | CU | Clases y métodos | Tabla | Prueba |
| RF-TN02 | CU001 (pre.) | Cliente, ClienteControlador.registrarCliente, actualizarContacto; ClienteDAO.insertar, actualizar; ImportadorClientesCsv | cliente | CPS05-E5, CPI04-8/9/10, CPC09, s09, s10 |
| RF-TN03 | CU001 (pre.) | Vehiculo.setKmAlta; ClienteControlador.registrarVehiculo; VehiculoDAO.insertar | vehiculo | CPS05-E5/E7, CPI04-10, CPV01, CPI05, CPS03, s09 |
| RF-TN04 | CU001 | TurnoControlador.registrarTurno; TurnoDAO.insertarSiDisponible | turno | CPI04-2, CPS05-E2 |
| RF-TN05 | CU001 (S5) | Turno.seSuperponeCon; TurnoDAO.verificarDisponibilidad; AgendaDiaria.primerHuecoLibre | turno, servicio | CPC01, CPI04-1/3/4/5, CPC07, CPP01, CPE2E-01, CPS05-E1, s10 |
| RF-TN06 | CU001 | TurnoControlador.reprogramarTurno, cancelarTurno, deshacerUltimaCancelacion | turno | CPI04-5/6, CPS05-E6 |
| RF-TN17 | CU001 (pre.) | Servicio y subclases; Servicio.crear; ServicioDAO.actualizar; CatalogoControlador.actualizarPrecio | servicio | CPI04-11, CPF03, CPI06, s07, s09 |
| RF-TN22 | — | ClienteControlador.historialVehiculo; TurnoDAO.historialPorPatente | turno | s05 |
| Visión consolidada | — | CatalogoControlador.cargaPorSede, reporteSemanal; ReporteSemanal; ReporteSemanalTxt | turno, sede | CPI04-7/12, CPC08, s07, s10, s11 |
| RNF-TN04 | CU001 | ValidadorTurno.validarDiaHabil, validarFranja; Sede.contieneIntervalo | sede | CPF02, CPC03, CPS05-E3/E4 |
| RNF-TN06 | CU001 | ValidadorTurno; setters del modelo; CatalogoControlador.convertirPrecio | CHECK | CPF02, CPF03, CPS02, CPS05-E7, s09 |
| RNF-TN07 | — | ConexionBD.getConexion; TraductorSQLException; ConexionException | — | CPI04-13, CPC10, CPI06, CPI07, CPE2E-03, s12, s13 |
| RNF-TN09 | — | ConexionBD (sslMode=REQUIRED); script 06 (REQUIRE SSL); diagnosticar | — | s09 (cifrado TLS informado) |

TABLA: Características exigidas por la consigna del AP4 y su evidencia
WIDTHS: 34,14,52
| Característica | Sección | Evidencia en el código y en la ejecución |
| El código compila y se ejecuta | 5.7, 6.2 | 193 pruebas aprobadas (64 en la versión 1); 14 sesiones reproducibles y 3 pruebas de punta a punta; compilación sin advertencias |
| Selección y justificación del patrón de diseño | 5.1 | DAO + método plantilla (DaoJdbc) + Singleton (ConexionBD) + método de fábrica (Servicio.crear) |
| Persistencia y consulta en MySQL | 5.2 | Conectar, consultar, insertar, actualizar y presentar: Tabla 15, Figuras 21 a 23 |
| Excepciones en la interacción con MySQL | 5.3 | TraductorSQLException, ConexionException, RegistroDuplicadoException, rollback y reintento; s12 y s13 |
| Clases abstractas o interfaces | 5.4 | 4 clases abstractas y 9 interfaces con reutilización real |
| Arreglos y ArrayList complementarios | 5.5 | AgendaDiaria (Turno[]), ReporteSemanal (int[], BigDecimal[], int[][], String[]), filas CSV, búfer de la bitácora |
| Archivos (opcional) | 5.6 | Exportación CSV y TXT, importación CSV, bitácora |
| Corrección de las observaciones previas | Correcciones | Tabla 3 |
| Verificación y corrección de la versión 1 (versión 2) | 6.6 | 11 defectos corregidos con prueba de regresión; cobertura de líneas del 90 %; mutación del 80 %; análisis estático sin observaciones abiertas |

## 6.6 Verificación de la versión 2

Durante la ejecución local del prototipo se observó que, al registrar un vehículo, «el registro no queda guardado en la base». A partir de ese reporte se hizo una campaña de verificación sobre la versión 1 con un objetivo verificable: encontrar, documentar y corregir los defectos, y dejar cada uno cubierto por una prueba automática que falla antes de la corrección y pasa después. El método sigue a Myers et al. (2011) y a Ammann y Offutt (2017):

- Se reprodujo cada defecto antes de corregirlo.
- Se comparó lo que informa el programa con una consulta directa a MySQL (oráculo independiente).
- Los controladores y la vista se aislaron con dobles de prueba (Mockito), y la persistencia se probó contra un MySQL 8.0 descartable creado con Testcontainers (Testcontainers, 2024).
- La suite se evaluó con cobertura (JaCoCo) y mutación (PIT), que introduce cambios pequeños en el código y verifica que alguna prueba falle (Coles et al., 2016).
- Un revisor que no participó de su escritura revisó las correcciones.

El caso reportado no era una pérdida de datos. El cliente ingresado, el 007, no existía porque la base se había restaurado a la carga inicial, y el programa recién lo rechazaba después de pedir todos los datos del vehículo, sin aclarar que no se había guardado nada (DEF-26 y DEF-27). Además, MySQL Workbench consultaba otro servidor, el del puerto 3306 (H-28). Con un cliente válido, el alta persiste, como confirma la consulta directa (Figura 35).

FIG: c22_def26.png | Alta de vehículo en la versión 2: rechazo inmediato del cliente inexistente y alta verificada con el oráculo SQL | 15

La Tabla 24 resume los defectos. DEF-34 lo introdujo la propia corrección de DEF-26 y lo detectó la revisión independiente. Las fichas completas están en docs/pruebas/defectos del repositorio.

TABLA: Defectos de la versión 1 detectados y corregidos en la versión 2
WIDTHS: 9,17,39,35
| ID | Detectado por | Descripción y causa | Corrección y prueba de regresión |
| DEF-25 | Puesta en marcha | El script de evidencias fallaba con rutas con espacios: comandos guardados en variables sin comillas. | Función java_sesion con argumentos citados y eval; carpeta de salida configurable. Sesiones s01 a s14. |
| DEF-26 | Caso reportado y CPV01 | El cliente inexistente se detectaba después de pedir todos los datos del vehículo; igual con sede, patente, servicio y turno. | Validación temprana de cada dato y opción de registrar el cliente en el mismo flujo. VistaAltasTest y oráculo SQL. |
| DEF-27 | Análisis de DEF-26 | Los rechazos no decían que no se había guardado nada. | Línea «Resultado: … no se guardó ningún dato.» en las 10 operaciones de escritura. VistaAltasTest, CPE2E-03. |
| DEF-28 | Exploración | «La marca es obligatorio»: mensaje sin concordancia. | «La marca es un dato obligatorio.» LimitesTest. |
| DEF-29 | Exploración y oráculo | El precio de un servicio podía actualizarse a $ 0,00. | El precio base debe ser mayor que cero. LimitesTest. |
| DEF-30 | Exploración y CPE2E-02 | Ctrl-D en medio de un alta mostraba «[ERROR INTERNO]» y quedaba en la bitácora. | La excepción de fin de entrada se relanza antes de la red de seguridad. CPE2E-02 en verde. |
| DEF-31 | ArchUnit | La vista dependía de la capa de conexión y Main usaba java.sql. | InfoConexion pasa al modelo; ConexionBD.verificar(). ArquitecturaTest. |
| DEF-32 | SpotBugs | toUpperCase y toLowerCase sin configuración regional: con la JVM en turco, «abi 123» se convertía en «ABİ123». | Locale.ROOT en las cinco conversiones. RobustezTest. |
| DEF-33 | PMD | La clave generada por un INSERT se leía sin comprobar que la base la devolviera. | DaoJdbc.claveGenerada con mensaje propio. CPI05 y CPI06. |
| DEF-34 | Revisión independiente | Introducido por la corrección de DEF-26: si se registraba el cliente y luego fallaba el vehículo, se informaba «no se guardó ningún dato». | El resultado informa lo que sí quedó registrado. VistaAltasTest. |
| DEF-35 | Revisión independiente | Reprogramar o cancelar un turno no confirmado pedía datos antes de rechazarlo. | Se verifica el estado del turno apenas se lo muestra. VistaAltasTest. |
| DEF-36 | Revisión independiente | El CHECK de la base admitía precio 0, que el modelo rechaza desde DEF-29. | CHECK (precio_base > 0) en el script 01. CPI06. |
| H-28 | Relevamiento de conexiones | Workbench consultaba el servidor del puerto 3306, no la base del prototipo. Es un hallazgo de entorno, no un defecto del código. | Conexión documentada en la guía de puesta en marcha; la opción 6 › 1 del programa muestra el servidor real. |

La suite, escrita con JUnit 5 (JUnit Team, 2024), pasó de 64 a 193 pruebas en nueve niveles: unitario, componente con dobles de prueba, propiedades (jqwik), arquitectura (ArchUnit), integración con la base de desarrollo, integración aislada (Testcontainers), sistema en proceso, punta a punta con expect y regresión de las sesiones s01 a s14. La primera medición, con 120 pruebas, mostró que los controladores (19 %) y la vista (32 %) casi no se ejercitaban; las pruebas nuevas cubrieron esos huecos (Tabla 25 y Figura 36).

TABLA: Métricas de la suite: inicio de la campaña y versión 2
WIDTHS: 40,20,20,20
| Métrica | Inicio | Versión 2 | Umbral |
| Pruebas automatizadas | 120 | 193, sin fallas | 100 % aprobadas |
| Cobertura de líneas: modelo / controlador | 90,9 % / 18,9 % | 95,0 % / 95,0 % | ≥ 85 % |
| Cobertura de líneas: persistencia / archivos | 64,2 % / 88,6 % | 89,3 % / 92,8 % | ≥ 85 % |
| Cobertura de líneas: vista | 32,3 % | 92,3 % | ≥ 70 % |
| Cobertura total de líneas / ramas | 54,4 % / 51,5 % | 90,0 % / 83,4 % | — |
| Puntaje de mutación (451 mutantes) | 62 % | 80 % | ≥ 75 % |
| SpotBugs y FindSecBugs | 50 avisos | 44 avisos, todos clasificados | Sin avisos abiertos |
| PMD / CPD / Checkstyle | 6 / 0 / 0 | 0 / 0 / 0 | 0 |
| Reglas de arquitectura (ArchUnit) | 2 violadas | 7 de 7 | Todas |

FIG: fig_cobertura_v2.png | Cobertura de líneas por paquete al inicio de la campaña y en la versión 2 | 16

Resultado de la verificación:

- **Análisis estático.** Produjo DEF-32 y DEF-33. Los 44 avisos restantes de SpotBugs se clasificaron uno por uno como diseño aceptado o falso positivo; por ejemplo, el aviso de inyección SQL es falso porque los datos viajan por PreparedStatement.
- **Regresión.** Las 14 sesiones guionadas no perdieron ninguna línea de resultado de negocio; solo agregan las líneas informativas nuevas.
- **Punta a punta.** Las tres pruebas con expect se aprobaron: dos sedes que reservan la misma franja a la vez, fin de la entrada y servidor detenido durante un alta.
- **Riesgos residuales.** Quedan la cobertura del paquete conexion (61 %) y el análisis de vulnerabilidades de las dependencias, que se planifica para la próxima iteración.

El detalle (plan, informe, fichas y evidencias) está en docs/pruebas del repositorio.

# Conclusiones y próximas iteraciones

TALLERNET responde al problema que se relevó en el AP1: las dos sedes de Talleres Ruiz Díaz comparten ahora una única agenda, un único registro de clientes y vehículos y un catálogo común, y el sistema impide los turnos superpuestos incluso cuando ambas sedes reservan al mismo tiempo. El AP4 completó el ciclo de la persistencia con la actualización de registros y un tratamiento de los errores de MySQL que le dice al recepcionista qué hacer, y agregó herramientas que atacan directamente el diagnóstico: la grilla de disponibilidad y la sugerencia de horario reducen la búsqueda manual de un hueco, y el reporte semanal le da al titular la visión consolidada de la carga que pidió desde el inicio.

El patrón DAO, definido en el diseño y reforzado con el método plantilla, demostró su valor durante el propio AP4: la actualización de registros y la traducción de errores se agregaron en un solo lugar y todos los DAO las heredaron, sin tocar la vista ni el modelo. Del mismo modo, la regla de usar arreglos cuando la cantidad la fija el negocio y listas cuando la fijan los datos produjo estructuras simples y fáciles de probar.

Las próximas iteraciones, en el orden de valor para el taller, son: CU002 (orden de trabajo, presupuesto y registro del odómetro en cada visita, que hará exacto el cálculo del próximo service), la gestión de repuestos con stock compartido, CU003 (asistencia del personal), la interfaz gráfica JavaFX prevista en el AP1, la autenticación con contraseña y el router con respaldo 4G para operar ante cortes de fibra. Antes de la puesta en marcha corresponde la prueba de aceptación CPA06 con el Sr. Ruiz Díaz y las recepcionistas.

La campaña de verificación de la versión 2 (sección 6.6) dejó tres aprendizajes. El primero: un defecto de usabilidad puede percibirse como pérdida de datos. El alta «que no se guardaba» estaba bien persistida; el problema era que el rechazo llegaba tarde y sin decir qué había ocurrido. Por eso cada operación de escritura informa ahora su resultado. El segundo: una corrección puede introducir un defecto nuevo. DEF-34 nació de la corrección de DEF-26 y lo detectó la revisión independiente, lo que confirma el valor de revisar los cambios con otra mirada. El tercero: medir la suite cambió las prioridades. Todas las pruebas se aprobaban, pero la primera medición mostró que los controladores y la vista tenían una cobertura menor al 35 %. La cobertura y la mutación permiten ahora fijar umbrales objetivos para las próximas iteraciones.

# Referencias

REF: Ammann, P., y Offutt, J. (2017). *Introduction to software testing* (2.ª ed.). Cambridge University Press.
REF: Coles, H., Laurent, T., Henard, C., Papadakis, M., y Ventresque, A. (2016). PIT: A practical mutation testing tool for Java. En *Proceedings of the 25th International Symposium on Software Testing and Analysis (ISSTA 2016)* (pp. 449-452). ACM.
REF: Deitel, P., y Deitel, H. (2016). *Cómo programar en Java* (10.ª ed.). Pearson Educación.
REF: Donenfeld, J. A. (2017). WireGuard: Next generation kernel network tunnel. En *Proceedings of the Network and Distributed System Security Symposium (NDSS 2017)*. Internet Society.
REF: Gamma, E., Helm, R., Johnson, R., y Vlissides, J. (1995). *Design patterns: Elements of reusable object-oriented software*. Addison-Wesley.
REF: Institute of Electrical and Electronics Engineers. (2008). *IEEE standard for software and system test documentation* (IEEE Std 829-2008). IEEE.
REF: Jacobson, I., Booch, G., y Rumbaugh, J. (2000). *El proceso unificado de desarrollo de software*. Addison Wesley.
REF: JUnit Team. (2024). *JUnit 5 user guide*. https://junit.org/junit5/docs/current/user-guide/
REF: Kendall, K., y Kendall, J. (2011). *Análisis y diseño de sistemas* (8.ª ed.). Pearson Educación.
REF: Myers, G. J., Sandler, C., y Badgett, T. (2011). *The art of software testing* (3.ª ed.). John Wiley & Sons.
REF: Oracle. (2021). *Java Platform, Standard Edition 17 API specification*. https://docs.oracle.com/en/java/javase/17/docs/api/
REF: Oracle. (2024a). *MySQL 8.0 reference manual*. https://dev.mysql.com/doc/refman/8.0/en/
REF: Oracle. (2024b). *MySQL Connector/J developer guide*. https://dev.mysql.com/doc/connector-j/en/
REF: Reinosa, E., Maldonado, C., Muñoz, R., Damiano, L., y Abrutsky, M. (2012). *Bases de datos*. Alfaomega.
REF: Rumbaugh, J., Jacobson, I., y Booch, G. (2006). *El lenguaje unificado de modelado: manual de referencia* (2.ª ed.). Pearson Addison Wesley.
REF: Saroka, R. (2002). *Sistemas de información en la era digital*. Fundación OSDE.
REF: Sommerville, I. (2005). *Ingeniería del software* (7.ª ed.). Pearson Addison Wesley.
REF: Tanenbaum, A. S., y Wetherall, D. J. (2012). *Redes de computadoras* (5.ª ed.). Pearson Educación.
REF: Testcontainers. (2024). *Testcontainers for Java*. https://java.testcontainers.org/
REF: Universidad Siglo 21. (2026a). *Módulo 3: Programación orientada a objetos* [Material de lectura]. Seminario de Práctica de Informática.
REF: Universidad Siglo 21. (2026b). *Trabajo práctico 4: consigna* [Material de la asignatura]. Seminario de Práctica de Informática.

# Declaración sobre herramientas utilizadas

Los diagramas de análisis y diseño se elaboraron con draw.io y Mermaid; los de implementación se generaron con Graphviz a partir de fuentes editables incluidas en el repositorio. En el código, las pruebas automatizadas, la corrección de defectos y la redacción de este informe se contó con la asistencia de Claude (Anthropic, modelo Opus 5.5). El autor revisó y es responsable de las decisiones de diseño y del contenido. En la versión 2 se utilizaron JUnit 5, AssertJ, Mockito, jqwik, ArchUnit, Testcontainers, JaCoCo, PIT, SpotBugs con FindSecBugs, PMD, Checkstyle y expect. La revisión independiente de las correcciones la realizó una instancia de Claude que no participó de su escritura. Todas las salidas, conteos y capturas citados provienen de ejecuciones reales del prototipo, guardadas en las carpetas docs/evidencias y docs/pruebas/evidencias del repositorio.
