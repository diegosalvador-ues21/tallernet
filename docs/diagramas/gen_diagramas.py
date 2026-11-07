"""Genera los diagramas del informe AP3 con Graphviz (fuente editable: archivos .dot)."""
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

# ---------------------------------------------------------------- Modelo de dominio
m = ""
m += nodo("Persona", "Persona", "abstract", ["- id: int", "- nombre: String", "- apellido: String"],
          ["# Persona(id, nombre, apellido)", "+ <I>getDescripcionRol(): String</I>", "+ <I>getDatoDeContacto(): String</I>",
           "+ getNombreCompleto(): String", "+ setNombre(String) throws DatoInvalidoException", "+ toString(): String"], abstracta=True)
m += nodo("Cliente", "Cliente", "entity", ["- telefono: String", "- email: String", "- fechaAlta: LocalDateTime"],
          ["+ Cliente(nombre, apellido, telefono, email)", "+ getDescripcionRol(): String", "+ getDatoDeContacto(): String",
           "+ setTelefono(String)", "+ setEmail(String)"])
m += nodo("Empleado", "Empleado", "entity", ["- sedeId: int", "- rol: Rol", "- activo: boolean"],
          ["+ Empleado(id, sedeId, nombre, apellido, rol, activo)", "+ puedeRegistrarTurnos(): boolean",
           "+ getDescripcionRol(): String", "+ getDatoDeContacto(): String"])
m += nodo("Servicio", "Servicio", "abstract", ["- id: int", "- descripcion: String", "- precioBase: BigDecimal", "- duracionMin: int"],
          ["+ <U>crear(id, desc, precio, min, Integer km): Servicio</U>", "+ <I>calcularProximoServiceKm(int): OptionalInt</I>",
           "+ <I>describirIntervalo(): String</I>", "+ <I>getKmServicio(): Integer</I>"], abstracta=True)
m += nodo("SPK", "ServicioPorKilometraje", "entity", ["- kmServicio: int"],
          ["+ calcularProximoServiceKm(int): OptionalInt", "+ describirIntervalo(): String"])
m += nodo("SG", "ServicioGeneral", "entity", [], ["+ calcularProximoServiceKm(int): OptionalInt", "+ describirIntervalo(): String"])
m += nodo("Turno", "Turno", "entity", ["- id: int", "- fechaHora: LocalDateTime", "- estado: EstadoTurno", "- observaciones: String", "- empleadoId: int"],
          ["+ Turno(sede, vehiculo, servicio, empleadoId, fechaHora, obs)", "+ getFin(): LocalDateTime",
           "+ seSuperponeCon(Turno): boolean", "+ cancelar(): void", "+ getFranja(): String"])
m += nodo("Sede", "Sede", "entity", ["- id: int", "- nombre: String", "- horaApertura: LocalTime", "- horaCierre: LocalTime", "- activa: boolean"],
          ["+ contieneIntervalo(LocalTime, LocalTime): boolean", "+ getFranja(): String"])
m += nodo("Vehiculo", "Vehiculo", "entity", ["- id: int", "- clienteId: int", "- patente: String", "- marca, modelo: String", "- anio: int", "- kmAlta: int"],
          ["+ Vehiculo(clienteId, patente, marca, modelo, anio, km)", "+ setPatente(String)", "+ setKmAlta(int) throws DatoInvalidoException"])
m += nodo("EstadoTurno", "EstadoTurno", "enumeration", None, ["CONFIRMADO", "CANCELADO", "ATENDIDO", "+ ocupaAgenda(): boolean"])
m += nodo("Rol", "Rol", "enumeration", None, ["RECEPCIONISTA", "MECANICO", "ENCARGADO_REPUESTOS", "ADMINISTRADOR"])
m += f'''  Persona -> Cliente [arrowtail=empty, dir=back];
  Persona -> Empleado [arrowtail=empty, dir=back];
  Servicio -> SPK [arrowtail=empty, dir=back];
  Servicio -> SG [arrowtail=empty, dir=back];
  Turno -> Sede [arrowhead=vee, label=" 1"];
  Turno -> Vehiculo [arrowhead=vee, label=" 1"];
  Turno -> Servicio [arrowhead=vee, label=" 1", constraint=false];
  Turno -> EstadoTurno [arrowhead=vee, style=dashed];
  Empleado -> Rol [arrowhead=vee, style=dashed, constraint=false];
  Cliente -> Vehiculo [arrowtail=odiamond, dir=back, arrowhead=none, label=" 0..*"];
'''
# De izquierda a derecha y sin filas forzadas: el diagrama entra en una página vertical con texto legible
render("fig_clases_modelo", m, rankdir="LR")

# --------------------------------------------- Persistencia, controladores y excepciones
p = ""
p += nodo("Dao", "Dao&lt;T, K&gt;", "interface", None, ["+ insertar(T): K", "+ listar(): List&lt;T&gt;", "+ buscarPorId(K): Optional&lt;T&gt;"], interfaz=True)
p += nodo("TurnoRepo", "TurnoRepositorio", "interface", None, ["+ insertarSiDisponible(Turno): int", "+ reprogramarSiDisponible(Turno)",
          "+ reconfirmarSiDisponible(Turno)", "+ cambiarEstado(int, esperado, nuevo)", "+ listarAgenda(int, LocalDate)", "+ historialPorPatente(String)", "+ resumenPorSede()"], interfaz=True)
p += nodo("VehRepo", "VehiculoRepositorio", "interface", None, ["+ buscarPorPatente(String)", "+ listarOrdenadoPorPatente()"], interfaz=True)
p += nodo("DaoJdbc", "DaoJdbc", "abstract", None, ["# abrir(): Connection", "# error(String, SQLException)", "# mapearSede / mapearVehiculo", "# mapearServicio (DEF-02)"], abstracta=True)
p += nodo("TurnoDAO", "TurnoDAO", "", None, ["- prepararTransaccion(...)  READ COMMITTED", "- bloquearAgendaDeSede(...)  FOR UPDATE", "- verificarDisponibilidad(...)", "+ insertarSiDisponible(Turno): int"])
p += nodo("OtrosDAO", "ClienteDAO · VehiculoDAO · ServicioDAO<BR/>SedeDAO · EmpleadoDAO", "", None, ["implementan Dao&lt;T, Integer&gt;"])
p += nodo("Conexion", "ConexionBD", "singleton", ["- <U>instancia: ConexionBD</U>", "- url, usuario, clave"], ["+ <U>getInstancia(): ConexionBD</U>", "+ getConexion(): Connection"])
p += nodo("TC", "TurnoControlador", "control", ["- colaRecepcion: Queue&lt;Turno&gt;", "- cancelacionesRecientes: Deque&lt;Turno&gt;"],
          ["+ registrarTurno(...): Turno", "+ reprogramarTurno / cancelarTurno", "+ deshacerUltimaCancelacion()", "+ registrarLlegada / atenderSiguiente"])
p += nodo("VT", "ValidadorTurno", "control", ["- reloj: Clock"], ["+ validarHora(String): LocalTime", "+ validarFranja(Sede, LocalTime, Servicio)", "+ validarTodo(...): LocalDateTime"])
p += nodo("CC", "ClienteControlador · CatalogoControlador<BR/>SesionControlador", "control", None, ["+ listarClientes(criterio)", "+ buscarVehiculoPorPatente(String)", "+ estimarProximosServices()", "+ listarHabilitados() / iniciarSesion(int)"])
p += nodo("Vista", "VistaConsola", "boundary", None, ["- ejecutar(Accion)  ← captura excepciones", "- menuPrincipal() / menuTurnos() ..."])
p += nodo("TNE", "TallerNetException", "exception", None, ["extends Exception"])
p += nodo("TIE", "TurnoInvalidoException", "exception", None, [""])
p += nodo("TSE", "TurnoSuperpuestoException", "exception", ["- turnoExistenteId: int"], [""])
p += nodo("OtrasExc", "DatoInvalidoException · PersistenciaException<BR/>EntidadNoEncontradaException", "exception", None, [""])
p += f'''  TurnoRepo -> Dao [{HER}];
  VehRepo -> Dao [{HER}];
  TurnoDAO -> TurnoRepo [{IMP}];
  TurnoDAO -> DaoJdbc [{HER}];
  OtrosDAO -> DaoJdbc [{HER}];
  OtrosDAO -> Dao [{IMP}];
  DaoJdbc -> Conexion [arrowhead=vee, style=dashed, label=" usa"];
  Vista -> TC [arrowhead=vee];
  Vista -> CC [arrowhead=vee];
  TC -> VT [arrowhead=vee];
  TC -> TurnoRepo [arrowhead=vee, style=dashed, label=" depende de"];
  CC -> Dao [arrowhead=vee, style=dashed];
  TIE -> TNE [{HER}];
  TSE -> TIE [{HER}];
  OtrasExc -> TNE [{HER}];
'''
render("fig_clases_control_persistencia", p)

# ------------------------------------------------------------- Paquetes / capas
k = f'''  node [shape=tab, style=filled, fillcolor="{FONDO}", color="{VERDE}", fontcolor="{GRIS}", fontsize=11];
  vista [label="vista\\nVistaConsola, LectorEntrada"];
  controlador [label="controlador\\nTurnoControlador, ValidadorTurno, SesionControlador,\\nClienteControlador, CatalogoControlador"];
  persistencia [label="persistencia\\nDao, repositorios, DaoJdbc, DAOs"];
  conexion [label="conexion\\nConexionBD (Singleton)"];
  modelo [label="modelo\\nPersona, Cliente, Empleado, Sede,\\nVehiculo, Servicio (+2), Turno, enums"];
  excepciones [label="excepciones\\nTallerNetException y subclases"];
  util [label="util\\nOrdenamiento, Busqueda, Formatos"];
  db [shape=cylinder, fillcolor="white", label="MySQL 8.0\\nbase tallernet"];
  vista -> controlador; controlador -> persistencia; persistencia -> conexion; conexion -> db [label=" JDBC / TLS"];
  vista -> modelo [style=dashed]; controlador -> modelo [style=dashed]; persistencia -> modelo [style=dashed];
  controlador -> util [style=dashed]; vista -> excepciones [style=dashed]; controlador -> excepciones [style=dashed];
  persistencia -> excepciones [style=dashed];
  {{rank=same; modelo; util; excepciones}}
'''
render("fig_paquetes", k)
print("ok")
