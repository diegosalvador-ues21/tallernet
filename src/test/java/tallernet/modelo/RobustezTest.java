package tallernet.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import tallernet.excepciones.DatoInvalidoException;
import java.time.Duration;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * CPS02 - Hallazgos del análisis estático convertidos en pruebas: mayúsculas independientes de la
 * configuración regional (DEF-32, SpotBugs DM_CONVERT_CASE) y expresión del correo sin
 * retroceso catastrófico (FindSecBugs REDOS, falso positivo comprobado).
 */
@DisplayName("CPS02 - Configuración regional y ReDoS")
class RobustezTest {

    private Locale anterior;

    @BeforeEach
    void turco() {
        anterior = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
    }

    @AfterEach
    void restaurar() {
        Locale.setDefault(anterior);
    }

    @Test
    @DisplayName("DEF-32: en una JVM con idioma turco la patente «abi 123» sigue siendo ABI123")
    void patenteEnTurco() throws Exception {
        assertThat(Vehiculo.normalizarPatente("abi 123")).isEqualTo("ABI123");
        Vehiculo v = new Vehiculo(1, "kiı 001", "Fiat", "Uno", 2010, 1000);
        assertThat(v.getPatente()).doesNotContain("İ");
    }

    @Test
    @DisplayName("DEF-32: el correo «INFO@TALLER.COM» se guarda como info@taller.com")
    void correoEnTurco() throws Exception {
        Cliente c = new Cliente("Ana", "Paz", "11-4444-5555", "INFO@TALLER.COM");
        assertThat(c.getEmail()).isEqualTo("info@taller.com");
    }

    @Test
    @DisplayName("REDOS: un correo malicioso de 119 caracteres se rechaza en menos de 1 s")
    void correoMaliciosoSinRetroceso() {
        String ataque = "a@b" + ".a".repeat(57) + "!";
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
                assertThatThrownBy(() -> new Cliente("Ana", "Paz", "11-4444-5555", ataque))
                        .isInstanceOf(DatoInvalidoException.class));
    }
}
