# DEF-34 — «No se guardó ningún dato» cuando el cliente nuevo sí había quedado registrado
Severidad: Media     Tipo: Usabilidad / información incorrecta al usuario
Detectado por: revisión independiente del código (agente revisor) sobre la corrección de DEF-26/27    Fecha: 2026-11-07
## Cómo reproducirlo
2 › 2 Alta de vehículo; cliente 99 (inexistente) › S › se registran los datos del cliente › patente ya existente (AE123BC).
## Resultado esperado / obtenido
Esperado: informar que el cliente quedó registrado y el vehículo no. Obtenido: «Resultado: el vehículo no se registró; no se guardó ningún dato.», aunque el cliente N.° 9 estaba en la base.
## Causa raíz
Defecto introducido por la corrección de DEF-26: el alta del cliente se confirma (autocommit) dentro del mismo flujo que luego falla, y `ejecutarEscritura` no sabía que una parte ya se había grabado.
## Corrección
`resultadoParcial`: cuando el flujo registró el cliente, el resultado de una falla posterior es «el cliente N.° X quedó registrado, pero el vehículo no se registró».
## Prueba de regresión
`VistaAltasTest.clienteRegistradoYVehiculoRechazado`
## Estado: Verificado
