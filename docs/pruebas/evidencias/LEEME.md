# Evidencias de la campaña de pruebas (versión 2)

Salidas reales de las ejecuciones de la campaña, guardadas sin editar salvo por un cambio: las rutas del equipo del autor se reemplazaron por marcadores.

| Marcador | Significado |
|---|---|
| `<proyecto>` | Carpeta del proyecto (`tallernet/`) |
| `<prototipo>` | Carpeta que contiene el proyecto y los scripts del entorno de verificación |
| `<copia-limpia>` | Copia del ZIP descomprimida en una carpeta vacía para verificarlo |
| `<home>` | Carpeta personal del autor |

**Entorno de verificación.** La campaña se ejecutó con MySQL 8.0.46 en Docker publicado en `127.0.0.1:3307`, porque el puerto 3306 del equipo estaba ocupado por otro servidor (hallazgo H-28). Por eso varias salidas muestran `127.0.0.1:3307/tallernet`. La configuración entregada (`db.properties.example`, `src/test/resources/db-test.properties`) usa el puerto estándar 3306.
