package co.edu.uniquindio.poo.controller;

import co.edu.uniquindio.poo.excepcion.ClienteNoEncontradoException;
import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.excepcion.PuntosInsuficientesException;
import co.edu.uniquindio.poo.excepcion.SaldoInsuficienteException;
import co.edu.uniquindio.poo.excepcion.TransaccionNoReversibleException;
import co.edu.uniquindio.poo.modelo.Categoria;
import co.edu.uniquindio.poo.modelo.TipoTransaccion;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.servicio.MonederoServicio;

import java.time.LocalDate;
import java.util.List;

/** Intermediario de la pantalla de historial. No usa JavaFX. Un filtro en null significa "todos". */
public class HistorialController {

    private final MonederoServicio monederoServicio;

    public HistorialController(MonederoServicio monederoServicio) {
        this.monederoServicio = monederoServicio;
    }

    public List<Transaccion> obtenerHistorial(String idCliente, TipoTransaccion tipo, Categoria categoria,
                                              LocalDate desde, LocalDate hasta)
            throws ClienteNoEncontradoException, DatoInvalidoException {
        return monederoServicio.historialFiltrado(idCliente, tipo, categoria, desde, hasta);
    }

    public Transaccion revertir(String idTransaccion)
            throws DatoInvalidoException, TransaccionNoReversibleException, SaldoInsuficienteException,
            PuntosInsuficientesException, MontoInvalidoException {
        return monederoServicio.revertir(idTransaccion);
    }
}
