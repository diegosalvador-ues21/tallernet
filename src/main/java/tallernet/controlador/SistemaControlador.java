package tallernet.controlador;

import tallernet.archivos.Bitacora;
import tallernet.conexion.ConexionBD;
import tallernet.excepciones.TallerNetException;
import tallernet.modelo.InfoConexion;
import java.util.List;

/** Opciones de soporte del menú Sistema: diagnóstico de la conexión y consulta de la bitácora. */
public class SistemaControlador {

    private final Bitacora bitacora;

    public SistemaControlador(Bitacora bitacora) {
        this.bitacora = bitacora;
    }

    /** Prueba la conexión con el servidor central y devuelve versión, cifrado y tiempo de respuesta. */
    public InfoConexion probarConexion() throws TallerNetException {
        return ConexionBD.getInstancia().diagnosticar();
    }

    public List<String> ultimasOperaciones(int cantidad) throws TallerNetException {
        return bitacora.ultimas(cantidad);
    }

    public Bitacora getBitacora() {
        return bitacora;
    }
}
