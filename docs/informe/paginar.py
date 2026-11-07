# Calcula la página de cada título leyendo el PDF y genera paginas.json para el índice.
import json, re, subprocess, sys
pdf = sys.argv[1]
n = int(re.search(r"Pages:\s+(\d+)", subprocess.run(["pdfinfo", pdf], capture_output=True, text=True).stdout).group(1))
textos = [subprocess.run(["pdftotext", "-f", str(i), "-l", str(i), "-layout", pdf, "-"], capture_output=True, text=True).stdout for i in range(1, n + 1)]
titulos = [l[2:].strip() if l.startswith("# ") else l[3:].strip() for l in open("contenido.md", encoding="utf-8") if l.startswith("# ") or l.startswith("## ")]
res = {}
for t in titulos:
    clave = re.sub(r"\s+", " ", t)
    for i, tx in enumerate(textos):
        if i < 2: continue  # portada e índice
        lineas = [re.sub(r"\s+", " ", l).strip() for l in tx.splitlines()]
        if clave in lineas:
            res[t] = i + 1; break
    else:
        print("sin página:", t)
json.dump(res, open("paginas.json", "w"), ensure_ascii=False, indent=1)
print(len(res), "títulos paginados")
