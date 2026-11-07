"""Genera los diagramas nuevos del informe AP4 con Graphviz (fuente editable: archivos .dot)."""
import subprocess

VERDE = "#004d40"; GRIS = "#212121"; BORDE = "#9e9e9e"; FONDO = "#e0f2f1"; ABS = "#fff8e1"

def clase(nombre, estereotipo, atributos, metodos, abstracta=False, interfaz=False):
    fondo = ABS if (abstracta or interfaz) else "white"
    titulo = f'<I>{nombre}</I>' if abstracta or interfaz else f'<B>{nombre}</B>'
    est = f'<FONT POINT-SIZE="9">«{estereotipo}»</FONT><BR/>' if estereotipo else ''
    filas = f'<TR><TD BGCOLOR="{VERDE}"><FONT COLOR="white">{est}{titulo}</FONT></TD></TR>'
    if atributos is not None:
        filas += '<TR><TD ALIGN="LEFT" BALIGN="LEFT">' + ('<BR/>'.join(atributos) or ' ') + '</TD></TR>'
    filas += '<TR><TD ALIGN="LEFT" BALIGN="LEFT">' + ('<BR/>'.join(metodos) or ' ') + '</TD></TR>'
    return f'<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4" BGCOLOR="{fondo}" COLOR="{BORDE}">{filas}</TABLE>>'

def esc(s):
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

def nodo(id_, *args, **kw):
    return f'  {id_} [label={clase(*args, **kw)}];\n'

CAB = f'''digraph G {{
  graph [fontname="Carlito", fontsize=11, rankdir=TB, nodesep=0.35, ranksep=0.55, bgcolor="white", pad=0.2];
  node [shape=plaintext, fontname="Carlito", fontsize=10, fontcolor="{GRIS}"];
  edge [fontname="Carlito", fontsize=9, color="#616161", fontcolor="#616161"];
'''
HER = 'arrowhead=empty'
IMP = 'arrowhead=empty, style=dashed'

def render(nombre, cuerpo, rankdir="TB"):
    with open(nombre + ".dot", "w", encoding="utf-8") as f:
        f.write(CAB.replace("rankdir=TB", "rankdir=" + rankdir) + cuerpo + "}\n")
    subprocess.run(["dot", "-Tpng", "-Gdpi=170", nombre + ".dot", "-o", nombre + ".png"], check=True)



# ---------------------------------------------------------------- Paquetes (AP4)
p = """  node [shape=tab, style=filled, fillcolor="#e0f2f1", color="#004d40", fontcolor="#212121", fontsize=11];
  vista [label="vista\\nVistaConsola, LectorEntrada"];
  controlador [label="controlador\\nTurnoControlador, ValidadorTurno, SesionControlador, ClienteControlador,\\nCatalogoControlador, ArchivoControlador, SistemaControlador"];
  persistencia [label="persistencia\\nDao<T,K>, repositorios, DaoJdbc (plantilla),\\nTraductorSQLException, DAOs"];
  archivos [label="archivos (AP4)\\nExportador<T>, ExportadorCsv<T>, AgendaCsv,\\nReporteSemanalTxt, ImportadorClientesCsv, Bitacora", fillcolor="#fff8e1"];
  conexion [label="conexion\\nConexionBD (Singleton)"];
  modelo [label="modelo\\nPersona, Cliente, Empleado, Sede, Vehiculo,\\nServicio (+2), Turno, AgendaDiaria, ReporteSemanal"];
  excepciones [label="excepciones\\nTallerNetException y 8 subclases"];
  util [label="util\\nOrdenamiento, Busqueda, Formatos"];
  db [shape=cylinder, fillcolor="white", label="MySQL 8.0\\nbase tallernet"];
  fs [shape=folder, fillcolor="white", label="Archivos\\nexportaciones/*.csv, *.txt\\nlogs/tallernet.log"];
  vista -> controlador; controlador -> persistencia; controlador -> archivos; persistencia -> conexion;
  conexion -> db [label=" JDBC / TLS"]; archivos -> fs [label=" java.nio.file"];
  controlador -> modelo [style=dashed]; persistencia -> modelo [style=dashed]; archivos -> modelo [style=dashed];
  controlador -> util [style=dashed]; vista -> excepciones [style=dashed]; persistencia -> excepciones [style=dashed];
  archivos -> excepciones [style=dashed];
  {rank=same; modelo; util; excepciones}
"""
render("fig_paquetes_ap4", p)

# ---------------------------------------------------------------- Patrón DAO + Template Method + Singleton
d = ""
d += nodo("Ctrl", "ClienteControlador", "control", None,
          ["- clientes: Dao&lt;Cliente, Integer&gt;", "+ actualizarContacto(id, tel, email): Cliente", "+ registrarCliente(...): Cliente"])
d += nodo("Dao", "Dao&lt;T, K&gt;", "interface", None,
          ["+ insertar(T): K", "+ listar(): List&lt;T&gt;", "+ buscarPorId(K): Optional&lt;T&gt;", "+ <I>default</I> actualizar(T): void"], interfaz=True)
d += nodo("TR", "TurnoRepositorio", "interface", None,
          ["+ insertarSiDisponible(Turno): int", "+ cambiarEstado(id, esperado, nuevo)", "+ listarAgenda(sede, fecha)", "+ listarEntre(desde, hasta)"], interfaz=True)
d += nodo("VR", "VehiculoRepositorio", "interface", None, ["+ buscarPorPatente(String)", "+ listarOrdenadoPorPatente()"], interfaz=True)
d += nodo("DJ", "DaoJdbc", "abstract", None,
          ["# abrir(): Connection", "# consultar(accion, sql, ParametrosSentencia, MapeadorFila&lt;T&gt;): List&lt;T&gt;",
           "# consultarUno(...): Optional&lt;T&gt;", "# actualizar(accion, sql, ParametrosSentencia, dup): int",
           "# <U>mapearSede / mapearCliente / mapearVehiculo / mapearServicio</U>"], abstracta=True)
d += nodo("MF", "MapeadorFila&lt;T&gt;", "interface", None, ["+ mapear(ResultSet): T"], interfaz=True)
d += nodo("PS", "ParametrosSentencia", "interface", None, ["+ asignar(PreparedStatement)"], interfaz=True)
d += nodo("CD", "ClienteDAO", "", None, ["+ listar() / buscarPorId()", "+ actualizar(Cliente)"])
d += nodo("SD", "ServicioDAO", "", None, ["+ actualizar(Servicio)"])
d += nodo("TD", "TurnoDAO", "", None, ["+ insertarSiDisponible(Turno)", "- insertarUnaVez(Turno)", "- <U>mapearTurno(ResultSet)</U>"])
d += nodo("VD", "VehiculoDAO", "", None, ["+ buscarPorPatente(String)"])
d += nodo("OD", "SedeDAO, EmpleadoDAO", "", None, ["(heredan actualizar por defecto)"])
d += nodo("CBD", "ConexionBD", "singleton", ["- <U>instancia: ConexionBD</U>", "- url, usuario, clave"],
          ["+ <U>getInstancia(): ConexionBD</U>", "+ getConexion(): Connection", "+ diagnosticar(): InfoConexion"])
d += nodo("TSE", "TraductorSQLException", "utility", None,
          ["+ <U>traducir(accion, SQLException, dup): TallerNetException</U>", "+ <U>tecnica(accion, SQLException)</U>",
           "+ <U>esDeConexion(e)</U> / <U>esReintentable(e)</U>"])
d += "  Ctrl -> Dao [arrowhead=open, label=\" usa\"];\n"
d += f"  TR -> Dao [{HER}]; VR -> Dao [{HER}];\n"
d += f"  CD -> DJ [{HER}]; SD -> DJ [{HER}]; TD -> DJ [{HER}]; VD -> DJ [{HER}]; OD -> DJ [{HER}];\n"
d += f"  CD -> Dao [{IMP}]; SD -> Dao [{IMP}]; OD -> Dao [{IMP}]; TD -> TR [{IMP}]; VD -> VR [{IMP}];\n"
d += "  DJ -> MF [arrowhead=open, style=dashed, label=\" paso variable\"]; DJ -> PS [arrowhead=open, style=dashed];\n"
d += "  DJ -> CBD [arrowhead=open, label=\" abrir()\"]; DJ -> TSE [arrowhead=open, style=dashed, label=\" traduce errores\"];\n"
d += "  {rank=same; TR; VR; Dao}\n  {rank=same; CD; SD; TD; VD; OD}\n"
render("fig_patron_dao", d)

# ---------------------------------------------------------------- Arreglos, ArrayList y archivos
a = ""
a += nodo("AD", "AgendaDiaria", "entity", ["+ <U>MINUTOS_FRANJA = 30</U>", "- sede: Sede", "- fecha: LocalDate", "- <B>franjas: Turno[ ]</B>"],
          ["+ AgendaDiaria(Sede, LocalDate, List&lt;Turno&gt;)", "+ primerHuecoLibre(min, desde): Optional&lt;LocalTime&gt;",
           "+ horariosDisponibles(min): List&lt;LocalTime&gt;", "+ contarOcupadas(): int", "+ getFranjas(): Turno[ ]  (copia)"])
a += nodo("RS", "ReporteSemanal", "entity", ["+ <U>DIAS: String[ ]</U>", "- <B>turnosPorDia: int[6]</B>", "- <B>minutosPorDia: int[6]</B>",
          "- <B>ingresoPorDia: BigDecimal[6]</B>", "- <B>turnosPorSedeYDia: int[ ][ ]</B>"],
          ["+ ReporteSemanal(LocalDate, List&lt;Sede&gt;, List&lt;Turno&gt;)", "+ getDiaDeMayorCarga(): int", "+ diasSinTurnos(): List&lt;String&gt;",
           "+ getTurnosPorDia(): int[ ]  (copia)"])
a += nodo("EX", "Exportador&lt;T&gt;", "interface", None, ["+ nombreArchivo(T): String", "+ exportar(T, Path): Path"], interfaz=True)
a += nodo("EC", "ExportadorCsv&lt;T&gt;", "abstract", None,
          ["+ <I>final</I> exportar(T, Path): Path", "# <I>encabezado(): String[ ]</I>", "# <I>filas(T): List&lt;String[ ]&gt;</I>"], abstracta=True)
a += nodo("AC", "AgendaCsv", "", None, ["# encabezado(), filas(AgendaDiaria)"])
a += nodo("RT", "ReporteSemanalTxt", "", None, ["+ exportar(ReporteSemanal, Path)"])
a += nodo("IC", "ImportadorClientesCsv", "", None, ["+ importar(Path): Resultado", "«interface» RegistroCliente"])
a += nodo("RO", "RegistroOperaciones", "interface", None, ["+ registrar(operacion, detalle)", "+ <U>NINGUNO</U>"], interfaz=True)
a += nodo("BI", "Bitacora", "", ["- archivo: Path"], ["+ registrar(...)", "+ ultimas(n): List&lt;String&gt;  (búfer String[ ])"])
a += nodo("ACt", "ArchivoControlador", "control", None, ["+ exportarAgenda(sede, fecha): Path", "+ exportarReporteSemanal(fecha): Path",
          "+ importarClientes(ruta): Resultado"])
a += f"  EC -> EX [{IMP}]; AC -> EC [{HER}]; RT -> EX [{IMP}]; BI -> RO [{IMP}];\n"
a += "  ACt -> EX [arrowhead=open, label=\" usa\"]; ACt -> IC [arrowhead=open];\n"
a += "  AC -> AD [arrowhead=open, style=dashed]; RT -> RS [arrowhead=open, style=dashed];\n"
render("fig_arreglos_archivos", a)

# ---------------------------------------------------------------- Excepciones
e = """  node [shape=box, style="rounded,filled", fillcolor="white", color="#9e9e9e", fontsize=10];
  Ex [label="java.lang.Exception", fillcolor="#eeeeee"];
  T [label="TallerNetException\\n(verificada)", fillcolor="#e0f2f1"];
  TI [label="TurnoInvalidoException"]; TS [label="TurnoSuperpuestoException\\n(flujo S5)"];
  DI [label="DatoInvalidoException"]; RD [label="RegistroDuplicadoException\\n(MySQL 1062) · AP4", fillcolor="#fff8e1"];
  EN [label="EntidadNoEncontradaException\\n(MySQL 1452)"];
  P [label="PersistenciaException\\n(envuelve SQLException)"]; C [label="ConexionException\\n(SQLState 08, error 1045) · AP4", fillcolor="#fff8e1"];
  A [label="ArchivoException\\n(IOException) · AP4", fillcolor="#fff8e1"];
"""
e += f"  T -> Ex [{HER}]; TI -> T [{HER}]; TS -> TI [{HER}]; DI -> T [{HER}]; RD -> DI [{HER}]; EN -> T [{HER}]; P -> T [{HER}]; C -> P [{HER}]; A -> T [{HER}];\n"
render("fig_excepciones", e, "BT")
