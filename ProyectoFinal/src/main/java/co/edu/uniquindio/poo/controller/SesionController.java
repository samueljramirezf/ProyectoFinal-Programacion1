package co.edu.uniquindio.poo.controller;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.servicio.MonederoServicio;

/**
 * Intermediario entre las pantallas de inicio de sesión / registro y el servicio. No usa JavaFX.
 * Como en el tercer parcial: no tiene lógica propia y deja pasar las excepciones para que el
 * viewController las muestre en un Alert.
 */
public class SesionController {

    private final MonederoServicio monederoServicio;

    public SesionController(MonederoServicio monederoServicio) {
        this.monederoServicio = monederoServicio;
    }

    public Cliente iniciarSesion(String identificacion, String contrasena) throws DatoInvalidoException {
        return monederoServicio.iniciarSesion(identificacion, contrasena);
    }

    public Cliente registrarCliente(String identificacion, String nombre, String correo, String telefono,
                                    String contrasena) throws DatoInvalidoException {
        return monederoServicio.registrarCliente(identificacion, nombre, correo, telefono, contrasena);
    }
}
