package tallernet.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import tallernet.excepciones.ConexionException;
import tallernet.excepciones.DatoInvalidoException;
import tallernet.excepciones.PersistenciaException;
import tallernet.excepciones.TallerNetException;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** CPI07 - Traducción de cada código de error de MySQL a un mensaje para el usuario (tabla de decisión). */
@DisplayName("CPI07 - Traductor de errores de MySQL: todos los códigos")
class TraductorCodigosTest {

    @ParameterizedTest(name = "código {0} -> {1}")
    @CsvSource({
        "3819, DatoInvalidoException, fuera del rango",
        "1406, DatoInvalidoException, fuera del rango",
        "1451, DatoInvalidoException, se conserva para el historial",
        "1142, PersistenciaException, no tiene permiso",
        "1205, PersistenciaException, otra sede",
        "1213, PersistenciaException, otra sede",
        "1045, ConexionException, credenciales",
        "3159, ConexionException, credenciales",
        "9999, PersistenciaException, código 9999"})
    @DisplayName("Escrituras: cada código produce la excepción y el mensaje previstos")
    void traduceEscrituras(int codigo, String tipo, String fragmento) {
        TallerNetException e = TraductorSQLException.traducir("registrar el turno", new SQLException("x", "HY000", codigo), null);
        assertThat(e.getClass().getSimpleName()).isEqualTo(tipo);
        assertThat(e.getMessage()).contains(fragmento);
    }

    @Test
    @DisplayName("Reintentables: solo interbloqueo (1213) y espera de bloqueo (1205)")
    void reintentables() {
        assertThat(TraductorSQLException.esReintentable(new SQLException("x", "40001", 1213))).isTrue();
        assertThat(TraductorSQLException.esReintentable(new SQLException("x", "HY000", 1205))).isTrue();
        assertThat(TraductorSQLException.esReintentable(new SQLException("x", "23000", 1062))).isFalse();
    }

    @Test
    @DisplayName("SQLState 08xxx es siempre un problema de conexión")
    void conexion() {
        PersistenciaException e = TraductorSQLException.tecnica("listar", new SQLException("x", "08S01", 0));
        assertThat(e).isInstanceOf(ConexionException.class);
        assertThat(TraductorSQLException.traducir("x", new SQLException("x", "23000", 3819), null))
                .isInstanceOf(DatoInvalidoException.class);
    }
}
