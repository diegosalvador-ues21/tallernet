package tallernet.persistencia;

import tallernet.excepciones.DatoInvalidoException;
import tallernet.excepciones.PersistenciaException;
import tallernet.modelo.Empleado;
import tallernet.modelo.Rol;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a la tabla empleado. El alta de personal queda fuera de esta iteración. */
public class EmpleadoDAO extends DaoJdbc implements Dao<Empleado, Integer> {

    private static final String SELECT = "SELECT empleado_id, sede_id, nombre, apellido, rol, activo FROM empleado";

    @Override
    public Integer insertar(Empleado empleado) {
        throw new UnsupportedOperationException("El alta de empleados no forma parte de esta iteración.");
    }

    @Override
    public List<Empleado> listar() throws PersistenciaException {
        List<Empleado> empleados = new ArrayList<>();
        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(SELECT + " ORDER BY empleado_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                empleados.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw error("listar empleados", e);
        }
        return empleados;
    }

    @Override
    public Optional<Empleado> buscarPorId(Integer id) throws PersistenciaException {
        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(SELECT + " WHERE empleado_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw error("buscar el empleado", e);
        }
    }

    private static Empleado mapear(ResultSet rs) throws SQLException {
        try {
            return new Empleado(rs.getInt("empleado_id"), rs.getInt("sede_id"), rs.getString("nombre"),
                    rs.getString("apellido"), Rol.valueOf(rs.getString("rol")), rs.getBoolean("activo"));
        } catch (DatoInvalidoException e) {
            throw new SQLException("Registro de empleado inconsistente: " + e.getMessage(), e);
        }
    }
}
