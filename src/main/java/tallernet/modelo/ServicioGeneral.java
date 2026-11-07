package tallernet.modelo;

import tallernet.excepciones.DatoInvalidoException;
import java.math.BigDecimal;
import java.util.OptionalInt;

/**
 * Servicio que no depende del kilometraje (diagnóstico con escáner, carga de aire).
 * Devolver un valor vacío en lugar de 0 evita el dato falso que originó el defecto DEF-02 del AP2.
 */
public class ServicioGeneral extends Servicio {

    public ServicioGeneral(int id, String descripcion, BigDecimal precioBase, int duracionMin)
            throws DatoInvalidoException {
        super(id, descripcion, precioBase, duracionMin);
    }

    @Override
    public OptionalInt calcularProximoServiceKm(int kmReferencia) {
        return OptionalInt.empty();
    }

    @Override
    public String describirIntervalo() {
        return "sin intervalo de km";
    }

    @Override
    public Integer getKmServicio() {
        return null;
    }
}
