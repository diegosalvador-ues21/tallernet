# DEF-28 — «La marca es obligatorio»: mensaje sin concordancia de género
Severidad: Baja     Tipo: Texto
Detectado por: exploración `exploracion_01`    Fecha: 2026-11-07    Requerimiento afectado: RNF-TN06
## Causa raíz
`Vehiculo.requerido` armaba `campo + " es obligatorio."` para «La marca» y «El modelo».
## Corrección
«La marca es un dato obligatorio.» / «El modelo es un dato obligatorio.»
## Prueba de regresión
`LimitesTest.mensajeDeObligatorio`
## Estado: Verificado
