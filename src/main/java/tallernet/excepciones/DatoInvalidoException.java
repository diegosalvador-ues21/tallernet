package tallernet.excepciones;

/** Un dato ingresado no cumple las reglas de validación (RNF-TN06). */
public class DatoInvalidoException extends TallerNetException {

    private static final long serialVersionUID = 1L;

    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
