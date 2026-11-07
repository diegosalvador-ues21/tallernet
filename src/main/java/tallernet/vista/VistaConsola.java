package tallernet.vista;

import tallernet.archivos.ImportadorClientesCsv;
import tallernet.modelo.InfoConexion;
import tallernet.controlador.ArchivoControlador;
import tallernet.controlador.CatalogoControlador;
import tallernet.controlador.CatalogoControlador.CriterioServicio;
import tallernet.controlador.CatalogoControlador.ProximoService;
import tallernet.controlador.ClienteControlador;
import tallernet.controlador.ClienteControlador.BusquedaVehiculo;
import tallernet.controlador.ClienteControlador.CriterioCliente;
import tallernet.controlador.ClienteControlador.ListadoClientes;
import tallernet.controlador.SesionControlador;
import tallernet.controlador.SistemaControlador;
import tallernet.controlador.TurnoControlador;
import tallernet.excepciones.ArchivoException;
import tallernet.excepciones.ConexionException;
import tallernet.excepciones.DatoInvalidoException;
import tallernet.excepciones.EntidadNoEncontradaException;
import tallernet.excepciones.PersistenciaException;
import tallernet.excepciones.TallerNetException;
import tallernet.excepciones.TurnoInvalidoException;
import tallernet.excepciones.TurnoSuperpuestoException;
import tallernet.modelo.AgendaDiaria;
import tallernet.modelo.Cliente;
import tallernet.modelo.Empleado;
import tallernet.modelo.EstadoTurno;
import tallernet.modelo.Sede;
import tallernet.modelo.Servicio;
import tallernet.modelo.Turno;
import tallernet.modelo.Vehiculo;
import tallernet.modelo.ReporteSemanal;
import tallernet.modelo.ResumenSede;
import tallernet.archivos.ReporteSemanalTxt;
import tallernet.util.Formatos;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.Optional;
import java.util.List;

/**
 * Vista de consola del patrón MVC. Solo conversa con el recepcionista: pide datos, invoca a
 * los controladores y muestra resultados o errores. No contiene reglas de negocio, por lo que
 * la interfaz gráfica prevista en el AP1 podrá reemplazarla sin tocar el resto del sistema.
 */
public class VistaConsola {

    private static final String LINEA = "=".repeat(64);
    private static final String SUBLINEA = "-".repeat(64);

    /** Acción de un menú que puede fallar con una excepción de negocio. */
    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws TallerNetException;
    }

    private final TurnoControlador turnos;
    private final ClienteControlador clientes;
    private final CatalogoControlador catalogo;
    private final SesionControlador sesion;
    private final ArchivoControlador archivos;
    private final SistemaControlador sistema;
    private final LectorEntrada lector;
    private final PrintStream out;
    private Empleado empleadoEnSesion;

    public VistaConsola(TurnoControlador turnos, ClienteControlador clientes, CatalogoControlador catalogo,
                        SesionControlador sesion, ArchivoControlador archivos, SistemaControlador sistema,
                        LectorEntrada lector, PrintStream out) {
        this.archivos = archivos;
        this.sistema = sistema;
        this.turnos = turnos;
        this.clientes = clientes;
        this.catalogo = catalogo;
        this.sesion = sesion;
        this.lector = lector;
        this.out = out;
    }

    /** Punto de entrada de la interfaz: identifica al empleado y abre el menú principal. */
    public void iniciar() {
        out.println(LINEA);
        out.println("  TALLERNET - Talleres Ruiz Díaz | Gestión de turnos multi-sede");
        out.println(LINEA);
        try {
            if (!iniciarSesion()) {
                return;
            }
            menuPrincipal();
        } catch (LectorEntrada.EntradaFinalizadaException e) {
            out.println("Fin de la entrada. Sesión cerrada.");
        }
    }

    private boolean iniciarSesion() {
        List<Empleado> habilitados;
        try {
            habilitados = sesion.listarHabilitados();
        } catch (TallerNetException e) {
            out.println("[ERROR DE BASE DE DATOS] " + e.getMessage());
            return false;
        }
        out.println("Personal habilitado para registrar turnos:");
        for (Empleado e : habilitados) {
            out.printf("  %d. %s - %s (sede %d)%n", e.getId(), e.getNombreCompleto(), e.getDescripcionRol(),
                    e.getSedeId());
        }
        while (empleadoEnSesion == null) {
            int id = lector.leerEntero("Ingrese su número de empleado: ");
            try {
                empleadoEnSesion = sesion.iniciarSesion(id);
            } catch (TallerNetException e) {
                out.println("  [!] " + e.getMessage());
            }
        }
        sistema.getBitacora().setPuesto(empleadoEnSesion.getNombreCompleto() + " (sede "
                + empleadoEnSesion.getSedeId() + ")");
        out.println("Sesión iniciada: " + empleadoEnSesion.getNombre() + " " + empleadoEnSesion.getApellido()
                + " (" + empleadoEnSesion.getDescripcionRol() + ").");
        return true;
    }

    private void menuPrincipal() {
        int opcion;
        do {
            out.println();
            out.println(LINEA);
            out.println("  MENÚ PRINCIPAL");
            out.println(LINEA);
            out.println("  1. Turnos");
            out.println("  2. Clientes y vehículos");
            out.println("  3. Sedes y catálogo de servicios");
            out.println("  4. Reporte de carga por sede");
            out.println("  5. Reportes semanales y archivos");
            out.println("  6. Sistema");
            out.println("  0. Salir");
            opcion = lector.leerEntero("Elija una opción: ");
            switch (opcion) {
                case 1 -> menuTurnos();
                case 2 -> menuClientes();
                case 3 -> menuCatalogo();
                case 4 -> ejecutar(this::reporteCargaPorSede);
                case 5 -> menuArchivos();
                case 6 -> menuSistema();
                case 0 -> out.println("Sesión finalizada. ¡Hasta luego!");
                default -> out.println("  [!] Opción no válida. Elija un número del menú.");
            }
        } while (opcion != 0);
    }

    private void menuTurnos() {
        int opcion;
        do {
            out.println();
            out.println(SUBLINEA);
            out.println("  TURNOS");
            out.println(SUBLINEA);
            out.println("  1. Nuevo turno");
            out.println("  2. Reprogramar turno");
            out.println("  3. Cancelar turno");
            out.println("  4. Deshacer la última cancelación");
            out.println("  5. Agenda de una sede por fecha");
            out.println("  6. Buscar turno por número");
            out.println("  7. Registrar llegada del vehículo (cola de recepción)");
            out.println("  8. Atender siguiente vehículo de la cola");
            out.println("  9. Ver cola de recepción");
            out.println(" 10. Disponibilidad de una sede (grilla de franjas)");
            out.println("  0. Volver");
            opcion = lector.leerEntero("Elija una opción: ");
            switch (opcion) {
                case 1 -> ejecutarEscritura("el turno no se registró", this::nuevoTurno);
                case 2 -> ejecutarEscritura("el turno no se reprogramó", this::reprogramarTurno);
                case 3 -> ejecutarEscritura("el turno no se canceló", this::cancelarTurno);
                case 4 -> ejecutarEscritura("la cancelación no se deshizo", () -> {
                    Turno t = turnos.deshacerUltimaCancelacion();
                    out.println("  Cancelación revertida: Turno N.° " + t.getId() + " " + t.getEstado());
                });
                case 5 -> ejecutar(this::agenda);
                case 6 -> ejecutar(() -> mostrarTurno(turnos.buscarTurno(lector.leerEntero("Número de turno: "))));
                case 7 -> ejecutarEscritura("la llegada no se registró", () -> {
                    Turno t = turnos.registrarLlegada(lector.leerEntero("Número de turno: "));
                    out.println("  Llegada registrada. Turno N.° " + t.getId() + " (" + t.getVehiculo().getPatente()
                            + ") en la posición " + turnos.verColaRecepcion().size() + " de la cola.");
                });
                case 8 -> ejecutarEscritura("no se registró la atención", () -> {
                    Turno t = turnos.atenderSiguiente();
                    out.println("  Atendiendo Turno N.° " + t.getId() + " (" + t.getVehiculo().getPatente()
                            + ", " + t.getServicio().getDescripcion() + ") -> estado " + t.getEstado());
                });
                case 9 -> verCola();
                case 10 -> ejecutar(this::disponibilidad);
                case 0 -> { }
                default -> out.println("  [!] Opción no válida. Elija un número del menú.");
            }
        } while (opcion != 0);
    }

    private void menuClientes() {
        int opcion;
        do {
            out.println();
            out.println(SUBLINEA);
            out.println("  CLIENTES Y VEHÍCULOS");
            out.println(SUBLINEA);
            out.println("  1. Alta de cliente");
            out.println("  2. Alta de vehículo");
            out.println("  3. Listar clientes ordenados");
            out.println("  4. Buscar vehículo por patente");
            out.println("  5. Historial de un vehículo (ambas sedes)");
            out.println("  6. Modificar datos de contacto de un cliente");
            out.println("  0. Volver");
            opcion = lector.leerEntero("Elija una opción: ");
            switch (opcion) {
                case 1 -> ejecutarEscritura("el cliente no se registró", this::altaCliente);
                case 2 -> ejecutarEscritura("el vehículo no se registró", this::altaVehiculo);
                case 3 -> ejecutar(this::listarClientes);
                case 4 -> ejecutar(this::buscarVehiculo);
                case 5 -> ejecutar(this::historial);
                case 6 -> ejecutarEscritura("los datos de contacto no se modificaron", this::modificarContacto);
                case 0 -> { }
                default -> out.println("  [!] Opción no válida. Elija un número del menú.");
            }
        } while (opcion != 0);
    }

    private void menuCatalogo() {
        int opcion;
        do {
            out.println();
            out.println(SUBLINEA);
            out.println("  SEDES Y CATÁLOGO DE SERVICIOS");
            out.println(SUBLINEA);
            out.println("  1. Listar sedes");
            out.println("  2. Listar servicios por precio");
            out.println("  3. Listar servicios por duración");
            out.println("  4. Próximo service estimado por kilometraje");
            out.println("  5. Actualizar precio de un servicio");
            out.println("  0. Volver");
            opcion = lector.leerEntero("Elija una opción: ");
            switch (opcion) {
                case 1 -> ejecutar(this::listarSedes);
                case 2 -> ejecutar(() -> listarServicios(CriterioServicio.PRECIO));
                case 3 -> ejecutar(() -> listarServicios(CriterioServicio.DURACION));
                case 4 -> ejecutar(this::proximosServices);
                case 5 -> ejecutarEscritura("el precio no se modificó", this::actualizarPrecio);
                case 0 -> { }
                default -> out.println("  [!] Opción no válida. Elija un número del menú.");
            }
        } while (opcion != 0);
    }

    private void menuArchivos() {
        int opcion;
        do {
            out.println();
            out.println(SUBLINEA);
            out.println("  REPORTES SEMANALES Y ARCHIVOS");
            out.println(SUBLINEA);
            out.println("  1. Reporte semanal por día (ambas sedes)");
            out.println("  2. Exportar agenda de una sede a CSV");
            out.println("  3. Exportar reporte semanal a TXT");
            out.println("  4. Importar clientes desde CSV");
            out.println("  0. Volver");
            opcion = lector.leerEntero("Elija una opción: ");
            switch (opcion) {
                case 1 -> ejecutar(this::reporteSemanal);
                case 2 -> ejecutar(this::exportarAgenda);
                case 3 -> ejecutar(() -> {
                    Path p = archivos.exportarReporteSemanal(lector.leerTexto("Una fecha de la semana (dd/mm/aaaa): "));
                    out.println("  Reporte exportado: " + p);
                });
                case 4 -> ejecutar(this::importarClientes);
                case 0 -> { }
                default -> out.println("  [!] Opción no válida. Elija un número del menú.");
            }
        } while (opcion != 0);
    }

    private void menuSistema() {
        int opcion;
        do {
            out.println();
            out.println(SUBLINEA);
            out.println("  SISTEMA");
            out.println(SUBLINEA);
            out.println("  1. Probar conexión con el servidor central");
            out.println("  2. Ver las últimas operaciones (bitácora)");
            out.println("  0. Volver");
            opcion = lector.leerEntero("Elija una opción: ");
            switch (opcion) {
                case 1 -> ejecutar(this::probarConexion);
                case 2 -> ejecutar(this::verBitacora);
                case 0 -> { }
                default -> out.println("  [!] Opción no válida. Elija un número del menú.");
            }
        } while (opcion != 0);
    }

    /**
     * Único lugar donde la vista captura las excepciones de negocio. Los bloques van del más
     * específico al más general: una superposición (S5) se informa distinto de una regla
     * violada, y una falla técnica de la base se distingue de ambas.
     */
    private boolean ejecutar(Accion accion) {
        boolean correcto = false;
        try {
            accion.ejecutar();
            correcto = true;
        } catch (OperacionRechazada ignored) {
            // El motivo ya se informó dentro de la operación.
        } catch (TurnoSuperpuestoException e) {
            out.println("  [S5] " + e.getMessage());
        } catch (TurnoInvalidoException | DatoInvalidoException e) {
            out.println("  [!] " + e.getMessage());
        } catch (EntidadNoEncontradaException e) {
            out.println("  [?] " + e.getMessage());
        } catch (ConexionException e) {
            out.println("  [SIN CONEXIÓN] " + e.getMessage());
        } catch (PersistenciaException e) {
            out.println("  [ERROR DE BASE DE DATOS] " + e.getMessage());
        } catch (ArchivoException e) {
            out.println("  [ARCHIVO] " + e.getMessage());
        } catch (TallerNetException e) {
            out.println("  [!] " + e.getMessage());
        } catch (LectorEntrada.EntradaFinalizadaException e) {
            // DEF-30: el fin de la entrada (Ctrl-D) no es un error interno; cierra la sesión en iniciar().
            throw e;
        } catch (RuntimeException e) {
            // Red de seguridad (DEF-20): un error imprevisto de programación no debe cerrar el menú.
            out.println("  [ERROR INTERNO] La operación no pudo completarse (" + e.getClass().getSimpleName()
                    + "). Informe al administrador; puede continuar con otra opción.");
            sistema.getBitacora().registrar("ERROR_INTERNO", e.toString());
        }
        // La bitácora no interrumpe la operación, pero si no pudo escribirse se avisa una sola vez.
        String errorBitacora = sistema.getBitacora().tomarUltimoError();
        if (errorBitacora != null) {
            out.println("  [AVISO] " + errorBitacora);
        }
        return correcto;
    }

    /**
     * Operación que modifica datos (DEF-27): si se rechaza, además del motivo se informa en forma
     * explícita que no se guardó nada, para que el recepcionista no suponga lo contrario.
     */
    private void ejecutarEscritura(String queNoOcurrio, Accion accion) {
        resultadoParcial = null;
        if (!ejecutar(accion)) {
            out.println("  Resultado: " + (resultadoParcial != null ? resultadoParcial
                    : queNoOcurrio + "; no se guardó ningún dato."));
        }
        resultadoParcial = null;
    }

    /**
     * DEF-34: cuando una operación ya grabó una parte (el cliente nuevo de un alta de vehículo) y
     * luego falla, el resultado informa lo que sí quedó guardado en lugar de «no se guardó nada».
     */
    private String resultadoParcial;

    /** Rechazo informado por la propia operación (por ejemplo, S5): se trata como una falla. */
    private static final class OperacionRechazada extends TallerNetException {
        private static final long serialVersionUID = 1L;

        OperacionRechazada() {
            super("");
        }
    }

    // ----------------------------------------------------------------- Turnos

    private void nuevoTurno() throws TallerNetException {
        out.println("Nuevo turno (CU001)");
        listarSedes();
        int sedeId = lector.leerEntero("Sede: ");
        turnos.verificarSede(sedeId);                        // DEF-26: cada dato se valida al ingresarlo
        String patente = lector.leerTexto("Patente del vehículo: ");
        out.println("  Vehículo: " + turnos.verificarVehiculo(patente));
        listarServiciosBreve();
        int servicioId = lector.leerEntero("Servicio: ");
        turnos.verificarServicio(servicioId);
        String fecha = lector.leerTexto("Fecha (dd/mm/aaaa): ");
        String hora = lector.leerTexto("Hora (HH:mm): ");
        String obs = lector.leerTexto("Observaciones (opcional): ");
        Turno t;
        try {
            t = turnos.registrarTurno(empleadoEnSesion, sedeId, patente, servicioId, fecha, hora, obs);
        } catch (TurnoSuperpuestoException e) {
            // Flujo S5: además de informar el choque, se sugiere el primer horario libre (TP4).
            out.println("  [S5] " + e.getMessage());
            Optional<LocalTime> sugerido = turnos.sugerirHorario(sedeId, servicioId, fecha, hora);
            out.println(sugerido.map(h -> "  Primer horario libre ese día para este servicio: " + h.format(Formatos.HORA))
                    .orElse("  No quedan horarios libres ese día para este servicio."));
            throw new OperacionRechazada();
        }
        out.println("  Turno N.° " + t.getId() + " CONFIRMADO");
        mostrarTurno(t);
    }

    private void disponibilidad() throws TallerNetException {
        listarSedes();
        int sedeId = lector.leerEntero("Sede: ");
        String fecha = lector.leerTexto("Fecha (dd/mm/aaaa): ");
        AgendaDiaria agenda = turnos.consultarDisponibilidad(sedeId, fecha);
        out.printf("  Disponibilidad de %s el %s: %d de %d franjas de %d min ocupadas (%d %%)%n",
                agenda.getSede().getNombre(), agenda.getFecha().format(Formatos.FECHA), agenda.contarOcupadas(),
                agenda.getCantidadFranjas(), AgendaDiaria.MINUTOS_FRANJA, agenda.porcentajeOcupacion());
        // Mapa compacto: una celda por franja, cuatro horas por renglón.
        StringBuilder renglon = new StringBuilder();
        for (int i = 0; i < agenda.getCantidadFranjas(); i++) {
            if (i % 8 == 0) {
                renglon.append("  ").append(agenda.horaDeFranja(i).format(Formatos.HORA)).append(" ");
            }
            renglon.append(agenda.getTurnoEn(i).map(t -> String.format("[%3d]", t.getId())).orElse("[   ]"));
            if (i % 8 == 7 || i == agenda.getCantidadFranjas() - 1) {
                out.println(renglon);
                renglon.setLength(0);
            }
        }
        out.println("  Referencias: [ n ] franja ocupada por el turno N.° n; [   ] franja libre.");
    }

    private void reprogramarTurno() throws TallerNetException {
        int id = lector.leerEntero("Número de turno a reprogramar: ");
        Turno actual = turnos.buscarTurno(id);               // DEF-26: el turno se verifica antes de pedir la fecha
        mostrarTurno(actual);
        rechazarSiNoConfirmado(actual, "reprogramar");      // DEF-35: también su estado
        String fecha = lector.leerTexto("Nueva fecha (dd/mm/aaaa): ");
        String hora = lector.leerTexto("Nueva hora (HH:mm): ");
        Turno t = turnos.reprogramarTurno(id, fecha, hora);
        out.println("  Turno N.° " + t.getId() + " reprogramado: " + t.getFranja());
    }

    private void cancelarTurno() throws TallerNetException {
        int id = lector.leerEntero("Número de turno a cancelar: ");
        Turno t = turnos.buscarTurno(id);
        mostrarTurno(t);
        rechazarSiNoConfirmado(t, "cancelar");
        if (lector.confirmar("¿Confirma la cancelación?")) {
            turnos.cancelarTurno(id);
            out.println("  Turno N.° " + id + " CANCELADO. La franja " + t.getFranja() + " quedó disponible.");
        } else {
            out.println("  Operación descartada.");
        }
    }

    /** DEF-35: un turno cancelado o atendido se rechaza antes de pedir más datos o confirmación. */
    private void rechazarSiNoConfirmado(Turno t, String accion) throws OperacionRechazada {
        if (t.getEstado() != EstadoTurno.CONFIRMADO) {
            out.println("  [?] Solo se pueden " + accion + " turnos confirmados; el turno N.° " + t.getId()
                    + " está " + t.getEstado() + ".");
            throw new OperacionRechazada();
        }
    }

    private void agenda() throws TallerNetException {
        listarSedes();
        int sedeId = lector.leerEntero("Sede: ");
        String fecha = lector.leerTexto("Fecha (dd/mm/aaaa): ");
        List<Turno> agenda = turnos.consultarAgenda(sedeId, fecha);
        out.println("  Agenda del " + fecha + " (" + agenda.size() + " turnos, ordenados por inserción):");
        if (agenda.isEmpty()) {
            out.println("  Sin turnos.");
        }
        for (Turno t : agenda) {
            out.printf("  %s-%s  N.° %-4d %-8s %-28s %s%n", t.getFechaHora().format(Formatos.HORA),
                    t.getFin().format(Formatos.HORA), t.getId(), t.getVehiculo().getPatente(),
                    recortar(t.getServicio().getDescripcion(), 28), t.getEstado());
        }
    }

    private void verCola() {
        List<Turno> cola = turnos.verColaRecepcion();
        if (cola.isEmpty()) {
            out.println("  La cola de recepción está vacía.");
            return;
        }
        out.println("  Cola de recepción (primero en llegar, primero en ser atendido):");
        int posicion = 1;
        for (Turno t : cola) {
            out.printf("  %d) Turno N.° %d - %s - %s%n", posicion++, t.getId(), t.getVehiculo().getPatente(),
                    t.getServicio().getDescripcion());
        }
    }

    private void mostrarTurno(Turno t) {
        out.println("  " + SUBLINEA.substring(0, 50));
        out.println("  Turno N.° " + t.getId() + " - " + t.getEstado());
        out.println("  Sede:      " + t.getSede().getNombre());
        out.println("  Franja:    " + t.getFranja());
        out.println("  Vehículo:  " + t.getVehiculo());
        out.println("  Servicio:  " + t.getServicio().getDescripcion() + " (" + t.getServicio().getDuracionMin()
                + " min)");
        if (t.getObservaciones() != null) {
            out.println("  Obs.:      " + t.getObservaciones());
        }
        out.println("  " + SUBLINEA.substring(0, 50));
    }

    // ------------------------------------------------------- Clientes y vehículos

    private void altaCliente() throws TallerNetException {
        registrarClienteNuevo();
    }

    private Cliente registrarClienteNuevo() throws TallerNetException {
        String nombre = lector.leerTexto("Nombre: ");
        String apellido = lector.leerTexto("Apellido: ");
        String telefono = lector.leerTexto("Teléfono: ");
        String email = lector.leerTexto("Correo electrónico (opcional): ");
        Cliente c = clientes.registrarCliente(nombre, apellido, telefono, email);
        out.println("  Cliente N.° " + c.getId() + " registrado: " + c.getNombreCompleto());
        return c;
    }

    /**
     * Alta de vehículo (RF-TN03). DEF-26: el titular se verifica apenas se ingresa su número; si no
     * existe, se ofrece registrarlo en el mismo flujo, como prevé la precondición de CU001.
     */
    private void altaVehiculo() throws TallerNetException {
        int numero = lector.leerEntero("Número de cliente: ");
        Cliente titular;
        try {
            titular = clientes.buscarCliente(numero);
        } catch (EntidadNoEncontradaException e) {
            out.println("  [?] " + e.getMessage());
            if (!lector.confirmar("¿Desea registrar el cliente ahora?")) {
                out.println("  Consulte los clientes con la opción 3 o regístrelo con la opción 1.");
                throw new OperacionRechazada();
            }
            titular = registrarClienteNuevo();
            resultadoParcial = "el cliente N.° " + titular.getId() + " quedó registrado, pero el vehículo no se registró";
        }
        out.println("  Titular: N.° " + titular.getId() + " " + titular.getNombreCompleto());
        int clienteId = titular.getId();
        String patente = lector.leerTexto("Patente: ");
        String marca = lector.leerTexto("Marca: ");
        String modelo = lector.leerTexto("Modelo: ");
        String anio = lector.leerTexto("Año: ");
        String km = lector.leerTexto("Kilometraje actual: ");
        Vehiculo v = clientes.registrarVehiculo(clienteId, patente, marca, modelo, anio, km);
        out.println("  Vehículo N.° " + v.getId() + " registrado: " + v);
    }

    private void listarClientes() throws TallerNetException {
        out.println("  1. Por apellido y nombre   2. Por fecha de alta");
        int criterio = lector.leerEntero("Criterio: ");
        CriterioCliente c = criterio == 2 ? CriterioCliente.ANTIGUEDAD : CriterioCliente.APELLIDO;
        ListadoClientes listado = clientes.listarClientes(c);
        out.printf("  %d clientes ordenados por %s (%s, %d comparaciones):%n", listado.clientes().size(),
                c.getDescripcion(), listado.algoritmo(), listado.comparaciones());
        for (Cliente cl : listado.clientes()) {
            out.printf("  N.° %-4d %-28s %s%n", cl.getId(), recortar(cl.getNombreCompleto(), 28), cl.getTelefono());
        }
    }

    private void buscarVehiculo() throws TallerNetException {
        String patente = lector.leerTexto("Patente a buscar: ");
        BusquedaVehiculo r = clientes.buscarVehiculoPorPatente(patente);
        if (r.vehiculo().isPresent()) {
            out.println("  Encontrado: " + r.vehiculo().get());
            r.titular().ifPresent(c -> out.println("  Titular:    " + c.getNombreCompleto() + " ("
                    + c.getDatoDeContacto() + ")"));
        } else {
            out.println("  No hay un vehículo con la patente " + r.patenteBuscada() + ".");
        }
        out.printf("  Comparaciones sobre %d vehículos: búsqueda binaria %d (lista ordenada por patente),"
                + " búsqueda lineal %d (lista sin ordenar).%n", r.total(), r.comparacionesBinaria(),
                r.comparacionesLineal());
    }

    private void modificarContacto() throws TallerNetException {
        int id = lector.leerEntero("Número de cliente: ");
        Cliente actual = clientes.buscarCliente(id);
        out.println("  Datos actuales: " + actual);
        String telefono = lector.leerTexto("Nuevo teléfono (Enter para conservar): ");
        String email = lector.leerTexto("Nuevo correo (Enter para conservar, - para borrar): ");
        Cliente c = clientes.actualizarContacto(id, telefono, email);
        out.println("  Cliente N.° " + c.getId() + " actualizado: " + c.getDatoDeContacto());
    }

    private void historial() throws TallerNetException {
        String patente = lector.leerTexto("Patente: ");
        List<Turno> lista = clientes.historialVehiculo(patente);
        out.println("  Historial de " + Vehiculo.normalizarPatente(patente) + " en ambas sedes (" + lista.size() + " turnos):");
        for (Turno t : lista) {
            out.printf("  %s  %-15s %-32s %s%n", t.getFechaHora().format(Formatos.FECHA_HORA), t.getSede().getNombre(),
                    recortar(t.getServicio().getDescripcion(), 32), t.getEstado());
        }
    }

    // ----------------------------------------------------- Sedes, catálogo, reportes

    private void listarSedes() throws TallerNetException {
        for (Sede s : catalogo.listarSedes()) {
            out.println("  " + s);
        }
    }

    private void listarServiciosBreve() throws TallerNetException {
        for (Servicio s : catalogo.listarServicios(CriterioServicio.DURACION)) {
            out.printf("  %2d. %s (%d min)%n", s.getId(), s.getDescripcion(), s.getDuracionMin());
        }
    }

    private void listarServicios(CriterioServicio criterio) throws TallerNetException {
        out.println("  Servicios ordenados por " + criterio.getDescripcion() + " (quicksort):");
        for (Servicio s : catalogo.listarServicios(criterio)) {
            out.printf("  %2d. %-36s %16s %4d min  %s%n", s.getId(), recortar(s.getDescripcion(), 36),
                    Formatos.pesos(s.getPrecioBase()), s.getDuracionMin(), s.describirIntervalo());
        }
    }

    private void proximosServices() throws TallerNetException {
        out.println("  Próximo service estimado (referencia: kilometraje al alta):");
        for (ProximoService p : catalogo.estimarProximosServices()) {
            Turno t = p.turno();
            String proximo = p.proximoKm().isPresent() ? Formatos.km(p.proximoKm().getAsInt()) + " km"
                    : "no depende del km";
            out.printf("  %-8s %-34s %s  ref. %9s km -> %s%n", t.getVehiculo().getPatente(),
                    recortar(t.getServicio().getDescripcion(), 34), t.getFechaHora().format(Formatos.FECHA),
                    Formatos.km(t.getVehiculo().getKmAlta()), proximo);
        }
    }

    private void actualizarPrecio() throws TallerNetException {
        listarServicios(CriterioServicio.PRECIO);
        int id = lector.leerEntero("Número de servicio: ");
        Servicio actual = catalogo.buscarServicio(id);       // DEF-26: se verifica antes de pedir el precio
        out.println("  Servicio: " + actual.getDescripcion() + " | precio actual " + Formatos.pesos(actual.getPrecioBase()));
        String precio = lector.leerTexto("Nuevo precio base (por ejemplo 52.000,00): ");
        Servicio s = catalogo.actualizarPrecio(id, precio);
        out.println("  Precio actualizado: " + s.getDescripcion() + " -> " + Formatos.pesos(s.getPrecioBase()));
    }

    private void reporteSemanal() throws TallerNetException {
        ReporteSemanal r = catalogo.reporteSemanal(lector.leerTexto("Una fecha de la semana (dd/mm/aaaa): "));
        for (String linea : ReporteSemanalTxt.FormatoReporteSemanal.lineas(r)) {
            out.println("  " + linea);
        }
    }

    private void exportarAgenda() throws TallerNetException {
        listarSedes();
        int sedeId = lector.leerEntero("Sede: ");
        String fecha = lector.leerTexto("Fecha (dd/mm/aaaa): ");
        out.println("  Agenda exportada: " + archivos.exportarAgenda(sedeId, fecha));
    }

    private void importarClientes() throws TallerNetException {
        String ruta = lector.leerTexto("Ruta del archivo CSV (nombre;apellido;telefono;email): ");
        ImportadorClientesCsv.Resultado r = archivos.importarClientes(ruta);
        out.printf("  Líneas leídas: %d | Registrados: %d | Rechazados: %d%n", r.lineasLeidas(),
                r.registrados().size(), r.rechazos().size());
        for (Cliente c : r.registrados()) {
            out.println("  + Cliente N.° " + c.getId() + ": " + c.getNombreCompleto());
        }
        for (String rechazo : r.rechazos()) {
            out.println("  - " + rechazo);
        }
    }

    private void probarConexion() throws TallerNetException {
        InfoConexion info = sistema.probarConexion();
        out.println("  Conexión correcta con el servidor central.");
        out.println("  Servidor:  " + info.servidor());
        out.println("  Usuario:   " + info.usuario());
        out.println("  Motor:     " + info.motor());
        out.println("  Driver:    " + info.driver());
        out.println("  Cifrado:   " + info.cifrado());
        out.println("  Respuesta: " + info.milisegundos() + " ms");
    }

    private void verBitacora() throws TallerNetException {
        List<String> lineas = sistema.ultimasOperaciones(10);
        out.println("  Últimas operaciones registradas en " + sistema.getBitacora().getArchivo() + ":");
        if (lineas.isEmpty()) {
            out.println("  (sin operaciones registradas)");
        }
        for (String l : lineas) {
            out.println("  " + l);
        }
    }

    private void reporteCargaPorSede() throws TallerNetException {
        out.println("  Carga consolidada de turnos confirmados por sede:");
        out.printf("  %-16s %8s %10s %18s%n", "Sede", "Turnos", "Minutos", "Ingreso estimado");
        for (ResumenSede r : catalogo.cargaPorSede()) {
            out.printf("  %-16s %8d %10d %18s%n", r.sede(), r.turnosConfirmados(), r.minutosAgendados(),
                    Formatos.pesos(r.ingresoEstimado()));
        }
    }

    private static String recortar(String texto, int largo) {
        return texto.length() <= largo ? texto : texto.substring(0, largo - 1) + "…";
    }
}
