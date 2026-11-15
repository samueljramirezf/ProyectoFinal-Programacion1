package co.edu.uniquindio.poo.controller;

import co.edu.uniquindio.poo.excepcion.ClienteNoEncontradoException;
import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.excepcion.PuntosInsuficientesException;
import co.edu.uniquindio.poo.modelo.Beneficio;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.MovimientoPuntos;
import co.edu.uniquindio.poo.modelo.Rango;
import co.edu.uniquindio.poo.modelo.TipoBeneficio;
import co.edu.uniquindio.poo.servicio.MonederoServicio;
import co.edu.uniquindio.poo.servicio.PuntosServicio;

import java.util.List;

/** Intermediario de la pantalla de puntos y beneficios. No usa JavaFX. */
public class PuntosController {

    private final PuntosServicio puntosServicio;
    private final MonederoServicio monederoServicio;

    public PuntosController(PuntosServicio puntosServicio, MonederoServicio monederoServicio) {
        this.puntosServicio = puntosServicio;
        this.monederoServicio = monederoServicio;
    }

    public Rango consultarRango(String idCliente) throws ClienteNoEncontradoException {
        return puntosServicio.consultarRango(idCliente);
    }

    public List<MovimientoPuntos> historialPuntos(String idCliente) throws ClienteNoEncontradoException {
        return puntosServicio.historialPuntos(idCliente);
    }

    public List<Beneficio> beneficiosVigentes(String idCliente) throws ClienteNoEncontradoException {
        return puntosServicio.beneficiosVigentes(idCliente);
    }

    /** Para elegir el monedero que recibe el bono de 1000 puntos. */
    public List<Monedero> monederosDe(String idCliente) throws ClienteNoEncontradoException {
        return monederoServicio.buscarCliente(idCliente).getMonederos();
    }

    /** @param idMonederoBono solo se usa en el bono de saldo; en los demás puede ir null. */
    public Beneficio canjear(String idCliente, TipoBeneficio tipo, String idMonederoBono)
            throws ClienteNoEncontradoException, PuntosInsuficientesException,
            DatoInvalidoException, MontoInvalidoException {
        return puntosServicio.canjear(idCliente, tipo, idMonederoBono);
    }
}
