package tallernet.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import tallernet.excepciones.DatoInvalidoException;
import java.math.BigDecimal;
import java.time.Year;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** CPF03 - Valores límite y formatos reales de entrada del modelo (campaña de pruebas del TP4). */
@Tag("unit")
@DisplayName("CPF03 - Límites y formatos del modelo")
class LimitesTest {

    private static final int ANIO_MAX = Year.now().getValue() + 1;

    @ParameterizedTest(name = "año {0} -> válido={1}")
    @CsvSource({"1949,false", "1950,true", "2010,true"})
    void anioLimiteInferior(int anio, boolean valido) {
        verificar(() -> new Vehiculo(1, "AB123CD", "Fiat", "Uno", anio, 0), valido);
    }

    @Test
    void anioLimiteSuperior() throws Exception {
        new Vehiculo(1, "AB123CD", "Fiat", "Uno", ANIO_MAX, 0);
        assertThatThrownBy(() -> new Vehiculo(1, "AB123CD", "Fiat", "Uno", ANIO_MAX + 1, 0))
                .isInstanceOf(DatoInvalidoException.class);
    }

    @ParameterizedTest(name = "km {0} -> válido={1}")
    @CsvSource({"-1,false", "0,true", "2000000,true", "2000001,false"})
    void kilometraje(int km, boolean valido) {
        verificar(() -> new Vehiculo(1, "AB123CD", "Fiat", "Uno", 2010, km), valido);
    }

    @ParameterizedTest(name = "patente «{0}» -> {1}")
    @CsvSource({"ab 123 cd,AB123CD", "AB-123-CD,AB123CD", "abc123,ABC123", "  AE123BC  ,AE123BC"})
    void patentesQueSeNormalizan(String ingresada, String guardada) throws Exception {
        assertThat(new Vehiculo(1, ingresada, "Fiat", "Uno", 2010, 0).getPatente()).isEqualTo(guardada);
    }

    @ParameterizedTest(name = "patente inválida «{0}»")
    @ValueSource(strings = {"A1", "AB12CD", "ABCD123", "123ABC", "AB123CD' OR '1'='1", "ÑB123CD"})
    void patentesInvalidas(String patente) {
        assertThatThrownBy(() -> new Vehiculo(1, patente, "Fiat", "Uno", 2010, 0))
                .isInstanceOf(DatoInvalidoException.class).hasMessageContaining("formato");
    }

    @Test
    @DisplayName("DEF-28: los mensajes de dato obligatorio concuerdan en género")
    void mensajeDeObligatorio() {
        assertThatThrownBy(() -> new Vehiculo(1, "AB123CD", " ", "Uno", 2010, 0))
                .hasMessage("La marca es un dato obligatorio.");
        assertThatThrownBy(() -> new Vehiculo(1, "AB123CD", "Fiat", "", 2010, 0))
                .hasMessage("El modelo es un dato obligatorio.");
    }

    @Test
    @DisplayName("Marca de 40 y modelo de 60 caracteres: límite de las columnas")
    void largosMaximos() throws Exception {
        new Vehiculo(1, "AB123CD", "x".repeat(40), "y".repeat(60), 2010, 0);
        assertThatThrownBy(() -> new Vehiculo(1, "AB123CD", "x".repeat(41), "Uno", 2010, 0))
                .hasMessageContaining("40");
        assertThatThrownBy(() -> new Vehiculo(1, "AB123CD", "Fiat", "y".repeat(61), 2010, 0))
                .hasMessageContaining("60");
    }

    @Test
    @DisplayName("DEF-29: el precio base debe ser mayor que cero")
    void precioCero() throws Exception {
        Servicio s = Fixtures.cambioDeAceite();
        assertThatThrownBy(() -> s.setPrecioBase(BigDecimal.ZERO)).hasMessage("El precio base debe ser mayor que cero.");
        assertThatThrownBy(() -> s.setPrecioBase(new BigDecimal("-1"))).hasMessageContaining("negativo");
        s.setPrecioBase(new BigDecimal("0.01"));
        assertThat(s.getPrecioBase()).isEqualByComparingTo("0.01");
    }

    @ParameterizedTest(name = "correo «{0}» -> válido={1}")
    @CsvSource({"ana@mail.com,true", "ANA.PAZ@MAIL.COM,true", "ana@,false", "@mail.com,false", "ana mail.com,false"})
    void correos(String correo, boolean valido) {
        verificar(() -> new Cliente("Ana", "Paz", "11-4444-5555", correo), valido);
    }

    @ParameterizedTest(name = "teléfono «{0}» -> válido={1}")
    @CsvSource({"1234567,false", "12345678,true", "(011) 4567-1234,true", "+54 9 11 4567 1234,true", "11-ABCD-1234,false"})
    void telefonos(String telefono, boolean valido) {
        verificar(() -> new Cliente("Ana", "Paz", telefono, null), valido);
    }

    @Test
    @DisplayName("Inyección SQL en el nombre: es texto válido y se guarda literal (consultas parametrizadas)")
    void textoConComillas() throws Exception {
        Cliente c = new Cliente("Robert'); DROP TABLE cliente;--", "O'Brien", "11-4444-6666", null);
        assertThat(c.getNombre()).isEqualTo("Robert'); DROP TABLE cliente;--");
    }

    private interface Creacion {
        void crear() throws Exception;
    }

    private static void verificar(Creacion creacion, boolean valido) {
        if (valido) {
            try {
                creacion.crear();
            } catch (Exception e) {
                throw new AssertionError("Se esperaba válido, pero: " + e.getMessage(), e);
            }
        } else {
            assertThatThrownBy(creacion::crear).isInstanceOf(DatoInvalidoException.class);
        }
    }
}
