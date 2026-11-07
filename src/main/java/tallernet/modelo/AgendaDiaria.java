package tallernet.modelo;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Grilla de ocupación de una sede en un día, dividida en franjas fijas de 30 minutos.
 *
 * <p>Combina las dos estructuras que pide el TP4 según la naturaleza de cada dato:</p>
 * <ul>
 *   <li>Los turnos llegan de la base en un {@link ArrayList}: su cantidad no se conoce de antemano.</li>
 *   <li>Las franjas se guardan en un <b>arreglo</b> {@code Turno[]}: su cantidad es fija y la define
 *   el horario de la sede (08:00 a 20:00 = 24 franjas). El acceso por índice es directo, O(1):
 *   la franja de las 10:30 es siempre la posición (10:30 − 08:00) / 30 = 5.</li>
 * </ul>
 * Con la grilla armada, el recepcionista ve la disponibilidad de la sede de un vistazo y el sistema
 * puede sugerir el primer horario libre cuando un turno se rechaza por superposición (flujo S5).
 */
public class AgendaDiaria {

    /** Duración de cada franja de la grilla, coherente con la carga de turnos del AP2. */
    public static final int MINUTOS_FRANJA = 30;

    private final Sede sede;
    private final LocalDate fecha;
    /** Una posición por franja; {@code null} indica franja libre. */
    private final Turno[] franjas;

    /**
     * @param turnosDelDia turnos no cancelados de la sede en esa fecha, en cualquier orden
     */
    public AgendaDiaria(Sede sede, LocalDate fecha, List<Turno> turnosDelDia) {
        this.sede = sede;
        this.fecha = fecha;
        long minutosDeAtencion = Duration.between(sede.getHoraApertura(), sede.getHoraCierre()).toMinutes();
        this.franjas = new Turno[(int) (minutosDeAtencion / MINUTOS_FRANJA)];
        // ArrayList -> arreglo para ordenar con la API estándar (Arrays.sort) y recorrer por índice.
        Turno[] ordenados = turnosDelDia.toArray(new Turno[0]);
        Arrays.sort(ordenados, Comparator.comparing(Turno::getFechaHora));
        for (Turno t : ordenados) {
            ocupar(t);
        }
    }

    /** Marca en el arreglo todas las franjas que toca el intervalo [inicio, fin) del turno. */
    private void ocupar(Turno turno) {
        if (!turno.getFechaHora().toLocalDate().equals(fecha) || !turno.getEstado().ocupaAgenda()) {
            return;
        }
        int desde = indiceDe(turno.getFechaHora().toLocalTime());
        int finMinutos = minutosDesdeApertura(turno.getFin().toLocalTime());
        int hasta = (finMinutos + MINUTOS_FRANJA - 1) / MINUTOS_FRANJA; // redondeo hacia arriba
        // Los límites se recortan al tamaño del arreglo: nunca se accede fuera de rango.
        for (int i = Math.max(0, desde); i < Math.min(hasta, franjas.length); i++) {
            franjas[i] = turno;
        }
    }

    private int minutosDesdeApertura(LocalTime hora) {
        return (int) Duration.between(sede.getHoraApertura(), hora).toMinutes();
    }

    /** Índice de la franja que contiene la hora (puede ser negativo o exceder el arreglo). */
    private int indiceDe(LocalTime hora) {
        return Math.floorDiv(minutosDesdeApertura(hora), MINUTOS_FRANJA);
    }

    /** Hora de inicio de la franja {@code i}. */
    public LocalTime horaDeFranja(int i) {
        return sede.getHoraApertura().plusMinutes((long) i * MINUTOS_FRANJA);
    }

    public int getCantidadFranjas() {
        return franjas.length;
    }

    public boolean estaLibre(int i) {
        return franjas[i] == null;
    }

    /** Turno que ocupa la franja, o vacío si está libre. */
    public Optional<Turno> getTurnoEn(int i) {
        return Optional.ofNullable(franjas[i]);
    }

    public int contarOcupadas() {
        int ocupadas = 0;
        for (Turno t : franjas) {
            if (t != null) {
                ocupadas++;
            }
        }
        return ocupadas;
    }

    /** Porcentaje de la capacidad diaria comprometida, de 0 a 100. */
    public int porcentajeOcupacion() {
        return franjas.length == 0 ? 0 : Math.round(contarOcupadas() * 100f / franjas.length);
    }

    /**
     * Primer horario a partir de {@code desde} en el que caben {@code duracionMin} minutos
     * consecutivos sin superponerse y sin pasar el cierre. Recorre el arreglo buscando una
     * secuencia de franjas libres del largo necesario (búsqueda secuencial, O(n)).
     */
    public Optional<LocalTime> primerHuecoLibre(int duracionMin, LocalTime desde) {
        int necesarias = (duracionMin + MINUTOS_FRANJA - 1) / MINUTOS_FRANJA;
        int inicio = Math.max(0, (minutosDesdeApertura(desde) + MINUTOS_FRANJA - 1) / MINUTOS_FRANJA);
        int consecutivas = 0;
        for (int i = inicio; i < franjas.length; i++) {
            consecutivas = franjas[i] == null ? consecutivas + 1 : 0;
            if (consecutivas == necesarias) {
                return Optional.of(horaDeFranja(i - necesarias + 1));
            }
        }
        return Optional.empty();
    }

    /**
     * Todos los horarios de inicio posibles para un servicio. Se devuelve un ArrayList porque la
     * cantidad de resultados depende de la ocupación y no se conoce de antemano.
     */
    public List<LocalTime> horariosDisponibles(int duracionMin) {
        List<LocalTime> horarios = new ArrayList<>();
        int necesarias = (duracionMin + MINUTOS_FRANJA - 1) / MINUTOS_FRANJA;
        for (int i = 0; i + necesarias <= franjas.length; i++) {
            boolean libre = true;
            for (int j = i; j < i + necesarias && libre; j++) {
                libre = franjas[j] == null;
            }
            if (libre) {
                horarios.add(horaDeFranja(i));
            }
        }
        return horarios;
    }

    /** Copia defensiva del arreglo: quien la recibe no puede alterar la grilla (encapsulamiento). */
    public Turno[] getFranjas() {
        return Arrays.copyOf(franjas, franjas.length);
    }

    public Sede getSede() {
        return sede;
    }

    public LocalDate getFecha() {
        return fecha;
    }
}
