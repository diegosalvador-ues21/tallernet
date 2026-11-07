package tallernet.persistencia;

import tallernet.excepciones.PersistenciaException;
import tallernet.excepciones.TallerNetException;
import java.util.List;
import java.util.Optional;

/**
 * Contrato común de acceso a datos (patrón DAO, AP2 sección 2.1).
 * Los controladores dependen de esta interfaz y no de la implementación JDBC:
 * cualquier clase que la implemente puede usarse en su lugar (polimorfismo por interfaz).
 *
 * @param <T> tipo de la entidad
 * @param <K> tipo de la clave primaria
 */
public interface Dao<T, K> {

    /** Persiste la entidad y devuelve la clave generada. */
    K insertar(T entidad) throws TallerNetException;

    /** Devuelve todos los registros. */
    List<T> listar() throws PersistenciaException;

    /** Busca por clave primaria; vacío si no existe. */
    Optional<T> buscarPorId(K id) throws PersistenciaException;

    /**
     * Actualiza los datos modificables de un registro existente (UPDATE).
     * Método por defecto (Java 8+): las entidades que no admiten modificación, como las sedes
     * en este prototipo, heredan este comportamiento sin escribir código; las que sí la admiten
     * lo sobrescriben.
     * @throws TallerNetException si la entidad no admite modificación o el registro no existe
     */
    default void actualizar(T entidad) throws TallerNetException {
        throw new TallerNetException("Esta entidad no admite modificaciones en el prototipo.");
    }
}
