#!/usr/bin/env python3
"""
TALLERNET - Generador del script 03_insercion_masiva.sql (semilla fija).

Cada ejecución produce exactamente el mismo archivo. Respeta las reglas del CU001:
lunes a sábado, franja 08:00-20:00, sin superposición entre turnos activos de una
misma sede y sin turnos anteriores al alta del vehículo. Excluye las fechas que
usan las pruebas del informe (4, 5, 6 y 10 de octubre de 2026).

Uso:  python3 tools/generar_insercion_masiva.py > db/03_insercion_masiva.sql
"""
import random
from datetime import datetime, date, time, timedelta

rng = random.Random(2026)

# Catálogo adicional (ids 6 a 13). 12 y 13 no dependen del kilometraje (NULL).
SERVICIOS = [
    (6, "Cambio de correa de distribución", "185000.00", 240, 60000),
    (7, "Cambio de pastillas de freno", "65000.00", 90, 30000),
    (8, "Cambio de bujías", "42000.00", 60, 40000),
    (9, "Cambio de filtro de aire", "28000.00", 30, 15000),
    (10, "Cambio de amortiguadores", "160000.00", 180, 80000),
    (11, "Cambio de líquido refrigerante", "35000.00", 60, 40000),
    (12, "Carga de aire acondicionado", "55000.00", 60, None),
    (13, "Revisión pre-VTV", "40000.00", 60, None),
]
# Duraciones del catálogo completo (1-5 provienen del script 02).
DURACION = {1: 60, 2: 60, 3: 180, 4: 90, 5: 30}
DURACION.update({s[0]: s[3] for s in SERVICIOS})

NOMBRES = ["Paula", "Matías", "Rodrigo", "Valentina", "Hernán", "Camila", "Nicolás", "Florencia",
           "Santiago", "Micaela", "Martín", "Rocío", "Julieta", "Maximiliano", "Diego", "Lucía",
           "Gustavo", "Belén", "Claudio", "Emiliano", "Agustina", "Federico", "Carolina", "Pablo",
           "Mariana", "Javier", "Natalia", "Sebastián", "Daniela", "Facundo", "Gabriela", "Tomás",
           "Romina", "Ignacio", "Silvina", "Leandro", "Victoria", "Ezequiel", "Andrea", "Lautaro",
           "Cecilia", "Gonzalo", "Mercedes", "Bruno", "Soledad", "Franco", "Lorena", "Alejandro",
           "Noelia", "Ramiro"]
APELLIDOS = ["Correa", "Olivera", "Godoy", "Ponce", "Aguirre", "Carrizo", "Peralta", "Campos",
             "Campos", "Álvarez", "Luna", "Bustos", "Juárez", "Medina", "Ruiz", "Castro", "Villalba",
             "Méndez", "Bustos", "Peralta", "Ríos", "Romero", "Ortiz", "Sánchez", "Domínguez",
             "Herrera", "Molina", "Suárez", "Torres", "Acuña", "Ledesma", "Navarro", "Figueroa",
             "Cabrera", "Vega", "Rojas", "Paz", "Sosa", "Quiroga", "Giménez", "Benítez", "Vera",
             "Moreno", "Arias", "Coronel", "Maidana", "Ferreyra", "Gómez", "Silva", "Ojeda"]
DOMINIOS = ["gmail.com", "outlook.com", "fibertel.com.ar", "yahoo.com.ar", "hotmail.com"]

MODELOS = {
    "Toyota": ["Corolla", "Etios", "Hilux", "Yaris", "Corolla Cross"],
    "Chevrolet": ["Onix", "Cruze", "Prisma", "Tracker", "S10"],
    "Ford": ["Ka", "Fiesta", "Focus", "Ranger", "EcoSport"],
    "Volkswagen": ["Gol Trend", "Polo", "Vento", "Amarok", "T-Cross"],
    "Renault": ["Sandero", "Logan", "Kangoo", "Duster", "Clio"],
    "Fiat": ["Cronos", "Palio", "Argo", "Strada"],
    "Peugeot": ["208", "308", "2008", "Partner"],
    "Nissan": ["Versa", "March", "Frontier", "Kicks"],
    "Jeep": ["Renegade", "Compass"],
}
MARCAS = list(MODELOS)

LETRAS = "ABCDEFGHJKLMNPRSTUVWXYZ"


def patente(anio, usadas):
    while True:
        if anio >= 2016:
            p = "A" + rng.choice("ABCDEFG") + f"{rng.randint(0, 999):03d}" + rng.choice(LETRAS) + rng.choice(LETRAS)
        else:
            p = "".join(rng.choice(LETRAS) for _ in range(3)) + f"{rng.randint(0, 999):03d}"
        if p not in usadas:
            usadas.add(p)
            return p


def sql_str(v):
    return "NULL" if v is None else "'" + str(v).replace("'", "''") + "'"


def main():
    out = []
    out.append("-- TALLERNET - Script 03: inserción masiva de datos (DML) - versión 4 (AP3)")
    out.append("-- Generado por tools/generar_insercion_masiva.py con semilla fija (2026).")
    out.append("-- Ejecutar después de 01_ddl_creacion.sql y 02_insercion.sql, sobre una base recién creada.")
    out.append("-- Agrega 8 servicios al catálogo, 50 clientes nuevos, 70 vehículos y 133 turnos.")
    out.append("-- Los turnos respetan las reglas del CU001: lunes a sábado, dentro de la")
    out.append("-- franja 08:00-20:00 de cada sede y sin superposición entre turnos activos.")
    out.append("USE tallernet;")
    out.append("SET NAMES utf8mb4;")
    out.append("START TRANSACTION;")
    out.append("")
    out.append("-- Servicios adicionales del catálogo (km_servicio = intervalo recomendado)")
    out.append("INSERT INTO servicio (servicio_id, descripcion, precio_base, duracion_min, km_servicio) VALUES")
    out.append(",\n".join(f"({s[0]}, {sql_str(s[1])}, {s[2]}, {s[3]}, {'NULL' if s[4] is None else s[4]})"
                          for s in SERVICIOS) + ";")

    # Clientes 5..54
    clientes = []
    emails = set()
    for i in range(50):
        cid = 5 + i
        nombre, apellido = NOMBRES[i], APELLIDOS[i]
        alta = datetime(2026, 7, 1, 9, 0) + timedelta(days=rng.randint(0, 85), minutes=rng.randint(0, 600))
        tel = f"11-{rng.randint(2000, 6999)}-{rng.randint(1000, 9999)}"
        email = None
        if rng.random() < 0.75:
            base = (nombre[0] + apellido).lower()
            for a, b in zip("áéíóúñ", "aeioun"):
                base = base.replace(a, b)
            email = f"{base}{rng.randint(10, 99)}@{rng.choice(DOMINIOS)}"
            if email in emails:
                email = None
            else:
                emails.add(email)
        clientes.append([cid, nombre, apellido, tel, email, alta])
    # Primeras filas idénticas al extracto publicado en el AP2
    clientes[0][3:6] = ["11-2840-4658", None, datetime(2026, 9, 9, 18, 29)]
    clientes[1][3:6] = ["11-5977-8645", "molivera64@outlook.com", datetime(2026, 9, 5, 9, 34)]
    clientes[2][3:6] = ["11-6159-7750", "rgodoy20@fibertel.com.ar", datetime(2026, 8, 13, 15, 25)]

    out.append("")
    out.append("-- 50 clientes nuevos")
    out.append("INSERT INTO cliente (cliente_id, nombre, apellido, telefono, email, fecha_alta) VALUES")
    out.append(",\n".join(f"({c[0]}, {sql_str(c[1])}, {sql_str(c[2])}, {sql_str(c[3])}, {sql_str(c[4])}, "
                          f"'{c[5]:%Y-%m-%d %H:%M:00}')" for c in clientes) + ";")

    # Distribución de vehículos: 10 clientes con dos y 5 con tres (70 en total)
    indices = list(range(3, 50))
    rng.shuffle(indices)
    cantidad = {i: 1 for i in range(50)}
    cantidad[2] = 3  # Rodrigo Godoy, como en el AP2
    for i in indices[:4]:
        cantidad[i] = 3
    for i in indices[4:14]:
        cantidad[i] = 2

    usadas = {"AE123BC", "AC456DE", "OKL482", "AF789GH", "AD321JK"}
    vehiculos = []
    vid = 6
    for i, c in enumerate(clientes):
        for k in range(cantidad[i]):
            marca = rng.choice(MARCAS)
            modelo = rng.choice(MODELOS[marca])
            anio = rng.randint(2006, 2026)
            km = max(0, (2026 - anio) * rng.randint(9000, 18000) + rng.randint(0, 900))
            km = round(km, -2)
            alta = c[5] + timedelta(minutes=5 * k)
            vehiculos.append([vid, c[0], patente(anio, usadas), marca, modelo, anio, km, alta])
            vid += 1
    vehiculos[0][2:8] = ["AA294DR", "Jeep", "Renegade", 2026, 1300, datetime(2026, 9, 9, 18, 29)]
    vehiculos[1][2:8] = ["AF733TJ", "Chevrolet", "Onix", 2022, 49600, datetime(2026, 9, 5, 9, 34)]
    vehiculos[2][2:8] = ["AG355XM", "Toyota", "Corolla", 2020, 62100, datetime(2026, 8, 13, 15, 25)]

    out.append("")
    out.append("-- Vehículos de los clientes nuevos (algunos clientes tienen más de uno)")
    out.append("INSERT INTO vehiculo (vehiculo_id, cliente_id, patente, marca, modelo, anio, km_alta, fecha_alta) VALUES")
    out.append(",\n".join(f"({v[0]}, {v[1]}, '{v[2]}', {sql_str(v[3])}, {sql_str(v[4])}, {v[5]}, {v[6]}, "
                          f"'{v[7]:%Y-%m-%d %H:%M:00}')" for v in vehiculos) + ";")

    # Turnos: ocupación por sede (incluye los activos de la carga base)
    ocupado = {1: [], 2: []}

    def libre(sede, ini, fin):
        return all(not (a < fin and b > ini) for a, b in ocupado[sede])

    for sede, fh, dur in [(1, datetime(2026, 10, 5, 9), 60), (1, datetime(2026, 10, 5, 10), 180),
                          (2, datetime(2026, 10, 5, 9), 90), (2, datetime(2026, 10, 5, 14), 60),
                          (2, datetime(2026, 9, 21, 11), 30)]:
        ocupado[sede].append((fh, fh + timedelta(minutes=dur)))

    excluidas = {date(2026, 10, 4), date(2026, 10, 5), date(2026, 10, 6), date(2026, 10, 10)}
    servicios_ids = list(DURACION)
    turnos = []
    estados = ["ATENDIDO"] * 40 + ["CANCELADO"] * 11 + ["CONFIRMADO"] * 82
    rng.shuffle(estados)
    for estado in estados:
        while True:
            v = rng.choice(vehiculos)
            sid = rng.choice(servicios_ids)
            dur = DURACION[sid]
            if estado == "ATENDIDO":
                d = date(2026, 8, 3) + timedelta(days=rng.randint(0, 57))
            else:
                d = date(2026, 10, 7) + timedelta(days=rng.randint(0, 24))
            if d.weekday() == 6 or d in excluidas:
                continue
            slot = rng.randint(0, 23)  # medias horas desde las 08:00
            ini = datetime.combine(d, time(8, 0)) + timedelta(minutes=30 * slot)
            fin = ini + timedelta(minutes=dur)
            if fin > datetime.combine(d, time(20, 0)) or ini < v[7]:
                continue
            sede = rng.choice([1, 2])
            if estado != "CANCELADO" and not libre(sede, ini, fin):
                continue
            if estado != "CANCELADO":
                ocupado[sede].append((ini, fin))
            emp = 2 if sede == 1 else 3
            turnos.append((sede, v[0], sid, emp, ini, estado))
            break
    turnos.sort(key=lambda t: t[4])

    out.append("")
    out.append("-- Turnos: historial atendido (agosto-septiembre) y agenda de octubre")
    out.append("INSERT INTO turno (sede_id, vehiculo_id, servicio_id, empleado_id, fecha_hora, estado) VALUES")
    out.append(",\n".join(f"({t[0]}, {t[1]}, {t[2]}, {t[3]}, '{t[4]:%Y-%m-%d %H:%M:00}', '{t[5]}')"
                          for t in turnos) + ";")
    out.append("COMMIT;")
    out.append("")
    out.append("-- Verificación de la carga total (base + masiva)")
    out.append("""SELECT 'sede' AS tabla, COUNT(*) AS registros FROM sede
UNION ALL SELECT 'cliente', COUNT(*) FROM cliente
UNION ALL SELECT 'vehiculo', COUNT(*) FROM vehiculo
UNION ALL SELECT 'servicio', COUNT(*) FROM servicio
UNION ALL SELECT 'empleado', COUNT(*) FROM empleado
UNION ALL SELECT 'turno', COUNT(*) FROM turno;""")
    out.append("")
    out.append("-- Verificación de reglas de negocio: los cuatro controles deben devolver 0")
    out.append("""SELECT
  (SELECT COUNT(*) FROM turno a
     JOIN servicio sa ON sa.servicio_id = a.servicio_id
     JOIN turno b ON b.sede_id = a.sede_id AND b.turno_id > a.turno_id
     JOIN servicio sb ON sb.servicio_id = b.servicio_id
   WHERE a.estado <> 'CANCELADO' AND b.estado <> 'CANCELADO'
     AND a.fecha_hora < DATE_ADD(b.fecha_hora, INTERVAL sb.duracion_min MINUTE)
     AND DATE_ADD(a.fecha_hora, INTERVAL sa.duracion_min MINUTE) > b.fecha_hora) AS superposiciones,
  (SELECT COUNT(*) FROM turno WHERE DAYOFWEEK(fecha_hora) = 1) AS turnos_en_domingo,
  (SELECT COUNT(*) FROM turno t JOIN servicio s ON s.servicio_id = t.servicio_id
                                JOIN sede se ON se.sede_id = t.sede_id
   WHERE TIME(t.fecha_hora) < se.hora_apertura
      OR TIME(DATE_ADD(t.fecha_hora, INTERVAL s.duracion_min MINUTE)) > se.hora_cierre
      OR DATE(DATE_ADD(t.fecha_hora, INTERVAL s.duracion_min MINUTE)) <> DATE(t.fecha_hora)) AS fuera_de_franja,
  (SELECT COUNT(*) FROM turno t JOIN vehiculo v ON v.vehiculo_id = t.vehiculo_id
   WHERE t.fecha_hora < v.fecha_alta) AS turno_antes_del_alta;""")
    print("\n".join(out))


if __name__ == "__main__":
    main()
