package tallernet.persistencia;

import tallernet.excepciones.EntidadNoEncontradaException;
import tallernet.excepciones.PersistenciaException;
import tallernet.excepciones.TallerNetException;
import tallernet.excepciones.TurnoInvalidoException;
import tallernet.excepciones.TurnoSuperpuestoException;
import tallernet.modelo.EstadoTurno;
import tallernet.modelo.ResumenSede;
import tallernet.modelo.Turno;
import tallernet.util.Formatos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a la tabla turno. Resuelve el principal riesgo técnico del CU001:
 * dos recepcionistas de sedes distintas reservando el mismo horario al mismo tiempo.
 */
public class TurnoDAO extends DaoJdbc implements TurnoRepositorio {

    /** Une las cuatro tablas que necesita un objeto Turno completo. */
    private static final String SELECT = "SELECT t.turno_id, t.empleado_id, t.fecha_hora, t.estado, t.observaciones, "
            + "s.sede_id, s.nombre AS sede_nombre, s.direccion AS sede_direccion, s.hora_apertura, s.hora_cierre, "
            + "s.activa AS sede_activa, "
            + "v.vehiculo_id, v.cliente_id, v.patente, v.marca, v.modelo, v.anio, v.km_alta, "
            + "v.fecha_alta AS vehiculo_fecha_alta, "
            + "sv.servicio_id, sv.descripcion AS servicio_descripcion, sv.precio_base, sv.duracion_min, sv.km_servicio "
            + "FROM turno t "
            + "INNER JOIN sede s ON s.sede_id = t.sede_id "
            + "INNER JOIN vehiculo v ON v.vehiculo_id = t.vehiculo_id "
            + "INNER JOIN servicio sv ON sv.servicio_id = t.servicio_id";

    /** Consulta Q4 del AP2: turnos activos de la sede cuyo intervalo se interseca con el nuevo. */
    private static final String SUPERPOSICION = "SELECT t.turno_id, t.fecha_hora, "
            + "DATE_ADD(t.fecha_hora, INTERVAL sv.duracion_min MINUTE) AS fin "
            + "FROM turno t INNER JOIN servicio sv ON sv.servicio_id = t.servicio_id "
            + "WHERE t.sede_id = ? AND t.estado <> 'CANCELADO' AND t.turno_id <> ? "
            + "AND t.fecha_hora < ? AND DATE_ADD(t.fecha_hora, INTERVAL sv.duracion_min MINUTE) > ? "
            + "ORDER BY t.fecha_hora LIMIT 1";

    /** Bloqueo de la fila de la sede: serializa las reservas de una misma sede (RF-TN05). */
    private static final String BLOQUEO_SEDE = "SELECT sede_id FROM sede WHERE sede_id = ? FOR UPDATE";

    @Override
    public Integer insertar(Turno turno) throws TallerNetException {
        return insertarSiDisponible(turno);
    }

    /**
     * Si MySQL elige esta transacción como víctima de un interbloqueo (error 1213) o se agota la
     * espera de bloqueo (1205), se reintenta una sola vez: la transacción ya se revirtió completa,
     * por lo que repetirla es seguro. Un segundo fallo se informa al usuario.
     */
    @Override
    public int insertarSiDisponible(Turno turno) throws TallerNetException {
        try {
            return insertarUnaVez(turno);
        } catch (PersistenciaException e) {
            if (e.getCause() instanceof SQLException sql && TraductorSQLException.esReintentable(sql)) {
                return insertarUnaVez(turno);
            }
            throw e;
        }
    }

    private int insertarUnaVez(Turno turno) throws TallerNetException {
        String sql = "INSERT INTO turno (sede_id, vehiculo_id, servicio_id, empleado_id, fecha_hora, estado, observaciones) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = abrir()) {
            prepararTransaccion(con);
            try {
                bloquearAgendaDeSede(con, turno.getSede().getId());
                verificarDisponibilidad(con, turno);
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, turno.getSede().getId());
                    ps.setInt(2, turno.getVehiculo().getId());
                    ps.setInt(3, turno.getServicio().getId());
                    ps.setInt(4, turno.getEmpleadoId());
                    ps.setObject(5, turno.getFechaHora());
                    ps.setString(6, turno.getEstado().name());
                    if (turno.getObservaciones() == null) {
                        ps.setNull(7, Types.VARCHAR);
                    } else {
                        ps.setString(7, turno.getObservaciones());
                    }
                    ps.executeUpdate();
                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        turno.setId(claveGenerada(claves));
                    }
                }
                con.commit();
                return turno.getId();
            } catch (TallerNetException | SQLException e) {
                revertir(con, e);
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw error("registrar el turno", e);
        }
    }

    @Override
    public void reprogramarSiDisponible(Turno turno) throws TallerNetException {
        actualizarConVerificacion(turno,
                "UPDATE turno SET fecha_hora = ? WHERE turno_id = ? AND estado = 'CONFIRMADO'",
                "No existe un turno confirmado N.° " + turno.getId() + ".", true);
    }

    @Override
    public void reconfirmarSiDisponible(Turno turno) throws TallerNetException {
        actualizarConVerificacion(turno,
                "UPDATE turno SET estado = 'CONFIRMADO' WHERE turno_id = ? AND estado = 'CANCELADO'",
                "El turno N.° " + turno.getId() + " ya no está cancelado.", false);
    }

    /** Bloqueo de la sede, verificación de superposición y UPDATE en una única transacción. */
    private void actualizarConVerificacion(Turno turno, String sql, String siNoAfecta, boolean conFecha)
            throws TallerNetException {
        try (Connection con = abrir()) {
            prepararTransaccion(con);
            try {
                bloquearAgendaDeSede(con, turno.getSede().getId());
                verificarDisponibilidad(con, turno);
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    int i = 1;
                    if (conFecha) {
                        ps.setObject(i++, turno.getFechaHora());
                    }
                    ps.setInt(i, turno.getId());
                    if (ps.executeUpdate() == 0) {
                        throw new EntidadNoEncontradaException(siNoAfecta);
                    }
                }
                con.commit();
            } catch (TallerNetException | SQLException e) {
                revertir(con, e);
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw error("actualizar el turno", e);
        }
    }

    /**
     * READ COMMITTED explícito: cada lectura ve lo confirmado por otras transacciones, de modo que la
     * verificación posterior al bloqueo de la sede siempre ve la reserva que lo liberó (DEF-11).
     */
    private static void prepararTransaccion(Connection con) throws SQLException {
        con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        con.setAutoCommit(false);
    }

    /** Revierte sin ocultar el error original si el propio rollback falla (DEF-10). */
    private static void revertir(Connection con, Exception original) {
        try {
            con.rollback();
        } catch (SQLException e) {
            original.addSuppressed(e);
        }
    }

    /**
     * Bloquea la fila de la sede hasta el fin de la transacción (SELECT ... FOR UPDATE).
     * Una segunda transacción sobre la misma sede espera aquí, y al continuar ya ve el
     * turno confirmado por la primera: así se evita la doble reserva sin bloquear la otra sede.
     */
    private void bloquearAgendaDeSede(Connection con, int sedeId) throws SQLException, EntidadNoEncontradaException {
        try (PreparedStatement ps = con.prepareStatement(BLOQUEO_SEDE)) {
            ps.setInt(1, sedeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new EntidadNoEncontradaException("No existe la sede N.° " + sedeId + ".");
                }
            }
        }
    }

    private void verificarDisponibilidad(Connection con, Turno turno) throws SQLException, TurnoSuperpuestoException {
        try (PreparedStatement ps = con.prepareStatement(SUPERPOSICION)) {
            ps.setInt(1, turno.getSede().getId());
            ps.setInt(2, turno.getId());
            ps.setObject(3, turno.getFin());
            ps.setObject(4, turno.getFechaHora());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int existente = rs.getInt("turno_id");
                    String franja = fecha(rs, "fecha_hora").format(Formatos.HORA) + " a "
                            + fecha(rs, "fin").format(Formatos.HORA);
                    throw new TurnoSuperpuestoException("El horario no está disponible en "
                            + turno.getSede().getNombre() + ": se superpone con el turno N.° " + existente
                            + " (" + franja + "). Seleccione otro horario.", existente);
                }
            }
        }
    }

    @Override
    public void cambiarEstado(int turnoId, EstadoTurno esperado, EstadoTurno nuevo) throws TallerNetException {
        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE turno SET estado = ? WHERE turno_id = ? AND estado = ?")) {
            ps.setString(1, nuevo.name());
            ps.setInt(2, turnoId);
            ps.setString(3, esperado.name());
            if (ps.executeUpdate() != 1) {
                throw new TurnoInvalidoException("El turno N.° " + turnoId + " ya no está " + esperado
                        + ": otra terminal lo modificó. Vuelva a consultarlo.");
            }
        } catch (SQLException e) {
            throw error("actualizar el estado del turno", e);
        }
    }

    @Override
    public List<Turno> listar() throws PersistenciaException {
        return consultar(SELECT + " ORDER BY t.fecha_hora", ps -> { });
    }

    @Override
    public Optional<Turno> buscarPorId(Integer id) throws PersistenciaException {
        List<Turno> lista = consultar(SELECT + " WHERE t.turno_id = ?", ps -> ps.setInt(1, id));
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    @Override
    public List<Turno> listarAgenda(int sedeId, LocalDate fecha) throws PersistenciaException {
        // Sin ORDER BY: la agenda se ordena con el algoritmo de inserción del controlador.
        return consultar(SELECT + " WHERE t.sede_id = ? AND t.fecha_hora >= ? AND t.fecha_hora < ? "
                + "AND t.estado <> 'CANCELADO'", ps -> {
                    ps.setInt(1, sedeId);
                    ps.setObject(2, fecha.atStartOfDay());
                    ps.setObject(3, fecha.plusDays(1).atStartOfDay());
                });
    }

    @Override
    public List<Turno> historialPorPatente(String patente) throws PersistenciaException {
        return consultar(SELECT + " WHERE v.patente = ? ORDER BY t.fecha_hora DESC", ps -> ps.setString(1, patente));
    }

    @Override
    public List<Turno> listarAtendidos() throws PersistenciaException {
        return consultar(SELECT + " WHERE t.estado = 'ATENDIDO' ORDER BY t.fecha_hora", ps -> { });
    }

    @Override
    public List<ResumenSede> resumenPorSede() throws PersistenciaException {
        // LEFT JOIN: una sede sin turnos confirmados aparece con ceros en lugar de omitirse (DEF-12).
        String sql = "SELECT s.nombre AS sede, COUNT(t.turno_id) AS turnos, "
                + "COALESCE(SUM(sv.duracion_min), 0) AS minutos, COALESCE(SUM(sv.precio_base), 0) AS ingreso "
                + "FROM sede s "
                + "LEFT JOIN turno t ON t.sede_id = s.sede_id AND t.estado = 'CONFIRMADO' "
                + "LEFT JOIN servicio sv ON sv.servicio_id = t.servicio_id "
                + "WHERE s.activa = 1 GROUP BY s.sede_id, s.nombre ORDER BY turnos DESC, s.nombre";
        List<ResumenSede> filas = new ArrayList<>();
        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                filas.add(new ResumenSede(rs.getString("sede"), rs.getInt("turnos"), rs.getInt("minutos"),
                        rs.getBigDecimal("ingreso")));
            }
        } catch (SQLException e) {
            throw error("consultar la carga por sede", e);
        }
        return filas;
    }

    /** Paso variable del método plantilla: arma un Turno completo desde la fila del JOIN. */
    private static Turno mapearTurno(ResultSet rs) throws SQLException {
        return new Turno(rs.getInt("turno_id"), mapearSede(rs), mapearVehiculo(rs), mapearServicio(rs),
                rs.getInt("empleado_id"), fecha(rs, "fecha_hora"), EstadoTurno.valueOf(rs.getString("estado")),
                rs.getString("observaciones"));
    }

    /** Atajo a la plantilla heredada de {@link DaoJdbc} para todas las consultas de turnos. */
    private List<Turno> consultar(String sql, ParametrosSentencia parametros) throws PersistenciaException {
        return consultar("consultar turnos", sql, parametros, TurnoDAO::mapearTurno);
    }

    /** Turnos no cancelados entre dos fechas (ambas sedes), insumo del reporte semanal. */
    @Override
    public List<Turno> listarEntre(LocalDate desde, LocalDate hastaExclusive) throws PersistenciaException {
        return consultar("consultar los turnos de la semana",
                SELECT + " WHERE t.estado <> 'CANCELADO' AND t.fecha_hora >= ? AND t.fecha_hora < ?"
                        + " ORDER BY t.fecha_hora",
                ps -> {
                    ps.setObject(1, desde.atStartOfDay());
                    ps.setObject(2, hastaExclusive.atStartOfDay());
                },
                TurnoDAO::mapearTurno);
    }
}
