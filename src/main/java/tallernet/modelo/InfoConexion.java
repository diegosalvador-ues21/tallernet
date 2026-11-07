package tallernet.modelo;

/**
 * Datos de la conexión vigente con el servidor central, para la opción «Probar conexión».
 * Vive en el modelo (y no en el paquete conexion) para que la vista no dependa de la capa de
 * acceso a datos (DEF-31, detectado por ArchUnit).
 */
public record InfoConexion(String servidor, String usuario, String motor, String driver, String cifrado,
                           long milisegundos) {
}
