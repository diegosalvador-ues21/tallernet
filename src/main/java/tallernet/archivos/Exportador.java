package tallernet.archivos;

import tallernet.excepciones.ArchivoException;
import java.nio.file.Path;

/**
 * Contrato común de las exportaciones a archivo. La vista y los controladores trabajan con
 * esta interfaz: agregar un formato nuevo (por ejemplo, PDF) no exige cambiar a quien exporta.
 *
 * @param <T> tipo de dato que se exporta (una agenda, un reporte)
 */
public interface Exportador<T> {

    /** Nombre del archivo a generar para esos datos, sin carpeta. */
    String nombreArchivo(T datos);

    /**
     * Escribe el archivo dentro de {@code carpeta} (la crea si no existe).
     * @return ruta del archivo generado
     */
    Path exportar(T datos, Path carpeta) throws ArchivoException;
}
