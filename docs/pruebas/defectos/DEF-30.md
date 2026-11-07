# DEF-30 — Ctrl-D en medio de un alta mostraba «[ERROR INTERNO]» y lo registraba en la bitácora
Severidad: Baja     Tipo: Robustez
Detectado por: exploración `exploracion_01` y E2E `fin_de_entrada.exp` (CPE2E-02, rojo)    Fecha: 2026-11-07
## Causa raíz
`LectorEntrada.EntradaFinalizadaException` es una RuntimeException y la red de seguridad de DEF-20 en `VistaConsola.ejecutar` la capturaba como error interno.
## Corrección
Se relanza antes de la red de seguridad; `iniciar()` cierra la sesión en forma ordenada.
## Prueba de regresión
E2E `cpe2e_02_fin_entrada_verde.log` (APROBADO).
## Estado: Verificado
