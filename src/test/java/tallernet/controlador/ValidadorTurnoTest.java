package tallernet.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import tallernet.excepciones.TallerNetException;
import tallernet.excepciones.TurnoInvalidoException;
import tallernet.modelo.Fixtures;
import tallernet.modelo.Sede;
import tallernet.modelo.Servicio;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Pruebas del validador, aislado de la base de datos. */
class ValidadorTurnoTest {

    private static final ZoneId BA = ZoneId.of("America/Argentina/Buenos_Aires");
    /** "Hoy" fijo: 1 de octubre de 2026, 08:00, para que las fechas de octubre sean futuras. */
    private static final Clock RELOJ = Clock.fixed(LocalDateTime.of(2026, 10, 1, 8, 0).atZone(BA).toInstant(), BA);
    private static final String FECHA_HABIL = "07/10/2026";
    private static final String FUERA_DE_FRANJA = "Horario fuera de la franja de atención de la sede (08:00 a 20:00).";

    private final ValidadorTurno validador = new ValidadorTurno(RELOJ);
    private final Sede echeandia = Fixtures.echeandia();
    private final Servicio aceite = Fixtures.cambioDeAceite();

    /** Ejecuta la validación completa y devuelve el mensaje obtenido, o la franja registrada. */
    private String resultado(String hora) {
        try {
            LocalDateTime inicio = validador.validarTodo(echeandia, aceite, FECHA_HABIL, hora);
            return "REGISTRA " + inicio.toLocalTime() + "-" + inicio.toLocalTime().plusMinutes(60);
        } catch (TallerNetException e) {
            return e.getMessage();
        }
    }

    /**
     * CPF02 - Partición de equivalencias y valores límite de la hora de inicio (caja negra).
     * Las doce filas reproducen la Tabla 10 del AP2.
     */
    @DisplayName("CPF02 - Hora de inicio contra la franja de la sede")
    @ParameterizedTest(name = "{0}: h = {1} ({2}) -> {3}")
    @CsvSource(delimiter = '|', value = {
        "CPF02-01 | -1:00 | CEI1 | Formato de hora no válido (use HH:mm, entre 00:00 y 23:59).",
        "CPF02-02 | 00:00 | CEV1 | " + FUERA_DE_FRANJA,
        "CPF02-03 | 01:00 | CEV1 | " + FUERA_DE_FRANJA,
        "CPF02-04 | 07:00 | CEV1 | " + FUERA_DE_FRANJA,
        "CPF02-05 | 08:00 | CEV2 | REGISTRA 08:00-09:00",
        "CPF02-06 | 09:00 | CEV2 | REGISTRA 09:00-10:00",
        "CPF02-07 | 18:00 | CEV2 | REGISTRA 18:00-19:00",
        "CPF02-08 | 19:00 | CEV2 | REGISTRA 19:00-20:00",
        "CPF02-09 | 20:00 | CEV3 | " + FUERA_DE_FRANJA,
        "CPF02-10 | 22:00 | CEV3 | " + FUERA_DE_FRANJA,
        "CPF02-11 | 23:00 | CEV3 | " + FUERA_DE_FRANJA,
        "CPF02-12 | 24:00 | CEI2 | Formato de hora no válido (use HH:mm, entre 00:00 y 23:59)."
    })
    void cpf02(String caso, String hora, String clase, String esperado) {
        assertEquals(esperado, resultado(hora), caso + " / " + clase);
    }

    /** CPC03 - Casos puntuales de reglas de negocio. */
    @Nested
    @DisplayName("CPC03 - Reglas de negocio puntuales")
    class Cpc03 {

        @Test
        @DisplayName("CPC03-1 servicio de 180 min a las 18:00 termina después del cierre")
        void terminaDespuesDelCierre() {
            TurnoInvalidoException e = assertThrows(TurnoInvalidoException.class,
                    () -> validador.validarTodo(Fixtures.flores(), Fixtures.serviceCompleto(), "10/10/2026", "18:00"));
            assertEquals(FUERA_DE_FRANJA, e.getMessage());
        }

        @Test
        @DisplayName("CPC03-2 domingo 04/10/2026")
        void domingo() {
            TurnoInvalidoException e = assertThrows(TurnoInvalidoException.class,
                    () -> validador.validarTodo(Fixtures.flores(), aceite, "04/10/2026", "09:00"));
            assertEquals(ValidadorTurno.MSJ_DOMINGO, e.getMessage());
        }

        @Test
        @DisplayName("CPC03-3 fecha pasada respecto del reloj del sistema")
        void fechaPasada() {
            TurnoInvalidoException e = assertThrows(TurnoInvalidoException.class,
                    () -> validador.validarTodo(echeandia, aceite, "30/09/2026", "10:00"));
            assertEquals(ValidadorTurno.MSJ_PASADO, e.getMessage());
        }
    }
}
