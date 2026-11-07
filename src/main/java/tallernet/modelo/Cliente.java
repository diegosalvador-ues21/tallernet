package tallernet.modelo;

import java.util.Locale;
import tallernet.excepciones.DatoInvalidoException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/** Cliente del taller, registro único compartido por ambas sedes (RF-TN02). */
public class Cliente extends Persona {

    private static final Pattern TELEFONO = Pattern.compile("^[0-9 +()-]{8,20}$");
    private static final int LARGO_EMAIL = 120;
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private String telefono;
    private String email;
    private LocalDateTime fechaAlta;

    /** Constructor completo, usado al leer un cliente existente de la base. */
    public Cliente(int id, String nombre, String apellido, String telefono, String email,
                   LocalDateTime fechaAlta) throws DatoInvalidoException {
        super(id, nombre, apellido);
        setTelefono(telefono);
        setEmail(email);
        this.fechaAlta = fechaAlta;
    }

    /** Constructor para un cliente nuevo: todavía no tiene id ni fecha de alta. */
    public Cliente(String nombre, String apellido, String telefono, String email)
            throws DatoInvalidoException {
        this(0, nombre, apellido, telefono, email, null);
    }

    @Override
    public String getDescripcionRol() {
        return "Cliente";
    }

    @Override
    public String getDatoDeContacto() {
        return email == null ? "tel. " + telefono : "tel. " + telefono + ", " + email;
    }

    public String getTelefono() {
        return telefono;
    }

    public final void setTelefono(String telefono) throws DatoInvalidoException {
        if (telefono == null || !TELEFONO.matcher(telefono.strip()).matches()) {
            throw new DatoInvalidoException(
                    "El teléfono debe tener entre 8 y 20 caracteres (dígitos, espacios, +, -, paréntesis).");
        }
        this.telefono = telefono.strip();
    }

    public String getEmail() {
        return email;
    }

    /** El correo es opcional; si se informa, debe tener un formato válido. */
    public final void setEmail(String email) throws DatoInvalidoException {
        if (email == null || email.isBlank()) {
            this.email = null;
            return;
        }
        if (email.strip().length() > LARGO_EMAIL || !EMAIL.matcher(email.strip()).matches()) {
            throw new DatoInvalidoException("El correo electrónico no tiene un formato válido.");
        }
        this.email = email.strip().toLowerCase(Locale.ROOT);
    }

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }
}
