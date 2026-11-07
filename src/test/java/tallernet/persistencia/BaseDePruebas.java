package tallernet.persistencia;

import tallernet.conexion.ConexionBD;
import tallernet.excepciones.PersistenciaException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Prepara la base para las pruebas de integración: recrea el esquema y la carga base
 * ejecutando los scripts 01 y 02 del repositorio, y configura la conexión de la aplicación.
 * La configuración se toma de src/test/resources/db-test.properties.
 */
final class BaseDePruebas {

    private static Properties props;

    private BaseDePruebas() {
    }

    static synchronized Properties propiedades() throws IOException {
        if (props == null) {
            props = new Properties();
            try (InputStream in = BaseDePruebas.class.getClassLoader().getResourceAsStream("db-test.properties")) {
                if (in == null) {
                    throw new IOException("Falta src/test/resources/db-test.properties");
                }
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        }
        return props;
    }

    /** true si el servidor MySQL de pruebas responde; si no, las pruebas se omiten (no fallan). */
    static boolean disponible() {
        try (Connection con = conexionAdmin()) {
            return con.isValid(3);
        } catch (IOException | SQLException e) {
            return false;
        }
    }

    static Connection conexionAdmin() throws IOException, SQLException {
        Properties p = propiedades();
        return DriverManager.getConnection(p.getProperty("admin.url"), p.getProperty("admin.usuario"),
                p.getProperty("admin.clave"));
    }

    /** Ejecuta 01_ddl_creacion.sql y 02_insercion.sql y apunta ConexionBD al usuario de la aplicación. */
    static void recrear() throws IOException, SQLException, PersistenciaException {
        try (Connection con = conexionAdmin(); Statement st = con.createStatement()) {
            for (String script : new String[] {"db/01_ddl_creacion.sql", "db/02_insercion.sql"}) {
                for (String sentencia : sentencias(Path.of(script))) {
                    st.execute(sentencia);
                }
            }
        }
        Properties app = new Properties();
        app.setProperty("db.url", propiedades().getProperty("app.url"));
        app.setProperty("db.usuario", propiedades().getProperty("app.usuario"));
        app.setProperty("db.clave", propiedades().getProperty("app.clave"));
        ConexionBD.configurar(app);
    }

    /** Separa un script en sentencias: descarta comentarios de línea y corta en cada ';'. */
    static String[] sentencias(Path script) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String linea : Files.readAllLines(script, StandardCharsets.UTF_8)) {
            String l = linea.strip();
            if (!l.startsWith("--")) {
                sb.append(linea).append('\n');
            }
        }
        return java.util.Arrays.stream(sb.toString().split(";"))
                .map(String::strip).filter(s -> !s.isEmpty()).toArray(String[]::new);
    }

    static int contarTurnos() throws IOException, SQLException {
        try (Connection con = conexionAdmin(); Statement st = con.createStatement();
             var rs = st.executeQuery("SELECT COUNT(*) FROM tallernet.turno")) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
