package tallernet.excepciones;

/**
 * Flujo alternativo S5 del CU001: el horario ya está ocupado en la sede.
 * Especializa {@link TurnoInvalidoException}, de modo que la vista puede tratar
 * ambos casos en un único bloque catch o diferenciarlos cuando le conviene.
 */
public class TurnoSuperpuestoException extends TurnoInvalidoException {

    private static final long serialVersionUID = 1L;

    private final int turnoExistenteId;

    public TurnoSuperpuestoException(String mensaje, int turnoExistenteId) {
        super(mensaje);
        this.turnoExistenteId = turnoExistenteId;
    }

    public int getTurnoExistenteId() {
        return turnoExistenteId;
    }
}
