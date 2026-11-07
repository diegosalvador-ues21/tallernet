package tallernet.modelo;

import tallernet.excepciones.DatoInvalidoException;

/**
 * Abstracción de los datos comunes a clientes y empleados (diagrama de clases del AP2).
 * No puede instanciarse: cada subclase concreta define cómo se identifica y describe.
 */
public abstract class Persona {

    private static final int LARGO_MAXIMO_NOMBRE = 60;

    private int id;
    private String nombre;
    private String apellido;

    protected Persona(int id, String nombre, String apellido) throws DatoInvalidoException {
        this.id = id;
        setNombre(nombre);
        setApellido(apellido);
    }

    /** Descripción del papel que cumple la persona en el taller. */
    public abstract String getDescripcionRol();

    /** Dato que permite contactar o identificar a la persona en la operación diaria. */
    public abstract String getDatoDeContacto();

    public String getNombreCompleto() {
        return apellido + ", " + nombre;
    }

    public int getId() {
        return id;
    }

    /** Lo asigna la capa de persistencia al insertar el registro. */
    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) throws DatoInvalidoException {
        this.nombre = validarTexto(nombre, "nombre");
    }

    public String getApellido() {
        return apellido;
    }

    public final void setApellido(String apellido) throws DatoInvalidoException {
        this.apellido = validarTexto(apellido, "apellido");
    }

    private static String validarTexto(String valor, String campo) throws DatoInvalidoException {
        if (valor == null || valor.isBlank()) {
            throw new DatoInvalidoException("El " + campo + " es obligatorio.");
        }
        String limpio = valor.strip();
        if (limpio.length() > LARGO_MAXIMO_NOMBRE) {
            throw new DatoInvalidoException("El " + campo + " no puede superar "
                    + LARGO_MAXIMO_NOMBRE + " caracteres.");
        }
        return limpio;
    }

    @Override
    public String toString() {
        return String.format("%s N.° %d: %s (%s)", getDescripcionRol(), id, getNombreCompleto(),
                getDatoDeContacto());
    }
}
