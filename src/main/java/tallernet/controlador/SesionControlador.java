package tallernet.controlador;

import tallernet.excepciones.EntidadNoEncontradaException;
import tallernet.excepciones.TallerNetException;
import tallernet.modelo.Empleado;
import tallernet.persistencia.Dao;
import java.util.ArrayList;
import java.util.List;

/**
 * Identificación del empleado en sesión. La autenticación con contraseña (RF-TN01) queda para
 * una iteración posterior; mientras tanto se elige el empleado entre los habilitados.
 */
public class SesionControlador {

    private final Dao<Empleado, Integer> empleados;

    public SesionControlador(Dao<Empleado, Integer> empleados) {
        this.empleados = empleados;
    }

    /** Empleados activos con permiso para registrar turnos (recepcionistas y administrador). */
    public List<Empleado> listarHabilitados() throws TallerNetException {
        List<Empleado> habilitados = new ArrayList<>();
        for (Empleado e : empleados.listar()) {
            if (e.puedeRegistrarTurnos()) {
                habilitados.add(e);
            }
        }
        return habilitados;
    }

    public Empleado iniciarSesion(int empleadoId) throws TallerNetException {
        for (Empleado e : listarHabilitados()) {
            if (e.getId() == empleadoId) {
                return e;
            }
        }
        throw new EntidadNoEncontradaException("El número no corresponde a un empleado habilitado.");
    }
}
