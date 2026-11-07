package tallernet.controlador;

import tallernet.archivos.RegistroOperaciones;
import tallernet.excepciones.DatoInvalidoException;
import tallernet.excepciones.EntidadNoEncontradaException;
import tallernet.excepciones.TallerNetException;
import tallernet.modelo.ReporteSemanal;
import tallernet.modelo.Sede;
import tallernet.modelo.Servicio;
import tallernet.modelo.Turno;
import tallernet.persistencia.Dao;
import tallernet.modelo.ResumenSede;
import tallernet.persistencia.TurnoRepositorio;
import tallernet.util.Ordenamiento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;

/** Sedes, catálogo de servicios y reportes para el titular (paquete "Sedes y catálogo" del AP2). */
public class CatalogoControlador {

    /** Criterios de orden del catálogo. */
    public enum CriterioServicio {
        PRECIO("precio base", Comparator.comparing(Servicio::getPrecioBase)),
        DURACION("duración", Comparator.comparingInt(Servicio::getDuracionMin)
                .thenComparing(Servicio::getPrecioBase));

        private final String descripcion;
        private final Comparator<Servicio> comparador;

        CriterioServicio(String descripcion, Comparator<Servicio> comparador) {
            this.descripcion = descripcion;
            this.comparador = comparador;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    /** Estimación del próximo service de un turno atendido. */
    public record ProximoService(Turno turno, OptionalInt proximoKm) {
    }

    private final Dao<Sede, Integer> sedes;
    private final Dao<Servicio, Integer> servicios;
    private final TurnoRepositorio turnos;
    private final RegistroOperaciones registro;

    public CatalogoControlador(Dao<Sede, Integer> sedes, Dao<Servicio, Integer> servicios, TurnoRepositorio turnos) {
        this(sedes, servicios, turnos, RegistroOperaciones.NINGUNO);
    }

    public CatalogoControlador(Dao<Sede, Integer> sedes, Dao<Servicio, Integer> servicios, TurnoRepositorio turnos,
                               RegistroOperaciones registro) {
        this.registro = registro;
        this.sedes = sedes;
        this.servicios = servicios;
        this.turnos = turnos;
    }

    public List<Sede> listarSedes() throws TallerNetException {
        return sedes.listar();
    }

    public List<Servicio> listarServicios(CriterioServicio criterio) throws TallerNetException {
        List<Servicio> lista = new ArrayList<>(servicios.listar());
        Ordenamiento.quicksort(lista, criterio.comparador);
        return lista;
    }

    /**
     * Equivalente en Java de la consulta Q7 del AP2. Cada servicio calcula su próximo
     * kilometraje según su propia clase: los servicios generales devuelven un valor vacío
     * en lugar de un número inventado (polimorfismo).
     */
    public List<ProximoService> estimarProximosServices() throws TallerNetException {
        List<ProximoService> resultado = new ArrayList<>();
        for (Turno t : turnos.listarAtendidos()) {
            int referencia = t.getVehiculo().getKmAlta();
            resultado.add(new ProximoService(t, t.getServicio().calcularProximoServiceKm(referencia)));
        }
        Ordenamiento.insercion(resultado, Comparator.comparingInt(
                (ProximoService p) -> p.proximoKm().isPresent() ? p.proximoKm().getAsInt() : Integer.MAX_VALUE));
        return resultado;
    }

    /** Carga de trabajo consolidada por sede (Q3). */
    public List<ResumenSede> cargaPorSede() throws TallerNetException {
        return turnos.resumenPorSede();
    }

    /** Servicio por número; la vista lo muestra antes de pedir el precio nuevo (DEF-26). */
    public Servicio buscarServicio(int servicioId) throws TallerNetException {
        return servicios.buscarPorId(servicioId)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el servicio N.° " + servicioId + "."));
    }

    /**
     * Actualiza el precio base de un servicio del catálogo (UPDATE, TP4). Acepta "52000",
     * "52.000" o "52.000,50" (punto de miles y coma decimal, como se escribe en Argentina).
     */
    public Servicio actualizarPrecio(int servicioId, String precioTexto) throws TallerNetException {
        Servicio servicio = servicios.buscarPorId(servicioId)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el servicio N.° " + servicioId + "."));
        BigDecimal anterior = servicio.getPrecioBase();
        servicio.setPrecioBase(convertirPrecio(precioTexto));
        servicios.actualizar(servicio);
        registro.registrar("MODIFICACION_PRECIO", "Servicio N.° " + servicioId + ": " + anterior + " -> "
                + servicio.getPrecioBase());
        return servicios.buscarPorId(servicioId).orElse(servicio);
    }

    /** Reporte de carga de lunes a sábado de la semana que contiene la fecha indicada (TP4). */
    public ReporteSemanal reporteSemanal(String fechaTexto) throws TallerNetException {
        LocalDate fecha = convertirFecha(fechaTexto);
        LocalDate lunes = fecha.minusDays(fecha.getDayOfWeek().getValue() - 1L);
        List<Sede> activas = new ArrayList<>();
        for (Sede s : sedes.listar()) {
            if (s.isActiva()) {
                activas.add(s);
            }
        }
        return new ReporteSemanal(fecha, activas, turnos.listarEntre(lunes, lunes.plusDays(7)));
    }

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private static LocalDate convertirFecha(String texto) throws DatoInvalidoException {
        try {
            return LocalDate.parse(texto == null ? "" : texto.strip(), FECHA);
        } catch (DateTimeParseException e) {
            throw new DatoInvalidoException("Formato de fecha no válido (use dd/mm/aaaa).");
        }
    }

    static BigDecimal convertirPrecio(String texto) throws DatoInvalidoException {
        String limpio = texto == null ? "" : texto.strip().replace("$", "").strip();
        if (!limpio.matches("\\d{1,8}(,\\d{1,2})?|\\d{1,3}(\\.\\d{3}){1,2}(,\\d{1,2})?")) {
            throw new DatoInvalidoException("Precio no válido. Use, por ejemplo, 52000 o 52.000,50.");
        }
        return new BigDecimal(limpio.replace(".", "").replace(",", ".")).setScale(2);
    }
}
