package co.edu.uniquindio.poo.controller;

import co.edu.uniquindio.poo.excepcion.ClienteNoEncontradoException;
import co.edu.uniquindio.poo.modelo.Alerta;
import co.edu.uniquindio.poo.servicio.AlertasServicio;

import java.util.List;

/** Intermediario de la pestaña de alertas. No usa JavaFX. */
public class AlertasController {

    private final AlertasServicio alertasServicio;

    public AlertasController(AlertasServicio alertasServicio) {
        this.alertasServicio = alertasServicio;
    }

    /** Las más recientes primero (lo ordena el servicio). */
    public List<Alerta> alertasDe(String idCliente) throws ClienteNoEncontradoException {
        return alertasServicio.alertasDe(idCliente);
    }
}
