package tallernet;

import tallernet.archivos.Bitacora;
import tallernet.conexion.ConexionBD;
import tallernet.controlador.ArchivoControlador;
import tallernet.controlador.CatalogoControlador;
import tallernet.controlador.ClienteControlador;
import tallernet.controlador.SesionControlador;
import tallernet.controlador.SistemaControlador;
import tallernet.controlador.TurnoControlador;
import tallernet.controlador.ValidadorTurno;
import tallernet.excepciones.ConexionException;
import tallernet.excepciones.PersistenciaException;
import tallernet.persistencia.ClienteDAO;
import tallernet.persistencia.EmpleadoDAO;
import tallernet.persistencia.SedeDAO;
import tallernet.persistencia.ServicioDAO;
import tallernet.persistencia.TurnoDAO;
import tallernet.persistencia.VehiculoDAO;
import tallernet.vista.LectorEntrada;
import tallernet.vista.VistaConsola;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/**
 * Punto de entrada del prototipo. Crea los objetos de cada capa y los conecta
 * (vista → controladores → repositorios → conexión). A diferencia del modelo ECOViento,
 * {@code main} no declara excepciones: todas se tratan dentro del programa.
 */
public final class Main {

    private static final ZoneId BUENOS_AIRES = ZoneId.of("America/Argentina/Buenos_Aires");

    private Main() {
    }

    public static void main(String[] args) {
        // Con consola interactiva se respeta su juego de caracteres (en Windows, la página de códigos
        // activa); con entrada y salida redirigidas se usa UTF-8. Ver DEF-01 del AP2 y DEF-04.
        Charset juego = System.console() != null ? System.console().charset() : StandardCharsets.UTF_8;
        PrintStream out = new PrintStream(System.out, true, juego);
        try {
            verificarConexion();
        } catch (ConexionException e) {
            out.println("[SIN CONEXIÓN] " + e.getMessage());
            System.exit(1); // código distinto de cero: un script de arranque puede detectar el fallo
        } catch (PersistenciaException e) {
            out.println("[ERROR DE BASE DE DATOS] " + e.getMessage());
            System.exit(1);
        }

        Clock reloj = crearReloj(out);
        SedeDAO sedeDAO = new SedeDAO();
        ServicioDAO servicioDAO = new ServicioDAO();
        ClienteDAO clienteDAO = new ClienteDAO();
        VehiculoDAO vehiculoDAO = new VehiculoDAO();
        EmpleadoDAO empleadoDAO = new EmpleadoDAO();
        TurnoDAO turnoDAO = new TurnoDAO();

        // Carpeta de trabajo de los archivos (TP4): -Dtallernet.datos=<ruta>; por omisión, el directorio actual.
        Path datos = Path.of(System.getProperty("tallernet.datos", "."));
        Bitacora bitacora = new Bitacora(datos.resolve("logs").resolve("tallernet.log"), reloj);

        ValidadorTurno validador = new ValidadorTurno(reloj);
        TurnoControlador turnoControlador = new TurnoControlador(turnoDAO, vehiculoDAO, sedeDAO, servicioDAO,
                validador, bitacora);
        ClienteControlador clienteControlador = new ClienteControlador(clienteDAO, vehiculoDAO, turnoDAO, bitacora);
        CatalogoControlador catalogoControlador = new CatalogoControlador(sedeDAO, servicioDAO, turnoDAO, bitacora);
        ArchivoControlador archivoControlador = new ArchivoControlador(turnoControlador, catalogoControlador,
                clienteControlador, datos.resolve("exportaciones"));

        SesionControlador sesionControlador = new SesionControlador(empleadoDAO);
        SistemaControlador sistemaControlador = new SistemaControlador(bitacora);

        boolean entradaRedirigida = System.console() == null;
        LectorEntrada lector = new LectorEntrada(System.in, juego, out, entradaRedirigida);
        new VistaConsola(turnoControlador, clienteControlador, catalogoControlador, sesionControlador,
                archivoControlador, sistemaControlador, lector, out)
                .iniciar();
    }

    private static void verificarConexion() throws PersistenciaException {
        ConexionBD.getInstancia().verificar();
    }

    /**
     * Reloj del sistema en hora de Buenos Aires. La propiedad {@code tallernet.ahora}
     * (por ejemplo 2026-10-01T08:00) fija la fecha actual para reproducir los escenarios
     * de prueba del AP2, que usan fechas sintéticas de octubre de 2026.
     */
    private static Clock crearReloj(PrintStream out) {
        String ahora = System.getProperty("tallernet.ahora");
        if (ahora == null) {
            return Clock.system(BUENOS_AIRES);
        }
        try {
            LocalDateTime fijo = LocalDateTime.parse(ahora);
            out.println("(Fecha de referencia fijada para pruebas: " + fijo + ")");
            return Clock.fixed(fijo.atZone(BUENOS_AIRES).toInstant(), BUENOS_AIRES);
        } catch (DateTimeParseException e) {
            out.println("[!] tallernet.ahora no tiene el formato aaaa-mm-ddTHH:mm; se usa la fecha actual.");
            return Clock.system(BUENOS_AIRES);
        }
    }
}
