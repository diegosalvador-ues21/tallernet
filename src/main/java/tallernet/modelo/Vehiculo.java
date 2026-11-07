package tallernet.modelo;

import java.util.Locale;
import tallernet.excepciones.DatoInvalidoException;
import tallernet.util.Formatos;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.regex.Pattern;

/** Vehículo de un cliente, identificado por su patente (RF-TN03). */
public class Vehiculo {

    public static final int KM_MAXIMO = 2_000_000;
    private static final int ANIO_MINIMO = 1950;
    /** Formato Mercosur (AB123CD) o formato anterior (ABC123). */
    private static final Pattern PATENTE = Pattern.compile("^([A-Z]{2}\\d{3}[A-Z]{2}|[A-Z]{3}\\d{3})$");

    private int id;
    private final int clienteId;
    private String patente;
    private final String marca;
    private final String modelo;
    private int anio;
    private int kmAlta;
    private final LocalDateTime fechaAlta;

    public Vehiculo(int id, int clienteId, String patente, String marca, String modelo, int anio,
                    int kmAlta, LocalDateTime fechaAlta) throws DatoInvalidoException {
        this.id = id;
        this.clienteId = clienteId;
        setPatente(patente);
        this.marca = requerido(marca, "La marca", 40);
        this.modelo = requerido(modelo, "El modelo", 60);
        setAnio(anio);
        setKmAlta(kmAlta);
        this.fechaAlta = fechaAlta;
    }

    /** Constructor para un vehículo nuevo, antes de persistirlo. */
    public Vehiculo(int clienteId, String patente, String marca, String modelo, int anio, int kmAlta)
            throws DatoInvalidoException {
        this(0, clienteId, patente, marca, modelo, anio, kmAlta, null);
    }

    /** Obligatorio y con el largo máximo de su columna en la base (DEF-13). */
    private static String requerido(String valor, String campo, int largoMaximo) throws DatoInvalidoException {
        if (valor == null || valor.isBlank()) {
            throw new DatoInvalidoException(campo + " es un dato obligatorio."); // DEF-28: concordancia
        }
        if (valor.strip().length() > largoMaximo) {
            throw new DatoInvalidoException(campo + " no puede superar " + largoMaximo + " caracteres.");
        }
        return valor.strip();
    }

    /** Quita espacios y guiones y pasa a mayúsculas: "ae-123 bc" se convierte en "AE123BC". */
    public static String normalizarPatente(String patente) {
        return patente == null ? "" : patente.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    public final void setPatente(String patente) throws DatoInvalidoException {
        String normalizada = normalizarPatente(patente);
        if (!PATENTE.matcher(normalizada).matches()) {
            throw new DatoInvalidoException("La patente debe tener el formato AB123CD o ABC123.");
        }
        this.patente = normalizada;
    }

    public final void setAnio(int anio) throws DatoInvalidoException {
        int maximo = Year.now().getValue() + 1;
        if (anio < ANIO_MINIMO || anio > maximo) {
            throw new DatoInvalidoException("El año debe estar entre " + ANIO_MINIMO + " y " + maximo + ".");
        }
        this.anio = anio;
    }

    /** Regla del AP2 (CHECK ck_vehiculo_km): el kilometraje se valida antes de persistir. */
    public final void setKmAlta(int kmAlta) throws DatoInvalidoException {
        if (kmAlta < 0 || kmAlta > KM_MAXIMO) {
            throw new DatoInvalidoException("El kilometraje debe estar entre 0 y " + KM_MAXIMO + " km.");
        }
        this.kmAlta = kmAlta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClienteId() {
        return clienteId;
    }

    public String getPatente() {
        return patente;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public int getAnio() {
        return anio;
    }

    public int getKmAlta() {
        return kmAlta;
    }

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }

    public String getDescripcion() {
        return marca + " " + modelo + " " + anio;
    }

    @Override
    public String toString() {
        return patente + " - " + getDescripcion() + " (" + Formatos.km(kmAlta) + " km al alta)";
    }
}
