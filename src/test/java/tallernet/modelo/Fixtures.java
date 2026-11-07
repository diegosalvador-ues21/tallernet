package tallernet.modelo;

import tallernet.excepciones.DatoInvalidoException;
import java.math.BigDecimal;
import java.time.LocalTime;

/** Objetos de prueba equivalentes a la carga base del script 02. */
public final class Fixtures {

    private Fixtures() {
    }

    public static Sede echeandia() {
        return new Sede(1, "Sede Echeandía", "Echeandía 3042, CABA", LocalTime.of(8, 0), LocalTime.of(20, 0), true);
    }

    public static Sede flores() {
        return new Sede(2, "Sede Flores", "Av. Rivadavia 8450, Flores, CABA", LocalTime.of(8, 0),
                LocalTime.of(20, 0), true);
    }

    public static Servicio servicio(int id, int minutos, Integer km) {
        try {
            return Servicio.crear(id, "Servicio de prueba " + id, new BigDecimal("1000.00"), minutos, km);
        } catch (DatoInvalidoException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Servicio cambioDeAceite() {
        return servicio(1, 60, 10000);
    }

    public static Servicio serviceCompleto() {
        return servicio(3, 180, 10000);
    }

    public static Vehiculo gol() {
        try {
            return new Vehiculo(1, 1, "AE123BC", "Volkswagen", "Gol Trend", 2018, 86500, null);
        } catch (DatoInvalidoException e) {
            throw new IllegalStateException(e);
        }
    }
}
