package tallernet.modelo;

/** Roles del personal definidos en RF-TN01. Coinciden con el ENUM de la tabla empleado. */
public enum Rol {
    RECEPCIONISTA("Recepcionista"),
    MECANICO("Mecánico"),
    ENCARGADO_REPUESTOS("Encargado de repuestos"),
    ADMINISTRADOR("Administrador");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
