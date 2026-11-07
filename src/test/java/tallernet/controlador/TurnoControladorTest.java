package tallernet.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import tallernet.archivos.RegistroOperaciones;
import tallernet.excepciones.DatoInvalidoException;
import tallernet.excepciones.EntidadNoEncontradaException;
import tallernet.excepciones.PersistenciaException;
import tallernet.excepciones.TallerNetException;
import tallernet.excepciones.TurnoInvalidoException;
import tallernet.excepciones.TurnoSuperpuestoException;
import tallernet.modelo.AgendaDiaria;
import tallernet.modelo.Empleado;
import tallernet.modelo.EstadoTurno;
import tallernet.modelo.Fixtures;
import tallernet.modelo.Rol;
import tallernet.modelo.Sede;
import tallernet.modelo.Servicio;
import tallernet.modelo.Turno;
import tallernet.persistencia.Dao;
import tallernet.persistencia.TurnoRepositorio;
import tallernet.persistencia.VehiculoRepositorio;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CPC12 - TurnoControlador aislado de la base (Mockito). Cubre el CU001 completo (alta, flujos
 * alternativos, S5), reprogramación, cancelación y su pila de deshacer, cola de recepción y
 * sugerencia de horario. Reloj fijo: jueves 01/10/2026 08:00.
 */
@DisplayName("CPC12 - TurnoControlador (Mockito)")
class TurnoControladorTest {

    private static final ZoneId BA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 1, 8, 0);
    private static final String MIERCOLES = "07/10/2026";

    private TurnoRepositorio turnos;
    private VehiculoRepositorio vehiculos;
    private Dao<Sede, Integer> sedes;
    private Dao<Servicio, Integer> servicios;
    private RegistroOperaciones registro;
    private TurnoControlador controlador;
    private Empleado recepcionista;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void preparar() throws Exception {
        turnos = mock(TurnoRepositorio.class);
        vehiculos = mock(VehiculoRepositorio.class);
        sedes = mock(Dao.class);
        servicios = mock(Dao.class);
        registro = mock(RegistroOperaciones.class);
        controlador = conReloj(AHORA);
        recepcionista = new Empleado(5, 1, "Laura", "Gómez", Rol.RECEPCIONISTA, true);
        when(sedes.buscarPorId(1)).thenReturn(Optional.of(Fixtures.echeandia()));
        when(vehiculos.buscarPorPatente("AE123BC")).thenReturn(Optional.of(Fixtures.gol()));
        when(servicios.buscarPorId(1)).thenReturn(Optional.of(Fixtures.cambioDeAceite()));
        when(turnos.insertarSiDisponible(any())).thenReturn(10);
    }

    private TurnoControlador conReloj(LocalDateTime ahora) {
        Clock reloj = Clock.fixed(ahora.atZone(BA).toInstant(), BA);
        return new TurnoControlador(turnos, vehiculos, sedes, servicios, new ValidadorTurno(reloj), registro);
    }

    private static Turno turno(int id, LocalDateTime inicio, EstadoTurno estado) {
        return new Turno(id, Fixtures.echeandia(), Fixtures.gol(), Fixtures.cambioDeAceite(), 5, inicio, estado, null);
    }

    private Turno guardado(int id, EstadoTurno estado) throws Exception {
        Turno t = turno(id, LocalDateTime.of(2026, 10, 7, 10, 0), estado);
        when(turnos.buscarPorId(id)).thenReturn(Optional.of(t));
        return t;
    }

    @Nested
    @DisplayName("Alta de turno (CU001)")
    class Alta {

        @Test
        @DisplayName("Flujo principal: normaliza la patente, confirma y registra en la bitácora")
        void flujoPrincipal() throws Exception {
            Turno t = controlador.registrarTurno(recepcionista, 1, "ae 123 bc", 1, MIERCOLES, "10:00", "  Ruido  ");
            assertThat(t.getFechaHora()).isEqualTo(LocalDateTime.of(2026, 10, 7, 10, 0));
            assertThat(t.getEstado()).isEqualTo(EstadoTurno.CONFIRMADO);
            assertThat(t.getObservaciones()).isEqualTo("Ruido");
            verify(turnos).insertarSiDisponible(t);
            verify(registro).registrar(eq("ALTA_TURNO"), contains("AE123BC"));
        }

        @Test
        @DisplayName("Un mecánico, un empleado inactivo o sin sesión no registran turnos")
        void empleadoNoHabilitado() throws Exception {
            Empleado mecanico = new Empleado(6, 1, "Raúl", "Díaz", Rol.MECANICO, true);
            Empleado inactivo = new Empleado(7, 1, "Ana", "Paz", Rol.RECEPCIONISTA, false);
            for (Empleado e : new Empleado[] {mecanico, inactivo, null}) {
                assertThatThrownBy(() -> controlador.registrarTurno(e, 1, "AE123BC", 1, MIERCOLES, "10:00", null))
                        .isInstanceOf(TurnoInvalidoException.class).hasMessageContaining("no está habilitado");
            }
            verifyNoInteractions(turnos, registro);
        }

        @Test
        @DisplayName("El administrador sí registra turnos")
        void administrador() throws Exception {
            Empleado admin = new Empleado(1, 1, "Carlos", "Ruiz Díaz", Rol.ADMINISTRADOR, true);
            assertThat(controlador.registrarTurno(admin, 1, "AE123BC", 1, MIERCOLES, "10:00", null)).isNotNull();
        }

        @Test
        @DisplayName("Sede inexistente o inactiva")
        void sedeInvalida() throws Exception {
            Sede cerrada = new Sede(3, "Sede Cerrada", "Calle 1", LocalTime.of(8, 0), LocalTime.of(20, 0), false);
            when(sedes.buscarPorId(3)).thenReturn(Optional.of(cerrada));
            when(sedes.buscarPorId(9)).thenReturn(Optional.empty());
            for (int sede : new int[] {3, 9}) {
                assertThatThrownBy(() -> controlador.registrarTurno(recepcionista, sede, "AE123BC", 1, MIERCOLES, "10:00", null))
                        .isInstanceOf(EntidadNoEncontradaException.class)
                        .hasMessage("No existe una sede activa con el número " + sede + ".");
            }
            verify(turnos, never()).insertarSiDisponible(any());
        }

        @Test
        @DisplayName("Patente vacía, nula o inexistente")
        void patenteInvalida() {
            assertThatThrownBy(() -> controlador.registrarTurno(recepcionista, 1, "  ", 1, MIERCOLES, "10:00", null))
                    .isInstanceOf(DatoInvalidoException.class).hasMessage("La patente es obligatoria.");
            assertThatThrownBy(() -> controlador.verificarVehiculo(null)).isInstanceOf(DatoInvalidoException.class);
            assertThatThrownBy(() -> controlador.registrarTurno(recepcionista, 1, "zz999zz", 1, MIERCOLES, "10:00", null))
                    .isInstanceOf(EntidadNoEncontradaException.class).hasMessageContaining("ZZ999ZZ");
        }

        @Test
        @DisplayName("Servicio inexistente")
        void servicioInexistente() {
            assertThatThrownBy(() -> controlador.registrarTurno(recepcionista, 1, "AE123BC", 99, MIERCOLES, "10:00", null))
                    .isInstanceOf(EntidadNoEncontradaException.class).hasMessage("No existe el servicio N.° 99.");
        }

        @Test
        @DisplayName("Observaciones: 255 caracteres se aceptan, 256 se rechazan sin insertar")
        void observaciones() throws Exception {
            assertThat(controlador.registrarTurno(recepcionista, 1, "AE123BC", 1, MIERCOLES, "10:00", "x".repeat(255)))
                    .isNotNull();
            assertThatThrownBy(() -> controlador.registrarTurno(recepcionista, 1, "AE123BC", 1, MIERCOLES, "11:00",
                    "x".repeat(256))).isInstanceOf(DatoInvalidoException.class).hasMessageContaining("255");
            verify(turnos, org.mockito.Mockito.times(1)).insertarSiDisponible(any());
        }

        @Test
        @DisplayName("S5: la superposición se propaga y no se registra en la bitácora")
        void superposicion() throws Exception {
            when(turnos.insertarSiDisponible(any())).thenThrow(new TurnoSuperpuestoException("Horario ocupado.", 3));
            assertThatThrownBy(() -> controlador.registrarTurno(recepcionista, 1, "AE123BC", 1, MIERCOLES, "10:00", null))
                    .isInstanceOf(TurnoSuperpuestoException.class);
            verifyNoInteractions(registro);
        }

        @Test
        @DisplayName("Validación temprana (DEF-26): cada dato se verifica por separado")
        void verificaciones() throws Exception {
            assertThat(controlador.verificarSede(1).getId()).isEqualTo(1);
            assertThat(controlador.verificarVehiculo("ae-123-bc").getPatente()).isEqualTo("AE123BC");
            assertThat(controlador.verificarServicio(1).getId()).isEqualTo(1);
            assertThatThrownBy(() -> controlador.verificarSede(2)).isInstanceOf(EntidadNoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("Reprogramación y cancelación (RF-TN06)")
    class Modificaciones {

        @Test
        @DisplayName("Reprograma un turno confirmado")
        void reprograma() throws Exception {
            guardado(7, EstadoTurno.CONFIRMADO);
            Turno t = controlador.reprogramarTurno(7, "08/10/2026", "14:00");
            assertThat(t.getFechaHora()).isEqualTo(LocalDateTime.of(2026, 10, 8, 14, 0));
            verify(turnos).reprogramarSiDisponible(t);
            verify(registro).registrar(eq("REPROGRAMACION"), contains("N.° 7"));
        }

        @Test
        @DisplayName("No reprograma ni cancela turnos que no están confirmados")
        void soloConfirmados() throws Exception {
            guardado(8, EstadoTurno.CANCELADO);
            assertThatThrownBy(() -> controlador.reprogramarTurno(8, "08/10/2026", "14:00"))
                    .isInstanceOf(TurnoInvalidoException.class).hasMessageContaining("está CANCELADO");
            assertThatThrownBy(() -> controlador.cancelarTurno(8))
                    .isInstanceOf(TurnoInvalidoException.class).hasMessageContaining("no se puede cancelar");
            verify(turnos, never()).reprogramarSiDisponible(any());
            verify(turnos, never()).cambiarEstado(anyInt(), any(), any());
        }

        @Test
        @DisplayName("Turno inexistente")
        void inexistente() {
            assertThatThrownBy(() -> controlador.buscarTurno(404))
                    .isInstanceOf(EntidadNoEncontradaException.class).hasMessage("No existe el turno N.° 404.");
        }

        @Test
        @DisplayName("Cancelar y deshacer: pila LIFO, la reconfirmación vuelve a verificar disponibilidad")
        void cancelarYDeshacer() throws Exception {
            Turno a = guardado(7, EstadoTurno.CONFIRMADO);
            Turno b = guardado(8, EstadoTurno.CONFIRMADO);
            controlador.cancelarTurno(7);
            controlador.cancelarTurno(8);
            assertThat(a.getEstado()).isEqualTo(EstadoTurno.CANCELADO);
            verify(turnos).cambiarEstado(7, EstadoTurno.CONFIRMADO, EstadoTurno.CANCELADO);
            assertThat(controlador.deshacerUltimaCancelacion()).isSameAs(b);
            assertThat(b.getEstado()).isEqualTo(EstadoTurno.CONFIRMADO);
            assertThat(controlador.deshacerUltimaCancelacion()).isSameAs(a);
            verify(turnos).reconfirmarSiDisponible(a);
            verify(registro, org.mockito.Mockito.times(2)).registrar(eq("RECONFIRMACION"), any());
            assertThatThrownBy(controlador::deshacerUltimaCancelacion)
                    .isInstanceOf(TurnoInvalidoException.class).hasMessageContaining("No hay cancelaciones");
        }

        @Test
        @DisplayName("Si la franja se ocupó, el turno sigue cancelado y sale de la pila (DEF-06)")
        void deshacerConFranjaOcupada() throws Exception {
            Turno a = guardado(7, EstadoTurno.CONFIRMADO);
            controlador.cancelarTurno(7);
            doThrow(new TurnoSuperpuestoException("Ocupado.", 9)).when(turnos).reconfirmarSiDisponible(a);
            assertThatThrownBy(controlador::deshacerUltimaCancelacion).isInstanceOf(TurnoSuperpuestoException.class);
            assertThat(a.getEstado()).isEqualTo(EstadoTurno.CANCELADO);
            assertThatThrownBy(controlador::deshacerUltimaCancelacion).hasMessageContaining("No hay cancelaciones");
        }

        @Test
        @DisplayName("No se deshace la cancelación de un turno que ya pasó")
        void deshacerTurnoPasado() throws Exception {
            // cancelarTurno no exige que sea futuro: con el reloj el 03/10 se cancela uno del 02/10.
            TurnoControlador despues = conReloj(LocalDateTime.of(2026, 10, 3, 9, 0));
            Turno viejo = turno(4, LocalDateTime.of(2026, 10, 2, 9, 0), EstadoTurno.CONFIRMADO);
            when(turnos.buscarPorId(4)).thenReturn(Optional.of(viejo));
            despues.cancelarTurno(4);
            assertThatThrownBy(despues::deshacerUltimaCancelacion).isInstanceOf(TurnoInvalidoException.class);
            assertThat(viejo.getEstado()).isEqualTo(EstadoTurno.CANCELADO);
            verify(turnos, never()).reconfirmarSiDisponible(any());
            assertThatThrownBy(despues::deshacerUltimaCancelacion).hasMessageContaining("No hay cancelaciones");
        }
    }

    @Nested
    @DisplayName("Agenda, disponibilidad y sugerencia (S5)")
    class Agenda {

        @Test
        @DisplayName("La agenda se devuelve ordenada por hora")
        void agendaOrdenada() throws Exception {
            Turno t11 = turno(2, LocalDateTime.of(2026, 10, 7, 11, 0), EstadoTurno.CONFIRMADO);
            Turno t9 = turno(1, LocalDateTime.of(2026, 10, 7, 9, 0), EstadoTurno.CONFIRMADO);
            when(turnos.listarAgenda(1, LocalDate.of(2026, 10, 7))).thenReturn(List.of(t11, t9));
            assertThat(controlador.consultarAgenda(1, MIERCOLES)).containsExactly(t9, t11);
        }

        @Test
        @DisplayName("Disponibilidad: grilla del día; domingo rechazado")
        void disponibilidad() throws Exception {
            when(turnos.listarAgenda(1, LocalDate.of(2026, 10, 7)))
                    .thenReturn(List.of(turno(1, LocalDateTime.of(2026, 10, 7, 10, 0), EstadoTurno.CONFIRMADO)));
            AgendaDiaria agenda = controlador.consultarDisponibilidad(1, MIERCOLES);
            assertThat(agenda.getFecha()).isEqualTo(LocalDate.of(2026, 10, 7));
            assertThatThrownBy(() -> controlador.consultarDisponibilidad(1, "04/10/2026"))
                    .isInstanceOf(TurnoInvalidoException.class);
        }

        @Test
        @DisplayName("Sugiere el primer hueco posterior a la hora pedida")
        void sugerenciaPosterior() throws Exception {
            when(turnos.listarAgenda(1, LocalDate.of(2026, 10, 7)))
                    .thenReturn(List.of(turno(1, LocalDateTime.of(2026, 10, 7, 10, 0), EstadoTurno.CONFIRMADO)));
            assertThat(controlador.sugerirHorario(1, 1, MIERCOLES, "10:00")).contains(LocalTime.of(11, 0));
        }

        @Test
        @DisplayName("Si no hay hueco posterior, busca desde la apertura")
        void sugerenciaDesdeApertura() throws Exception {
            when(turnos.listarAgenda(1, LocalDate.of(2026, 10, 7))).thenReturn(List.of());
            assertThat(controlador.sugerirHorario(1, 1, MIERCOLES, "19:30")).contains(LocalTime.of(8, 0));
        }

        @Test
        @DisplayName("Para hoy nunca sugiere un horario ya pasado (DEF-24)")
        void sugerenciaHoy() throws Exception {
            TurnoControlador mediodia = conReloj(LocalDateTime.of(2026, 10, 1, 12, 10));
            when(turnos.listarAgenda(1, LocalDate.of(2026, 10, 1))).thenReturn(List.of());
            assertThat(mediodia.sugerirHorario(1, 1, "01/10/2026", "09:00"))
                    .hasValueSatisfying(h -> assertThat(h).isAfterOrEqualTo(LocalTime.of(12, 10)));
        }
    }

    @Nested
    @DisplayName("Cola de recepción (FIFO)")
    class Recepcion {

        @Test
        @DisplayName("Llegada, atención en orden y estado ATENDIDO")
        void colaFifo() throws Exception {
            Turno a = guardado(7, EstadoTurno.CONFIRMADO);
            Turno b = guardado(8, EstadoTurno.CONFIRMADO);
            controlador.registrarLlegada(7);
            controlador.registrarLlegada(8);
            assertThat(controlador.verColaRecepcion()).containsExactly(a, b);
            assertThat(controlador.atenderSiguiente()).isSameAs(a);
            assertThat(a.getEstado()).isEqualTo(EstadoTurno.ATENDIDO);
            verify(turnos).cambiarEstado(7, EstadoTurno.CONFIRMADO, EstadoTurno.ATENDIDO);
            verify(registro).registrar(eq("ATENCION"), contains("N.° 7"));
            assertThat(controlador.verColaRecepcion()).containsExactly(b);
        }

        @Test
        @DisplayName("Rechaza llegada duplicada, de turno no confirmado y atender con la cola vacía")
        void rechazos() throws Exception {
            guardado(7, EstadoTurno.CONFIRMADO);
            guardado(8, EstadoTurno.ATENDIDO);
            controlador.registrarLlegada(7);
            assertThatThrownBy(() -> controlador.registrarLlegada(7)).hasMessageContaining("ya está en la cola");
            assertThatThrownBy(() -> controlador.registrarLlegada(8)).hasMessageContaining("Solo pueden registrar llegada");
            controlador.atenderSiguiente();
            assertThatThrownBy(controlador::atenderSiguiente).hasMessageContaining("vacía");
        }

        @Test
        @DisplayName("Cancelar un turno lo retira de la cola")
        void cancelarRetiraDeLaCola() throws Exception {
            guardado(7, EstadoTurno.CONFIRMADO);
            controlador.registrarLlegada(7);
            controlador.cancelarTurno(7);
            assertThat(controlador.verColaRecepcion()).isEmpty();
        }

        @Test
        @DisplayName("Si otra terminal ya cambió el turno, sale de la cola; si falla la base, queda")
        void fallasAlAtender() throws Exception {
            guardado(7, EstadoTurno.CONFIRMADO);
            guardado(8, EstadoTurno.CONFIRMADO);
            controlador.registrarLlegada(7);
            controlador.registrarLlegada(8);
            doThrow(new TurnoInvalidoException("Ya cambió.")).when(turnos)
                    .cambiarEstado(7, EstadoTurno.CONFIRMADO, EstadoTurno.ATENDIDO);
            assertThatThrownBy(controlador::atenderSiguiente).isInstanceOf(TurnoInvalidoException.class);
            assertThat(controlador.verColaRecepcion()).extracting(Turno::getId).containsExactly(8);
            doThrow(new PersistenciaException("Sin conexión.", null)).when(turnos)
                    .cambiarEstado(8, EstadoTurno.CONFIRMADO, EstadoTurno.ATENDIDO);
            assertThatThrownBy(controlador::atenderSiguiente).isInstanceOf(PersistenciaException.class);
            assertThat(controlador.verColaRecepcion()).extracting(Turno::getId).containsExactly(8);
        }

        @Test
        @DisplayName("La cola expuesta es una copia inmodificable")
        void copiaInmodificable() throws TallerNetException {
            assertThatThrownBy(() -> controlador.verColaRecepcion().add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
