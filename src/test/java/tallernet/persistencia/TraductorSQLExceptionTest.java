package tallernet.persistencia;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import tallernet.excepciones.ConexionException;
import tallernet.excepciones.DatoInvalidoException;
import tallernet.excepciones.EntidadNoEncontradaException;
import tallernet.excepciones.PersistenciaException;
import tallernet.excepciones.RegistroDuplicadoException;
import tallernet.excepciones.TallerNetException;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** CPC10 - Traducción de los errores de MySQL a excepciones del dominio. */
@DisplayName("CPC10 - TraductorSQLException")
class TraductorSQLExceptionTest {

    private static SQLException sql(String estado, int codigo) {
        return new SQLException("mensaje técnico del driver", estado, codigo);
    }

    @Test
    @DisplayName("1062 clave duplicada -> RegistroDuplicadoException con el mensaje de negocio")
    void duplicado() {
        TallerNetException e = TraductorSQLException.traducir("registrar el vehículo", sql("23000", 1062),
                "Ya existe un vehículo con la patente AE123BC.");
        assertInstanceOf(RegistroDuplicadoException.class, e);
        assertInstanceOf(DatoInvalidoException.class, e, "la vista lo trata como un dato a corregir");
        assertTrue(e.getMessage().contains("AE123BC"));
    }

    @Test
    @DisplayName("1452 y 1451 integridad referencial; 3819 CHECK violado")
    void integridad() {
        assertInstanceOf(EntidadNoEncontradaException.class,
                TraductorSQLException.traducir("registrar el turno", sql("23000", 1452), null));
        assertInstanceOf(DatoInvalidoException.class,
                TraductorSQLException.traducir("borrar el cliente", sql("23000", 1451), null));
        assertInstanceOf(DatoInvalidoException.class,
                TraductorSQLException.traducir("registrar el vehículo", sql("HY000", 3819), null));
    }

    @Test
    @DisplayName("SQLState 08xxx y error 1045 -> ConexionException; nunca se muestra el texto del driver")
    void conexion() {
        PersistenciaException caida = TraductorSQLException.tecnica("listar clientes", sql("08S01", 0));
        assertInstanceOf(ConexionException.class, caida);
        assertFalse(caida.getMessage().contains("mensaje técnico"));
        assertInstanceOf(ConexionException.class, TraductorSQLException.tecnica("conectar", sql("28000", 1045)));
    }

    @Test
    @DisplayName("1213 interbloqueo es reintentable; la causa original se conserva")
    void interbloqueo() {
        SQLException original = sql("40001", 1213);
        assertTrue(TraductorSQLException.esReintentable(original));
        PersistenciaException e = TraductorSQLException.tecnica("registrar el turno", original);
        assertSame(original, e.getCause());
        assertFalse(TraductorSQLException.esReintentable(sql("23000", 1062)));
    }
}
