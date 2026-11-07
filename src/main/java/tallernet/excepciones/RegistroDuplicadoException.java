package tallernet.excepciones;

/**
 * Se intentó registrar un dato que la base exige único (patente, correo, descripción de servicio).
 * Proviene del error 1062 de MySQL y especializa {@link DatoInvalidoException}: para el usuario
 * es un dato a corregir, no una falla técnica.
 */
public class RegistroDuplicadoException extends DatoInvalidoException {

    private static final long serialVersionUID = 1L;

    public RegistroDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
