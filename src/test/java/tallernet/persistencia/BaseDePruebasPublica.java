package tallernet.persistencia;

import java.io.IOException;
import java.nio.file.Path;

/** Acceso público al lector de scripts SQL de {@link BaseDePruebas} para pruebas de otros paquetes. */
public final class BaseDePruebasPublica {

    private BaseDePruebasPublica() {
    }

    public static String[] sentencias(Path script) throws IOException {
        return BaseDePruebas.sentencias(script);
    }
}
