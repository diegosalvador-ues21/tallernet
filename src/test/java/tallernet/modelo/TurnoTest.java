package tallernet.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * CPC01 - Cobertura de decisiones de {@link Turno#seSuperponeCon(Turno)} (caja blanca).
 * Cada caso recorre una rama distinta del método.
 */
@DisplayName("CPC01 - Regla de superposición de la clase Turno")
class TurnoTest {

    private Turno existente;

    @BeforeEach
    void crearTurnoExistente() {
        // Turno N.° 2 del script 02: Echeandía, 05/10/2026 de 10:00 a 13:00
        existente = new Turno(2, Fixtures.echeandia(), Fixtures.gol(), Fixtures.serviceCompleto(), 2,
                LocalDateTime.of(2026, 10, 5, 10, 0), EstadoTurno.CONFIRMADO, null);
    }

    private Turno nuevo(Sede sede, int hora, int minuto) {
        return new Turno(sede, Fixtures.gol(), Fixtures.cambioDeAceite(), 2,
                LocalDateTime.of(2026, 10, 5, hora, minuto), null);
    }

    @Test
    @DisplayName("CPC01-1 choque parcial: 10:30-11:30 contra 10:00-13:00")
    void chocaParcialmente() {
        assertTrue(nuevo(Fixtures.echeandia(), 10, 30).seSuperponeCon(existente));
    }

    @Test
    @DisplayName("CPC01-2 turnos contiguos: 13:00-14:00 no choca con 10:00-13:00")
    void contiguosNoChocan() {
        assertFalse(nuevo(Fixtures.echeandia(), 13, 0).seSuperponeCon(existente));
        assertFalse(nuevo(Fixtures.echeandia(), 9, 0).seSuperponeCon(existente));
    }

    @Test
    @DisplayName("CPC01-3 otra sede: mismo horario en Flores no choca")
    void otraSedeNoChoca() {
        assertFalse(nuevo(Fixtures.flores(), 10, 30).seSuperponeCon(existente));
    }

    @Test
    @DisplayName("CPC01-4 turno cancelado: libera la franja")
    void canceladoNoChoca() {
        existente.cancelar();
        assertEquals(EstadoTurno.CANCELADO, existente.getEstado());
        assertFalse(nuevo(Fixtures.echeandia(), 10, 30).seSuperponeCon(existente));
    }

    @Test
    @DisplayName("CPC01-5 turno inexistente (null): no hay superposición")
    void inexistenteNoChoca() {
        assertFalse(nuevo(Fixtures.echeandia(), 10, 30).seSuperponeCon(null));
    }
}
