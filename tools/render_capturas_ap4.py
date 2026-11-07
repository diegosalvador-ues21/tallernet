"""Capturas de las sesiones del AP4 (s09-s13) y de las pruebas, a partir de las salidas reales."""
import re
import render_capturas as rc
from render_capturas import leer, tramo, render, SAL

_render = rc.render

def main():
    global compacto
    # El menú del AP4 tiene opciones nuevas: se agregan al filtro de líneas de menú.
    rc.RUIDO = re.compile(r"^(=+|-+|  MENÚ PRINCIPAL|  TURNOS|  CLIENTES Y VEHÍCULOS|  SEDES Y CATÁLOGO DE SERVICIOS|"
                          r"  REPORTES SEMANALES Y ARCHIVOS|  SISTEMA|\s{1,2}\d{1,2}\. (Turnos|Clientes|Sedes|Reporte|Salir|Nuevo|"
                          r"Reprogramar|Cancelar|Deshacer|Agenda|Buscar|Registrar|Atender|Ver|Volver|Alta|Listar|Historial|"
                          r"Próximo|Sistema|Disponibilidad|Modificar|Actualizar|Exportar|Importar|Probar).*|Elija una opción: \d+|)$")
    compacto = rc.compacto
    _resaltar = rc.resaltar
    rc.resaltar = lambda l: _resaltar(l) or any(k in l for k in (
        "[SIN CONEXIÓN]", "[ARCHIVO]", "actualizado", "Precio actualizado", "Primer horario libre", "exportad",
        "Registrados:", "Conexión correcta", "Cifrado:", "tests successful", "tests failed"))
    c = compacto(leer("s09_actualizacion_excepciones.log"))
    render(tramo(c, "Conexión correcta", "Respuesta:"), "c12_probar_conexion.png", "Sistema: probar la conexión con el servidor central")
    render(tramo(c, "Número de cliente: 1", "No existe el cliente N.° 99"), "c13_update_cliente.png",
           "UPDATE de cliente y excepciones de MySQL (correo duplicado, registro inexistente)")
    t = tramo(c, "Patente: AE123BC", "Precio no válido")
    render([l for l in t if not re.match(r"^\s+\d\. (Diagnóstico|Alineación|Service)", l)], "c14_update_precio.png",
           "INSERT con patente duplicada (1062) y UPDATE del precio de un servicio")
    c = compacto(leer("s10_arreglos_archivos.log"))
    t = tramo(c, "Disponibilidad de Sede", "Primer horario libre")
    render([l for l in t if not re.match(r"^\s+\d\. (Sede|Diagnóstico|Alineación|Cambio|Service)", l)], "c15_grilla_S5.png",
           "Arreglo de franjas: disponibilidad de la sede y sugerencia en el flujo S5")
    render(tramo(c, "Una fecha de la semana", "Días sin turnos"), "c16_reporte_semanal.png",
           "Reporte semanal: arreglos por día y matriz sede × día (carga base)")
    t = tramo(c, "Agenda exportada", "No existe el archivo")
    render([l for l in t if not re.match(r"^\s+\d\. Sede", l)], "c17_archivos.png", "Archivos: exportación CSV/TXT e importación de clientes")
    render(tramo(c, "Últimas operaciones", "ALTA_CLIENTE | Cliente N.° 8"), "c18_bitacora.png", "Bitácora de operaciones (archivo en modo agregar)")
    c = compacto(leer("s11_volumen_arreglos.log"))
    render(tramo(c, "Disponibilidad de Sede", "Días sin turnos"), "c19_volumen_arreglos.png",
           "Volumen: grilla de un día cargado y reporte de una semana con 26 turnos")
    s12 = compacto(leer("s12_servidor_detenido.log"))
    t = [l for l in s12 if "Sede" in l or "SIN CONEXIÓN" in l or l.startswith("Sesión")]
    t += ["", "--- s13: inicio con credenciales inválidas ---"] + leer("s13_credenciales_invalidas.log")
    render(t, "c20_sin_conexion.png", "Servidor detenido en medio de la sesión (s12) y credenciales inválidas (s13)")
    lineas = leer("pruebas_ap4.log")
    def nivel(l):
        m = re.match(r"^((?:│  |   )*)[├└]─ ", l)
        return len(m.group(1)) // 3 if m else -1
    clases, actual, cuenta = [], None, 0
    for i, l in enumerate(lineas):
        n = nivel(l)
        if n == 1:
            if actual:
                clases.append(f"  {actual}: {cuenta} aprobadas")
            actual, cuenta = re.sub(r"^.*?─ | ✔$", "", l), 0
        elif n >= 2 and "✔" in l:
            hoja = i + 1 >= len(lineas) or nivel(lineas[i + 1]) <= n
            cuenta += 1 if hoja else 0
    if actual:
        clases.append(f"  {actual}: {cuenta} aprobadas")
    resumen = [l for l in lineas if re.search(r"tests (found|successful|failed|skipped)", l)]
    render(clases + [""] + resumen, "c21_pruebas.png", "Pruebas automatizadas del AP4 (JUnit 5 + MySQL 8.0)")

if __name__ == "__main__":
    rc.main()
    main()
    print("capturas:", len(list(rc.CAP.glob("*.png"))))
