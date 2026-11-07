package tallernet.vista;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tallernet.archivos.Bitacora;
import tallernet.controlador.ArchivoControlador;
import tallernet.controlador.CatalogoControlador;
import tallernet.controlador.ClienteControlador;
import tallernet.controlador.SesionControlador;
import tallernet.controlador.SistemaControlador;
import tallernet.controlador.TurnoControlador;
import tallernet.excepciones.EntidadNoEncontradaException;
import tallernet.modelo.Cliente;
import tallernet.modelo.Empleado;
import tallernet.excepciones.RegistroDuplicadoException;
import tallernet.modelo.EstadoTurno;
import tallernet.modelo.Fixtures;
import tallernet.modelo.Rol;
import tallernet.modelo.Turno;
import java.time.LocalDateTime;
import tallernet.modelo.Vehiculo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CPV01 - Flujos de alta de la vista con entrada guionada y controladores simulados (Mockito).
 * Reproduce DEF-26 (el cliente inexistente se detectaba después de pedir todos los datos del
 * vehículo) y DEF-27 (el rechazo no decía que no se había registrado nada).
 */
@Tag("unit")
@DisplayName("CPV01 - Vista: altas con validación temprana")
class VistaAltasTest {

    private final TurnoControlador turnos = mock(TurnoControlador.class);
    private final ClienteControlador clientes = mock(ClienteControlador.class);
    private final CatalogoControlador catalogo = mock(CatalogoControlador.class);
    private final SesionControlador sesion = mock(SesionControlador.class);
    private final ArchivoControlador archivos = mock(ArchivoControlador.class);
    private final SistemaControlador sistema = mock(SistemaControlador.class);

    @BeforeEach
    void sesionDeCarla() throws Exception {
        Empleado carla = new Empleado(2, 1, "Carla", "Medina", Rol.RECEPCIONISTA, true);
        when(sesion.listarHabilitados()).thenReturn(List.of(carla));
        when(sesion.iniciarSesion(2)).thenReturn(carla);
        when(sistema.getBitacora()).thenReturn(mock(Bitacora.class));
    }

    private String ejecutar(String entrada) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(salida, true, StandardCharsets.UTF_8);
        LectorEntrada lector = new LectorEntrada(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8, out, true);
        new VistaConsola(turnos, clientes, catalogo, sesion, archivos, sistema, lector, out).iniciar();
        return salida.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("DEF-26: con un cliente inexistente no se piden los datos del vehículo ni se intenta el alta")
    void clienteInexistenteSeRechazaAntesDePedirElVehiculo() throws Exception {
        when(clientes.buscarCliente(7)).thenThrow(new EntidadNoEncontradaException("No existe el cliente N.° 7."));
        // login 2 · menú 2 · alta de vehículo · cliente 007 · no registrar cliente ahora · volver · salir
        String salida = ejecutar("2\n2\n2\n007\nN\n0\n0\n");

        String tramo = salida.substring(salida.indexOf("Número de cliente:"));
        assertThat(tramo).contains("No existe el cliente N.° 7.");
        assertThat(tramo.substring(0, tramo.indexOf("CLIENTES Y VEHÍCULOS")))
                .doesNotContain("Patente:").doesNotContain("Marca:");
        assertThat(salida).contains("Resultado: el vehículo no se registró; no se guardó ningún dato.");
        verify(clientes, never()).registrarVehiculo(anyInt(), anyString(), anyString(), anyString(), anyString(),
                anyString());
    }

    @Test
    @DisplayName("DEF-26: el cliente inexistente puede darse de alta en el mismo flujo y luego su vehículo")
    void clienteYVehiculoEnElMismoFlujo() throws Exception {
        when(clientes.buscarCliente(7)).thenThrow(new EntidadNoEncontradaException("No existe el cliente N.° 7."));
        Cliente nuevo = new Cliente(9, "Diego", "Salvador", "11-4000-0000", null, null);
        when(clientes.registrarCliente(any(), any(), any(), any())).thenReturn(nuevo);
        Vehiculo fluence = new Vehiculo(6, 9, "LBS854", "Renault", "Fluence 2.0 CVT Privilege", 2013, 195555, null);
        when(clientes.registrarVehiculo(9, "LBS854", "Renault", "Fluence 2.0 CVT Privilege", "2013", "195555"))
                .thenReturn(fluence);

        String salida = ejecutar("2\n2\n2\n7\nS\nDiego\nSalvador\n11-4000-0000\n\n"
                + "LBS854\nRenault\nFluence 2.0 CVT Privilege\n2013\n195555\n0\n0\n");

        assertThat(salida).contains("Cliente N.° 9 registrado: Salvador, Diego")
                .contains("Vehículo N.° 6 registrado: LBS854");
        verify(clientes).registrarVehiculo(9, "LBS854", "Renault", "Fluence 2.0 CVT Privilege", "2013", "195555");
    }

    @Test
    @DisplayName("Alta de vehículo con cliente existente: se muestra el titular antes de pedir los datos")
    void clienteExistenteSeMuestra() throws Exception {
        Cliente laura = new Cliente(1, "Laura", "Gómez", "11-4567-8901", "laura.gomez@mail.com", null);
        when(clientes.buscarCliente(1)).thenReturn(laura);
        when(clientes.registrarVehiculo(anyInt(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new Vehiculo(6, 1, "AB123CD", "Fiat", "Uno", 2010, 1000, null));
        String salida = ejecutar("2\n2\n2\n1\nAB123CD\nFiat\nUno\n2010\n1000\n0\n0\n");
        assertThat(salida).contains("Titular: N.° 1 Gómez, Laura");
        assertThat(salida.indexOf("Titular: N.° 1 Gómez, Laura")).isLessThan(salida.indexOf("Patente:"));
        assertThat(salida).contains("Vehículo N.° 6 registrado");
    }

    @Test
    @DisplayName("Nuevo turno: una patente inexistente se rechaza antes de pedir servicio, fecha y hora")
    void turnoConPatenteInexistente() throws Exception {
        when(turnos.verificarVehiculo("ZZ999ZZ"))
                .thenThrow(new EntidadNoEncontradaException("No hay un vehículo registrado con la patente ZZ999ZZ."));
        String salida = ejecutar("2\n1\n1\n1\nZZ999ZZ\n0\n0\n");
        String tramo = salida.substring(salida.indexOf("Patente del vehículo:"));
        assertThat(tramo).contains("No hay un vehículo registrado con la patente ZZ999ZZ.");
        assertThat(tramo.substring(0, tramo.indexOf("  TURNOS"))).doesNotContain("Servicio:").doesNotContain("Fecha");
        assertThat(salida).contains("Resultado: el turno no se registró; no se guardó ningún dato.");
        verify(turnos, never()).registrarTurno(any(), anyInt(), anyString(), anyInt(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("DEF-34: si el cliente se registró y el vehículo falla, el resultado lo dice (no «no se guardó nada»)")
    void clienteRegistradoYVehiculoRechazado() throws Exception {
        when(clientes.buscarCliente(99)).thenThrow(new EntidadNoEncontradaException("No existe el cliente N.° 99."));
        Cliente nuevo = new Cliente("Tomás", "Núñez", "11-4444-8888", null);
        nuevo.setId(9);
        when(clientes.registrarCliente(anyString(), anyString(), anyString(), anyString())).thenReturn(nuevo);
        when(clientes.registrarVehiculo(anyInt(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RegistroDuplicadoException("Ya existe un vehículo con la patente AE123BC."));
        String salida = ejecutar("2\n2\n2\n99\nS\nTomás\nNúñez\n11-4444-8888\n\nAE123BC\nFord\nKa\n2015\n1000\n0\n0\n");
        assertThat(salida).contains("Resultado: el cliente N.° 9 quedó registrado, pero el vehículo no se registró")
                .doesNotContain("no se guardó ningún dato");
    }

    @Test
    @DisplayName("DEF-35: un turno cancelado se rechaza antes de pedir la fecha nueva o la confirmación")
    void turnoNoConfirmadoSeRechazaAntes() throws Exception {
        Turno cancelado = new Turno(8, Fixtures.echeandia(), Fixtures.gol(), Fixtures.cambioDeAceite(), 2,
                LocalDateTime.of(2026, 10, 7, 10, 0), EstadoTurno.CANCELADO, null);
        when(turnos.buscarTurno(8)).thenReturn(cancelado);
        String salida = ejecutar("2\n1\n2\n8\n3\n8\n0\n0\n");
        assertThat(salida).contains("Solo se pueden reprogramar turnos confirmados; el turno N.° 8 está CANCELADO",
                "Solo se pueden cancelar turnos confirmados", "Resultado: el turno no se reprogramó",
                "Resultado: el turno no se canceló")
                .doesNotContain("Nueva fecha").doesNotContain("¿Confirma la cancelación?");
        verify(turnos, never()).reprogramarTurno(anyInt(), anyString(), anyString());
        verify(turnos, never()).cancelarTurno(anyInt());
    }
}
