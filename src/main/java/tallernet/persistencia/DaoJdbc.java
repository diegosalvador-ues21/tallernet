package tallernet.persistencia;

import tallernet.conexion.ConexionBD;
import tallernet.excepciones.DatoInvalidoException;
import tallernet.excepciones.PersistenciaException;
import tallernet.excepciones.TallerNetException;
import tallernet.modelo.Cliente;
import tallernet.modelo.Sede;
import tallernet.modelo.Servicio;
import tallernet.modelo.Vehiculo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Superclase abstracta de los DAO JDBC: concentra la obtención de conexiones,
 * la traducción de errores y el mapeo de filas a objetos que comparten varias tablas.
 *
 * <p>Aplica el patrón <b>método plantilla</b> (Template Method; Gamma et al., 1995):
 * {@link #consultar}, {@link #consultarUno} y {@link #actualizar} fijan el esqueleto de toda
 * operación JDBC (abrir la conexión, preparar la sentencia, asignar parámetros, ejecutar,
 * recorrer el resultado, cerrar los recursos y traducir la {@link SQLException}); cada DAO
 * concreto aporta solo la sentencia SQL, los parámetros y el mapeo de la fila.</p>
 */
abstract class DaoJdbc {

    protected Connection abrir() throws PersistenciaException {
        return ConexionBD.getInstancia().getConexion();
    }

    /** Falla técnica de una consulta, traducida a un mensaje comprensible (TP4). */
    protected static PersistenciaException error(String accion, SQLException e) {
        return TraductorSQLException.tecnica(accion, e);
    }

    /**
     * Plantilla de consulta: devuelve todas las filas como una lista dinámica ({@link ArrayList}),
     * porque la cantidad de registros no se conoce antes de ejecutar la consulta.
     */
    protected <T> List<T> consultar(String accion, String sql, ParametrosSentencia parametros,
                                    MapeadorFila<T> mapeador) throws PersistenciaException {
        List<T> resultado = new ArrayList<>();
        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            parametros.asignar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapeador.mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw error(accion, e);
        }
        return resultado;
    }

    /**
     * Número generado por un INSERT (DEF-33): si la base no devuelve la clave se informa un
     * error en lugar de leer una fila inexistente.
     */
    protected static int claveGenerada(ResultSet claves) throws SQLException {
        if (!claves.next()) {
            throw new SQLException("La base no devolvió el número generado para el registro.");
        }
        return claves.getInt(1);
    }

    /** Plantilla de búsqueda por clave: a lo sumo una fila. */
    protected <T> Optional<T> consultarUno(String accion, String sql, ParametrosSentencia parametros,
                                           MapeadorFila<T> mapeador) throws PersistenciaException {
        List<T> filas = consultar(accion, sql, parametros, mapeador);
        return filas.isEmpty() ? Optional.empty() : Optional.of(filas.get(0));
    }

    /**
     * Plantilla de actualización (INSERT sin clave generada, UPDATE).
     * @param duplicado mensaje para una clave única repetida, o {@code null}
     * @return cantidad de filas afectadas, para que el DAO verifique que el registro existía
     */
    protected int actualizar(String accion, String sql, ParametrosSentencia parametros, String duplicado)
            throws TallerNetException {
        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            parametros.asignar(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw TraductorSQLException.traducir(accion, e, duplicado);
        }
    }

    /** DATETIME se lee como LocalDateTime, sin conversión de zona horaria (DEF-08). */
    protected static LocalDateTime fecha(ResultSet rs, String columna) throws SQLException {
        return rs.getObject(columna, LocalDateTime.class);
    }

    protected static Sede mapearSede(ResultSet rs) throws SQLException {
        return new Sede(rs.getInt("sede_id"), rs.getString("sede_nombre"), rs.getString("sede_direccion"),
                rs.getTime("hora_apertura").toLocalTime(), rs.getTime("hora_cierre").toLocalTime(),
                rs.getBoolean("sede_activa"));
    }

    protected static Cliente mapearCliente(ResultSet rs) throws SQLException {
        try {
            return new Cliente(rs.getInt("cliente_id"), rs.getString("cliente_nombre"),
                    rs.getString("cliente_apellido"), rs.getString("telefono"), rs.getString("email"),
                    fecha(rs, "cliente_fecha_alta"));
        } catch (DatoInvalidoException e) {
            throw new SQLException("Registro de cliente inconsistente: " + e.getMessage(), e);
        }
    }

    protected static Vehiculo mapearVehiculo(ResultSet rs) throws SQLException {
        try {
            return new Vehiculo(rs.getInt("vehiculo_id"), rs.getInt("cliente_id"), rs.getString("patente"),
                    rs.getString("marca"), rs.getString("modelo"), rs.getInt("anio"), rs.getInt("km_alta"),
                    fecha(rs, "vehiculo_fecha_alta"));
        } catch (DatoInvalidoException e) {
            throw new SQLException("Registro de vehículo inconsistente: " + e.getMessage(), e);
        }
    }

    /**
     * Lee km_servicio y comprueba wasNull() inmediatamente después de leer esa misma
     * columna: el orden equivocado de esta comprobación fue la causa del DEF-02 del AP2.
     */
    protected static Servicio mapearServicio(ResultSet rs) throws SQLException {
        int km = rs.getInt("km_servicio");
        Integer kmServicio = rs.wasNull() ? null : km;
        try {
            return Servicio.crear(rs.getInt("servicio_id"), rs.getString("servicio_descripcion"),
                    rs.getBigDecimal("precio_base"), rs.getInt("duracion_min"), kmServicio);
        } catch (DatoInvalidoException e) {
            throw new SQLException("Registro de servicio inconsistente: " + e.getMessage(), e);
        }
    }
}
