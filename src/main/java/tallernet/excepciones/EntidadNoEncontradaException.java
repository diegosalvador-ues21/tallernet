package tallernet.excepciones;

/** Se buscó un registro (cliente, vehículo, turno, sede, servicio) que no existe. */
public class EntidadNoEncontradaException extends TallerNetException {

    private static final long serialVersionUID = 1L;

    public EntidadNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
