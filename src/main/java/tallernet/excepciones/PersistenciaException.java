package tallernet.excepciones;

/**
 * Falla técnica de la base de datos. Envuelve la {@link java.sql.SQLException}
 * original para que la vista la informe por separado de las reglas de negocio
 * (AP2, sección 3.2) sin depender de clases de JDBC.
 */
public class PersistenciaException extends TallerNetException {

    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
