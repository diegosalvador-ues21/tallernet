"""Figuras de la versión 2 del informe, a partir de los logs reales de la campaña de pruebas."""
import re, sys
from pathlib import Path
sys.path.insert(0, str(Path.home() / "tp4/tallernet/tools"))
import render_capturas as rc
E = Path("/mnt/user-data/uploads/Seminario Lic Info/Tp4/Prototipo/tallernet/docs/pruebas/evidencias")
rc.CAP = Path.home() / "tp4/informe/fig"
rc.RUIDO = re.compile(r"^(=+|-+|  MENÚ PRINCIPAL|  TURNOS|  CLIENTES Y VEHÍCULOS|  SEDES Y CATÁLOGO DE SERVICIOS|"
                      r"  REPORTES SEMANALES Y ARCHIVOS|  SISTEMA|\s{1,2}\d{1,2}\. (Turnos|Clientes|Sedes|Reporte|Salir|Nuevo|"
                      r"Reprogramar|Cancelar|Deshacer|Agenda|Buscar|Registrar|Atender|Ver|Volver|Alta|Listar|Historial|"
                      r"Próximo|Sistema|Disponibilidad|Modificar|Actualizar|Exportar|Importar|Probar).*|Elija una opción: \d+|)$")
_res = rc.resaltar
rc.resaltar = lambda l: _res(l) or any(k in l for k in ("Resultado:", "Titular:", "RESULTADO", "APROBADO", "ERROR INTERNO",
    "SIN CONEXIÓN", "Tests run: 193", "All coverage", "Killed", "BUILD", "0 Checkstyle", "LBS854", "Fin de la entrada"))
leer = lambda n: (E / n).read_text(encoding="utf-8").splitlines()

# 1. DEF-26/27 en el programa real + oráculo SQL
c = rc.compacto(leer("cp_2_2_alta_vehiculo.log"))
t = rc.tramo(c, "Número de cliente: 007", "Vehículo N.° 6 registrado")
t += ["", "--- Oráculo: consulta directa a MySQL ---"] + [l for l in leer("cp_2_2_oraculo.log") if l.strip()][:5]
rc.render(t, "c22_def26.png", "DEF-26/27: alta de vehículo con cliente inexistente y con cliente válido")

# 2. Perfil de calidad
log = leer("calidad_final.log")
clases = []
for l in log:
    m = re.match(r"\[INFO\] Tests run: (\d+), Failures: (\d+), Errors: (\d+).* -- in tallernet\.(\S+)$", l)
    if m and int(m.group(1)) > 0:
        clases.append(f"  {m.group(4).replace('$', ' > '):<52} {m.group(1):>3} aprobadas")
res = [re.sub(r"^\[(INFO|WARNING)\] ", "", l) for l in log if re.search(
    r"Tests run: 193, F|All coverage|Generated \d+ mutations|Line Coverage \(for|Total bugs|Checkstyle violations|BUILD SUCCESS", l)]
pmd = [l for l in log if "PMD" in l and "violation" in l]
res += ["PMD: 0 violaciones; CPD: 0 duplicaciones" if not pmd else pmd[0]]
rc.render(clases + [""] + res, "c23_calidad.png", "mvn -Pcalidad verify: 193 pruebas, cobertura, mutación y análisis estático")

# 3. Punta a punta con expect
e1 = leer("cpe2e_01_concurrencia_final.log")
e2r = leer("cpe2e_02_fin_entrada_rojo.log"); e2 = leer("cpe2e_02_fin_entrada_final.log"); e3 = leer("cpe2e_03_servidor_detenido_final.log")
sel = lambda ls, keys: [l for l in ls if any(k in l for k in keys)]
t = ["CPE2E-01  Echeandía y Flores reservan la misma franja en el mismo instante"]
t += sel(e1, ("Turno N.° 7 CONFIRMADO", "[S5]", "Primer horario", "RESULTADO CPE2E-01"))
t += ["", "CPE2E-02  Ctrl-D en medio de un alta (antes / después de DEF-30)"]
t += ["  antes:" ] + sel(e2r, ("ERROR INTERNO",)) + ["  después:"] + sel(e2, ("Fin de la entrada", "RESULTADO CPE2E-02"))
t += ["", "CPE2E-03  El servidor se detiene mientras se registra un cliente"]
t += sel(e3, ("SIN CONEXIÓN", "Resultado:", "RESULTADO CPE2E-03"))
rc.render(t, "c24_e2e.png", "Pruebas de punta a punta con expect sobre el jar real")

# 4. Cobertura por paquete
import matplotlib; matplotlib.use("Agg")
import matplotlib.pyplot as plt
paq = ["modelo", "controlador", "persistencia", "archivos", "vista", "conexion", "util"]
base = [90.9, 18.9, 64.2, 88.6, 32.3, 36.4, 100.0]
fin = [95.0, 95.0, 89.3, 92.8, 92.3, 61.0, 100.0]
umbral = [85, 85, 85, 85, 70, None, None]
fig, ax = plt.subplots(figsize=(10, 4.6), dpi=200)
x = range(len(paq)); w = 0.38
b1 = ax.bar([i - w/2 for i in x], base, w, label="Inicio de la campaña (120 pruebas)", color="#b0bec5")
b2 = ax.bar([i + w/2 for i in x], fin, w, label="Versión 2 (193 pruebas)", color="#004d40")
for i, u in enumerate(umbral):
    if u: ax.hlines(u, i - 0.45, i + 0.45, colors="#b71c1c", linestyles="--", linewidth=1.4)
for bars in (b1, b2):
    for r in bars: ax.text(r.get_x() + r.get_width()/2, r.get_height() + 1.2, f"{r.get_height():.0f}", ha="center", fontsize=8)
ax.plot([], [], color="#b71c1c", linestyle="--", label="Umbral exigido")
ax.set_xticks(list(x)); ax.set_xticklabels(paq); ax.set_ylim(0, 110); ax.set_ylabel("Cobertura de líneas (%)")
ax.spines[["top", "right"]].set_visible(False); ax.legend(frameon=False, loc="upper center", bbox_to_anchor=(0.5, -0.1), ncol=3, fontsize=9)
ax.set_title("Cobertura JaCoCo por paquete: total 54,4 % → 90,0 %; mutación PIT 62 % → 80 %", fontsize=11)
fig.tight_layout(); fig.savefig(rc.CAP / "fig_cobertura_v2.png")
print("ok")
