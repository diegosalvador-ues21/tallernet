package tallernet.excepciones;

/**
 * Raíz de la jerarquía de excepciones de negocio de TALLERNET.
 * Es una excepción verificada (checked): el compilador obliga a quien invoca
 * una operación de negocio a tratar el error o declararlo.
 */
public class TallerNetException extends Exception {

    private static final long serialVersionUID = 1L;

    public TallerNetException(String mensaje) {
        super(mensaje);
    }

    public TallerNetException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
