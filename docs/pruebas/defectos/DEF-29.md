# DEF-29 — Se podía actualizar el precio de un servicio a $ 0,00
Severidad: Media     Tipo: Validación
Detectado por: exploración `exploracion_01` (3 › 5, servicio 1, precio «0»)    Fecha: 2026-11-07    Requerimiento afectado: RF-TN17
## Resultado obtenido
«Precio actualizado: Cambio de aceite y filtros -> $ 0,00», persistido en la base (oráculo `exploracion_01_oraculo.log`).
## Causa raíz
`Servicio.validarPrecio` solo rechazaba negativos; el CHECK de la base admite 0.
## Corrección
El precio base debe ser mayor que cero («El precio base debe ser mayor que cero.»).
## Prueba de regresión
`LimitesTest.precioCero`
## Estado: Verificado
