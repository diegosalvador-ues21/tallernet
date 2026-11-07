package tallernet.persistencia;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Convierte la fila actual de un {@link ResultSet} en un objeto del modelo.
 * Es el "paso variable" del método plantilla {@link DaoJdbc#consultar}: cada DAO aporta
 * solo cómo se arma su entidad.
 */
@FunctionalInterface
interface MapeadorFila<T> {
    T mapear(ResultSet rs) throws SQLException;
}
