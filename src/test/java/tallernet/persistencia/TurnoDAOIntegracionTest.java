package tallernet.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import tallernet.excepciones.TallerNetException;
import tallernet.excepciones.TurnoSuperpuestoException;
import tallernet.modelo.Sede;
import tallernet.modelo.Servicio;
import tallernet.modelo.Turno;
import tallernet.modelo.Vehiculo;
import java.time.LocalDateTime;
import tallernet.modelo.ResumenSede;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * CPI04 - Integración controlador/DAO/MySQL: transacción, bloqueo y reversión.
 * Corre contra un MySQL real recreado desde los scripts 01 y 02 antes de cada prueba.
 * Si el servidor no está disponible, las pruebas se informan como omitidas.
 */
@DisplayName("CPI04 - Transacción de alta de turnos contra MySQL")
class TurnoDAOIntegracionTest {

    private final TurnoDAO turnos = new TurnoDAO();
    private final SedeDAO sedes = new SedeDAO();
    private final ServicioDAO servicios = new ServicioDAO();
    private final VehiculoDAO vehiculos = new VehiculoDAO();

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

    private Turno turno(int sedeId, String patente, int servicioId, LocalDateTime inicio) throws TallerNetException {
        Sede sede = sedes.buscarPorId(sedeId).orElseThrow();
        Vehiculo v = vehiculos.buscarPorPatente(patente).orElseThrow();
        Servicio s = servicios.buscarPorId(servicioId).orElseThrow();
        return new Turno(sede, v, s, sedeId == 1 ? 2 : 3, inicio, null);
    }

    @Test
    @DisplayName("CPI04-1 alta en horario ocupado: se revierte sin insertar filas")
    void altaOcupadaSeRevierte() throws Exception {
        int antes = BaseDePruebas.contarTurnos();
        Turno t = turno(1, "AE123BC", 1, LocalDateTime.of(2026, 10, 5, 10, 30));
        TurnoSuperpuestoException e = assertThrows(TurnoSuperpuestoException.class, () -> turnos.insertarSiDisponible(t));
        assertEquals(2, e.getTurnoExistenteId());
        assertEquals(antes, BaseDePruebas.contarTurnos());
    }

    @Test
    @DisplayName("CPI04-2 alta en horario libre: se confirma con número correlativo 7")
    void altaLibreSeConfirma() throws Exception {
        Turno t = turno(1, "AD321JK", 1, LocalDateTime.of(2026, 10, 5, 13, 0));
        assertEquals(7, turnos.insertarSiDisponible(t));
        assertEquals(7, BaseDePruebas.contarTurnos());
    }

    @Test
    @DisplayName("CPI04-3 concurrencia: dos sedes piden la misma franja de Flores y solo una la obtiene")
    void concurrenciaMismaSede() throws Exception {
        LocalDateTime franja = LocalDateTime.of(2026, 10, 7, 10, 0);
        List<Turno> pedidos = List.of(turno(2, "AF789GH", 1, franja), turno(2, "AC456DE", 1, franja));
        int[] resultado = competir(pedidos);
        assertEquals(1, resultado[0], "exactamente una reserva confirmada");
        assertEquals(1, resultado[1], "exactamente un rechazo por superposición (S5)");
        assertEquals(7, BaseDePruebas.contarTurnos());
    }

    @Test
    @DisplayName("CPI04-4 concurrencia: la misma franja en sedes distintas no se bloquea")
    void concurrenciaSedesDistintas() throws Exception {
        LocalDateTime franja = LocalDateTime.of(2026, 10, 7, 10, 0);
        int[] resultado = competir(List.of(turno(1, "AF789GH", 1, franja), turno(2, "AC456DE", 1, franja)));
        assertEquals(2, resultado[0]);
        assertEquals(8, BaseDePruebas.contarTurnos());
    }

    @Test
    @DisplayName("CPI04-5 reprogramar sobre una franja ocupada: la fecha original se conserva")
    void reprogramarOcupadoSeRevierte() throws Exception {
        Turno t1 = turnos.buscarPorId(1).orElseThrow();
        t1.setFechaHora(LocalDateTime.of(2026, 10, 5, 11, 0));
        assertThrows(TurnoSuperpuestoException.class, () -> turnos.reprogramarSiDisponible(t1));
        assertEquals(LocalDateTime.of(2026, 10, 5, 9, 0), turnos.buscarPorId(1).orElseThrow().getFechaHora());
    }

    @Test
    @DisplayName("CPI04-6 cambio de estado condicionado: no se cancela un turno ya atendido")
    void cambioDeEstadoCondicionado() throws Exception {
        turnos.cambiarEstado(1, tallernet.modelo.EstadoTurno.CONFIRMADO,
                tallernet.modelo.EstadoTurno.ATENDIDO);
        assertThrows(tallernet.excepciones.TurnoInvalidoException.class,
                () -> turnos.cambiarEstado(1, tallernet.modelo.EstadoTurno.CONFIRMADO,
                        tallernet.modelo.EstadoTurno.CANCELADO));
        assertEquals(tallernet.modelo.EstadoTurno.ATENDIDO,
                turnos.buscarPorId(1).orElseThrow().getEstado());
    }

    @Test
    @DisplayName("CPI04-7 reporte por sede: una sede sin turnos confirmados aparece con ceros")
    void reporteIncluyeSedesSinTurnos() throws Exception {
        for (int id : new int[] {3, 4}) {
            turnos.cambiarEstado(id, tallernet.modelo.EstadoTurno.CONFIRMADO,
                    tallernet.modelo.EstadoTurno.CANCELADO);
        }
        List<ResumenSede> filas = turnos.resumenPorSede();
        assertEquals(2, filas.size());
        ResumenSede flores = filas.stream().filter(f -> f.sede().equals("Sede Flores")).findFirst().orElseThrow();
        assertEquals(0, flores.turnosConfirmados());
    }

    /** Lanza los pedidos a la vez desde hilos distintos; devuelve {confirmados, rechazados}. */
    private int[] competir(List<Turno> pedidos) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(pedidos.size());
        CountDownLatch largada = new CountDownLatch(1);
        try {
            List<Future<Boolean>> futuros = new java.util.ArrayList<>();
            for (Turno t : pedidos) {
                Callable<Boolean> tarea = () -> {
                    largada.await();
                    try {
                        turnos.insertarSiDisponible(t);
                        return true;
                    } catch (TurnoSuperpuestoException e) {
                        return false;
                    }
                };
                futuros.add(pool.submit(tarea));
            }
            largada.countDown();
            int confirmados = 0;
            int rechazados = 0;
            for (Future<Boolean> f : futuros) {
                if (f.get()) {
                    confirmados++;
                } else {
                    rechazados++;
                }
            }
            assertTrue(confirmados + rechazados == pedidos.size());
            return new int[] {confirmados, rechazados};
        } finally {
            pool.shutdownNow();
        }
    }
}
