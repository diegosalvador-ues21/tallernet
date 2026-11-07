package tallernet.archivos;

import tallernet.excepciones.ArchivoException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Bitácora de operaciones en un archivo de texto (una línea por operación, modo agregar).
 * Permite al titular reconstruir quién hizo qué y cuándo en cada puesto. No guarda datos
 * sensibles: solo números de turno, cliente o servicio.
 */
public class Bitacora implements RegistroOperaciones {

    private static final DateTimeFormatter MARCA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path archivo;
    private final Clock reloj;
    private String puesto = "-";
    /** Último error de escritura, para avisar una sola vez sin interrumpir la operación. */
    private String ultimoError;

    public Bitacora(Path archivo, Clock reloj) {
        this.archivo = archivo;
        this.reloj = reloj;
    }

    /** Identifica a quién se atribuyen las operaciones siguientes (empleado y sede en sesión). */
    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    @Override
    public void registrar(String operacion, String detalle) {
        String linea = String.join(" | ", LocalDateTime.now(reloj).format(MARCA), puesto, operacion, detalle)
                + System.lineSeparator();
        try {
            Path carpeta = archivo.toAbsolutePath().getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            Files.writeString(archivo, linea, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
            ultimoError = null;
        } catch (IOException e) {
            ultimoError = "No se pudo escribir la bitácora " + archivo + ": " + e.getMessage();
        }
    }

    /** Mensaje del último error de escritura, o {@code null} si la última escritura fue correcta. */
    public String getUltimoError() {
        return ultimoError;
    }

    /** Devuelve el último error y lo descarta, para avisarlo una sola vez (DEF-22). */
    public String tomarUltimoError() {
        String error = ultimoError;
        ultimoError = null;
        return error;
    }

    /**
     * Últimas {@code cantidad} líneas, de la más antigua a la más reciente. Se leen con un búfer
     * circular (arreglo de tamaño fijo) para no conservar en memoria todo el archivo.
     */
    public List<String> ultimas(int cantidad) throws ArchivoException {
        String[] buffer = new String[Math.max(1, cantidad)];
        int leidas = 0;
        try (var lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                buffer[leidas % buffer.length] = linea;
                leidas++;
            }
        } catch (NoSuchFileException e) {
            return new ArrayList<>();
        } catch (IOException e) {
            throw new ArchivoException("No se pudo leer la bitácora " + archivo + ": " + e.getMessage(), e);
        }
        List<String> resultado = new ArrayList<>();
        int total = Math.min(leidas, buffer.length);
        for (int i = leidas - total; i < leidas; i++) {
            resultado.add(buffer[i % buffer.length]);
        }
        return resultado;
    }

    public Path getArchivo() {
        return archivo;
    }
}
