package tallernet.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import tallernet.conexion.ConexionBD;
import tallernet.excepciones.ConexionException;
import tallernet.excepciones.EntidadNoEncontradaException;
import tallernet.excepciones.RegistroDuplicadoException;
import tallernet.excepciones.TallerNetException;
import tallernet.modelo.Cliente;
import tallernet.modelo.Sede;
import tallernet.modelo.Servicio;
import tallernet.modelo.Vehiculo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Properties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * CPI04 (continuación TP4) - Actualización de registros y excepciones de MySQL contra la base real.
 * Recrea la carga base (scripts 01 y 02) antes de cada prueba.
 */
@DisplayName("CPI04 - Actualización y excepciones de MySQL (TP4)")
class PersistenciaTp4IntegracionTest {

    private final ClienteDAO clientes = new ClienteDAO();
    private final ServicioDAO servicios = new ServicioDAO();
    private final VehiculoDAO vehiculos = new VehiculoDAO();
    private final TurnoDAO turnos = new TurnoDAO();
    private final Dao<Sede, Integer> sedes = new SedeDAO();

    @BeforeEach
    void recrearBase() throws Exception {
        assumeTrue(BaseDePruebas.disponible(), "MySQL de pruebas no disponible: se omite CPI04");
        BaseDePruebas.recrear();
    }

    @AfterAll
    static void dejarBaseLimpia() throws Exception {
        if (BaseDePruebas.disponible()) {
            BaseDePruebas.recrear();
        }
    }

    @Test
    @DisplayName("CPI04-8 UPDATE de teléfono y correo: el cambio queda persistido")
    void actualizarCliente() throws TallerNetException {
        Cliente laura = clientes.buscarPorId(1).orElseThrow();
        laura.setTelefono("11-5555-0001");
        laura.setEmail(null);
        clientes.actualizar(laura);
        Cliente leida = clientes.buscarPorId(1).orElseThrow();
        assertEquals("11-5555-0001", leida.getTelefono());
        assertNull(leida.getEmail());
    }

    @Test
    @DisplayName("CPI04-9 UPDATE sobre un registro inexistente: cero filas -> EntidadNoEncontradaException")
    void actualizarInexistente() throws TallerNetException {
        Cliente fantasma = new Cliente(999, "No", "Existe", "11-0000-0000", null, null);
        assertThrows(EntidadNoEncontradaException.class, () -> clientes.actualizar(fantasma));
    }

    @Test
    @DisplayName("CPI04-10 error 1062 en INSERT y en UPDATE -> RegistroDuplicadoException")
    void duplicados() throws TallerNetException {
        Vehiculo repetido = new Vehiculo(1, "AE123BC", "Fiat", "Uno", 2010, 1000);
        RegistroDuplicadoException e1 = assertThrows(RegistroDuplicadoException.class,
                () -> vehiculos.insertar(repetido));
        assertEquals("Ya existe un vehículo con la patente AE123BC.", e1.getMessage());

        Cliente martin = clientes.buscarPorId(2).orElseThrow();
        martin.setEmail("laura.gomez@mail.com"); // correo de la clienta N.° 1
        assertThrows(RegistroDuplicadoException.class, () -> clientes.actualizar(martin));
    }

    @Test
    @DisplayName("CPI04-11 UPDATE del precio de un servicio")
    void actualizarPrecio() throws TallerNetException {
        Servicio aceite = servicios.buscarPorId(1).orElseThrow();
        aceite.setPrecioBase(new BigDecimal("52000.50"));
        servicios.actualizar(aceite);
        assertEquals(0, new BigDecimal("52000.50").compareTo(servicios.buscarPorId(1).orElseThrow().getPrecioBase()));
    }

    @Test
    @DisplayName("CPI04-12 consulta por rango: la semana del 05/10/2026 tiene 4 turnos no cancelados")
    void turnosDeLaSemana() throws TallerNetException {
        assertEquals(4, turnos.listarEntre(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 12)).size());
    }

    @Test
    @DisplayName("CPI04-13 servidor inaccesible -> ConexionException con indicación para la sede")
    void servidorCaido() throws Exception {
        Properties caido = new Properties();
        caido.setProperty("db.url", "jdbc:mysql://127.0.0.1:1/tallernet?connectTimeout=2000");
        caido.setProperty("db.usuario", "tallernet_app");
        caido.setProperty("db.clave", "x");
        ConexionBD.configurar(caido);
        try {
            ConexionException e = assertThrows(ConexionException.class, sedes::listar);
            assertTrue(e.getMessage().contains("Verifique Internet, la VPN de la sede"));
        } finally {
            BaseDePruebas.recrear(); // restaura la configuración de la aplicación
        }
    }
}
