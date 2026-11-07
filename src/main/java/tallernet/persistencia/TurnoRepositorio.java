package tallernet.persistencia;

import tallernet.excepciones.PersistenciaException;
import tallernet.excepciones.TallerNetException;
import tallernet.modelo.EstadoTurno;
import tallernet.modelo.ResumenSede;
import tallernet.modelo.Turno;
import java.time.LocalDate;
import java.util.List;

/** Operaciones de turnos propias del CU001, además del contrato común. */
public interface TurnoRepositorio extends Dao<Turno, Integer> {

    /**
     * Verifica la disponibilidad y registra el turno en una única transacción (RF-TN05).
     * @throws tallernet.excepciones.TurnoSuperpuestoException si el horario está ocupado
     */
    int insertarSiDisponible(Turno turno) throws TallerNetException;

    /** Cambia fecha y hora de un turno existente con la misma verificación transaccional (RF-TN06). */
    void reprogramarSiDisponible(Turno turno) throws TallerNetException;

    /** Vuelve a confirmar un turno cancelado si su franja sigue libre (deshacer cancelación). */
    void reconfirmarSiDisponible(Turno turno) throws TallerNetException;

    /**
     * Cambia el estado solo si el turno sigue en el estado esperado (DEF-05): evita, por ejemplo,
     * cancelar desde una sede un turno que otra terminal ya marcó como atendido.
     * @throws tallernet.excepciones.TurnoInvalidoException si el estado ya no es el esperado
     */
    void cambiarEstado(int turnoId, EstadoTurno esperado, EstadoTurno nuevo) throws TallerNetException;

    /** Turnos no cancelados de una sede en un día (agenda). */
    List<Turno> listarAgenda(int sedeId, LocalDate fecha) throws PersistenciaException;

    /** Turnos de un vehículo en cualquiera de las sedes (RF-TN22). */
    List<Turno> historialPorPatente(String patente) throws PersistenciaException;

    /** Turnos atendidos, insumo de la estimación del próximo service. */
    List<Turno> listarAtendidos() throws PersistenciaException;

    /** Turnos no cancelados de ambas sedes en el intervalo [desde, hastaExclusive) (reporte semanal). */
    List<Turno> listarEntre(LocalDate desde, LocalDate hastaExclusive) throws PersistenciaException;

    /** Carga consolidada de turnos confirmados por sede (consulta Q3 del AP2). */
    List<ResumenSede> resumenPorSede() throws PersistenciaException;
}
