# Verificación final del AP4

| Criterio de la rúbrica | Veredicto | Evidencia |
|---|---|---|
| C1 Código completo, compila y se ejecuta (10) | CUMPLE | `javac --release 17 -Xlint:all` sin advertencias (`docs/evidencias/salidas/javac_xlint_ap4.log` vacío); 64/64 pruebas (`pruebas_ap4.log`); 13 sesiones (`s01`–`s13`). |
| C2 Patrón seleccionado y justificado | CUMPLE | DAO + método plantilla (`DaoJdbc`), Singleton (`ConexionBD`), fábrica (`Servicio.crear`); `docs/decisiones.md` D11; informe 5.1. |
| C2 Persistencia: conectar, consultar, actualizar, presentar | CUMPLE | `ConexionBD.diagnosticar`; DAO `consultar`; `ClienteDAO.actualizar`, `ServicioDAO.actualizar`; s09. |
| C2 Excepciones con MySQL | CUMPLE | `TraductorSQLException`, `ConexionException`, `RegistroDuplicadoException`; rollback + reintento; CPI04-10/13, CPC10, s12, s13. |
| C2 Clases abstractas o interfaces | CUMPLE | 4 abstractas, 9 interfaces (informe 5.4). |
| C2 Arreglos y ArrayList complementarios | CUMPLE | `AgendaDiaria` (Turno[]), `ReporteSemanal` (int[], BigDecimal[], int[][], String[]); CPC07, CPC08; s10, s11. |
| C2 Archivos (opcional) | CUMPLE | `archivos/*`; CPC09; s10. |
| C3 Documento que explica el código (15) | CUMPLE | Informe Parte V, fragmentos ≤ 12 líneas aprox. con clase/método y justificación; Tabla 22. |
| C4 Formato, video, correcciones (15) | PARCIAL hasta grabar el video | PDF A4, Calibri 11 (Carlito métricamente equivalente en la exportación), simple, portada, índice, APA; tabla de correcciones; guion, subtítulos, diapositivas y demo listos. Falta: grabar y publicar el video, completar URL de GitHub. |
| C5 Participación (15) | Depende del alumno | Kit en `docs/tp4/participacion.md`. |

Revisión independiente del código: todos los ítems de C2 en CUMPLE, sin defectos de severidad alta; DEF-19 a DEF-24 corregidos y verificados.
