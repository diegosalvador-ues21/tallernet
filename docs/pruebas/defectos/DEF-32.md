# DEF-32 — Mayúsculas y minúsculas dependientes de la configuración regional de la JVM
Severidad: Media     Tipo: Internacionalización / integridad de datos
Detectado por: SpotBugs `DM_CONVERT_CASE` (5 ocurrencias) en `calidad_iter1.log`    Fecha: 2026-11-07    Requerimiento afectado: RF-TN03 (patente única), RNF-TN06
## Cómo reproducirlo
Ejecutar la aplicación con `-Duser.language=tr -Duser.country=TR` (o en un equipo con Windows en turco) y dar de alta la patente `abi 123`.
## Resultado esperado / obtenido
Esperado: `ABI123`. Obtenido: `ABİ123` (I con punto, U+0130): la validación de formato la rechaza; un correo `INFO@TALLER.COM` se guardaba como `ınfo@taller.com` (ı sin punto).
## Causa raíz
`toUpperCase()`/`toLowerCase()` sin `Locale` usan el idioma por omisión de la JVM. Afectaba `Vehiculo.normalizarPatente`, `Cliente` (correo), `AgendaCsv` (nombre de archivo), `ImportadorClientesCsv` (encabezado) y `LectorEntrada.confirmar` (S/N).
## Corrección
`Locale.ROOT` en las cinco conversiones.
## Prueba de regresión
`RobustezTest.patenteEnTurco`, `RobustezTest.correoEnTurco` (fijan `tr-TR` como idioma por omisión y lo restauran).
## Estado: Verificado
