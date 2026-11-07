# Video del AP4 — guion único (3 minutos) · versión 3

Este es el único guion del video. Lo acompañan:
- las diapositivas de apoyo «TALLERNET – Video AP4 (diapositivas de apoyo)», que llevan este mismo texto en las notas del orador;
- el doc «TALLERNET – Guion de grabación del video AP4 (3 min)», con la preparación paso a paso.

La demo corre en vivo contra MySQL. Las teclas y las salidas que figuran abajo salen de un ensayo real con la base en la carga inicial.

## Preparación (≈ 10 minutos)

1. **Terminal 1:** abrir `Tp4/Prototipo/video` y ejecutar `./preparar_video.command`. El script vuelve la base a la carga inicial, agranda la letra y espera un Enter para abrir TALLERNET con la fecha fijada en el 01/10/2026.
2. **Terminal 2:** en otra pestaña, dejar tipeado `./preparar_video.command verificar`, **sin** Enter.
3. Grabar con QuickTime › Nueva grabación de pantalla, con micrófono. Tener abiertas las diapositivas en otra ventana.
4. Publicar el video como **no listado** y pegar el enlace en la Tabla 1 del informe.

## Guion

↵ = Enter.

| Tramo | Tiempo | Pantalla y teclas | Texto (≈ 420 palabras) |
|---|---|---|---|
| 1. Problema | 0:00–0:20 | Diapositivas 1 y 2 | Hola, soy Diego Salvador. TALLERNET es mi proyecto integrador para Talleres Ruiz Díaz, que tiene dos sedes en Buenos Aires con agendas de papel separadas. Eso genera turnos superpuestos y un titular sin visión de conjunto. TALLERNET centraliza turnos, clientes y vehículos en una base MySQL que comparten las dos sedes. |
| 2. Patrón de diseño | 0:20–0:45 | Diapositiva 3 | Elegí el patrón DAO porque la arquitectura del AP2 ya lo definía y porque el AP4 se centra en la persistencia: los controladores trabajan contra la interfaz Dao y nunca ven SQL. Lo refuerzo con un método plantilla en la clase abstracta DaoJdbc, que concentra el código JDBC y la traducción de errores, y con un Singleton para la conexión. Descarté Active Record y un ORM porque mezclan el negocio con la base u ocultan las consultas. |
| 3. Demo en vivo | 0:45–2:00 | Terminal 1: ↵ y `2` ↵ (Carla). Luego `2` ↵ `2` ↵ `7` ↵ `N` ↵. Luego `2` ↵ `1` ↵ `lbs 854` ↵ `Renault` ↵ `Fluence 2.0 CVT Privilege` ↵ `2013` ↵ `195.555` ↵ `0` ↵. Luego `1` ↵ `1` ↵ `1` ↵ `LBS854` ↵ `1` ↵ `05/10/2026` ↵ `10:30` ↵ ↵. Luego `1` ↵ `1` ↵ `LBS854` ↵ `1` ↵ `05/10/2026` ↵ `13:00` ↵ ↵. Por último `10` ↵ `1` ↵ `05/10/2026` ↵ | Inicio sesión como Carla, recepcionista de Echeandía. Doy de alta un vehículo para el cliente 7: no existe, y el sistema lo dice en el acto y aclara que no se guardó nada. Ahora con la clienta 1: escribo la patente en minúsculas y el sistema la normaliza a LBS854 y valida el año y el kilometraje. Le pido un cambio de aceite el lunes 5 de octubre a las 10:30. Ese horario lo ocupa un service de 10 a 13: es el flujo S5 del caso de uso crítico. El sistema rechaza el turno y sugiere el primer horario libre, las 13. Acepto: turno 7 confirmado. La grilla del día lo muestra: es un arreglo de 24 franjas de media hora. |
| 4. Verificación en MySQL | 2:00–2:15 | Terminal 2: ↵ sobre `./preparar_video.command verificar` | Y la prueba de que quedó guardado: consulto MySQL directamente, sin pasar por el programa. Ahí están el vehículo y el turno. |
| 5. Excepciones, arreglos y ArrayList | 2:15–2:40 | Diapositiva 5 | ¿Qué pasa si se corta la conexión? Las excepciones de MySQL se traducen a mensajes que dicen qué revisar, y el menú sigue funcionando. Para los arreglos apliqué una regla simple: arreglo cuando la cantidad la fija el negocio, como las 24 franjas o los seis días hábiles; ArrayList cuando la fijan los datos, como los turnos que vienen de la base. |
| 6. Cierre | 2:40–3:00 | Diapositiva 6 | Detrás hay 193 pruebas automáticas con el 90 % del código cubierto. Lo próximo son presupuestos y órdenes de trabajo, repuestos y asistencia del personal. El código está en GitHub, en diegosalvador-ues21/tallernet. Gracias. |

## Qué debe verse en la demo (tramos 3 y 4)

| Momento | Resultado esperado en pantalla |
|---|---|
| Cliente 7 | «[?] No existe el cliente N.° 7.» y «Resultado: el vehículo no se registró; no se guardó ningún dato.» |
| Alta | «Titular: N.° 1 Gómez, Laura» y «Vehículo N.° 6 registrado: LBS854 - Renault Fluence 2.0 CVT Privilege 2013 (195.555 km al alta)» |
| S5 | «[S5] … se superpone con el turno N.° 2 (10:00 a 13:00).» y «Primer horario libre ese día para este servicio: 13:00» |
| Turno | «Turno N.° 7 CONFIRMADO», franja 05/10/2026 13:00 a 14:00 |
| Grilla | «10 de 24 franjas de 30 min ocupadas (42 %)», con el turno 7 en la fila de las 12:00 |
| Verificación | LBS854 de Laura Gómez y el turno 7 CONFIRMADO en Sede Echeandía, 05/10/2026 13:00 |

Si hay que repetir la toma: salir con `0` ↵ y volver a ejecutar `./preparar_video.command`. Al terminar: `./preparar_video.command reiniciar`.
