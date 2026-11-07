package tallernet.controlador;

import tallernet.excepciones.DatoInvalidoException;
import tallernet.excepciones.TurnoInvalidoException;
import tallernet.modelo.Sede;
import tallernet.modelo.Servicio;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reglas de fecha y horario del CU001 (RNF-TN04, RNF-TN06). No tiene estado ni accede a la
 * base, por eso se prueba de forma aislada (casos CPF02 y CPC03). El reloj se inyecta para
 * que las pruebas y las evidencias sean reproducibles con fechas fijas.
 */
public class ValidadorTurno {

    static final String MSJ_FORMATO_HORA = "Formato de hora no válido (use HH:mm, entre 00:00 y 23:59).";
    static final String MSJ_DOMINGO = "El taller no atiende los domingos. Seleccione una fecha de lunes a sábado.";
    static final String MSJ_PASADO = "La fecha y hora del turno no pueden ser anteriores al momento actual.";

    private static final Pattern HORA = Pattern.compile("^(-?\\d{1,2}):(\\d{2})$");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private final Clock reloj;

    public ValidadorTurno(Clock reloj) {
        this.reloj = reloj;
    }

    /** Convierte "HH:mm" en una hora válida del día; -1:00 o 24:00 quedan fuera del dominio. */
    public LocalTime validarHora(String texto) throws DatoInvalidoException {
        if (texto == null) {
            throw new DatoInvalidoException(MSJ_FORMATO_HORA);
        }
        Matcher m = HORA.matcher(texto.strip());
        if (!m.matches()) {
            throw new DatoInvalidoException(MSJ_FORMATO_HORA);
        }
        int horas = Integer.parseInt(m.group(1));
        int minutos = Integer.parseInt(m.group(2));
        if (horas < 0 || horas > 23 || minutos > 59) {
            throw new DatoInvalidoException(MSJ_FORMATO_HORA);
        }
        return LocalTime.of(horas, minutos);
    }

    /** Convierte "dd/MM/aaaa" en fecha, rechazando fechas inexistentes como 31/02/2026. */
    /** Momento actual según el reloj inyectado (fijo en pruebas y evidencias). */
    public LocalDateTime ahora() {
        return LocalDateTime.now(reloj);
    }

    public LocalDate validarFecha(String texto) throws DatoInvalidoException {
        try {
            return LocalDate.parse(texto == null ? "" : texto.strip(), FECHA);
        } catch (DateTimeParseException e) {
            throw new DatoInvalidoException("Formato de fecha no válido (use dd/mm/aaaa).");
        }
    }

    /** Lunes a sábado (RNF-TN04). */
    public void validarDiaHabil(LocalDate fecha) throws TurnoInvalidoException {
        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new TurnoInvalidoException(MSJ_DOMINGO);
        }
    }

    /** El turno no puede quedar en el pasado respecto del reloj del sistema. */
    public void validarNoPasado(LocalDateTime inicio) throws TurnoInvalidoException {
        if (inicio.isBefore(LocalDateTime.now(reloj))) {
            throw new TurnoInvalidoException(MSJ_PASADO);
        }
    }

    /** El servicio debe empezar y terminar dentro de la franja de la sede. */
    public void validarFranja(Sede sede, LocalTime inicio, Servicio servicio) throws TurnoInvalidoException {
        LocalTime fin = inicio.plusMinutes(servicio.getDuracionMin());
        if (!sede.contieneIntervalo(inicio, fin)) {
            throw new TurnoInvalidoException("Horario fuera de la franja de atención de la sede ("
                    + sede.getFranja() + ").");
        }
    }

    /**
     * Aplica todas las reglas en el orden en que el recepcionista las corregiría
     * y devuelve la fecha-hora lista para persistir.
     */
    public LocalDateTime validarTodo(Sede sede, Servicio servicio, String fechaTexto, String horaTexto)
            throws DatoInvalidoException, TurnoInvalidoException {
        LocalDate fecha = validarFecha(fechaTexto);
        LocalTime hora = validarHora(horaTexto);
        validarDiaHabil(fecha);
        LocalDateTime inicio = LocalDateTime.of(fecha, hora);
        validarNoPasado(inicio);
        validarFranja(sede, hora, servicio);
        return inicio;
    }

}
