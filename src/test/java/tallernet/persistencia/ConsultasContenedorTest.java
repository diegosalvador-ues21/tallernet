package tallernet.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import tallernet.conexion.ConexionBD;
import tallernet.controlador.TurnoControlador;
import tallernet.controlador.ValidadorTurno;
import tallernet.excepciones.ConexionException;
import tallernet.excepciones.EntidadNoEncontradaException;
import tallernet.excepciones.PersistenciaException;
import tallernet.excepciones.RegistroDuplicadoException;
import tallernet.excepciones.TallerNetException;
import tallernet.excepciones.TurnoSuperpuestoException;
import tallernet.modelo.Empleado;
import tallernet.modelo.EstadoTurno;
import tallernet.modelo.InfoConexion;
import tallernet.modelo.ResumenSede;
import tallernet.modelo.Rol;
import tallernet.modelo.Servicio;
import tallernet.modelo.Turno;
import tallernet.modelo.Vehiculo;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * CPI06 - Consultas de todos los DAO y el CU001 de punta a punta (controlador + DAO reales)
 * contra un MySQL 8.0 descartable. Cada resultado se compara con un oráculo SQL directo.
 */
@Tag("integracion")
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("CPI06 - Consultas y CU001 con MySQL descartable (Testcontainers)")
class ConsultasContenedorTest {

    private static final String CLAVE = "raiz-de-prueba";

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("tallernet").withUsername("root").withPassword(CLAVE)
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

    private static String url;

    @BeforeAll
    static void crearEsquema() throws Exception {
        url = MYSQL.getJdbcUrl() + "?sslMode=PREFERRED&characterEncoding=UTF-8";
        try (Connection con = DriverManager.getConnection(url, "root", CLAVE); Statement st = con.createStatement()) {
            for (String script : new String[] {"db/01_ddl_creacion.sql", "db/02_insercion.sql"}) {
                for (String sentencia : BaseDePruebas.sentencias(Path.of(script))) {
                    st.execute(sentencia);
                }
            }
        }
        ConexionBD.configurar(configuracion());
    }

    private static Properties configuracion() {
        Properties p = new Properties();
        p.setProperty("db.url", url);
        p.setProperty("db.usuario", "root");
        p.setProperty("db.clave", CLAVE);
        return p;
    }

    private static void ejecutarSql(String sql) throws Exception {
        try (Connection con = DriverManager.getConnection(url, "root", CLAVE); Statement st = con.createStatement()) {
            st.execute(sql);
        }
    }

    private static int contar(String sql) throws Exception {
        try (Connection con = DriverManager.getConnection(url, "root", CLAVE);
             Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Test
    @DisplayName("Sedes, empleados, servicios y clientes: lo listado coincide con la base")
    void catalogos() throws Exception {
        assertThat(new SedeDAO().listar()).hasSize(contar("SELECT COUNT(*) FROM tallernet.sede"));
        assertThat(new SedeDAO().buscarPorId(2)).hasValueSatisfying(s -> assertThat(s.getNombre()).isEqualTo("Sede Flores"));
        assertThat(new SedeDAO().buscarPorId(99)).isEmpty();
        List<Empleado> empleados = new EmpleadoDAO().listar();
        assertThat(empleados).hasSize(contar("SELECT COUNT(*) FROM tallernet.empleado"));
        assertThat(new EmpleadoDAO().buscarPorId(1)).hasValueSatisfying(e -> assertThat(e.getRol()).isEqualTo(Rol.ADMINISTRADOR));
        assertThat(new ServicioDAO().listar()).hasSize(contar("SELECT COUNT(*) FROM tallernet.servicio"));
        assertThat(new ClienteDAO().listar()).hasSizeGreaterThanOrEqualTo(4);
        assertThat(new ClienteDAO().buscarPorId(3)).hasValueSatisfying(c -> assertThat(c.getEmail()).isNull());
    }

    @Test
    @DisplayName("Vehículos: búsqueda por patente y orden binario por patente")
    void vehiculos() throws Exception {
        VehiculoDAO dao = new VehiculoDAO();
        assertThat(dao.buscarPorPatente("OKL482")).hasValueSatisfying(v -> assertThat(v.getKmAlta()).isEqualTo(198750));
        assertThat(dao.buscarPorPatente("ZZ000ZZ")).isEmpty();
        assertThat(dao.buscarPorId(1)).isPresent();
        assertThat(dao.listarOrdenadoPorPatente()).extracting(Vehiculo::getPatente).isSorted();
        assertThat(dao.listar()).hasSize(contar("SELECT COUNT(*) FROM tallernet.vehiculo"));
    }

    @Test
    @DisplayName("Turnos: agenda, historial, atendidos, semana y resumen por sede (Q3)")
    void consultasDeTurnos() throws Exception {
        TurnoDAO dao = new TurnoDAO();
        assertThat(dao.listarAgenda(1, LocalDate.of(2026, 10, 5))).hasSize(
                contar("SELECT COUNT(*) FROM tallernet.turno WHERE sede_id=1 AND DATE(fecha_hora)='2026-10-05' AND estado<>'CANCELADO'"));
        assertThat(dao.historialPorPatente("AE123BC")).hasSizeGreaterThanOrEqualTo(2);
        assertThat(dao.listarAtendidos()).extracting(Turno::getEstado).containsOnly(EstadoTurno.ATENDIDO);
        assertThat(dao.listarEntre(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 12))).isNotEmpty();
        List<ResumenSede> resumen = dao.resumenPorSede();
        assertThat(resumen).extracting(ResumenSede::sede).contains("Sede Echeandía", "Sede Flores");
        assertThat(dao.listar()).hasSize(contar("SELECT COUNT(*) FROM tallernet.turno"));
    }

    @Test
    @DisplayName("Conexión: verificación y diagnóstico informan el servidor real")
    void conexion() throws Exception {
        ConexionBD.getInstancia().verificar();
        InfoConexion info = ConexionBD.getInstancia().diagnosticar();
        assertThat(info.motor()).contains("MySQL");
        assertThat(info.usuario()).startsWith("root");
    }

    @Test
    @DisplayName("CU001 de punta a punta: alta, S5 en la misma sede, otra sede libre, cancelar/deshacer y atención")
    void cu001DePuntaAPunta() throws Exception {
        ZoneId ba = ZoneId.of("America/Argentina/Buenos_Aires");
        Clock reloj = Clock.fixed(LocalDateTime.of(2026, 10, 1, 8, 0).atZone(ba).toInstant(), ba);
        TurnoControlador c = new TurnoControlador(new TurnoDAO(), new VehiculoDAO(), new SedeDAO(), new ServicioDAO(),
                new ValidadorTurno(reloj));
        Empleado carla = new EmpleadoDAO().buscarPorId(2).orElseThrow();
        Turno t = c.registrarTurno(carla, 1, "ad 321 jk", 1, "14/10/2026", "10:00", null);
        assertThat(contar("SELECT COUNT(*) FROM tallernet.turno WHERE turno_id=" + t.getId() + " AND estado='CONFIRMADO'"))
                .isEqualTo(1);
        assertThatThrownBy(() -> c.registrarTurno(carla, 1, "AF789GH", 1, "14/10/2026", "10:30", null))
                .isInstanceOf(TurnoSuperpuestoException.class);
        assertThat(c.registrarTurno(carla, 2, "AF789GH", 1, "14/10/2026", "10:30", null).getId()).isPositive();
        c.cancelarTurno(t.getId());
        assertThat(contar("SELECT COUNT(*) FROM tallernet.turno WHERE turno_id=" + t.getId() + " AND estado='CANCELADO'"))
                .isEqualTo(1);
        c.deshacerUltimaCancelacion();
        c.reprogramarTurno(t.getId(), "14/10/2026", "15:00");
        c.registrarLlegada(t.getId());
        c.atenderSiguiente();
        assertThat(contar("SELECT COUNT(*) FROM tallernet.turno WHERE turno_id=" + t.getId()
                + " AND estado='ATENDIDO' AND TIME(fecha_hora)='15:00:00'")).isEqualTo(1);
    }

    @Test
    @DisplayName("Servicios: alta general y por kilometraje, duplicado y actualización de uno inexistente")
    void altasDeServicios() throws Exception {
        ServicioDAO dao = new ServicioDAO();
        int general = dao.insertar(Servicio.crear(0, "Lavado de motor", new BigDecimal("15000.00"), 30, null));
        int porKm = dao.insertar(Servicio.crear(0, "Cambio de correa", new BigDecimal("95000.00"), 120, 60000));
        assertThat(dao.buscarPorId(general)).hasValueSatisfying(x -> assertThat(x.getKmServicio()).isNull());
        assertThat(dao.buscarPorId(porKm)).hasValueSatisfying(x -> assertThat(x.getKmServicio()).isEqualTo(60000));
        assertThatThrownBy(() -> dao.insertar(Servicio.crear(0, "Lavado de motor", BigDecimal.TEN, 30, null)))
                .isInstanceOf(RegistroDuplicadoException.class).hasMessageContaining("descripción");
        assertThatThrownBy(() -> dao.actualizar(Servicio.crear(999, "Fantasma", BigDecimal.TEN, 30, null)))
                .isInstanceOf(EntidadNoEncontradaException.class);
    }

    @Test
    @DisplayName("Entidades sin alta ni modificación en el prototipo")
    void operacionesNoSoportadas() {
        SedeDAO sedes = new SedeDAO();
        assertThatThrownBy(() -> sedes.insertar(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> new EmpleadoDAO().insertar(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> sedes.actualizar(null)).isInstanceOf(TallerNetException.class)
                .hasMessageContaining("no admite modificaciones");
    }

    @Test
    @DisplayName("Un registro inconsistente cargado por fuera de la aplicación se informa sin romper la sesión")
    void registroInconsistente() throws Exception {
        ejecutarSql("INSERT INTO tallernet.vehiculo (cliente_id, patente, marca, modelo, anio, km_alta) "
                + "VALUES (1, 'X1', 'Marca', 'Modelo', 2010, 1)");
        try {
            assertThatThrownBy(() -> new VehiculoDAO().listar()).isInstanceOf(PersistenciaException.class);
        } finally {
            ejecutarSql("DELETE FROM tallernet.vehiculo WHERE patente='X1'");
        }
        assertThat(new VehiculoDAO().listar()).isNotEmpty();
    }

    @Test
    @DisplayName("Servidor inaccesible: cada DAO informa ConexionException (y la base se recupera)")
    void servidorInaccesible() throws Exception {
        Properties caido = configuracion();
        caido.setProperty("db.url", "jdbc:mysql://127.0.0.1:1/tallernet?connectTimeout=1000");
        ConexionBD.configurar(caido);
        try {
            Executable[] operaciones = {
                () -> new SedeDAO().listar(), () -> new SedeDAO().buscarPorId(1),
                () -> new EmpleadoDAO().listar(), () -> new EmpleadoDAO().buscarPorId(1),
                () -> new VehiculoDAO().listar(), () -> new ClienteDAO().listar(),
                () -> new ServicioDAO().listar(), () -> new TurnoDAO().listarAtendidos(),
                () -> new TurnoDAO().resumenPorSede(), () -> ConexionBD.getInstancia().verificar(),
                () -> new ClienteDAO().insertar(new tallernet.modelo.Cliente("A", "B", "11-4444-0000", null)),
                () -> new VehiculoDAO().insertar(new Vehiculo(1, "AA111AA", "M", "M", 2010, 1)),
            };
            for (Executable op : operaciones) {
                assertThatThrownBy(op::execute).isInstanceOf(ConexionException.class);
            }
        } finally {
            ConexionBD.configurar(configuracion());
        }
        assertThat(new SedeDAO().listar()).isNotEmpty();
    }

    @Test
    @DisplayName("TurnoDAO.insertar (interfaz Dao) delega en el alta con verificación de disponibilidad")
    void insertarPorInterfaz() throws Exception {
        Turno t = new Turno(new SedeDAO().buscarPorId(2).orElseThrow(), new VehiculoDAO().buscarPorPatente("AD321JK").orElseThrow(),
                new ServicioDAO().buscarPorId(5).orElseThrow(), 3, LocalDateTime.of(2026, 10, 20, 17, 0), null);
        Dao<Turno, Integer> dao = new TurnoDAO();
        assertThat(dao.insertar(t)).isPositive();
        assertThatThrownBy(() -> dao.insertar(new Turno(t.getSede(), t.getVehiculo(), t.getServicio(), 3,
                LocalDateTime.of(2026, 10, 20, 17, 15), null))).isInstanceOf(TurnoSuperpuestoException.class);
    }

    @Test
    @DisplayName("DEF-36: la base rechaza un precio base de cero, igual que el modelo (DEF-29)")
    void baseRechazaPrecioCero() {
        assertThatThrownBy(() -> ejecutarSql("UPDATE tallernet.servicio SET precio_base = 0 WHERE servicio_id = 4"))
                .isInstanceOf(java.sql.SQLException.class).hasMessageContaining("ck_servicio_precio");
    }
}
