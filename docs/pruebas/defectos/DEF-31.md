# DEF-31 — Dependencias que violaban la arquitectura MVC/DAO
Severidad: Baja     Tipo: Diseño
Detectado por: ArchUnit (`ArquitecturaTest.capas` y `sqlSoloEnPersistencia`)    Fecha: 2026-11-07
## Descripción
1. `VistaConsola.probarConexion` usaba `ConexionBD.InfoConexion` (la vista dependía de la capa de conexión).
2. `Main.verificarConexion` usaba `java.sql.Connection` fuera de la persistencia.
## Corrección
`InfoConexion` pasa a `tallernet.modelo`; la verificación inicial pasa a `ConexionBD.verificar()`.
## Prueba de regresión
`ArquitecturaTest` (7 reglas en verde).
## Estado: Verificado
