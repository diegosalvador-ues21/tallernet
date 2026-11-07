package tallernet.archivos;

import java.util.Locale;
import tallernet.excepciones.ArchivoException;
import tallernet.excepciones.TallerNetException;
import tallernet.modelo.Cliente;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Importa clientes por lote desde un CSV (nombre;apellido;telefono;email), por ejemplo la
 * planilla de contactos que hoy cada sede lleva por separado (AP1, sección 5.1).
 *
 * <p>Cada línea se valida y registra por separado: una línea con errores se informa con su
 * número y su motivo, y no impide registrar las demás. El importador no conoce la base de
 * datos: recibe la operación de alta como {@link RegistroCliente}, lo que permite probarlo sin
 * MySQL y reutilizar las mismas validaciones del alta manual.</p>
 */
public class ImportadorClientesCsv {

    /** Cantidad de columnas esperada en cada línea. */
    public static final int COLUMNAS = 4;

    /** Operación de alta que usa el importador (en la aplicación, ClienteControlador::registrarCliente). */
    @FunctionalInterface
    public interface RegistroCliente {
        Cliente registrar(String nombre, String apellido, String telefono, String email) throws TallerNetException;
    }

    /** Resultado de la importación: registrados y rechazos con su motivo. */
    public record Resultado(int lineasLeidas, List<Cliente> registrados, List<String> rechazos) {
    }

    private final RegistroCliente registro;

    public ImportadorClientesCsv(RegistroCliente registro) {
        this.registro = registro;
    }

    public Resultado importar(Path archivo) throws ArchivoException {
        List<Cliente> registrados = new ArrayList<>();
        List<String> rechazos = new ArrayList<>();
        int numero = 0;
        int leidas = 0;
        try (BufferedReader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                numero++;
                if (numero == 1) {
                    linea = linea.replace("﻿", ""); // BOM que agrega Excel
                    if (linea.toLowerCase(Locale.ROOT).startsWith("nombre")) {
                        continue; // encabezado
                    }
                }
                if (linea.isBlank()) {
                    continue;
                }
                leidas++;
                String[] campos = linea.split(";", -1); // -1 conserva el correo vacío del final
                if (campos.length != COLUMNAS) {
                    rechazos.add("Línea " + numero + ": se esperaban " + COLUMNAS + " columnas y hay "
                            + campos.length + ".");
                    continue;
                }
                try {
                    registrados.add(registro.registrar(campos[0], campos[1], campos[2], campos[3]));
                } catch (TallerNetException e) {
                    rechazos.add("Línea " + numero + " (" + campos[1].strip() + ", " + campos[0].strip() + "): "
                            + e.getMessage());
                }
            }
        } catch (NoSuchFileException e) {
            throw new ArchivoException("No existe el archivo " + archivo + ".", e);
        } catch (CharacterCodingException e) {
            throw new ArchivoException("El archivo " + archivo + " no está en UTF-8. Guárdelo como"
                    + " «CSV UTF-8» desde la planilla de cálculo.", e);
        } catch (IOException e) {
            throw new ArchivoException("No se pudo leer el archivo " + archivo + ": " + e.getMessage(), e);
        }
        return new Resultado(leidas, registrados, rechazos);
    }
}
