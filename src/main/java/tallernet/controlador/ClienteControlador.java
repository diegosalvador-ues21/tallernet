package tallernet.controlador;

import tallernet.archivos.RegistroOperaciones;
import tallernet.excepciones.DatoInvalidoException;
import tallernet.excepciones.EntidadNoEncontradaException;
import tallernet.excepciones.TallerNetException;
import tallernet.modelo.Cliente;
import tallernet.modelo.Turno;
import tallernet.modelo.Vehiculo;
import tallernet.persistencia.Dao;
import tallernet.persistencia.TurnoRepositorio;
import tallernet.persistencia.VehiculoRepositorio;
import tallernet.util.Busqueda;
import tallernet.util.Ordenamiento;
import tallernet.util.ResultadoBusqueda;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Precondiciones del CU001: alta y consulta de clientes y vehículos (RF-TN02, RF-TN03, RF-TN22). */
public class ClienteControlador {

    /** Compara texto en español ignorando tildes y mayúsculas: "Álvarez" queda junto a "Alonso". */
    private static final Collator ESPANOL = Collator.getInstance(tallernet.util.Formatos.ARGENTINA);

    static {
        ESPANOL.setStrength(Collator.PRIMARY);
    }

    /** Criterios de orden disponibles para el listado de clientes. */
    public enum CriterioCliente {
        APELLIDO("apellido y nombre", Comparator.comparing(Cliente::getApellido, ESPANOL)
                .thenComparing(Cliente::getNombre, ESPANOL)),
        ANTIGUEDAD("fecha de alta", Comparator.comparing(Cliente::getFechaAlta,
                Comparator.nullsLast(Comparator.naturalOrder())));

        private final String descripcion;
        private final Comparator<Cliente> comparador;

        CriterioCliente(String descripcion, Comparator<Cliente> comparador) {
            this.descripcion = descripcion;
            this.comparador = comparador;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public Comparator<Cliente> getComparador() {
            return comparador;
        }
    }

    /** Resultado del listado: los clientes ordenados y el costo del algoritmo. */
    public record ListadoClientes(List<Cliente> clientes, String algoritmo, long comparaciones) {
    }

    /** Resultado de la búsqueda de un vehículo, con el costo de ambas estrategias para compararlas. */
    public record BusquedaVehiculo(String patenteBuscada, Optional<Vehiculo> vehiculo, Optional<Cliente> titular,
                                   int total, int comparacionesBinaria, int comparacionesLineal) {
    }

    private final Dao<Cliente, Integer> clientes;
    private final VehiculoRepositorio vehiculos;
    private final TurnoRepositorio turnos;
    private final RegistroOperaciones registro;

    public ClienteControlador(Dao<Cliente, Integer> clientes, VehiculoRepositorio vehiculos, TurnoRepositorio turnos) {
        this(clientes, vehiculos, turnos, RegistroOperaciones.NINGUNO);
    }

    public ClienteControlador(Dao<Cliente, Integer> clientes, VehiculoRepositorio vehiculos, TurnoRepositorio turnos,
                              RegistroOperaciones registro) {
        this.registro = registro;
        this.clientes = clientes;
        this.vehiculos = vehiculos;
        this.turnos = turnos;
    }

    /** RF-TN02: valida en el modelo y persiste. El cliente queda visible desde ambas sedes. */
    public Cliente registrarCliente(String nombre, String apellido, String telefono, String email)
            throws TallerNetException {
        Cliente cliente = new Cliente(nombre, apellido, telefono, email);
        clientes.insertar(cliente);
        registro.registrar("ALTA_CLIENTE", "Cliente N.° " + cliente.getId());
        return cliente;
    }

    /** Cliente por número; se usa para mostrar los datos actuales antes de modificarlos. */
    public Cliente buscarCliente(int clienteId) throws TallerNetException {
        return clientes.buscarPorId(clienteId)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el cliente N.° " + clienteId + "."));
    }

    /**
     * Modifica teléfono y correo (UPDATE, TP4). Un dato vacío conserva el valor actual, salvo
     * el correo, que se borra escribiendo un guion. Los setters del modelo validan antes de tocar
     * la base, igual que en el alta (RNF-TN06).
     */
    public Cliente actualizarContacto(int clienteId, String telefono, String email) throws TallerNetException {
        Cliente cliente = buscarCliente(clienteId);
        if (telefono != null && !telefono.isBlank()) {
            cliente.setTelefono(telefono);
        }
        if ("-".equals(email == null ? null : email.strip())) {
            cliente.setEmail(null);
        } else if (email != null && !email.isBlank()) {
            cliente.setEmail(email);
        }
        clientes.actualizar(cliente);
        registro.registrar("MODIFICACION_CLIENTE", "Cliente N.° " + clienteId);
        return buscarCliente(clienteId);
    }

    /** RF-TN03: registra un vehículo con su kilometraje al alta. */
    public Vehiculo registrarVehiculo(int clienteId, String patente, String marca, String modelo,
                                      String anioTexto, String kmTexto) throws TallerNetException {
        clientes.buscarPorId(clienteId)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el cliente N.° " + clienteId + "."));
        int anio = convertirEntero(anioTexto, "El año");
        int km = convertirEntero(kmTexto, "El kilometraje");
        Vehiculo vehiculo = new Vehiculo(clienteId, patente, marca, modelo, anio, km);
        vehiculos.insertar(vehiculo);
        registro.registrar("ALTA_VEHICULO", "Vehículo " + vehiculo.getPatente() + " del cliente N.° " + clienteId);
        return vehiculos.buscarPorId(vehiculo.getId()).orElse(vehiculo);
    }

    /**
     * Lista los clientes con un algoritmo propio: inserción para el orden por antigüedad
     * (los registros llegan casi ordenados por id) y quicksort para el orden alfabético.
     */
    public ListadoClientes listarClientes(CriterioCliente criterio) throws TallerNetException {
        List<Cliente> lista = new ArrayList<>(clientes.listar());
        long comparaciones;
        String algoritmo;
        if (criterio == CriterioCliente.ANTIGUEDAD) {
            algoritmo = "inserción";
            comparaciones = Ordenamiento.insercion(lista, criterio.getComparador());
        } else {
            algoritmo = "quicksort";
            comparaciones = Ordenamiento.quicksort(lista, criterio.getComparador());
        }
        return new ListadoClientes(lista, algoritmo, comparaciones);
    }

    /**
     * Busca un vehículo por patente con búsqueda binaria. La lista llega ordenada por patente
     * desde la base (ORDER BY con intercalación binaria, el mismo orden que String.compareTo),
     * de modo que la aplicación no paga un ordenamiento en cada consulta (DEF-07). Como
     * referencia se corre también la búsqueda lineal sobre la lista sin ordenar.
     */
    public BusquedaVehiculo buscarVehiculoPorPatente(String patente) throws TallerNetException {
        if (patente == null || patente.isBlank()) {
            throw new DatoInvalidoException("La patente es obligatoria.");
        }
        String clave = Vehiculo.normalizarPatente(patente);
        List<Vehiculo> ordenada = vehiculos.listarOrdenadoPorPatente();
        ResultadoBusqueda binaria = Busqueda.binaria(ordenada, clave, Vehiculo::getPatente, Comparator.naturalOrder());
        ResultadoBusqueda lineal = Busqueda.lineal(vehiculos.listar(), clave, Vehiculo::getPatente);
        Optional<Vehiculo> vehiculo = binaria.encontrado() ? Optional.of(ordenada.get(binaria.indice())) : Optional.empty();
        Optional<Cliente> titular = vehiculo.isPresent()
                ? clientes.buscarPorId(vehiculo.get().getClienteId()) : Optional.empty();
        return new BusquedaVehiculo(clave, vehiculo, titular, ordenada.size(), binaria.comparaciones(),
                lineal.comparaciones());
    }

    /** RF-TN22: historial del vehículo en cualquiera de las sedes. */
    public List<Turno> historialVehiculo(String patente) throws TallerNetException {
        String clave = Vehiculo.normalizarPatente(patente);
        if (vehiculos.buscarPorPatente(clave).isEmpty()) {
            throw new EntidadNoEncontradaException("No hay un vehículo registrado con la patente " + clave + ".");
        }
        return turnos.historialPorPatente(clave);
    }

    /** Acepta "18500" o "18.500" (punto como separador de miles); rechaza "1.5" (DEF-09). */
    private static int convertirEntero(String texto, String campo) throws DatoInvalidoException {
        String limpio = texto == null ? "" : texto.strip();
        if (!limpio.matches("-?\\d+|-?\\d{1,3}(\\.\\d{3})+")) {
            throw new DatoInvalidoException(campo + " debe ser un número entero.");
        }
        try {
            return Integer.parseInt(limpio.replace(".", ""));
        } catch (NumberFormatException e) {
            throw new DatoInvalidoException(campo + " es demasiado grande.");
        }
    }
}
