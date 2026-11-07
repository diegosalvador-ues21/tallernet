package tallernet.archivos;

import tallernet.excepciones.ArchivoException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Exportación a CSV con separador punto y coma, el que Excel usa por omisión con la
 * configuración regional de Argentina (la coma es el separador decimal).
 *
 * <p>Clase abstracta con método plantilla: {@link #exportar} resuelve la escritura, la
 * codificación, el escape de campos y los errores de E/S; cada subclase define solo el
 * encabezado y las filas. Cada fila es un arreglo {@code String[]} con una posición por columna,
 * porque la cantidad de columnas es fija para cada tipo de exportación.</p>
 */
public abstract class ExportadorCsv<T> implements Exportador<T> {

    public static final char SEPARADOR = ';';
    /** Marca de orden de bytes: hace que Excel en Windows reconozca el archivo como UTF-8. */
    private static final char BOM = '﻿';

    /** Títulos de las columnas. */
    protected abstract String[] encabezado();

    /** Filas de datos; cada arreglo debe tener tantas posiciones como el encabezado. */
    protected abstract List<String[]> filas(T datos);

    @Override
    public final Path exportar(T datos, Path carpeta) throws ArchivoException {
        Path destino = carpeta.resolve(nombreArchivo(datos));
        try {
            Files.createDirectories(carpeta);
            try (BufferedWriter w = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
                w.write(BOM);
                escribirFila(w, encabezado());
                for (String[] fila : filas(datos)) {
                    escribirFila(w, fila);
                }
            }
            return destino;
        } catch (IOException e) {
            throw new ArchivoException("No se pudo escribir el archivo " + destino + ": " + e.getMessage(), e);
        }
    }

    private static void escribirFila(BufferedWriter w, String[] campos) throws IOException {
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                w.write(SEPARADOR);
            }
            w.write(escapar(campos[i]));
        }
        w.write("\r\n");
    }

    /** Un campo con separador, comillas o salto de línea va entre comillas, con las comillas duplicadas. */
    static String escapar(String campo) {
        if (campo == null) {
            return "";
        }
        if (campo.indexOf(SEPARADOR) >= 0 || campo.indexOf('"') >= 0 || campo.indexOf('\n') >= 0
                || campo.indexOf('\r') >= 0) {
            return '"' + campo.replace("\"", "\"\"") + '"';
        }
        return campo;
    }
}
