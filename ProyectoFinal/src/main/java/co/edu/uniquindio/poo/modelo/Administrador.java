package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;

/**
 * Usuario que gestiona el sistema (ver clientes, reportes globales).
 * Herencia: reutiliza todo lo de Usuario y solo sobrescribe getRol().
 */
public class Administrador extends Usuario {

    public Administrador(String identificacion, String nombre, String correo, String telefono, String contrasena)
            throws DatoInvalidoException {
        super(identificacion, nombre, correo, telefono, contrasena);
    }

    @Override
    public String getRol() {
        return "ADMINISTRADOR";
    }
}
