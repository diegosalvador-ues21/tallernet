package tallernet.persistencia;

import tallernet.excepciones.ConexionException;
import tallernet.excepciones.DatoInvalidoException;
import tallernet.excepciones.EntidadNoEncontradaException;
import tallernet.excepciones.PersistenciaException;
import tallernet.excepciones.RegistroDuplicadoException;
import tallernet.excepciones.TallerNetException;
import java.sql.SQLException;

/**
 * Traduce una {@link SQLException} del driver a una excepción del dominio de TALLERNET.
 * Clasifica por código de error de MySQL y por la clase de SQLState (dos primeros caracteres),
 * que es estándar SQL y no depende del driver. Así ningún mensaje técnico del driver llega
 * al recepcionista y cada situación tiene una indicación concreta de qué hacer.
 */
public final class TraductorSQLException {

    /** Códigos de error del servidor MySQL 8.0 (Oracle, 2024a, apéndice B). */
    public static final int DUPLICADO = 1062;
    public static final int FK_PADRE_CON_HIJOS = 1451;
    public static final int FK_SIN_PADRE = 1452;
    public static final int DATO_DEMASIADO_LARGO = 1406;
    public static final int CHECK_VIOLADO = 3819;
    public static final int ACCESO_DENEGADO = 1045;
    public static final int SIN_PRIVILEGIO = 1142;
    public static final int ESPERA_BLOQUEO = 1205;
    public static final int INTERBLOQUEO = 1213;
    public static final int REQUIERE_TLS = 3159;

    private TraductorSQLException() {
        // Utilidad sin estado: no se instancia.
    }

    /** true si el error indica que no hay comunicación con el servidor (SQLState clase 08). */
    public static boolean esDeConexion(SQLException e) {
        String estado = e.getSQLState();
        return estado != null && estado.startsWith("08");
    }

    /** true si conviene reintentar la transacción una vez (interbloqueo o espera de bloqueo agotada). */
    public static boolean esReintentable(SQLException e) {
        return e.getErrorCode() == INTERBLOQUEO || e.getErrorCode() == ESPERA_BLOQUEO
                || "40001".equals(e.getSQLState());
    }

    /**
     * Traducción completa, para operaciones que modifican datos.
     * @param accion lo que se intentaba hacer, en infinitivo ("registrar el vehículo")
     * @param duplicado mensaje para el caso de clave única repetida (o {@code null} para uno genérico)
     */
    public static TallerNetException traducir(String accion, SQLException e, String duplicado) {
        switch (e.getErrorCode()) {
            case DUPLICADO:
                return new RegistroDuplicadoException(duplicado != null ? duplicado
                        : "No se pudo " + accion + ": ya existe un registro con ese dato.");
            case FK_SIN_PADRE:
                return new EntidadNoEncontradaException("No se pudo " + accion
                        + ": uno de los datos relacionados (sede, cliente, servicio o empleado) no existe.");
            case FK_PADRE_CON_HIJOS:
                return new DatoInvalidoException("No se pudo " + accion
                        + ": el registro tiene información asociada y se conserva para el historial.");
            case CHECK_VIOLADO:
            case DATO_DEMASIADO_LARGO:
                return new DatoInvalidoException("No se pudo " + accion
                        + ": un dato está fuera del rango permitido por la base.");
            default:
                return tecnica(accion, e);
        }
    }

    /** Traducción para consultas: solo puede tratarse de una falla técnica. */
    public static PersistenciaException tecnica(String accion, SQLException e) {
        if (esDeConexion(e)) {
            return new ConexionException("Se perdió la conexión con el servidor central al " + accion
                    + ". Verifique Internet y la VPN de la sede, y vuelva a intentar.", e);
        }
        switch (e.getErrorCode()) {
            case ACCESO_DENEGADO:
            case REQUIERE_TLS:
                return new ConexionException("El servidor rechazó las credenciales o la conexión cifrada."
                        + " Revise db.url, db.usuario y db.clave en db.properties.", e);
            case SIN_PRIVILEGIO:
                return new PersistenciaException("El usuario de la aplicación no tiene permiso para "
                        + accion + ". Avise al administrador del sistema.", e);
            case ESPERA_BLOQUEO:
            case INTERBLOQUEO:
                return new PersistenciaException("La agenda está siendo modificada desde otra sede."
                        + " Vuelva a intentar en unos segundos.", e);
            default:
                return new PersistenciaException("Error de base de datos al " + accion
                        + " (código " + e.getErrorCode() + ", estado " + e.getSQLState() + ").", e);
        }
    }
}
