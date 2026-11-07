package tallernet.modelo;

/** Estados posibles de un turno. Coinciden con el ENUM de la tabla turno. */
public enum EstadoTurno {
    CONFIRMADO,
    CANCELADO,
    ATENDIDO;

    /** Un turno ocupa la agenda mientras no esté cancelado. */
    public boolean ocupaAgenda() {
        return this != CANCELADO;
    }
}
