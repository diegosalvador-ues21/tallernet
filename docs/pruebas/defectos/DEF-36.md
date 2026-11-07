# DEF-36 — La base admitía precio base 0, que el modelo ya no acepta
Severidad: Media     Tipo: Integridad de datos (base y modelo desalineados)
Detectado por: revisión independiente del código, a partir de la corrección de DEF-29    Fecha: 2026-11-07
## Descripción
Tras DEF-29 el modelo exige precio > 0, pero el CHECK de la tabla seguía en `>= 0`. Una fila con precio 0 cargada por fuera del programa (o grabada antes de DEF-29, como en `exploracion_01`) haría fallar el mapeo del servicio y dejaría inutilizables el catálogo, la agenda y los reportes.
## Corrección
`db/01_ddl_creacion.sql`: `CONSTRAINT ck_servicio_precio CHECK (precio_base > 0)`. Los scripts 02 y 03 no tienen precios en cero.
## Prueba de regresión
`ConsultasContenedorTest.baseRechazaPrecioCero`; además `registroInconsistente` comprueba que un registro inválido cargado por fuera se informa como error de base sin cortar la sesión.
## Estado: Verificado
