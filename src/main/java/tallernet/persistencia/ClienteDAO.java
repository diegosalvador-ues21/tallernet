package tallernet.persistencia;

import tallernet.excepciones.EntidadNoEncontradaException;
import tallernet.excepciones.PersistenciaException;
import tallernet.excepciones.TallerNetException;
import tallernet.modelo.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

/** Acceso a la tabla cliente, compartida por ambas sedes (RF-TN02). */
public class ClienteDAO extends DaoJdbc implements Dao<Cliente, Integer> {

    private static final String SELECT = "SELECT cliente_id, nombre AS cliente_nombre, apellido AS cliente_apellido, "
            + "telefono, email, fecha_alta AS cliente_fecha_alta FROM cliente";

    @Override
    public Integer insertar(Cliente cliente) throws TallerNetException {
        String sql = "INSERT INTO cliente (nombre, apellido, telefono, email) VALUES (?, ?, ?, ?)";
        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getApellido());
            ps.setString(3, cliente.getTelefono());
            if (cliente.getEmail() == null) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, cliente.getEmail());
            }
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                int id = claveGenerada(claves);
                cliente.setId(id);
                return id;
            }
        } catch (SQLException e) {
            throw TraductorSQLException.traducir("registrar el cliente", e, "Ya existe un cliente registrado con ese correo electrónico.");
        }
    }

    @Override
    public List<Cliente> listar() throws PersistenciaException {
        // Sin ORDER BY a propósito: el orden lo resuelven los algoritmos de la capa de control.
        return consultar("listar clientes", SELECT, ParametrosSentencia.NINGUNO, DaoJdbc::mapearCliente);
    }

    @Override
    public Optional<Cliente> buscarPorId(Integer id) throws PersistenciaException {
        return consultarUno("buscar el cliente", SELECT + " WHERE cliente_id = ?",
                ps -> ps.setInt(1, id), DaoJdbc::mapearCliente);
    }

    /**
     * Actualiza teléfono y correo (RF-TN02). Se verifica que se haya modificado exactamente una
     * fila: cero significa que el cliente no existe (o fue dado de baja desde otra sede).
     */
    @Override
    public void actualizar(Cliente cliente) throws TallerNetException {
        int filas = actualizar("actualizar el cliente",
                "UPDATE cliente SET telefono = ?, email = ? WHERE cliente_id = ?",
                ps -> {
                    ps.setString(1, cliente.getTelefono());
                    if (cliente.getEmail() == null) {
                        ps.setNull(2, Types.VARCHAR);
                    } else {
                        ps.setString(2, cliente.getEmail());
                    }
                    ps.setInt(3, cliente.getId());
                },
                "Ya existe otro cliente registrado con el correo " + cliente.getEmail() + ".");
        if (filas != 1) {
            throw new EntidadNoEncontradaException("No existe el cliente N.° " + cliente.getId() + ".");
        }
    }
}
