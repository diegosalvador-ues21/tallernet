package tallernet.modelo;

import tallernet.excepciones.DatoInvalidoException;
import java.math.BigDecimal;
import java.util.OptionalInt;

/**
 * Servicio del catálogo común a ambas sedes (RF-TN17).
 * Es abstracta porque el catálogo tiene dos clases de trabajos: los que se repiten
 * según el kilometraje (cambio de aceite, correa) y los que no dependen del uso
 * (diagnóstico con escáner). Cada subclase responde distinto a
 * {@link #calcularProximoServiceKm(int)}: es el punto de polimorfismo del modelo.
 */
public abstract class Servicio {

    /** Máximo representable por la columna precio_base DECIMAL(10,2). */
    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("99999999.99");

    private final int id;
    private final String descripcion;
    private BigDecimal precioBase;
    private final int duracionMin;

    protected Servicio(int id, String descripcion, BigDecimal precioBase, int duracionMin)
            throws DatoInvalidoException {
        if (descripcion == null || descripcion.isBlank()) {
            throw new DatoInvalidoException("La descripción del servicio es obligatoria.");
        }
        validarPrecio(precioBase);
        if (duracionMin <= 0) {
            throw new DatoInvalidoException("La duración del servicio debe ser mayor que cero.");
        }
        this.id = id;
        this.descripcion = descripcion.strip();
        this.precioBase = precioBase;
        this.duracionMin = duracionMin;
    }

    /**
     * Fábrica: decide la subclase según exista o no un intervalo de kilometraje.
     * Así la capa de persistencia traduce el NULL de km_servicio a un tipo concreto.
     */
    public static Servicio crear(int id, String descripcion, BigDecimal precioBase, int duracionMin,
                                 Integer kmServicio) throws DatoInvalidoException {
        if (kmServicio == null) {
            return new ServicioGeneral(id, descripcion, precioBase, duracionMin);
        }
        return new ServicioPorKilometraje(id, descripcion, precioBase, duracionMin, kmServicio);
    }

    /**
     * Kilometraje al que corresponde volver a realizar el servicio.
     * @param kmReferencia lectura del odómetro tomada como base
     * @return el kilometraje estimado, o vacío si el servicio no depende del uso
     */
    public abstract OptionalInt calcularProximoServiceKm(int kmReferencia);

    /** Intervalo recomendado expresado para el usuario. */
    public abstract String describirIntervalo();

    /** Intervalo en km, o {@code null}: es el valor que se guarda en servicio.km_servicio. */
    public abstract Integer getKmServicio();

    public int getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    /** Actualización de la lista de precios (TP4). Se valida igual que en el alta. */
    public void setPrecioBase(BigDecimal precioBase) throws DatoInvalidoException {
        validarPrecio(precioBase);
        this.precioBase = precioBase;
    }

    private static void validarPrecio(BigDecimal precio) throws DatoInvalidoException {
        if (precio == null || precio.signum() < 0) {
            throw new DatoInvalidoException("El precio base no puede ser negativo.");
        }
        if (precio.signum() == 0) {
            // DEF-29: un precio en cero suele ser un error de tipeo y deja el servicio sin cobrar.
            throw new DatoInvalidoException("El precio base debe ser mayor que cero.");
        }
        if (precio.scale() > 2 || precio.compareTo(PRECIO_MAXIMO) > 0) {
            throw new DatoInvalidoException("El precio debe tener hasta dos decimales y no superar "
                    + "99.999.999,99 (columna DECIMAL(10,2)).");
        }
    }

    public int getDuracionMin() {
        return duracionMin;
    }

    @Override
    public String toString() {
        return String.format("%d. %s (%d min, %s)", id, descripcion, duracionMin, describirIntervalo());
    }
}
