package tallernet.excepciones;

/**
 * El servidor central no responde: VPN cortada, MySQL detenido o configuración errónea.
 * Especializa {@link PersistenciaException} para que la vista la distinga de un error de
 * sentencia y le indique al recepcionista qué revisar (RNF-TN07: sin modo offline).
 */
public class ConexionException extends PersistenciaException {

    private static final long serialVersionUID = 1L;

    public ConexionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
