package tallernet.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import tallernet.excepciones.DatoInvalidoException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas de componente del modelo: polimorfismo, herencia y encapsulamiento con validación. */
@DisplayName("Modelo de dominio")
class ModeloTest {

    @Test
    @DisplayName("Polimorfismo: cada servicio calcula su próximo service a su manera")
    void servicioPolimorfico() {
        Servicio aceite = Fixtures.servicio(1, 60, 10000);
        Servicio escaner = Fixtures.servicio(5, 30, null);
        assertInstanceOf(ServicioPorKilometraje.class, aceite);
        assertInstanceOf(ServicioGeneral.class, escaner);
        assertEquals(96500, aceite.calcularProximoServiceKm(86500).getAsInt());
        // Regresión DEF-02: un servicio sin intervalo no devuelve 0 sino "sin dato"
        assertFalse(escaner.calcularProximoServiceKm(86500).isPresent());
        assertNull(escaner.getKmServicio());
    }

    @Test
    @DisplayName("Herencia: Cliente y Empleado son Persona y describen su rol distinto")
    void personasPolimorficas() throws DatoInvalidoException {
        List<Persona> personas = List.of(
                new Cliente("Laura", "Gómez", "11-4567-8901", "laura.gomez@mail.com"),
                new Empleado(2, 1, "Carla", "Medina", Rol.RECEPCIONISTA, true));
        assertEquals("Cliente", personas.get(0).getDescripcionRol());
        assertEquals("Recepcionista", personas.get(1).getDescripcionRol());
        assertEquals("Gómez, Laura", personas.get(0).getNombreCompleto());
        assertTrue(((Empleado) personas.get(1)).puedeRegistrarTurnos());
    }

    @Test
    @DisplayName("Encapsulamiento: el kilometraje fuera de rango se rechaza (CPS05-E7)")
    void kilometrajeInvalido() {
        DatoInvalidoException e = assertThrows(DatoInvalidoException.class,
                () -> new Vehiculo(4, "AH777HV", "Peugeot", "208", 2021, -500));
        assertEquals("El kilometraje debe estar entre 0 y 2000000 km.", e.getMessage());
    }

    @Test
    @DisplayName("Encapsulamiento: la patente se normaliza y se valida")
    void patente() throws DatoInvalidoException {
        assertEquals("AG555PH", new Vehiculo(5, "ag 555-ph", "Fiat", "Argo", 2023, 18500).getPatente());
        assertThrows(DatoInvalidoException.class, () -> new Vehiculo(5, "12ABC", "Fiat", "Argo", 2023, 0));
    }

    @Test
    @DisplayName("Encapsulamiento: los textos respetan el largo de su columna en la base")
    void largosMaximos() {
        assertThrows(DatoInvalidoException.class,
                () -> new Vehiculo(5, "AG555PH", "M".repeat(41), "Argo", 2023, 0));
        assertThrows(DatoInvalidoException.class,
                () -> new Cliente("Ana", "Paz", "11-2222-3333", "a".repeat(115) + "@x.com"));
    }

    @Test
    @DisplayName("Un mecánico no puede registrar turnos")
    void mecanicoNoRegistraTurnos() throws DatoInvalidoException {
        assertFalse(new Empleado(4, 1, "Esteban", "Sosa", Rol.MECANICO, true).puedeRegistrarTurnos());
    }
}
