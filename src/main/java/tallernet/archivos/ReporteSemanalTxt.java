package tallernet.archivos;

import tallernet.excepciones.ArchivoException;
import tallernet.modelo.ReporteSemanal;
import tallernet.util.Formatos;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.temporal.IsoFields;

/** Exporta el reporte semanal como texto con columnas alineadas, listo para imprimir o enviar. */
public class ReporteSemanalTxt implements Exportador<ReporteSemanal> {

    @Override
    public String nombreArchivo(ReporteSemanal r) {
        return String.format("reporte_semanal_%d-S%02d.txt", r.getLunes().get(IsoFields.WEEK_BASED_YEAR),
                r.getLunes().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
    }

    @Override
    public Path exportar(ReporteSemanal r, Path carpeta) throws ArchivoException {
        Path destino = carpeta.resolve(nombreArchivo(r));
        try {
            Files.createDirectories(carpeta);
            try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(destino, StandardCharsets.UTF_8))) {
                for (String linea : FormatoReporteSemanal.lineas(r)) {
                    w.println(linea);
                }
                // PrintWriter no lanza IOException: se consulta el error explícitamente (DEF-21).
                if (w.checkError()) {
                    throw new IOException("error de escritura");
                }
            }
            return destino;
        } catch (IOException e) {
            throw new ArchivoException("No se pudo escribir el archivo " + destino + ": " + e.getMessage(), e);
        }
    }

    /** Formato compartido por la consola y el archivo, para que ambos muestren lo mismo. */
    public static final class FormatoReporteSemanal {

        private FormatoReporteSemanal() {
        }

        public static java.util.List<String> lineas(ReporteSemanal r) {
            java.util.List<String> l = new java.util.ArrayList<>();
            String[] dias = ReporteSemanal.getDias();
            int[] turnos = r.getTurnosPorDia();
            int[] minutos = r.getMinutosPorDia();
            java.math.BigDecimal[] ingresos = r.getIngresoPorDia();
            String[] sedes = r.getSedes();
            int[][] matriz = r.getTurnosPorSedeYDia();

            l.add("TALLERNET - Reporte semanal de carga (turnos no cancelados, ambas sedes)");
            l.add("Semana del " + r.getLunes().format(Formatos.FECHA) + " al "
                    + r.fechaDelDia(dias.length - 1).format(Formatos.FECHA));
            l.add("");
            StringBuilder cab = new StringBuilder(String.format("%-10s %-11s %7s %8s %17s", "Día", "Fecha",
                    "Turnos", "Minutos", "Ingreso estimado"));
            for (String sede : sedes) {
                cab.append(String.format(" %15s", sede.replaceFirst("^Sede ", "")));
            }
            l.add(cab.toString());
            for (int d = 0; d < dias.length; d++) {
                StringBuilder fila = new StringBuilder(String.format("%-10s %-11s %7d %8d %17s", dias[d],
                        r.fechaDelDia(d).format(Formatos.FECHA), turnos[d], minutos[d],
                        Formatos.pesos(ingresos[d])));
                for (int s = 0; s < sedes.length; s++) {
                    fila.append(String.format(" %15d", matriz[s][d]));
                }
                l.add(fila.toString());
            }
            l.add("");
            l.add(String.format(Formatos.ARGENTINA, "Total: %d turnos | Promedio diario: %.1f | Ingreso estimado: %s",
                    r.getTotalTurnos(), r.getPromedioDiario(), Formatos.pesos(r.getIngresoTotal())));
            if (r.estaVacia()) {
                l.add("La semana no tiene turnos registrados."); // DEF-23
            } else {
                l.add("Día de mayor carga: " + dias[r.getDiaDeMayorCarga()] + " ("
                        + turnos[r.getDiaDeMayorCarga()] + " turnos) | Día de menor carga: "
                        + dias[r.getDiaDeMenorCarga()] + " (" + turnos[r.getDiaDeMenorCarga()] + " turnos)");
            }
            java.util.List<String> libres = r.diasSinTurnos();
            l.add("Días sin turnos: " + (libres.isEmpty() ? "ninguno" : String.join(", ", libres)));
            return l;
        }
    }
}
