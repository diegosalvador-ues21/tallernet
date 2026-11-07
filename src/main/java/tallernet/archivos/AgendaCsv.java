package tallernet.archivos;

import java.util.Locale;
import tallernet.modelo.AgendaDiaria;
import tallernet.modelo.Turno;
import tallernet.util.Formatos;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Exporta la grilla de una sede en un día: una fila por franja de 30 minutos, libre u ocupada.
 * Permite imprimir la agenda o compartirla con la otra sede ante un corte de la VPN.
 */
public class AgendaCsv extends ExportadorCsv<AgendaDiaria> {

    private static final String[] ENCABEZADO = {"Sede", "Fecha", "Franja", "Estado", "Turno N.°", "Patente",
        "Servicio", "Inicio", "Fin"};

    @Override
    public String nombreArchivo(AgendaDiaria agenda) {
        // Sin tildes ni espacios: el nombre de archivo debe ser válido en cualquier sistema (DEF-16).
        String sede = Normalizer.normalize(agenda.getSede().getNombre().replaceFirst("^Sede ", ""), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replaceAll("[^A-Za-z0-9]+", "_").toLowerCase(Locale.ROOT);
        return "agenda_" + sede + "_" + agenda.getFecha() + ".csv";
    }

    @Override
    protected String[] encabezado() {
        return ENCABEZADO.clone();
    }

    @Override
    protected List<String[]> filas(AgendaDiaria agenda) {
        List<String[]> filas = new ArrayList<>(agenda.getCantidadFranjas());
        String sede = agenda.getSede().getNombre();
        String fecha = agenda.getFecha().format(Formatos.FECHA);
        for (int i = 0; i < agenda.getCantidadFranjas(); i++) {
            String franja = agenda.horaDeFranja(i).format(Formatos.HORA);
            Optional<Turno> turno = agenda.getTurnoEn(i);
            if (turno.isEmpty()) {
                filas.add(new String[] {sede, fecha, franja, "Libre", "", "", "", "", ""});
            } else {
                Turno t = turno.get();
                filas.add(new String[] {sede, fecha, franja, "Ocupada", String.valueOf(t.getId()),
                    t.getVehiculo().getPatente(), t.getServicio().getDescripcion(),
                    t.getFechaHora().format(Formatos.HORA), t.getFin().format(Formatos.HORA)});
            }
        }
        return filas;
    }
}
