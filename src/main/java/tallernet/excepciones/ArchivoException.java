package tallernet.excepciones;

/** No se pudo leer o escribir un archivo de exportación, importación o bitácora. */
public class ArchivoException extends TallerNetException {

    private static final long serialVersionUID = 1L;

    public ArchivoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
