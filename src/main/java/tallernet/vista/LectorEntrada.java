package tallernet.vista;

import java.util.Locale;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Lectura robusta de datos desde la consola. A diferencia del modelo ECOViento, que usaba
 * {@code nextInt()} y se interrumpía ante una letra, aquí toda entrada se lee como línea y se
 * convierte con manejo de {@link NumberFormatException}, repitiendo la pregunta hasta obtener
 * un valor válido.
 */
public class LectorEntrada {

    /** Se lanza cuando la entrada estándar se cierra (fin de archivo); el menú termina ordenadamente. */
    public static class EntradaFinalizadaException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public EntradaFinalizadaException() {
            super("Fin de la entrada");
        }
    }

    private final Scanner scanner;
    private final PrintStream salida;
    /** Con la entrada redirigida desde un archivo se repite lo leído, para que la salida sea legible. */
    private final boolean eco;

    public LectorEntrada(InputStream entrada, Charset juego, PrintStream salida, boolean eco) {
        this.scanner = new Scanner(entrada, juego);
        this.salida = salida;
        this.eco = eco;
    }

    public String leerTexto(String pregunta) {
        salida.print(pregunta);
        try {
            String linea = scanner.nextLine();
            if (eco) {
                salida.println(linea);
            }
            return linea.strip();
        } catch (NoSuchElementException e) {
            salida.println();
            throw new EntradaFinalizadaException();
        }
    }

    /** Insiste hasta recibir un número entero. */
    public int leerEntero(String pregunta) {
        while (true) {
            String texto = leerTexto(pregunta);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                salida.println("  [!] Ingrese un número entero válido.");
            }
        }
    }

    /** Insiste hasta recibir S o N. */
    public boolean confirmar(String pregunta) {
        while (true) {
            String texto = leerTexto(pregunta + " (S/N): ").toUpperCase(Locale.ROOT);
            if (texto.equals("S") || texto.equals("N")) {
                return texto.equals("S");
            }
            salida.println("  [!] Responda S o N.");
        }
    }
}
