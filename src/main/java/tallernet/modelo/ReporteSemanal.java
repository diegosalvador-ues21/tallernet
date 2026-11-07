package tallernet.modelo;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Carga de trabajo de la red en una semana, de lunes a sábado (RNF-TN04: el domingo no se atiende).
 *
 * <p>Los totales se acumulan en <b>arreglos de tamaño fijo</b>, uno por día hábil, y en una
 * <b>matriz</b> sede × día, porque la cantidad de días es una constante del negocio. Los turnos de
 * entrada llegan en un {@code List} (ArrayList), cuyo tamaño varía semana a semana.</p>
 */
public class ReporteSemanal {

    /** Nombres de los días hábiles; el índice coincide con el de los arreglos de totales. Privado: un
     * arreglo público y final igual podría modificarse elemento por elemento (DEF-19). */
    private static final String[] DIAS = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};

    /** Cantidad de días hábiles de la semana del taller. */
    public static final int DIAS_HABILES = DIAS.length;

    /** Copia de los nombres de los días hábiles. */
    public static String[] getDias() {
        return DIAS.clone();
    }

    /** true si la semana no tiene turnos: el día de mayor o menor carga no tiene sentido. */
    public boolean estaVacia() {
        return getTotalTurnos() == 0;
    }

    private final LocalDate lunes;
    private final String[] sedes;
    private final int[] turnosPorDia = new int[DIAS.length];
    private final int[] minutosPorDia = new int[DIAS.length];
    private final BigDecimal[] ingresoPorDia = new BigDecimal[DIAS.length];
    /** Filas: sedes (en el orden de {@link #getSedes()}); columnas: días. */
    private final int[][] turnosPorSedeYDia;

    /**
     * @param fechaDeLaSemana cualquier día de la semana a informar
     * @param sedesActivas    sedes a mostrar en la matriz
     * @param turnos          turnos no cancelados de la semana, de todas las sedes
     */
    public ReporteSemanal(LocalDate fechaDeLaSemana, List<Sede> sedesActivas, List<Turno> turnos) {
        this.lunes = fechaDeLaSemana.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        this.sedes = new String[sedesActivas.size()];
        int[] idsSede = new int[sedesActivas.size()];
        for (int i = 0; i < sedesActivas.size(); i++) {
            sedes[i] = sedesActivas.get(i).getNombre();
            idsSede[i] = sedesActivas.get(i).getId();
        }
        this.turnosPorSedeYDia = new int[sedes.length][DIAS.length];
        java.util.Arrays.fill(ingresoPorDia, BigDecimal.ZERO);

        for (Turno t : turnos) {
            int dia = indiceDelDia(t.getFechaHora().toLocalDate());
            if (dia < 0 || !t.getEstado().ocupaAgenda()) {
                continue; // fuera de la semana, domingo o cancelado
            }
            turnosPorDia[dia]++;
            minutosPorDia[dia] += t.getServicio().getDuracionMin();
            ingresoPorDia[dia] = ingresoPorDia[dia].add(t.getServicio().getPrecioBase());
            for (int s = 0; s < idsSede.length; s++) {
                if (idsSede[s] == t.getSede().getId()) {
                    turnosPorSedeYDia[s][dia]++;
                }
            }
        }
    }

    /** Posición del día en los arreglos (0 = lunes ... 5 = sábado), o -1 si no corresponde. */
    private int indiceDelDia(LocalDate fecha) {
        long diferencia = fecha.toEpochDay() - lunes.toEpochDay();
        return diferencia >= 0 && diferencia < DIAS.length ? (int) diferencia : -1;
    }

    public int getTotalTurnos() {
        int total = 0;
        for (int cantidad : turnosPorDia) {
            total += cantidad;
        }
        return total;
    }

    /** Índice del día con más turnos; ante empate, el primero de la semana. */
    public int getDiaDeMayorCarga() {
        int mayor = 0;
        for (int i = 1; i < turnosPorDia.length; i++) {
            if (turnosPorDia[i] > turnosPorDia[mayor]) {
                mayor = i;
            }
        }
        return mayor;
    }

    /** Índice del día con menos turnos; ante empate, el primero de la semana. */
    public int getDiaDeMenorCarga() {
        int menor = 0;
        for (int i = 1; i < turnosPorDia.length; i++) {
            if (turnosPorDia[i] < turnosPorDia[menor]) {
                menor = i;
            }
        }
        return menor;
    }

    public double getPromedioDiario() {
        return (double) getTotalTurnos() / DIAS.length;
    }

    /** Días sin ningún turno: resultado de tamaño variable, por eso se devuelve en un ArrayList. */
    public List<String> diasSinTurnos() {
        List<String> libres = new ArrayList<>();
        for (int i = 0; i < DIAS.length; i++) {
            if (turnosPorDia[i] == 0) {
                libres.add(DIAS[i]);
            }
        }
        return libres;
    }

    public BigDecimal getIngresoTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal monto : ingresoPorDia) {
            total = total.add(monto);
        }
        return total;
    }

    public LocalDate getLunes() {
        return lunes;
    }

    public LocalDate fechaDelDia(int indice) {
        return lunes.plusDays(indice);
    }

    // Copias defensivas: los arreglos internos no se exponen (encapsulamiento).

    public int[] getTurnosPorDia() {
        return turnosPorDia.clone();
    }

    public int[] getMinutosPorDia() {
        return minutosPorDia.clone();
    }

    public BigDecimal[] getIngresoPorDia() {
        return ingresoPorDia.clone();
    }

    public String[] getSedes() {
        return sedes.clone();
    }

    public int[][] getTurnosPorSedeYDia() {
        int[][] copia = new int[turnosPorSedeYDia.length][];
        for (int i = 0; i < turnosPorSedeYDia.length; i++) {
            copia[i] = turnosPorSedeYDia[i].clone();
        }
        return copia;
    }
}
