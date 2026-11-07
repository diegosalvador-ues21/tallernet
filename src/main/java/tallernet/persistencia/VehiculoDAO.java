package tallernet.persistencia;

import tallernet.excepciones.PersistenciaException;
import tallernet.excepciones.TallerNetException;
import tallernet.modelo.Vehiculo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a la tabla vehiculo (RF-TN03). */
public class VehiculoDAO extends DaoJdbc implements VehiculoRepositorio {

    private static final String SELECT = "SELECT vehiculo_id, cliente_id, patente, marca, modelo, anio, km_alta, "
            + "fecha_alta AS vehiculo_fecha_alta FROM vehiculo";

    @Override
    public Integer insertar(Vehiculo vehiculo) throws TallerNetException {
        String sql = "INSERT INTO vehiculo (cliente_id, patente, marca, modelo, anio, km_alta) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, vehiculo.getClienteId());
            ps.setString(2, vehiculo.getPatente());
            ps.setString(3, vehiculo.getMarca());
            ps.setString(4, vehiculo.getModelo());
            ps.setInt(5, vehiculo.getAnio());
            ps.setInt(6, vehiculo.getKmAlta());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                int id = claveGenerada(claves);
                vehiculo.setId(id);
                return id;
            }
        } catch (SQLException e) {
            throw TraductorSQLException.traducir("registrar el vehículo", e,
                    "Ya existe un vehículo con la patente " + vehiculo.getPatente() + ".");
        }
    }

    @Override
    public List<Vehiculo> listar() throws PersistenciaException {
        return consultar(SELECT, null);
    }

    @Override
    public Optional<Vehiculo> buscarPorId(Integer id) throws PersistenciaException {
        List<Vehiculo> lista = consultar(SELECT + " WHERE vehiculo_id = ?", id);
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    @Override
    public Optional<Vehiculo> buscarPorPatente(String patente) throws PersistenciaException {
        List<Vehiculo> lista = consultar(SELECT + " WHERE patente = ?", patente);
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    @Override
    public List<Vehiculo> listarOrdenadoPorPatente() throws PersistenciaException {
        // COLLATE utf8mb4_bin: orden binario, idéntico al de String.compareTo para patentes en mayúsculas.
        return consultar(SELECT + " ORDER BY patente COLLATE utf8mb4_bin", null);
    }

    private List<Vehiculo> consultar(String sql, Object parametro) throws PersistenciaException {
        List<Vehiculo> vehiculos = new ArrayList<>();
        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setObject(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vehiculos.add(mapearVehiculo(rs));
                }
            }
        } catch (SQLException e) {
            throw error("consultar vehículos", e);
        }
        return vehiculos;
    }
}
