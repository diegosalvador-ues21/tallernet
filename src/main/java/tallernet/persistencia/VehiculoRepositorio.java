package tallernet.persistencia;

import tallernet.excepciones.PersistenciaException;
import tallernet.modelo.Vehiculo;
import java.util.List;
import java.util.Optional;

/** Operaciones de vehículos que exceden el contrato común. */
public interface VehiculoRepositorio extends Dao<Vehiculo, Integer> {

    Optional<Vehiculo> buscarPorPatente(String patente) throws PersistenciaException;

    /** Vehículos ordenados por patente: precondición de la búsqueda binaria. */
    List<Vehiculo> listarOrdenadoPorPatente() throws PersistenciaException;
}
