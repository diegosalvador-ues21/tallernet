# Iteración 0 — Inventario de contexto y análisis de brechas (AP3)

| Archivo | Hechos clave | Restricciones | Inconsistencias detectadas |
|---|---|---|---|
| Salvador Diego-AP1-FINAL | Talleres Ruiz Díaz, dos sedes en CABA; 22 RF y 7 RNF; actores; CU001–CU003 | Java + MySQL (RNF-TN01/02), MVC (RNF-TN03), horario 8–20 h lunes a sábado (RNF-TN04) | Nombre "TALLER-NET" en la introducción; actor "Empleado" usado en CU003 sin definir (corregido en AP2) |
| SALVADOR_DIEGO_AP2-v3 | Diseño de CU001: clases, MVC + DAO + Singleton, 6 tablas, scripts 01–05, CPC01/CPF02/CPC03/CPI04/CPS05, DEF-01/02 | JDK 17, NetBeans 21 + Maven, MySQL 8.0, Connector/J 8.4, JUnit 5, TLS (RNF-TN09) | Erratas "Qquery1/Qquery7", "en una misma." (falta "sede"), "generaron"; índice desalineado; mezcla de 1.ª persona e impersonal; CPS05-E5 "Cliente N.° 5" depende de cargar solo los scripts 01+02 |
| Consigna TP3 | Entregables: explicación, presentación, programa que compila y corre; lista de características POO | PDF A4, Calibri 11, interlineado simple, portada, enlace a GitHub, APA | Nombre de archivo indicado como «APELLIDO-NOMBRE»-AP1.PDF (se usa AP3) |
| Lectura3 | Objeto, clase, constructor, herencia, encapsulamiento (tabla de modificadores), abstracción, polimorfismo; caso ECOViento | Vocabulario del informe | — |
| GestionCentralEolica.java / TurbinaExcepcion.java | Menú while + switch; excepción propia | Patrón de estilo | `main throws`, caracteres corruptos, `nextInt()` sin control, lógica en el menú |
| L1.md (×4) | Vacíos | — | Sin contenido |

## Brechas entre AP2 y AP3

| Requisito AP3 | Estado al cierre del AP2 | Acción en AP3 |
|---|---|---|
| Código Java completo que compila y corre | Prototipo descripto, código no incluido en este repositorio | Proyecto Maven completo en `src/` |
| Abstracción | `Persona` abstracta en el diagrama | `Persona`, `Servicio`, `DaoJdbc` abstractas; interfaces `Dao<T,K>`, `TurnoRepositorio`, `VehiculoRepositorio` |
| Herencia | Cliente/Empleado → Persona; excepciones | Se agregan `ServicioPorKilometraje`/`ServicioGeneral` y DAOs → `DaoJdbc` |
| Polimorfismo | No explicitado | `calcularProximoServiceKm`, `getDescripcionRol`, DAOs por interfaz |
| Menú de selección | Consola sin detalle | Menú principal y tres submenús con `do-while` + `switch` |
| Ordenamiento y búsqueda | No contemplado | Inserción, quicksort, búsqueda lineal y binaria propias |
| Estructuras de datos | Listas | Cola de recepción (`Queue`) y pila de cancelaciones (`Deque`) |
