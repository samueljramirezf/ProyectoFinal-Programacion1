package co.edu.uniquindio.poo.controller;

import co.edu.uniquindio.poo.excepcion.ClienteNoEncontradoException;
import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.TipoMonedero;
import co.edu.uniquindio.poo.servicio.MonederoServicio;
import co.edu.uniquindio.poo.util.Reloj;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Intermediario del panel principal: monederos, saldo total y fecha del reloj. No usa JavaFX. */
public class MonederoController {

    private final MonederoServicio monederoServicio;
    private final Reloj reloj;

    public MonederoController(MonederoServicio monederoServicio, Reloj reloj) {
        this.monederoServicio = monederoServicio;
        this.reloj = reloj;
    }

    public List<Monedero> obtenerMonederos(String idCliente) throws ClienteNoEncontradoException {
        return monederoServicio.buscarCliente(idCliente).getMonederos();
    }

    public Monedero crearMonedero(String idCliente, String nombre, TipoMonedero tipo)
            throws ClienteNoEncontradoException, DatoInvalidoException {
        return monederoServicio.crearMonedero(idCliente, nombre, tipo);
    }

    public BigDecimal saldoTotal(String idCliente) throws ClienteNoEncontradoException {
        return monederoServicio.saldoTotal(idCliente);
    }

    /** La fecha la da el Reloj de la aplicación (no la del computador), porque se puede adelantar. */
    public LocalDate fechaActual() {
        return reloj.hoy();
    }
}
