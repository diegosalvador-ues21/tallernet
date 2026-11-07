package tallernet.controlador;

import tallernet.archivos.AgendaCsv;
import tallernet.archivos.Exportador;
import tallernet.archivos.ImportadorClientesCsv;
import tallernet.archivos.ReporteSemanalTxt;
import tallernet.excepciones.DatoInvalidoException;
import tallernet.excepciones.TallerNetException;
import tallernet.modelo.AgendaDiaria;
import tallernet.modelo.ReporteSemanal;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Exportación e importación de archivos (TP4). Obtiene los datos de los otros controladores
 * y delega el formato en implementaciones de {@link Exportador}: depende de la interfaz, no
 * del formato concreto.
 */
public class ArchivoControlador {

    private final TurnoControlador turnos;
    private final CatalogoControlador catalogo;
    private final ClienteControlador clientes;
    private final Path carpetaExportaciones;
    private final Exportador<AgendaDiaria> exportadorAgenda = new AgendaCsv();
    private final Exportador<ReporteSemanal> exportadorReporte = new ReporteSemanalTxt();

    public ArchivoControlador(TurnoControlador turnos, CatalogoControlador catalogo, ClienteControlador clientes,
                              Path carpetaExportaciones) {
        this.turnos = turnos;
        this.catalogo = catalogo;
        this.clientes = clientes;
        this.carpetaExportaciones = carpetaExportaciones;
    }

    /** Exporta a CSV la grilla de una sede en un día y devuelve la ruta del archivo. */
    public Path exportarAgenda(int sedeId, String fecha) throws TallerNetException {
        return exportadorAgenda.exportar(turnos.consultarDisponibilidad(sedeId, fecha), carpetaExportaciones);
    }

    /** Exporta a texto el reporte semanal de la semana que contiene la fecha. */
    public Path exportarReporteSemanal(String fecha) throws TallerNetException {
        return exportadorReporte.exportar(catalogo.reporteSemanal(fecha), carpetaExportaciones);
    }

    /** Importa clientes desde un CSV usando la misma validación y alta que el menú. */
    public ImportadorClientesCsv.Resultado importarClientes(String ruta) throws TallerNetException {
        if (ruta == null || ruta.isBlank()) {
            throw new DatoInvalidoException("Indique la ruta del archivo CSV.");
        }
        Path archivo;
        try {
            archivo = Path.of(ruta.strip());
        } catch (InvalidPathException e) {
            throw new DatoInvalidoException("La ruta del archivo no es válida: " + e.getMessage());
        }
        return new ImportadorClientesCsv(clientes::registrarCliente).importar(archivo);
    }

    public Path getCarpetaExportaciones() {
        return carpetaExportaciones;
    }
}
