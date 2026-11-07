package tallernet.modelo;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Ubicación física de la red Talleres Ruiz Díaz con su franja de atención (RF-TN20). */
public class Sede {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final int id;
    private final String nombre;
    private final String direccion;
    private final LocalTime horaApertura;
    private final LocalTime horaCierre;
    private final boolean activa;

    public Sede(int id, String nombre, String direccion, LocalTime horaApertura,
                LocalTime horaCierre, boolean activa) {
        if (!horaApertura.isBefore(horaCierre)) {
            throw new IllegalArgumentException("La hora de apertura debe ser anterior a la de cierre.");
        }
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.horaApertura = horaApertura;
        this.horaCierre = horaCierre;
        this.activa = activa;
    }

    /** Indica si un intervalo [inicio, fin] cae completo dentro del horario de la sede. */
    public boolean contieneIntervalo(LocalTime inicio, LocalTime fin) {
        boolean cruzaMedianoche = fin.isBefore(inicio);
        return !cruzaMedianoche && !inicio.isBefore(horaApertura) && !fin.isAfter(horaCierre);
    }

    /** Texto de la franja, por ejemplo "08:00 a 20:00". */
    public String getFranja() {
        return horaApertura.format(HH_MM) + " a " + horaCierre.format(HH_MM);
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public LocalTime getHoraApertura() {
        return horaApertura;
    }

    public LocalTime getHoraCierre() {
        return horaCierre;
    }

    public boolean isActiva() {
        return activa;
    }

    @Override
    public String toString() {
        return String.format("%d. %s - %s (%s)", id, nombre, direccion, getFranja());
    }
}
