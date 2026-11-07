package tallernet.modelo;

import tallernet.excepciones.DatoInvalidoException;
import tallernet.util.Formatos;
import java.math.BigDecimal;
import java.util.OptionalInt;

/** Servicio que se repite cada cierta cantidad de kilómetros (p. ej., cambio de aceite cada 10.000 km). */
public class ServicioPorKilometraje extends Servicio {

    private final int kmServicio;

    public ServicioPorKilometraje(int id, String descripcion, BigDecimal precioBase, int duracionMin,
                                  int kmServicio) throws DatoInvalidoException {
        super(id, descripcion, precioBase, duracionMin);
        if (kmServicio <= 0) {
            throw new DatoInvalidoException("El intervalo de kilometraje debe ser mayor que cero.");
        }
        this.kmServicio = kmServicio;
    }

    @Override
    public OptionalInt calcularProximoServiceKm(int kmReferencia) {
        return OptionalInt.of(kmReferencia + kmServicio);
    }

    @Override
    public String describirIntervalo() {
        return "cada " + Formatos.km(kmServicio) + " km";
    }

    @Override
    public Integer getKmServicio() {
        return kmServicio;
    }
}
