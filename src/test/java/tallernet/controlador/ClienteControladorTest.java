package tallernet.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tallernet.archivos.RegistroOperaciones;
import tallernet.excepciones.DatoInvalidoException;
import tallernet.excepciones.EntidadNoEncontradaException;
import tallernet.modelo.Cliente;
import tallernet.modelo.Vehiculo;
import tallernet.persistencia.Dao;
import tallernet.persistencia.TurnoRepositorio;
import tallernet.persistencia.VehiculoRepositorio;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** CPC11 - ClienteControlador aislado de la base con Mockito: nada se persiste si una validación falla. */
@Tag("unit")
@DisplayName("CPC11 - ClienteControlador con DAO simulados")
class ClienteControladorTest {

    @SuppressWarnings("unchecked")
    private final Dao<Cliente, Integer> clientes = mock(Dao.class);
    private final VehiculoRepositorio vehiculos = mock(VehiculoRepositorio.class);
    private final TurnoRepositorio turnos = mock(TurnoRepositorio.class);
    private final RegistroOperaciones registro = mock(RegistroOperaciones.class);
    private final ClienteControlador controlador = new ClienteControlador(clientes, vehiculos, turnos, registro);

    @Test
    void clienteInexistenteNoInsertaNiRegistra() throws Exception {
        when(clientes.buscarPorId(7)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> controlador.registrarVehiculo(7, "LBS854", "Renault", "Fluence", "2013", "195555"))
                .isInstanceOf(EntidadNoEncontradaException.class).hasMessage("No existe el cliente N.° 7.");
        verify(vehiculos, never()).insertar(any());
        verify(registro, never()).registrar(any(), any());
    }

    @Test
    void kilometrajeConPuntoDeMilesSeConvierte() throws Exception {
        when(clientes.buscarPorId(1)).thenReturn(Optional.of(new Cliente(1, "Laura", "Gómez", "11-4567-8901", null, null)));
        doAnswer(inv -> {
            ((Vehiculo) inv.getArgument(0)).setId(6);
            return 6;
        }).when(vehiculos).insertar(any());
        controlador.registrarVehiculo(1, "lbs 854", "Renault", "Fluence", "2013", "195.555");
        ArgumentCaptor<Vehiculo> v = ArgumentCaptor.forClass(Vehiculo.class);
        verify(vehiculos).insertar(v.capture());
        assertThat(v.getValue().getKmAlta()).isEqualTo(195555);
        assertThat(v.getValue().getPatente()).isEqualTo("LBS854");
        verify(registro).registrar("ALTA_VEHICULO", "Vehículo LBS854 del cliente N.° 1");
    }

    @Test
    void kilometrajeConComaSeRechazaSinInsertar() throws Exception {
        when(clientes.buscarPorId(1)).thenReturn(Optional.of(new Cliente(1, "Laura", "Gómez", "11-4567-8901", null, null)));
        assertThatThrownBy(() -> controlador.registrarVehiculo(1, "LBS854", "Renault", "Fluence", "2013", "195,5"))
                .isInstanceOf(DatoInvalidoException.class);
        verify(vehiculos, never()).insertar(any());
    }

    @Test
    void contactoInvalidoNoActualiza() throws Exception {
        when(clientes.buscarPorId(1)).thenReturn(Optional.of(new Cliente(1, "Laura", "Gómez", "11-4567-8901", null, null)));
        assertThatThrownBy(() -> controlador.actualizarContacto(1, "123", null)).isInstanceOf(DatoInvalidoException.class);
        verify(clientes, never()).actualizar(any());
    }
}
