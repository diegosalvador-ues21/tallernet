package tallernet.persistencia;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Asigna los parámetros (?) de una sentencia preparada. Nunca se concatenan datos del usuario. */
@FunctionalInterface
interface ParametrosSentencia {

    /** Sentencia sin parámetros. */
    ParametrosSentencia NINGUNO = ps -> { };

    void asignar(PreparedStatement ps) throws SQLException;
}
