package tallernet.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** CPC07 - Grilla de franjas de una sede: arreglo de tamaño fijo cargado desde una lista. */
@DisplayName("CPC07 - AgendaDiaria (arreglo de franjas)")
class AgendaDiariaTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 10, 5);

    private static Turno turno(int id, LocalTime inicio, Servicio servicio) {
        Turno t = new Turno(Fixtures.echeandia(), Fixtures.gol(), servicio, 2, LocalDateTime.of(LUNES, inicio), null);
        t.setId(id);
        return t;
    }

    @Test
    @DisplayName("08:00 a 20:00 en franjas de 30 minutos son 24 posiciones")
    void tamanioDelArreglo() {
        AgendaDiaria a = new AgendaDiaria(Fixtures.echeandia(), LUNES, new ArrayList<>());
        assertEquals(24, a.getCantidadFranjas());
        assertEquals(LocalTime.of(10, 30), a.horaDeFranja(5));
        assertEquals(0, a.contarOcupadas());
    }

    @Test
    @DisplayName("un service de 180 min a las 10:00 ocupa las franjas 4 a 9")
    void turnoQueCruzaVariasFranjas() {
        AgendaDiaria a = new AgendaDiaria(Fixtures.echeandia(), LUNES,
                List.of(turno(2, LocalTime.of(10, 0), Fixtures.serviceCompleto())));
        assertTrue(a.estaLibre(3));
        for (int i = 4; i <= 9; i++) {
            assertEquals(2, a.getTurnoEn(i).orElseThrow().getId());
        }
        assertTrue(a.estaLibre(10));
        assertEquals(6, a.contarOcupadas());
        assertEquals(25, a.porcentajeOcupacion());
    }

    @Test
    @DisplayName("un turno en la última franja (19:30) ocupa la posición 23 sin salirse del arreglo")
    void ultimaFranja() {
        AgendaDiaria a = new AgendaDiaria(Fixtures.echeandia(), LUNES,
                List.of(turno(9, LocalTime.of(19, 30), Fixtures.servicio(5, 30, null))));
        assertFalse(a.estaLibre(23));
        assertEquals(1, a.contarOcupadas());
    }

    @Test
    @DisplayName("primer hueco libre: después del turno N.° 2 (10:00-13:00) el siguiente es a las 13:00")
    void primerHuecoDespuesDeUnTurno() {
        AgendaDiaria a = new AgendaDiaria(Fixtures.echeandia(), LUNES,
                List.of(turno(2, LocalTime.of(10, 0), Fixtures.serviceCompleto())));
        assertEquals(Optional.of(LocalTime.of(13, 0)), a.primerHuecoLibre(60, LocalTime.of(10, 30)));
        assertEquals(Optional.of(LocalTime.of(8, 0)), a.primerHuecoLibre(60, LocalTime.of(8, 0)));
        // Un pedido a mitad de franja se redondea a la franja siguiente.
        assertEquals(Optional.of(LocalTime.of(13, 30)), a.primerHuecoLibre(60, LocalTime.of(13, 10)));
    }

    @Test
    @DisplayName("sin espacio suficiente antes del cierre no se sugiere horario")
    void sinHueco() {
        AgendaDiaria a = new AgendaDiaria(Fixtures.echeandia(), LUNES, new ArrayList<>());
        // 180 min: el último inicio posible es 17:00 (termina 20:00, al cierre).
        assertEquals(Optional.of(LocalTime.of(17, 0)), a.primerHuecoLibre(180, LocalTime.of(17, 0)));
        assertTrue(a.primerHuecoLibre(180, LocalTime.of(17, 1)).isEmpty());
    }

    @Test
    @DisplayName("los turnos cancelados y los de otro día no ocupan la grilla")
    void ignoraCanceladosYOtrosDias() {
        Turno cancelado = turno(5, LocalTime.of(8, 0), Fixtures.cambioDeAceite());
        cancelado.cancelar();
        Turno otroDia = new Turno(Fixtures.echeandia(), Fixtures.gol(), Fixtures.cambioDeAceite(), 2,
                LocalDateTime.of(LUNES.plusDays(1), LocalTime.of(9, 0)), null);
        AgendaDiaria a = new AgendaDiaria(Fixtures.echeandia(), LUNES, List.of(cancelado, otroDia));
        assertEquals(0, a.contarOcupadas());
    }

    @Test
    @DisplayName("horarios disponibles en una lista y copia defensiva del arreglo")
    void horariosYCopiaDefensiva() {
        AgendaDiaria a = new AgendaDiaria(Fixtures.echeandia(), LUNES,
                List.of(turno(2, LocalTime.of(10, 0), Fixtures.serviceCompleto())));
        // 24 franjas - 6 ocupadas = 18 inicios posibles para 30 min.
        assertEquals(18, a.horariosDisponibles(30).size());
        Turno[] copia = a.getFranjas();
        copia[4] = null;
        assertNull(copia[4]);
        assertFalse(a.estaLibre(4), "modificar la copia no altera la grilla");
    }

    @Test
    @DisplayName("turnos que empiezan antes de la apertura o terminan después del cierre no salen del arreglo")
    void limitesDelArreglo() {
        AgendaDiaria a = new AgendaDiaria(Fixtures.echeandia(), LUNES, List.of(
                turno(1, LocalTime.of(7, 30), Fixtures.cambioDeAceite()),
                turno(2, LocalTime.of(19, 30), Fixtures.servicio(4, 90, null))));
        assertFalse(a.estaLibre(0));
        assertTrue(a.estaLibre(1));
        assertFalse(a.estaLibre(23));
        assertEquals(2, a.contarOcupadas());
    }
}
