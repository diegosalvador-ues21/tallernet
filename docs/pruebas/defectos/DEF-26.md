# DEF-26 — El cliente inexistente se detectaba después de pedir todos los datos del vehículo
Severidad: Media     Tipo: Validación / usabilidad
Detectado por: reporte del autor («al ingresar un auto, el registro no queda guardado») y prueba CPV01    Fecha: 2026-11-07    Requerimiento afectado: RF-TN03 (precondición de CU001)
## Cómo reproducirlo
Clientes y vehículos › 2 Alta de vehículo; número de cliente `007` (inexistente: la carga base tiene los clientes 1 a 4); completar patente LBS854, Renault, Fluence 2.0 CVT Privilege, 2013, 195555.
## Resultado esperado / obtenido
Esperado: rechazo inmediato al ingresar el número de cliente, con la posibilidad de registrarlo. Obtenido: el sistema pedía los seis datos y al final mostraba `[?] No existe el cliente N.° 7.`; el autor interpretó que el vehículo «no se guardaba».
## Causa raíz
`VistaConsola.altaVehiculo()` leía todos los datos y recién después `ClienteControlador.registrarVehiculo` verificaba el cliente. La misma secuencia existía en Nuevo turno (sede, patente, servicio), Reprogramar (turno) y Actualizar precio (servicio).
## Corrección
La vista verifica cada dato al ingresarlo: titular (`ClienteControlador.buscarCliente`), sede, vehículo y servicio (`TurnoControlador.verificarSede/verificarVehiculo/verificarServicio`), turno (`buscarTurno`) y servicio (`CatalogoControlador.buscarServicio`). Si el cliente no existe, se ofrece registrarlo en el mismo flujo (precondición de CU001 del AP1). Commit 82b556d.
## Prueba de regresión
`VistaAltasTest` (4 casos, Mockito: nunca se llama a `registrarVehiculo` con cliente inexistente), `ClienteControladorTest.clienteInexistenteNoInsertaNiRegistra`, sesión `evidencias/cp_2_2_alta_vehiculo` con oráculo SQL (`LBS854` guardado con 195555 km).
## Estado: Verificado
