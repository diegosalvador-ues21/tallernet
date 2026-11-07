package tallernet.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tallernet.archivos.RegistroOperaciones;
import tallernet.excepciones.DatoInvalidoException;
import tallernet.excepciones.EntidadNoEncontradaException;
import tallernet.modelo.Empleado;
import tallernet.modelo.EstadoTurno;
import tallernet.modelo.Fixtures;
import tallernet.modelo.ReporteSemanal;
import tallernet.modelo.Rol;
import tallernet.modelo.Sede;
import tallernet.modelo.Servicio;
import tallernet.modelo.Turno;
import tallernet.persistencia.Dao;
import tallernet.persistencia.TurnoRepositorio;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** CPC13 - CatalogoControlador y SesionControlador aislados de la base (Mockito). */
@DisplayName("CPC13 - Catálogo y sesión (Mockito)")
class CatalogoYSesionControladorTest {

    private Dao<Sede, Integer> sedes;
    private Dao<Servicio, Integer> servicios;
    private Dao<Empleado, Integer> empleados;
    private TurnoRepositorio turnos;
    private RegistroOperaciones registro;
    private CatalogoControlador catalogo;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void preparar() {
        sedes = mock(Dao.class);
        servicios = mock(Dao.class);
        empleados = mock(Dao.class);
        turnos = mock(TurnoRepositorio.class);
        registro = mock(RegistroOperaciones.class);
        catalogo = new CatalogoControlador(sedes, servicios, turnos, registro);
    }

    @Test
    @DisplayName("Lista el catálogo por precio y por duración")
    void ordenaCatalogo() throws Exception {
        Servicio caro = Servicio.crear(1, "Caro", new BigDecimal("90000.00"), 30, null);
        Servicio barato = Servicio.crear(2, "Barato", new BigDecimal("1000.00"), 120, null);
        when(servicios.listar()).thenReturn(List.of(caro, barato));
        assertThat(catalogo.listarServicios(CatalogoControlador.CriterioServicio.PRECIO)).containsExactly(barato, caro);
        assertThat(catalogo.listarServicios(CatalogoControlador.CriterioServicio.DURACION)).containsExactly(caro, barato);
        assertThat(CatalogoControlador.CriterioServicio.DURACION.getDescripcion()).isEqualTo("duración");
    }

    @Test
    @DisplayName("Actualiza el precio, lo registra en la bitácora y devuelve lo releído de la base")
    void actualizaPrecio() throws Exception {
        Servicio s = Fixtures.cambioDeAceite();
        when(servicios.buscarPorId(1)).thenReturn(Optional.of(s));
        Servicio r = catalogo.actualizarPrecio(1, "$ 52.000,50");
        assertThat(r.getPrecioBase()).isEqualByComparingTo("52000.50");
        verify(servicios).actualizar(s);
        verify(registro).registrar(eq("MODIFICACION_PRECIO"), contains("1000.00 -> 52000.50"));
    }

    @Test
    @DisplayName("Precio con formato inválido o servicio inexistente: no se actualiza nada")
    void precioRechazado() throws Exception {
        when(servicios.buscarPorId(1)).thenReturn(Optional.of(Fixtures.cambioDeAceite()));
        assertThatThrownBy(() -> catalogo.actualizarPrecio(1, "52,000.50")).isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> catalogo.actualizarPrecio(1, "0")).isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> catalogo.actualizarPrecio(9, "100")).isInstanceOf(EntidadNoEncontradaException.class);
        assertThatThrownBy(() -> catalogo.buscarServicio(9)).hasMessage("No existe el servicio N.° 9.");
        verify(servicios, never()).actualizar(any());
    }

    @ParameterizedTest(name = "«{0}» -> {1}")
    @CsvSource(delimiter = '|', value = {"52000|52000.00", "52.000|52000.00", "52.000,5|52000.50",
        "1.234.567,89|1234567.89", "' $ 7 '|7.00"})
    @DisplayName("Conversión de precios al formato argentino")
    void conviertePrecios(String texto, String esperado) throws Exception {
        assertThat(CatalogoControlador.convertirPrecio(texto)).isEqualTo(new BigDecimal(esperado));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"-5", "52,000.50", "123456789", "1,234", "abc", "12.34"})
    @DisplayName("Precios con formato inválido")
    void preciosInvalidos(String texto) {
        assertThatThrownBy(() -> CatalogoControlador.convertirPrecio(texto)).isInstanceOf(DatoInvalidoException.class);
    }

    @Test
    @DisplayName("Reporte semanal: de lunes a lunes siguiente y solo sedes activas")
    void reporteSemanal() throws Exception {
        Sede cerrada = new Sede(3, "Sede Cerrada", "Calle 1", LocalTime.of(8, 0), LocalTime.of(20, 0), false);
        when(sedes.listar()).thenReturn(List.of(Fixtures.echeandia(), cerrada, Fixtures.flores()));
        when(turnos.listarEntre(any(), any())).thenReturn(List.of());
        ReporteSemanal r = catalogo.reporteSemanal("07/10/2026");
        assertThat(r).isNotNull();
        verify(turnos).listarEntre(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 12));
        assertThatThrownBy(() -> catalogo.reporteSemanal("31/02/2026")).isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> catalogo.reporteSemanal(null)).isInstanceOf(DatoInvalidoException.class);
    }

    @Test
    @DisplayName("Próximos services: primero los que tienen kilometraje, ordenados; luego los generales")
    void proximosServices() throws Exception {
        Turno general = new Turno(1, Fixtures.echeandia(), Fixtures.gol(), Fixtures.servicio(2, 60, null), 5,
                LocalDateTime.of(2026, 9, 1, 9, 0), EstadoTurno.ATENDIDO, null);
        Turno aceite = new Turno(2, Fixtures.echeandia(), Fixtures.gol(), Fixtures.cambioDeAceite(), 5,
                LocalDateTime.of(2026, 9, 2, 9, 0), EstadoTurno.ATENDIDO, null);
        when(turnos.listarAtendidos()).thenReturn(List.of(general, aceite));
        List<CatalogoControlador.ProximoService> r = catalogo.estimarProximosServices();
        assertThat(r).extracting(CatalogoControlador.ProximoService::turno).containsExactly(aceite, general);
        assertThat(r.get(0).proximoKm()).hasValue(96500);
        assertThat(r.get(1).proximoKm()).isEmpty();
    }

    @Test
    @DisplayName("Sedes y carga por sede se delegan al repositorio")
    void delegaciones() throws Exception {
        when(sedes.listar()).thenReturn(List.of(Fixtures.echeandia()));
        assertThat(catalogo.listarSedes()).hasSize(1);
        when(turnos.resumenPorSede()).thenReturn(List.of());
        assertThat(catalogo.cargaPorSede()).isEmpty();
        assertThat(new CatalogoControlador(sedes, servicios, turnos).listarSedes()).hasSize(1);
    }

    @Test
    @DisplayName("Sesión: solo recepcionistas y administradores activos")
    void sesion() throws Exception {
        Empleado recep = new Empleado(2, 1, "Laura", "Gómez", Rol.RECEPCIONISTA, true);
        Empleado mecanico = new Empleado(3, 1, "Raúl", "Díaz", Rol.MECANICO, true);
        Empleado inactivo = new Empleado(4, 2, "Ana", "Paz", Rol.RECEPCIONISTA, false);
        Empleado admin = new Empleado(1, 1, "Carlos", "Ruiz Díaz", Rol.ADMINISTRADOR, true);
        when(empleados.listar()).thenReturn(List.of(admin, recep, mecanico, inactivo));
        SesionControlador sesion = new SesionControlador(empleados);
        assertThat(sesion.listarHabilitados()).containsExactly(admin, recep);
        assertThat(sesion.iniciarSesion(2)).isSameAs(recep);
        for (int id : new int[] {3, 4, 99}) {
            assertThatThrownBy(() -> sesion.iniciarSesion(id)).isInstanceOf(EntidadNoEncontradaException.class)
                    .hasMessage("El número no corresponde a un empleado habilitado.");
        }
    }
}
