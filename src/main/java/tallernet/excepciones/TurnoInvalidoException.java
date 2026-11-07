package tallernet.excepciones;

/** El turno solicitado viola una regla del CU001 (franja, día hábil, fecha pasada, estado). */
public class TurnoInvalidoException extends TallerNetException {

    private static final long serialVersionUID = 1L;

    public TurnoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
