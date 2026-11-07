"""Convierte extractos de las salidas de consola (docs/evidencias/salidas) en imágenes para el informe."""
import re, sys
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

RAIZ = Path(__file__).resolve().parent.parent
SAL = RAIZ / "docs/evidencias/salidas"
CAP = RAIZ / "docs/evidencias/capturas"
F = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf", 26)
FB = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSansMono-Bold.ttf", 26)
FT = ImageFont.truetype("/usr/share/fonts/truetype/crosextra/Carlito-Bold.ttf", 28)

RUIDO = re.compile(r"^(=+|-+|  MENÚ PRINCIPAL|  TURNOS|  CLIENTES Y VEHÍCULOS|  SEDES Y CATÁLOGO DE SERVICIOS|  \d\. (Turnos|Clientes|Sedes|Reportes|Salir|Nuevo|Reprogramar|Cancelar|Deshacer|Agenda|Buscar|Registrar|Atender|Ver|Volver|Alta|Listar|Historial|Próximo).*|)$")

def leer(nombre):
    return (SAL / nombre).read_text(encoding="utf-8").splitlines()

def compacto(lineas):
    return [l for l in lineas if not RUIDO.match(l)]

def tramo(lineas, desde, hasta=None, ocurrencia=1):
    idx = [i for i, l in enumerate(lineas) if desde in l][ocurrencia - 1]
    fin = len(lineas)
    if hasta:
        for j in range(idx + 1, len(lineas)):
            if hasta in lineas[j]:
                fin = j + 1
                break
    return lineas[idx:fin]

def resaltar(l):
    return any(k in l for k in ("[S5]", "[!]", "[?]", "CONFIRMADO", "CANCELADO.", "registrado", "Encontrado", "comparaciones", "Atendiendo", "revertida", "reprogramado", "BUILD SUCCESS", "Tests run: 37"))

def envolver(lineas, ancho=98):
    res = []
    for l in lineas:
        while len(l) > ancho:
            corte = l.rfind(" ", 0, ancho)
            corte = corte if corte > 20 else ancho
            res.append(l[:corte])
            l = "      " + l[corte:].lstrip()
        res.append(l)
    return res

def render(lineas, salida, titulo):
    lineas = envolver(lineas)
    ancho = max(len(l) for l in lineas + [titulo]) * 16 + 60
    alto = len(lineas) * 34 + 90
    img = Image.new("RGB", (max(ancho, 900), alto), "#fafafa")
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, img.width, 50], fill="#004d40")
    d.text((22, 10), titulo, font=FT, fill="white")
    y = 66
    for l in lineas:
        color = "#212121"
        fuente = F
        if resaltar(l):
            fuente = FB
            color = "#b71c1c" if any(k in l for k in ("[S5]", "[!]", "[?]")) else "#004d40"
        elif re.search(r": \S", l) and not l.startswith("  "):
            color = "#1565c0"
        d.text((22, y), l, font=fuente, fill=color)
        y += 34
    d.rectangle([0, 0, img.width - 1, img.height - 1], outline="#9e9e9e", width=2)
    img.save(CAP / salida)

def main():
    CAP.mkdir(parents=True, exist_ok=True)
    s1 = leer("s01_cps05_E1_E4.log")
    render(s1[:22], "c01_inicio_menu.png", "Inicio de sesión y menú principal")
    c = compacto(s1)
    render([l for l in tramo(c, "Nuevo turno (CU001)", "Servicio:  Cambio", 1) if not re.match(r"^\s+\d\. .*min\)$", l)],
           "c02_E1_E2.png", "CPS05 E1 (flujo S5) y E2 (alta exitosa)")
    render([l for l in tramo(c, "Nuevo turno (CU001)", "domingos", 3) if not re.match(r"^\s+\d\. .*min\)$", l)],
           "c03_E3_E4.png", "CPS05 E3 (fuera de franja) y E4 (domingo)")
    c = compacto(leer("s02_cps05_E5.log"))
    render([l for l in tramo(c, "Nombre: Paula", "Servicio:  Cambio") if not re.match(r"^\s+\d+\. .*(min\)|\))$", l)],
           "c04_E5.png", "CPS05 E5: alta de clienta, vehículo con kilometraje y turno")
    c = compacto(leer("s03_cps05_E6_E7.log"))
    render([l for l in tramo(c, "Número de turno a cancelar", "2000000 km") if not re.match(r"^  [12]\. Sede", l)], "c05_E6_E7.png", "CPS05 E6 (cancelación y agenda) y E7 (kilometraje inválido)")
    c = compacto(leer("s04_robustez.log"))
    render([l for l in tramo(c, "Elija una opción: abc", "debe ser un número entero") if not l.startswith("  1. Sede") and not l.startswith("  2. Sede")], "c06_robustez.png", "Entrada inválida: el menú no se interrumpe")
    c = compacto(leer("s05_ordenamiento_busqueda.log"))
    render(tramo(c, "Criterio: 1", "Diagnóstico por escáner"), "c07_orden_busqueda.png", "Ordenamiento (quicksort, inserción) y búsqueda binaria")
    c = compacto(leer("s06_cola_pila_reprogramacion.log"))
    t = tramo(c, "Elija una opción: 7", "16:30")
    t = [l for l in t if not re.match(r"^  [12]\. Sede", l) and not l.startswith("  ----") and not l.startswith("  Sede:") and not l.startswith("  Vehículo:") and not l.startswith("  Servicio:") and not l.startswith("  Franja:") and not l.startswith("  Turno N.° 7 - CONF")]
    render(t, "c08_cola_pila.png", "Cola de recepción (FIFO), pila de cancelaciones (LIFO) y reprogramación")
    c = compacto(leer("s07_catalogo_reportes.log"))
    render(tramo(c, "Elija una opción: 2", "Sede Flores"), "c09_catalogo.png", "Catálogo ordenado, polimorfismo del próximo service y reporte")
    c = compacto(leer("s08_volumen.log"))
    t = tramo(c, "Criterio: 1", "Comparaciones sobre 75")
    render(t[:9] + ["  (...)"] + t[-6:], "c10_volumen.png", "Volumen: 54 clientes y 75 vehículos (carga masiva)")
    mvn = (SAL / "mvn_clean_test.log").read_text(encoding="utf-8").splitlines()
    render([re.sub(r", Time elapsed: [0-9.]+ s - in tallernet\.", " | ", l) for l in mvn], "c11_mvn_test.png", "mvn clean test: 37 pruebas sobre MySQL 8.0")
    print("capturas:", len(list(CAP.glob("*.png"))))

if __name__ == "__main__":
    main()
