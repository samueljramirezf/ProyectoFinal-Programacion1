package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.util.Config;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Ingreso de dinero a un monedero. Hereda todo de Transaccion y solo define sus reglas propias. */
public class Deposito extends Transaccion {

    public Deposito(BigDecimal monto, Monedero destino, LocalDateTime fecha, String descripcion)
            throws MontoInvalidoException, DatoInvalidoException {
        // En un depósito, el monedero "origen" de la clase base es el que recibe el dinero.
        super(monto, destino, fecha, descripcion);
    }

    @Override
    public TipoTransaccion getTipo() {
        return TipoTransaccion.DEPOSITO;
    }

    /** 1 punto por cada 100 unidades. */
    @Override
    public int calcularPuntos() {
        return bloquesParaPuntos() * Config.PUNTOS_POR_BLOQUE_DEPOSITO;
    }

    @Override
    public BigDecimal getTasaComisionBase() {
        return Config.COMISION_DEPOSITO;
    }
}
