package tallernet.persistencia;

import tallernet.excepciones.PersistenciaException;
import tallernet.modelo.Sede;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a la tabla sede. El alta de sedes (RF-TN20) queda para una iteración posterior. */
public class SedeDAO extends DaoJdbc implements Dao<Sede, Integer> {

    private static final String SELECT = "SELECT sede_id, nombre AS sede_nombre, direccion AS sede_direccion, "
            + "hora_apertura, hora_cierre, activa AS sede_activa FROM sede";

    @Override
    public Integer insertar(Sede sede) {
        throw new UnsupportedOperationException("El alta de sedes (RF-TN20) no forma parte de esta iteración.");
    }

    @Override
    public List<Sede> listar() throws PersistenciaException {
        List<Sede> sedes = new ArrayList<>();
        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(SELECT + " WHERE activa = 1 ORDER BY sede_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                sedes.add(mapearSede(rs));
            }
        } catch (SQLException e) {
            throw error("listar sedes", e);
        }
        return sedes;
    }

    @Override
    public Optional<Sede> buscarPorId(Integer id) throws PersistenciaException {
        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(SELECT + " WHERE sede_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearSede(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw error("buscar la sede", e);
        }
    }
}
