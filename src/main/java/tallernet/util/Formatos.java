package tallernet.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatos de presentación para Argentina, centralizados para que toda la vista sea coherente. */
public final class Formatos {

    public static final Locale ARGENTINA = Locale.forLanguageTag("es-AR");
    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Formatos() {
        // Clase de utilidades: no se instancia.
    }

    /** 86500 se muestra como "86.500". */
    public static String km(int valor) {
        return NumberFormat.getIntegerInstance(ARGENTINA).format(valor);
    }

    /** 45000.00 se muestra como "$ 45.000,00". */
    public static String pesos(BigDecimal valor) {
        NumberFormat nf = NumberFormat.getNumberInstance(ARGENTINA);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return "$ " + nf.format(valor);
    }
}
