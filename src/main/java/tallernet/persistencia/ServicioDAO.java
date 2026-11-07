package tallernet.persistencia;

import tallernet.excepciones.EntidadNoEncontradaException;
import tallernet.excepciones.PersistenciaException;
import tallernet.excepciones.TallerNetException;
import tallernet.modelo.Servicio;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

/** Acceso al catálogo de servicios (RF-TN17). */
public class ServicioDAO extends DaoJdbc implements Dao<Servicio, Integer> {

    private static final String SELECT = "SELECT servicio_id, descripcion AS servicio_descripcion, precio_base, "
            + "duracion_min, km_servicio FROM servicio";

    @Override
    public Integer insertar(Servicio servicio) throws TallerNetException {
        String sql = "INSERT INTO servicio (descripcion, precio_base, duracion_min, km_servicio) VALUES (?, ?, ?, ?)";
        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, servicio.getDescripcion());
            ps.setBigDecimal(2, servicio.getPrecioBase());
            ps.setInt(3, servicio.getDuracionMin());
            if (servicio.getKmServicio() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, servicio.getKmServicio());
            }
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                return claveGenerada(claves);
            }
        } catch (SQLException e) {
            throw TraductorSQLException.traducir("registrar el servicio", e, "Ya existe un servicio con esa descripción.");
        }
    }

    @Override
    public List<Servicio> listar() throws PersistenciaException {
        return consultar("listar servicios", SELECT + " ORDER BY servicio_id", ParametrosSentencia.NINGUNO,
                DaoJdbc::mapearServicio);
    }

    @Override
    public Optional<Servicio> buscarPorId(Integer id) throws PersistenciaException {
        return consultarUno("buscar el servicio", SELECT + " WHERE servicio_id = ?", ps -> ps.setInt(1, id),
                DaoJdbc::mapearServicio);
    }

    /** Actualiza el precio base del catálogo (RF-TN17). Los turnos ya registrados no guardan precio (3FN). */
    @Override
    public void actualizar(Servicio servicio) throws TallerNetException {
        int filas = actualizar("actualizar el precio del servicio",
                "UPDATE servicio SET precio_base = ? WHERE servicio_id = ?",
                ps -> {
                    ps.setBigDecimal(1, servicio.getPrecioBase());
                    ps.setInt(2, servicio.getId());
                }, null);
        if (filas != 1) {
            throw new EntidadNoEncontradaException("No existe el servicio N.° " + servicio.getId() + ".");
        }
    }
}
