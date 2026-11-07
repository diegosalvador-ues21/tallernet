package tallernet.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import tallernet.conexion.ConexionBD;
import tallernet.excepciones.RegistroDuplicadoException;
import tallernet.modelo.Cliente;
import tallernet.modelo.Vehiculo;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * CPI05 - Altas contra un MySQL 8.0 DESCARTABLE (Testcontainers): independiente de la base de
 * desarrollo. Cada alta se verifica con un oráculo SQL directo (lo informado = lo guardado).
 */
@Tag("integracion")
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("CPI05 - Altas con MySQL descartable (Testcontainers)")
class AltasContenedorTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("tallernet").withUsername("root").withPassword("raiz-de-prueba")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

    private static String url;

    @BeforeAll
    static void crearEsquema() throws Exception {
        url = MYSQL.getJdbcUrl() + "?sslMode=PREFERRED&characterEncoding=UTF-8";
        try (Connection con = DriverManager.getConnection(url, "root", "raiz-de-prueba"); Statement st = con.createStatement()) {
            for (String script : new String[] {"db/01_ddl_creacion.sql", "db/02_insercion.sql"}) {
                for (String sentencia : BaseDePruebas.sentencias(Path.of(script))) {
                    st.execute(sentencia);
                }
            }
        }
        Properties p = new Properties();
        p.setProperty("db.url", url);
        p.setProperty("db.usuario", "root");
        p.setProperty("db.clave", "raiz-de-prueba");
        ConexionBD.configurar(p);
    }

    private static String consultar(String sql) throws Exception {
        try (Connection con = DriverManager.getConnection(url, "root", "raiz-de-prueba");
             Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    @Test
    @DisplayName("El vehículo informado como registrado está en la base con los datos normalizados")
    void altaDeVehiculoPersistida() throws Exception {
        Vehiculo v = new Vehiculo(1, "lbs 854", "Renault", "Fluence 2.0 CVT Privilege", 2013, 195555);
        new VehiculoDAO().insertar(v);
        assertThat(consultar("SELECT CONCAT(patente,'|',cliente_id,'|',modelo,'|',km_alta) FROM tallernet.vehiculo WHERE vehiculo_id=" + v.getId()))
                .isEqualTo("LBS854|1|Fluence 2.0 CVT Privilege|195555");
    }

    @Test
    @DisplayName("Inyección SQL en los textos: se guarda literal y las tablas quedan intactas")
    void inyeccionSeGuardaLiteral() throws Exception {
        Cliente c = new Cliente("Robert'); DROP TABLE cliente;--", "O'Brien", "11-4444-6666", null);
        new ClienteDAO().insertar(c);
        assertThat(consultar("SELECT nombre FROM tallernet.cliente WHERE cliente_id=" + c.getId()))
                .isEqualTo("Robert'); DROP TABLE cliente;--");
        assertThat(consultar("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='tallernet'")).isEqualTo("6");
    }

    @Test
    @DisplayName("Acentos: «Núñez» se guarda en UTF-8 (C3 91 / C3 BA)")
    void acentosEnUtf8() throws Exception {
        Cliente c = new Cliente("Tomás", "Núñez", "11-4444-8888", null);
        new ClienteDAO().insertar(c);
        assertThat(consultar("SELECT HEX(apellido) FROM tallernet.cliente WHERE cliente_id=" + c.getId()))
                .isEqualTo("4EC3BAC3B1657A");
    }

    @Test
    @DisplayName("Un rechazo por patente duplicada no deja filas nuevas")
    void duplicadoNoDejaFilas() throws Exception {
        String antes = consultar("SELECT COUNT(*) FROM tallernet.vehiculo");
        assertThatThrownBy(() -> new VehiculoDAO().insertar(new Vehiculo(2, "AE123BC", "Ford", "Ka", 2015, 1000)))
                .isInstanceOf(RegistroDuplicadoException.class);
        assertThat(consultar("SELECT COUNT(*) FROM tallernet.vehiculo")).isEqualTo(antes);
    }
}
