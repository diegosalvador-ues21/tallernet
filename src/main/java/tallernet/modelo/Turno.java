package tallernet.modelo;

import tallernet.util.Formatos;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Cita de servicio para un vehículo en una sede (RF-TN04).
 * Conoce su intervalo de ocupación y decide si se superpone con otro turno:
 * esa responsabilidad se le asignó en el diagrama de secuencia de análisis del AP2,
 * porque la duración surge del servicio que el turno conoce.
 */
public class Turno {

    /** Largo de la columna turno.observaciones. El controlador lo valida antes con un mensaje de negocio. */
    public static final int LARGO_OBSERVACIONES = 255;

    private int id;
    private final Sede sede;
    private final Vehiculo vehiculo;
    private final Servicio servicio;
    private final int empleadoId;
    private LocalDateTime fechaHora;
    private EstadoTurno estado;
    private final String observaciones;

    public Turno(int id, Sede sede, Vehiculo vehiculo, Servicio servicio, int empleadoId,
                 LocalDateTime fechaHora, EstadoTurno estado, String observaciones) {
        this.id = id;
        this.sede = Objects.requireNonNull(sede, "sede");
        this.vehiculo = Objects.requireNonNull(vehiculo, "vehiculo");
        this.servicio = Objects.requireNonNull(servicio, "servicio");
        this.empleadoId = empleadoId;
        this.fechaHora = Objects.requireNonNull(fechaHora, "fechaHora");
        this.estado = estado == null ? EstadoTurno.CONFIRMADO : estado;
        String obs = observaciones == null || observaciones.isBlank() ? null : observaciones.strip();
        if (obs != null && obs.length() > LARGO_OBSERVACIONES) {
            throw new IllegalArgumentException("Las observaciones no pueden superar " + LARGO_OBSERVACIONES
                    + " caracteres.");
        }
        this.observaciones = obs;
    }

    /** Constructor para un turno nuevo: nace confirmado y sin número. */
    public Turno(Sede sede, Vehiculo vehiculo, Servicio servicio, int empleadoId,
                 LocalDateTime fechaHora, String observaciones) {
        this(0, sede, vehiculo, servicio, empleadoId, fechaHora, EstadoTurno.CONFIRMADO, observaciones);
    }

    /** Hora de finalización: inicio más la duración del servicio. */
    public LocalDateTime getFin() {
        return fechaHora.plusMinutes(servicio.getDuracionMin());
    }

    /**
     * Regla de RF-TN05 precisada en el AP2: dos turnos no cancelados de la misma sede
     * se superponen cuando sus intervalos [inicio, fin) se intersecan.
     * Turnos contiguos (uno termina 10:00 y el otro empieza 10:00) no chocan.
     */
    public boolean seSuperponeCon(Turno otro) {
        if (otro == null || otro == this) {
            return false;
        }
        if (!estado.ocupaAgenda() || !otro.estado.ocupaAgenda()) {
            return false;
        }
        if (sede.getId() != otro.sede.getId()) {
            return false;
        }
        return fechaHora.isBefore(otro.getFin()) && getFin().isAfter(otro.fechaHora);
    }

    public void cancelar() {
        this.estado = EstadoTurno.CANCELADO;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Sede getSede() {
        return sede;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public int getEmpleadoId() {
        return empleadoId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = Objects.requireNonNull(fechaHora, "fechaHora");
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public void setEstado(EstadoTurno estado) {
        this.estado = Objects.requireNonNull(estado, "estado");
    }

    public String getObservaciones() {
        return observaciones;
    }

    /** Franja legible, por ejemplo "05/10/2026 10:00 a 13:00". */
    public String getFranja() {
        return fechaHora.format(Formatos.FECHA_HORA) + " a " + getFin().format(Formatos.HORA);
    }

    @Override
    public String toString() {
        return String.format("Turno N.° %d %s | %s | %s | %s | %s", id, estado, sede.getNombre(),
                getFranja(), vehiculo.getPatente(), servicio.getDescripcion());
    }
}
