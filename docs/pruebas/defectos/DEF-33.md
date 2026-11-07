# DEF-33 — Lectura de la clave generada sin comprobar que la base la devolvió
Severidad: Baja     Tipo: Robustez de persistencia
Detectado por: PMD `CheckResultSet` (4 ocurrencias) en `calidad_iter1.log`    Fecha: 2026-11-07
## Descripción
`ClienteDAO`, `VehiculoDAO`, `ServicioDAO` y `TurnoDAO` hacían `claves.next();` e ignoraban el resultado antes de `claves.getInt(1)`. Si el driver no devolviera la clave, el error sería un `SQLException` genérico («Illegal operation on empty result set») en lugar de un mensaje propio, y en `TurnoDAO` ocurriría con la transacción abierta.
## Corrección
Método `DaoJdbc.claveGenerada(ResultSet)`: si no hay fila lanza `SQLException("La base no devolvió el número generado…")`, que el traductor convierte en `PersistenciaException`; los cuatro DAO lo usan.
## Prueba de regresión
`AltasContenedorTest`, `ConsultasContenedorTest.cu001DePuntaAPunta`, `RecorridoContenedorTest` (cada alta verifica el número devuelto contra el oráculo SQL).
## Estado: Verificado
