package tallernet.controlador;

import tallernet.archivos.RegistroOperaciones;
import tallernet.excepciones.DatoInvalidoException;
import tallernet.excepciones.EntidadNoEncontradaException;
import tallernet.excepciones.TallerNetException;
import tallernet.excepciones.TurnoInvalidoException;
import tallernet.modelo.AgendaDiaria;
import tallernet.modelo.Empleado;
import tallernet.modelo.EstadoTurno;
import tallernet.modelo.Sede;
import tallernet.modelo.Servicio;
import tallernet.modelo.Turno;
import tallernet.modelo.Vehiculo;
import tallernet.persistencia.Dao;
import tallernet.persistencia.TurnoRepositorio;
import tallernet.persistencia.VehiculoRepositorio;
import tallernet.util.Ordenamiento;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

/**
 * Controlador del CU001 Gestionar turno. Orquesta el caso de uso: valida con
 * {@link ValidadorTurno}, consulta las entidades y delega la persistencia en los repositorios.
 * Depende de interfaces, no de clases JDBC, por lo que la vista o la base pueden cambiar
 * sin modificar este código.
 */
public class TurnoControlador {

    /** Orden cronológico de la agenda. */
    public static final Comparator<Turno> POR_FECHA_HORA = Comparator.comparing(Turno::getFechaHora);

    private final TurnoRepositorio turnos;
    private final VehiculoRepositorio vehiculos;
    private final Dao<Sede, Integer> sedes;
    private final Dao<Servicio, Integer> servicios;
    private final ValidadorTurno validador;
    private final RegistroOperaciones registro;

    /** Cola FIFO de vehículos que llegaron con turno y esperan ser atendidos (recepción del día). */
    private final Queue<Turno> colaRecepcion = new ArrayDeque<>();
    /** Pila LIFO de cancelaciones de la sesión, para deshacer la última. */
    private final Deque<Turno> cancelacionesRecientes = new ArrayDeque<>();

    public TurnoControlador(TurnoRepositorio turnos, VehiculoRepositorio vehiculos, Dao<Sede, Integer> sedes,
                            Dao<Servicio, Integer> servicios, ValidadorTurno validador) {
        this(turnos, vehiculos, sedes, servicios, validador, RegistroOperaciones.NINGUNO);
    }

    /** Constructor completo: incluye el registro de operaciones (bitácora) del TP4. */
    public TurnoControlador(TurnoRepositorio turnos, VehiculoRepositorio vehiculos, Dao<Sede, Integer> sedes,
                            Dao<Servicio, Integer> servicios, ValidadorTurno validador,
                            RegistroOperaciones registro) {
        this.registro = registro;
        this.turnos = turnos;
        this.vehiculos = vehiculos;
        this.sedes = sedes;
        this.servicios = servicios;
        this.validador = validador;
    }

    /**
     * Flujo principal del CU001: valida datos y reglas y registra el turno como confirmado.
     * Si el horario está ocupado se propaga TurnoSuperpuestoException (flujo S5).
     */
    public Turno registrarTurno(Empleado recepcionista, int sedeId, String patente, int servicioId,
                                String fecha, String hora, String observaciones) throws TallerNetException {
        if (recepcionista == null || !recepcionista.puedeRegistrarTurnos()) {
            throw new TurnoInvalidoException("El empleado en sesión no está habilitado para registrar turnos.");
        }
        Sede sede = buscarSede(sedeId);
        Vehiculo vehiculo = buscarVehiculo(patente);
        Servicio servicio = buscarServicio(servicioId);
        // No se compara contra la fecha de alta del vehículo: esa fecha la fija el reloj del servidor de
        // base de datos y no el reloj de la aplicación. En operación real el alta es anterior al
        // momento actual, y la regla "no pasado" ya impide turnos anteriores a él (DEF-03).
        LocalDateTime inicio = validador.validarTodo(sede, servicio, fecha, hora);
        if (observaciones != null && observaciones.strip().length() > Turno.LARGO_OBSERVACIONES) {
            throw new DatoInvalidoException("Las observaciones no pueden superar " + Turno.LARGO_OBSERVACIONES
                    + " caracteres.");
        }
        Turno turno = new Turno(sede, vehiculo, servicio, recepcionista.getId(), inicio, observaciones);
        turnos.insertarSiDisponible(turno);
        registro.registrar("ALTA_TURNO", "Turno N.° " + turno.getId() + " " + vehiculo.getPatente() + " "
                + turno.getFranja() + " en " + sede.getNombre());
        return turno;
    }

    /** RF-TN06: cambia fecha y hora manteniendo sede, vehículo y servicio. */
    public Turno reprogramarTurno(int turnoId, String fecha, String hora) throws TallerNetException {
        Turno turno = buscarTurno(turnoId);
        if (turno.getEstado() != EstadoTurno.CONFIRMADO) {
            throw new TurnoInvalidoException("Solo se pueden reprogramar turnos confirmados; el turno N.° "
                    + turnoId + " está " + turno.getEstado() + ".");
        }
        LocalDateTime inicio = validador.validarTodo(turno.getSede(), turno.getServicio(), fecha, hora);
        turno.setFechaHora(inicio);
        turnos.reprogramarSiDisponible(turno);
        registro.registrar("REPROGRAMACION", "Turno N.° " + turnoId + " -> " + turno.getFranja());
        return turno;
    }

    /** RF-TN06: el turno no se borra, pasa a CANCELADO y libera la franja. */
    public Turno cancelarTurno(int turnoId) throws TallerNetException {
        Turno turno = buscarTurno(turnoId);
        if (turno.getEstado() != EstadoTurno.CONFIRMADO) {
            throw new TurnoInvalidoException("El turno N.° " + turnoId + " no se puede cancelar porque está "
                    + turno.getEstado() + ".");
        }
        turnos.cambiarEstado(turnoId, EstadoTurno.CONFIRMADO, EstadoTurno.CANCELADO);
        turno.cancelar();
        colaRecepcion.removeIf(t -> t.getId() == turnoId);
        cancelacionesRecientes.push(turno);
        registro.registrar("CANCELACION", "Turno N.° " + turnoId);
        return turno;
    }

    /**
     * Deshace la última cancelación de la sesión (pila: último en entrar, primero en salir).
     * Vuelve a verificar disponibilidad, porque la franja pudo ocuparse mientras tanto.
     */
    public Turno deshacerUltimaCancelacion() throws TallerNetException {
        Turno turno = cancelacionesRecientes.poll();
        if (turno == null) {
            throw new TurnoInvalidoException("No hay cancelaciones para deshacer en esta sesión.");
        }
        // Se retira de la pila aunque falle: si la franja ya se ocupó o el turno quedó en el pasado,
        // no podrá deshacerse nunca y bloquearía las cancelaciones anteriores (DEF-06).
        validador.validarNoPasado(turno.getFechaHora());
        turno.setEstado(EstadoTurno.CONFIRMADO);
        try {
            turnos.reconfirmarSiDisponible(turno);
        } catch (TallerNetException e) {
            turno.cancelar();
            throw e;
        }
        registro.registrar("RECONFIRMACION", "Turno N.° " + turno.getId() + " (cancelación deshecha)");
        return turno;
    }

    /** Agenda de una sede en un día, ordenada por inserción (lista chica y casi ordenada). */
    public List<Turno> consultarAgenda(int sedeId, String fecha) throws TallerNetException {
        buscarSede(sedeId);
        LocalDate dia = validador.validarFecha(fecha);
        List<Turno> agenda = new ArrayList<>(turnos.listarAgenda(sedeId, dia));
        Ordenamiento.insercion(agenda, POR_FECHA_HORA);
        return agenda;
    }

    /**
     * Grilla de disponibilidad de una sede en un día (TP4): los turnos llegan en una lista y se
     * vuelcan a un arreglo de franjas de tamaño fijo.
     */
    public AgendaDiaria consultarDisponibilidad(int sedeId, String fecha) throws TallerNetException {
        Sede sede = buscarSede(sedeId);
        LocalDate dia = validador.validarFecha(fecha);
        validador.validarDiaHabil(dia);
        return new AgendaDiaria(sede, dia, turnos.listarAgenda(sedeId, dia));
    }

    /**
     * Sugerencia para el flujo S5: primer horario libre del mismo día, a partir de la hora
     * pedida, en el que cabe el servicio completo. Si no hay ninguno posterior, se busca desde
     * la apertura. Vacío si el día está completo.
     */
    public Optional<LocalTime> sugerirHorario(int sedeId, int servicioId, String fecha, String hora)
            throws TallerNetException {
        AgendaDiaria agenda = consultarDisponibilidad(sedeId, fecha);
        int duracion = buscarServicio(servicioId).getDuracionMin();
        LocalTime pedida = validador.validarHora(hora);
        // Si el turno es para hoy, nunca se sugiere un horario que ya pasó (DEF-24).
        LocalTime minimo = agenda.horaDeFranja(0);
        LocalDateTime ahora = validador.ahora();
        if (agenda.getFecha().equals(ahora.toLocalDate()) && ahora.toLocalTime().isAfter(minimo)) {
            minimo = ahora.toLocalTime();
        }
        LocalTime desde = pedida.isAfter(minimo) ? pedida : minimo;
        Optional<LocalTime> sugerida = agenda.primerHuecoLibre(duracion, desde);
        return sugerida.isPresent() ? sugerida : agenda.primerHuecoLibre(duracion, minimo);
    }

    public Turno buscarTurno(int turnoId) throws TallerNetException {
        return turnos.buscarPorId(turnoId)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el turno N.° " + turnoId + "."));
    }

    /**
     * Recepción: el vehículo llegó; se encola para ser atendido en orden de llegada.
     * La cola vive en la memoria de la sesión de cada puesto de recepción.
     */
    public Turno registrarLlegada(int turnoId) throws TallerNetException {
        Turno turno = buscarTurno(turnoId);
        if (turno.getEstado() != EstadoTurno.CONFIRMADO) {
            throw new TurnoInvalidoException("Solo pueden registrar llegada los turnos confirmados.");
        }
        for (Turno enCola : colaRecepcion) {
            if (enCola.getId() == turnoId) {
                throw new TurnoInvalidoException("El turno N.° " + turnoId + " ya está en la cola de recepción.");
            }
        }
        colaRecepcion.offer(turno);
        return turno;
    }

    /**
     * Atiende al primero de la cola y marca el turno como ATENDIDO. La generación de la
     * orden de trabajo (RF-TN07) corresponde al CU002 y se incorporará en esa iteración.
     */
    public Turno atenderSiguiente() throws TallerNetException {
        Turno turno = colaRecepcion.peek();
        if (turno == null) {
            throw new TurnoInvalidoException("La cola de recepción está vacía.");
        }
        try {
            turnos.cambiarEstado(turno.getId(), EstadoTurno.CONFIRMADO, EstadoTurno.ATENDIDO);
        } catch (TurnoInvalidoException e) {
            colaRecepcion.poll(); // otra terminal ya lo cambió: no tiene sentido mantenerlo en la cola
            throw e;
        }
        // Ante una falla de la base (PersistenciaException) el turno permanece en la cola.
        colaRecepcion.poll();
        turno.setEstado(EstadoTurno.ATENDIDO);
        registro.registrar("ATENCION", "Turno N.° " + turno.getId());
        return turno;
    }

    /** Copia de la cola para mostrarla sin exponer la estructura interna (encapsulamiento). */
    public List<Turno> verColaRecepcion() {
        return List.copyOf(colaRecepcion);
    }

    /**
     * Validación temprana para la vista (DEF-26): cada dato del alta de turno se verifica apenas se
     * ingresa, antes de pedir el siguiente. El alta vuelve a verificarlos dentro de registrarTurno.
     */
    public Sede verificarSede(int sedeId) throws TallerNetException {
        return buscarSede(sedeId);
    }

    /** Vehículo de la patente indicada (normalizada), o EntidadNoEncontradaException. */
    public Vehiculo verificarVehiculo(String patente) throws TallerNetException {
        return buscarVehiculo(patente);
    }

    /** Servicio del catálogo, o EntidadNoEncontradaException. */
    public Servicio verificarServicio(int servicioId) throws TallerNetException {
        return buscarServicio(servicioId);
    }

    private Sede buscarSede(int sedeId) throws TallerNetException {
        return sedes.buscarPorId(sedeId)
                .filter(Sede::isActiva)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe una sede activa con el número " + sedeId + "."));
    }

    private Servicio buscarServicio(int servicioId) throws TallerNetException {
        return servicios.buscarPorId(servicioId)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el servicio N.° " + servicioId + "."));
    }

    private Vehiculo buscarVehiculo(String patente) throws TallerNetException {
        if (patente == null || patente.isBlank()) {
            throw new DatoInvalidoException("La patente es obligatoria.");
        }
        String normalizada = Vehiculo.normalizarPatente(patente);
        return vehiculos.buscarPorPatente(normalizada)
                .orElseThrow(() -> new EntidadNoEncontradaException("No hay un vehículo registrado con la patente "
                        + normalizada + ". Regístrelo desde el menú Clientes y vehículos."));
    }
}
