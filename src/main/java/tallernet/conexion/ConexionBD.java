package tallernet.conexion;

import java.io.Reader;
import tallernet.excepciones.ConexionException;
import tallernet.excepciones.PersistenciaException;
import tallernet.modelo.InfoConexion;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Punto único de acceso a la configuración de la base (patrón Singleton, AP2 sección 2.1).
 * Conserva la configuración y entrega una conexión nueva por operación: compartir una
 * sola conexión entre sedes haría que un corte de red en una sede afecte a la otra.
 *
 * <p>Origen de la configuración: el archivo indicado por la propiedad de sistema
 * {@code tallernet.config} o, si no se indica, {@code db.properties} en el directorio de trabajo.
 * Si ese archivo no existe, se busca el recurso {@code db.properties} en el classpath.</p>
 */
public final class ConexionBD {

    private static final String ARCHIVO = "db.properties";
    private static ConexionBD instancia;

    private final String url;
    private final String usuario;
    private final String clave;

    private ConexionBD(Properties props) throws PersistenciaException {
        this.url = requerida(props, "db.url");
        this.usuario = requerida(props, "db.usuario");
        this.clave = props.getProperty("db.clave", "");
    }

    /** Devuelve la única instancia; la crea la primera vez (inicialización perezosa y sincronizada). */
    public static synchronized ConexionBD getInstancia() throws PersistenciaException {
        if (instancia == null) {
            instancia = new ConexionBD(cargarConfiguracion());
        }
        return instancia;
    }

    /** Permite a las pruebas de integración fijar una configuración propia. */
    public static synchronized void configurar(Properties props) throws PersistenciaException {
        instancia = new ConexionBD(props);
    }

    /**
     * Abre una conexión nueva. Quien la pide la cierra con try-with-resources.
     * Un error de comunicación (SQLState 08xxx), de credenciales o de TLS se informa como
     * {@link ConexionException}, con una indicación de qué revisar, sin la traza del driver.
     */
    public Connection getConexion() throws PersistenciaException {
        try {
            return DriverManager.getConnection(url, usuario, clave);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1045) {
                throw new ConexionException("El servidor central rechazó el usuario o la contraseña."
                        + " Revise db.usuario y db.clave en " + ARCHIVO + ".", e);
            }
            throw new ConexionException("No se pudo conectar con el servidor central de TALLERNET ("
                    + servidor() + "). Verifique Internet, la VPN de la sede y el archivo " + ARCHIVO + ".", e);
        }
    }

    /**
     * Verifica al iniciar que el servidor central responda (antes, en Main, que dependía de java.sql;
     * DEF-31). Lanza ConexionException con la indicación de qué revisar.
     */
    public void verificar() throws PersistenciaException {
        try (Connection con = getConexion()) {
            if (!con.isValid(5)) {
                throw new ConexionException("El servidor central no respondió a tiempo.", null);
            }
        } catch (SQLException e) {
            throw new ConexionException("No se pudo verificar la conexión con el servidor central.", e);
        }
    }

    /**
     * Abre una conexión, consulta la versión del motor y el cifrado negociado, y mide el tiempo.
     * Sirve para diagnosticar desde la sede si el problema es la red o la aplicación.
     */
    public InfoConexion diagnosticar() throws PersistenciaException {
        long inicio = System.nanoTime();
        try (Connection con = getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SHOW SESSION STATUS LIKE 'Ssl_cipher'")) {
            String valor = null;
            if (rs.next()) {
                valor = rs.getString(2);
            }
            String cifrado = valor != null && !valor.isBlank() ? "TLS (" + valor + ")" : "sin cifrar";
            DatabaseMetaData meta = con.getMetaData();
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            return new InfoConexion(servidor(), usuario, meta.getDatabaseProductName() + " "
                    + meta.getDatabaseProductVersion(), meta.getDriverName() + " " + meta.getDriverVersion(),
                    cifrado, ms);
        } catch (SQLException e) {
            throw new ConexionException("La conexión se abrió pero el servidor no respondió la consulta de"
                    + " diagnóstico. Vuelva a intentar.", e);
        }
    }

    /** Servidor, puerto y base tomados de la URL, sin parámetros (nunca se muestra la clave). */
    private String servidor() {
        String sinPrefijo = url.replaceFirst("^jdbc:mysql://", "");
        int q = sinPrefijo.indexOf('?');
        return q >= 0 ? sinPrefijo.substring(0, q) : sinPrefijo;
    }

    private static Properties cargarConfiguracion() throws PersistenciaException {
        Properties props = new Properties();
        String externo = System.getProperty("tallernet.config");
        Path local = externo != null ? Path.of(externo) : Path.of(ARCHIVO);
        try {
            if (Files.exists(local)) {
                try (var lector = Files.newBufferedReader(local, StandardCharsets.UTF_8)) {
                    props.load(lector);
                }
                return props;
            }
            InputStream in = ConexionBD.class.getClassLoader().getResourceAsStream(ARCHIVO);
            if (in == null) {
                throw new PersistenciaException("No se encontró " + ARCHIVO
                        + ". Copie db.properties.example como db.properties y complete los datos.", null);
            }
            try (Reader lector = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                props.load(lector);
                return props;
            }
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer la configuración de la base: " + e.getMessage(), e);
        }
    }

    private static String requerida(Properties props, String clave) throws PersistenciaException {
        String valor = props.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            throw new PersistenciaException("Falta la propiedad " + clave + " en " + ARCHIVO + ".", null);
        }
        return valor.strip();
    }
}
