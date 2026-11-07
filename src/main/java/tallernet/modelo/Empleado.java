package tallernet.modelo;

import tallernet.excepciones.DatoInvalidoException;

/** Integrante del personal de una sede (RF-TN01). Queda registrado quién tomó cada turno. */
public class Empleado extends Persona {

    private final int sedeId;
    private final Rol rol;
    private final boolean activo;

    public Empleado(int id, int sedeId, String nombre, String apellido, Rol rol, boolean activo)
            throws DatoInvalidoException {
        super(id, nombre, apellido);
        if (rol == null) {
            throw new DatoInvalidoException("El rol del empleado es obligatorio.");
        }
        this.sedeId = sedeId;
        this.rol = rol;
        this.activo = activo;
    }

    /** Solo recepcionistas y el administrador registran turnos (actores del CU001). */
    public boolean puedeRegistrarTurnos() {
        return activo && (rol == Rol.RECEPCIONISTA || rol == Rol.ADMINISTRADOR);
    }

    @Override
    public String getDescripcionRol() {
        return rol.getEtiqueta();
    }

    @Override
    public String getDatoDeContacto() {
        return "sede " + sedeId;
    }

    public int getSedeId() {
        return sedeId;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }
}
