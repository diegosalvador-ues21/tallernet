package tallernet.archivos;

/**
 * Registro de las operaciones que modifican datos (alta, cambio, cancelación, importación).
 * Los controladores dependen de esta interfaz; la implementación real escribe un archivo
 * ({@link Bitacora}) y las pruebas usan {@link #NINGUNO}.
 */
@FunctionalInterface
public interface RegistroOperaciones {

    /** Implementación nula: no registra nada. */
    RegistroOperaciones NINGUNO = (operacion, detalle) -> { };

    /** Nunca lanza excepciones: una falla del registro no debe impedir la operación del taller. */
    void registrar(String operacion, String detalle);
}
