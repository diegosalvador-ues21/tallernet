package tallernet.vista;

import static org.assertj.core.api.Assertions.assertThat;

import tallernet.archivos.Bitacora;
import tallernet.conexion.ConexionBD;
import tallernet.controlador.ArchivoControlador;
import tallernet.controlador.CatalogoControlador;
import tallernet.controlador.ClienteControlador;
import tallernet.controlador.SesionControlador;
import tallernet.controlador.SistemaControlador;
import tallernet.controlador.TurnoControlador;
import tallernet.controlador.ValidadorTurno;
import tallernet.persistencia.ClienteDAO;
import tallernet.persistencia.EmpleadoDAO;
import tallernet.persistencia.SedeDAO;
import tallernet.persistencia.ServicioDAO;
import tallernet.persistencia.TurnoDAO;
import tallernet.persistencia.VehiculoDAO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * CPS03 - Prueba de sistema dentro del proceso: la aplicación armada igual que en Main (vista +
 * controladores + DAO + archivos) recorre las 30 opciones del menú contra un MySQL descartable.
 * A diferencia de las sesiones s01-s14, aquí JaCoCo mide la vista. Oráculo: SQL directo y archivos.
 */
@Tag("integracion")
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("CPS03 - Recorrido completo de menús (sistema en proceso, Testcontainers)")
class RecorridoContenedorTest {

    private static final String CLAVE = "raiz-de-prueba";

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("tallernet").withUsername("root").withPassword(CLAVE)
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

    private static String url;

    @TempDir
    static Path datos;

    @BeforeAll
    static void crearEsquema() throws Exception {
        url = MYSQL.getJdbcUrl() + "?sslMode=PREFERRED&characterEncoding=UTF-8";
        try (Connection con = DriverManager.getConnection(url, "root", CLAVE); Statement st = con.createStatement()) {
            for (String script : new String[] {"db/01_ddl_creacion.sql", "db/02_insercion.sql"}) {
                for (String sentencia : tallernet.persistencia.BaseDePruebasPublica.sentencias(Path.of(script))) {
                    st.execute(sentencia);
                }
            }
        }
        Properties p = new Properties();
        p.setProperty("db.url", url);
        p.setProperty("db.usuario", "root");
        p.setProperty("db.clave", CLAVE);
        ConexionBD.configurar(p);
    }

    private static String consultar(String sql) throws Exception {
        try (Connection con = DriverManager.getConnection(url, "root", CLAVE);
             Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    private static String sesion(String... lineas) {
        ZoneId ba = ZoneId.of("America/Argentina/Buenos_Aires");
        Clock reloj = Clock.fixed(LocalDateTime.of(2026, 10, 1, 8, 0).atZone(ba).toInstant(), ba);
        SedeDAO sedes = new SedeDAO();
        ServicioDAO servicios = new ServicioDAO();
        VehiculoDAO vehiculos = new VehiculoDAO();
        TurnoDAO turnosDao = new TurnoDAO();
        Bitacora bitacora = new Bitacora(datos.resolve("logs").resolve("tallernet.log"), reloj);
        TurnoControlador turnos = new TurnoControlador(turnosDao, vehiculos, sedes, servicios,
                new ValidadorTurno(reloj), bitacora);
        ClienteControlador clientes = new ClienteControlador(new ClienteDAO(), vehiculos, turnosDao, bitacora);
        CatalogoControlador catalogo = new CatalogoControlador(sedes, servicios, turnosDao, bitacora);
        ArchivoControlador archivos = new ArchivoControlador(turnos, catalogo, clientes, datos.resolve("exportaciones"));
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(salida, true, StandardCharsets.UTF_8);
        String entrada = String.join("\n", lineas) + "\n";
        LectorEntrada lector = new LectorEntrada(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8, out, true);
        new VistaConsola(turnos, clientes, catalogo, new SesionControlador(new EmpleadoDAO()), archivos,
                new SistemaControlador(bitacora), lector, out).iniciar();
        return salida.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Las 30 opciones: lo informado en pantalla está en la base y en los archivos")
    void recorridoCompleto() throws Exception {
        Path csv = datos.resolve("clientes.csv");
        Files.writeString(csv, "nombre;apellido;telefono;email\nPaula;Ríos;11-4000-1111;paula@mail.com\nSin;Columnas\n",
                StandardCharsets.UTF_8);
        List<String> l = new ArrayList<>(List.of("9", "2")); // número inexistente, luego Carla
        l.addAll(List.of("1",
                "1", "1", "ae123bc", "1", "07/10/2026", "10:00", "Ruido",       // alta -> turno 7
                "1", "1", "AC456DE", "1", "07/10/2026", "10:30", "",            // S5 con sugerencia
                "1", "1", "ZZ999ZZ",                                            // patente inexistente (DEF-26)
                "10", "1", "07/10/2026", "5", "1", "07/10/2026", "6", "7",
                "2", "7", "08/10/2026", "11:00",
                "3", "7", "S", "4", "3", "7", "N",
                "7", "7", "9", "8", "8", "9", "0"));
        l.addAll(List.of("2",
                "1", "Tomás", "Núñez", "11-4444-8888", "",                    // cliente 5
                "2", "5", "lbs 854", "Renault", "Fluence 2.0 CVT Privilege", "2013", "195555",
                "2", "99", "N",
                "3", "1", "3", "2", "4", "LBS854", "4", "XX000XX", "5", "AE123BC",
                "6", "5", "11-5555-0000", "-", "0"));
        l.addAll(List.of("3", "1", "2", "3", "4", "5", "2", "40.000,00", "5", "2", "0", "0"));
        l.addAll(List.of("4"));
        l.addAll(List.of("5", "1", "07/10/2026", "2", "1", "07/10/2026", "3", "07/10/2026", "4", csv.toString(),
                "4", datos.resolve("no-existe.csv").toString(), "0"));
        l.addAll(List.of("6", "1", "2", "0", "0"));
        String s = sesion(l.toArray(String[]::new));

        assertThat(s).contains("Sesión iniciada: Carla Medina", "Turno N.° 7 CONFIRMADO", "[S5]",
                "Primer horario libre", "No hay un vehículo registrado con la patente ZZ999ZZ",
                "Cancelación revertida: Turno N.° 7 CONFIRMADO", "Atendiendo Turno N.° 7",
                "Registrados: 1 | Rechazados: 1", "Conexión correcta", "Sesión finalizada")
                .doesNotContain("[ERROR INTERNO]", "[ERROR DE BASE DE DATOS]");
        assertThat(consultar("SELECT CONCAT(estado,'|',DATE_FORMAT(fecha_hora,'%d/%m %H:%i')) FROM tallernet.turno WHERE turno_id=7"))
                .isEqualTo("ATENDIDO|08/10 11:00");
        assertThat(consultar("SELECT COUNT(*) FROM tallernet.turno")).isEqualTo("7");
        assertThat(consultar("SELECT CONCAT(cliente_id,'|',km_alta) FROM tallernet.vehiculo WHERE patente='LBS854'"))
                .isEqualTo("5|195555");
        assertThat(consultar("SELECT CONCAT(telefono,'|',IFNULL(email,'-')) FROM tallernet.cliente WHERE cliente_id=5"))
                .isEqualTo("11-5555-0000|-");
        assertThat(consultar("SELECT precio_base FROM tallernet.servicio WHERE servicio_id=2")).isEqualTo("40000.00");
        assertThat(consultar("SELECT COUNT(*) FROM tallernet.cliente WHERE apellido='Ríos'")).isEqualTo("1");
        try (var archivos = Files.list(datos.resolve("exportaciones"))) {
            assertThat(archivos.count()).isEqualTo(2);
        }
        assertThat(Files.readString(datos.resolve("logs").resolve("tallernet.log"))).contains("ALTA_TURNO", "CANCELACION");
    }
}
