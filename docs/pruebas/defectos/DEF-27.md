# DEF-27 — Los rechazos no aclaraban que la operación no se había guardado
Severidad: Media     Tipo: Usabilidad
Detectado por: análisis del caso de DEF-26    Fecha: 2026-11-07    Requerimiento afectado: RNF-TN06
## Cómo reproducirlo
Cualquier alta o modificación rechazada: solo se veía `[?] …` o `[!] …`.
## Resultado esperado / obtenido
Esperado: además del motivo, una línea inequívoca de que no se guardó nada. Obtenido: solo el motivo.
## Causa raíz
`VistaConsola.ejecutar` informaba la excepción sin distinguir operaciones de lectura y de escritura.
## Corrección
`ejecutar` devuelve si la operación terminó bien y `ejecutarEscritura(queNoOcurrio, accion)` agrega «Resultado: … no se registró; no se guardó ningún dato.» en las 10 operaciones que modifican datos. El flujo S5 se informa igual (excepción interna `OperacionRechazada`).
## Prueba de regresión
`VistaAltasTest`; E2E `cpe2e_01_concurrencia` y `cpe2e_03_servidor_detenido`.
## Estado: Verificado (cambia la salida de las sesiones de evidencia: se agrega una línea en cada rechazo; ver D5 del plan)
