package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.util.Validaciones;

import java.util.Objects;

/**
 * Persona que inicia sesión en el sistema.
 *
 * ABSTRACTA: en el negocio no existe un "usuario genérico"; siempre es un Cliente o un
 * Administrador. La clase reúne lo común (identificación, datos de contacto, contraseña)
 * para que las subclases lo hereden sin duplicarlo, y declara getRol() como abstracto
 * para que cada una lo defina (polimorfismo).
 */
public abstract class Usuario {

    // Encapsulamiento: atributos privados; la contraseña ni siquiera tiene getter.
    private final String identificacion;
    private final String nombre;
    private final String correo;
    private final String telefono;
    private final String contrasena;

    protected Usuario(String identificacion, String nombre, String correo, String telefono, String contrasena)
            throws DatoInvalidoException {
        this.identificacion = Validaciones.textoObligatorio(identificacion, "identificación");
        this.nombre = Validaciones.textoObligatorio(nombre, "nombre");
        this.correo = Validaciones.correo(correo);
        this.telefono = Validaciones.textoObligatorio(telefono, "teléfono");
        this.contrasena = Validaciones.textoObligatorio(contrasena, "contraseña");
    }

    /** Cada tipo de usuario dice cuál es su rol. */
    public abstract String getRol();

    /** Se compara dentro del objeto para no exponer la contraseña (encapsulamiento). */
    public boolean verificarContrasena(String intento) {
        return contrasena.equals(intento);
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTelefono() {
        return telefono;
    }

    /** Dos usuarios son el mismo si tienen la misma identificación. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        return identificacion.equals(((Usuario) o).identificacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identificacion);
    }

    @Override
    public String toString() {
        return getRol() + " " + nombre + " (" + identificacion + ")";
    }
}
