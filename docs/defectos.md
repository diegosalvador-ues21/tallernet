# Registro de defectos (AP3 y AP4)

Continúa la numeración del AP2 (DEF-01 y DEF-02). DEF-03 surgió de las sesiones de evidencia; DEF-04 a DEF-15, de la revisión independiente del código.

| ID | Descripción | Causa | Corrección | Estado |
|---|---|---|---|---|
| DEF-03 | El escenario E5 rechazaba el turno con «El turno no puede ser anterior al alta del vehículo». | Se comparaba el turno con la fecha de alta que asigna el reloj del servidor de base, distinto del reloj de la aplicación. | Se eliminó la comparación: la regla «no pasado» ya impide turnos anteriores al momento actual, posterior al alta. | Cerrado |
| DEF-04 | En una consola de Windows los acentos se mostraban y se grababan corruptos. | Se forzaba UTF-8 aun con consola interactiva en otra página de códigos. | Se usa el juego de caracteres de la consola (Console.charset) y UTF-8 solo con entrada redirigida. | Cerrado |
| DEF-05 | Desde dos puestos, un turno ya atendido podía quedar cancelado. | El UPDATE de estado no verificaba el estado vigente en la base. | cambiarEstado actualiza solo si el estado es el esperado y avisa si otra terminal lo modificó (CPI04-6). | Cerrado |
| DEF-06 | Si una cancelación no podía deshacerse, bloqueaba las anteriores en la pila. | El turno quedaba en el tope de la pila tras el error. | Se retira de la pila antes de intentar y se valida que no haya quedado en el pasado. | Cerrado |
| DEF-07 | La búsqueda binaria ordenaba la lista completa en cada consulta. | Ordenar cuesta O(n log n), más que la búsqueda lineal que se quería superar. | La lista llega ordenada por patente desde la base (ORDER BY). | Cerrado |
| DEF-08 | La fecha y hora de los turnos dependían de la conversión de zona horaria del driver. | Uso de Timestamp con parámetros propios de un driver. | Se leen y escriben como LocalDateTime (setObject / getObject), sin conversión. | Cerrado |
| DEF-09 | Un kilometraje «1.5» se aceptaba como 15. | Se eliminaban todos los puntos antes de convertir. | Solo se acepta el punto como separador de miles (18.500). | Cerrado |
| DEF-10 | Un error al revertir podía ocultar el error original. | rollback() dentro del catch sin protección. | El error del rollback se agrega como suprimido (addSuppressed). | Cerrado |
| DEF-11 | La verificación de superposición dependía del nivel de aislamiento por omisión. | Lectura sin bloqueo bajo REPEATABLE READ. | Se fija READ COMMITTED en las transacciones de turnos. | Cerrado |
| DEF-12 | El reporte por sede omitía las sedes sin turnos confirmados. | INNER JOIN con la tabla de turnos. | LEFT JOIN con COALESCE (CPI04-7). | Cerrado |
| DEF-13 | Textos más largos que su columna producían un error de base de datos. | El modelo no validaba el largo de marca, modelo, correo y observaciones. | Validación de largos en el modelo y el controlador. | Cerrado |
| DEF-14 | Un db.properties incompleto terminaba el programa con una traza. | Se lanzaba IllegalStateException, no verificada. | Se lanza PersistenciaException, que la vista informa. | Cerrado |
| DEF-15 | La vista consultaba la lista de empleados directamente a un DAO. | Faltaba un controlador para la sesión. | Nuevo SesionControlador; la vista solo habla con controladores. | Cerrado |

## AP4

DEF-16 surgió de las pruebas automatizadas; DEF-17 y DEF-18, de las sesiones de evidencia; DEF-19 a DEF-24, de la revisión independiente del código (veredicto: todos los ítems de la rúbrica en CUMPLE, sin defectos de severidad alta).

| ID | Descripción | Causa | Corrección | Estado |
|---|---|---|---|---|
| DEF-16 | La exportación de la agenda fallaba con `InvalidPathException` en un entorno sin UTF-8 como codificación de nombres de archivo. | El nombre del archivo incluía el nombre de la sede con tilde («Echeandía»). | El nombre se normaliza sin tildes ni espacios (`agenda_echeandia_2026-10-05.csv`); el contenido sigue en UTF-8. | Cerrado |
| DEF-17 | El reporte semanal mostraba el promedio con punto decimal («4.3»). | `String.format` sin configuración regional. | Se usa la configuración regional `es-AR` («4,3»), igual que en montos y kilómetros. | Cerrado |
| DEF-18 | Con credenciales inválidas o el servidor apagado, el inicio mostraba «[ERROR DE BASE DE DATOS]», el mismo rótulo que una falla de sentencia. | `Main` capturaba solo `PersistenciaException`. | Se captura antes `ConexionException` y se muestra «[SIN CONEXIÓN]» con la indicación de qué revisar (s13). | Cerrado |
| DEF-19 | El arreglo `ReporteSemanal.DIAS` era `public static final` y podía modificarse elemento por elemento. | `final` impide reasignar la referencia, no cambiar el contenido del arreglo. | Arreglo privado; `getDias()` devuelve una copia y `DIAS_HABILES` expone la cantidad. | Cerrado |
| DEF-20 | Una excepción no verificada imprevista (por ejemplo, `NullPointerException`) cerraba el programa con la traza. | `ejecutar` solo capturaba `TallerNetException`. | Último `catch (RuntimeException)` con «[ERROR INTERNO]» y registro en la bitácora; el menú continúa. | Cerrado |
| DEF-21 | Si fallaba la escritura del reporte TXT, igual se informaba «Reporte exportado». | `PrintWriter` no lanza `IOException`. | Se verifica `checkError()` y se lanza `ArchivoException`. | Cerrado |
| DEF-22 | El aviso de bitácora no escrita se repetía en cada acción. | El último error no se descartaba al informarlo. | `tomarUltimoError()` lo devuelve y lo limpia. | Cerrado |
| DEF-23 | Con una semana sin turnos, el reporte decía «Día de mayor carga: Lunes (0 turnos)». | No se contemplaba la semana vacía. | Se informa «La semana no tiene turnos registrados». | Cerrado |
| DEF-24 | Para un turno del día, la sugerencia del S5 podía proponer un horario ya pasado. | Se buscaba desde la apertura sin considerar la hora actual. | Se busca desde la hora actual si la fecha es hoy. La operación «deshacer cancelación» ahora también queda en la bitácora. | Cerrado |
| DEF-25 | `tools/ejecutar_evidencias.sh` fallaba si la ruta del proyecto tenía espacios (por ejemplo «Seminario Lic Info»). | El comando java y el cliente mysql se guardaban en variables de texto y se expandían sin comillas. | `java_sesion()` como función con argumentos citados; `MYSQL`, `DETENER_MYSQL` e `INICIAR_MYSQL` se ejecutan con `eval`; carpeta de salida configurable con `SALIDAS`. Detectado en la puesta en marcha en la MacBook. | Cerrado |
| DEF-26 | Alta de vehículo con cliente inexistente: el rechazo llegaba después de pedir los seis datos del vehículo y el autor interpretó que «el auto no se guardaba». Igual en Nuevo turno, Reprogramar y Actualizar precio. | La vista pedía todos los datos y recién al final el controlador verificaba la existencia. | Validación temprana de cada dato y opción de registrar el cliente en el mismo flujo (precondición de CU001). Ficha `pruebas/defectos/DEF-26.md`. | Cerrado |
| DEF-27 | Los rechazos no aclaraban que no se había guardado nada. | `ejecutar` no distinguía lectura de escritura. | `ejecutarEscritura`: «Resultado: … no se guardó ningún dato.» en las 10 operaciones de escritura. | Cerrado |
| DEF-28 | «La marca es obligatorio» (concordancia). | Mensaje armado como `campo + " es obligatorio."`. | «… es un dato obligatorio.» | Cerrado |
| DEF-29 | Se podía fijar el precio de un servicio en $ 0,00. | `validarPrecio` solo rechazaba negativos. | El precio base debe ser mayor que cero. | Cerrado |
| DEF-30 | Ctrl-D en medio de un alta mostraba «[ERROR INTERNO]» y quedaba en la bitácora. | La red de seguridad capturaba `EntradaFinalizadaException`. | Se relanza antes; cierre ordenado de la sesión. | Cerrado |
| DEF-31 | La vista dependía de la capa de conexión y `Main` usaba `java.sql`. | `InfoConexion` anidado en `ConexionBD`. | `InfoConexion` pasa a `modelo`; `ConexionBD.verificar()`. Detectado por ArchUnit. | Cerrado |
| DEF-32 | En una JVM con idioma turco la patente «abi 123» se convertía en «ABİ123» y el correo perdía el punto de la i. | `toUpperCase()`/`toLowerCase()` sin `Locale` (5 lugares). | `Locale.ROOT`. Detectado por SpotBugs. | Cerrado |
| DEF-33 | La clave generada por un INSERT se leía sin comprobar que la base la hubiera devuelto. | `claves.next()` ignorado en 4 DAO. | `DaoJdbc.claveGenerada`. Detectado por PMD. | Cerrado |
| DEF-34 | Alta de vehículo que registra un cliente nuevo y luego falla: decía «no se guardó ningún dato» aunque el cliente quedó grabado. | Introducido por la corrección de DEF-26. | `resultadoParcial`: informa lo que sí quedó registrado. Detectado por la revisión independiente. | Cerrado |
| DEF-35 | Reprogramar o cancelar un turno no confirmado pedía datos antes de rechazarlo. | Solo se verificaba la existencia del turno. | `rechazarSiNoConfirmado` en la vista. | Cerrado |
| DEF-36 | La base aceptaba precio base 0, que el modelo rechaza desde DEF-29. | CHECK `>= 0` en el DDL. | CHECK `precio_base > 0`. | Cerrado |

## Limitaciones conocidas (no corregidas en esta iteración)

- El mensaje de kilometraje inválido muestra 2000000 sin separador de miles: se conserva el texto exacto de la evidencia E7 del AP2.
- `Vehiculo.setAnio` usa el año del sistema y no el reloj inyectado.
- Quicksort con esquema de Lomuto se degrada con muchas claves repetidas; no incide con los volúmenes del taller.
- La cola de recepción vive en la memoria de cada puesto y no verifica que el turno sea del día ni de la sede del puesto.
- Al rechazarse un INSERT por clave duplicada, InnoDB consume igualmente el valor AUTO_INCREMENT: los números de cliente pueden saltear valores (s10: clientes 5, 6 y 8). Es el comportamiento documentado de MySQL y no afecta la integridad.
- El ingreso estimado de los reportes usa el precio base **vigente**: si se actualiza un precio, cambian también los ingresos estimados de semanas pasadas. Es consecuencia de la 3FN (el turno no guarda precio); el precio pactado se guardará en la orden de trabajo de CU002.
- La importación lee el CSV con `split(";")`, sin interpretar comillas: un campo con punto y coma exportado entre comillas no se reimporta correctamente.
- La importación de clientes registra cada línea por separado (no en una transacción única): es una decisión (D17), no un defecto.
- La regla de superposición existe en `Turno.seSuperponeCon` (probada por CPC01) y en la consulta SQL de `TurnoDAO`; ambas usan el intervalo [inicio, fin) y deben mantenerse alineadas.
