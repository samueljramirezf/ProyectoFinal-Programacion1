package co.edu.uniquindio.poo.controller;

import co.edu.uniquindio.poo.excepcion.ClienteNoEncontradoException;
import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.excepcion.SaldoInsuficienteException;
import co.edu.uniquindio.poo.modelo.Categoria;
import co.edu.uniquindio.poo.modelo.Deposito;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.Retiro;
import co.edu.uniquindio.poo.modelo.TipoTransaccion;
import co.edu.uniquindio.poo.modelo.Transferencia;
import co.edu.uniquindio.poo.servicio.MonederoServicio;

import java.math.BigDecimal;
import java.util.List;

/**
 * Intermediario de la pantalla de operaciones. No usa JavaFX.
 * La comisión NO se calcula aquí: se le pide al servicio (cotizarComision), que usa las mismas
 * reglas que la operación real.
 */
public class OperacionController {

    private final MonederoServicio monederoServicio;

    public OperacionController(MonederoServicio monederoServicio) {
        this.monederoServicio = monederoServicio;
    }

    /** Sirve tanto para los monederos propios como para los del cliente al que se le transfiere. */
    public List<Monedero> monederosDe(String idCliente) throws ClienteNoEncontradoException {
        return monederoServicio.buscarCliente(idCliente).getMonederos();
    }

    public BigDecimal cotizarComision(TipoTransaccion tipo, String idOrigen, String idDestino, BigDecimal monto)
            throws DatoInvalidoException, MontoInvalidoException {
        return monederoServicio.cotizarComision(tipo, idOrigen, idDestino, monto);
    }

    public Deposito depositar(String idMonedero, BigDecimal monto, String descripcion)
            throws DatoInvalidoException, MontoInvalidoException {
        return monederoServicio.depositar(idMonedero, monto, descripcion);
    }

    public Retiro retirar(String idMonedero, BigDecimal monto, Categoria categoria, String descripcion)
            throws DatoInvalidoException, MontoInvalidoException, SaldoInsuficienteException {
        return monederoServicio.retirar(idMonedero, monto, categoria, descripcion);
    }

    public Transferencia transferir(String idOrigen, String idDestino, BigDecimal monto, Categoria categoria,
                                    String descripcion)
            throws DatoInvalidoException, MontoInvalidoException, SaldoInsuficienteException {
        return monederoServicio.transferir(idOrigen, idDestino, monto, categoria, descripcion);
    }
}
