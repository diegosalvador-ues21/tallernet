package tallernet.modelo;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** CPC08 - Reporte semanal: acumulación de una lista de turnos en arreglos por día y una matriz sede × día. */
@DisplayName("CPC08 - ReporteSemanal (arreglos y matriz)")
class ReporteSemanalTest {

    private static Turno turno(Sede sede, LocalDateTime inicio, Servicio servicio) {
        return new Turno(sede, Fixtures.gol(), servicio, 2, inicio, null);
    }

    @Test
    @DisplayName("acumula por día y por sede; ignora cancelados, domingos y otras semanas")
    void acumulaEnArreglos() {
        List<Turno> turnos = new ArrayList<>();
        turnos.add(turno(Fixtures.echeandia(), LocalDateTime.of(2026, 10, 5, 9, 0), Fixtures.cambioDeAceite()));
        turnos.add(turno(Fixtures.echeandia(), LocalDateTime.of(2026, 10, 5, 10, 0), Fixtures.serviceCompleto()));
        turnos.add(turno(Fixtures.flores(), LocalDateTime.of(2026, 10, 5, 9, 0), Fixtures.cambioDeAceite()));
        turnos.add(turno(Fixtures.flores(), LocalDateTime.of(2026, 10, 10, 18, 0), Fixtures.cambioDeAceite()));
        Turno cancelado = turno(Fixtures.flores(), LocalDateTime.of(2026, 10, 6, 9, 0), Fixtures.cambioDeAceite());
        cancelado.cancelar();
        turnos.add(cancelado);
        turnos.add(turno(Fixtures.flores(), LocalDateTime.of(2026, 10, 11, 9, 0), Fixtures.cambioDeAceite())); // domingo
        turnos.add(turno(Fixtures.flores(), LocalDateTime.of(2026, 10, 12, 9, 0), Fixtures.cambioDeAceite())); // otra semana

        ReporteSemanal r = new ReporteSemanal(LocalDate.of(2026, 10, 8),
                List.of(Fixtures.echeandia(), Fixtures.flores()), turnos);

        assertEquals(LocalDate.of(2026, 10, 5), r.getLunes());
        assertArrayEquals(new int[] {3, 0, 0, 0, 0, 1}, r.getTurnosPorDia());
        assertArrayEquals(new int[] {300, 0, 0, 0, 0, 60}, r.getMinutosPorDia());
        assertArrayEquals(new int[][] {{2, 0, 0, 0, 0, 0}, {1, 0, 0, 0, 0, 1}}, r.getTurnosPorSedeYDia());
        assertEquals(4, r.getTotalTurnos());
        assertEquals(0, r.getDiaDeMayorCarga());
        assertEquals(1, r.getDiaDeMenorCarga());
        assertEquals(List.of("Martes", "Miércoles", "Jueves", "Viernes"), r.diasSinTurnos());
        assertEquals(0, new BigDecimal("4000.00").compareTo(r.getIngresoTotal()));
    }

    @Test
    @DisplayName("los getters devuelven copias: el reporte no se altera desde afuera")
    void copiasDefensivas() {
        ReporteSemanal r = new ReporteSemanal(LocalDate.of(2026, 10, 5), List.of(Fixtures.echeandia()), List.of());
        r.getTurnosPorDia()[0] = 99;
        r.getTurnosPorSedeYDia()[0][0] = 99;
        assertEquals(0, r.getTurnosPorDia()[0]);
        assertEquals(0, r.getTurnosPorSedeYDia()[0][0]);
        assertEquals(6, ReporteSemanal.DIAS_HABILES);
        ReporteSemanal.getDias()[0] = "x";
        assertEquals("Lunes", ReporteSemanal.getDias()[0]);
        assertTrue(r.estaVacia());
    }
}
