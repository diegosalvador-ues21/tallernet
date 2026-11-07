# DEF-35 — Reprogramar o cancelar un turno no confirmado pedía datos antes de rechazarlo
Severidad: Baja     Tipo: Validación / usabilidad (misma familia que DEF-26)
Detectado por: revisión independiente del código    Fecha: 2026-11-07    Requerimiento afectado: RF-TN06
## Descripción
Con un turno CANCELADO o ATENDIDO, «Reprogramar» pedía la fecha y la hora nuevas y «Cancelar» pedía la confirmación; recién después el controlador lo rechazaba.
## Corrección
`VistaConsola.rechazarSiNoConfirmado`: tras mostrar el turno se verifica su estado y se rechaza de inmediato. El controlador mantiene su propia verificación (defensa en profundidad).
## Prueba de regresión
`VistaAltasTest.turnoNoConfirmadoSeRechazaAntes`
## Estado: Verificado
