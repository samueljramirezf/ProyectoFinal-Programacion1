package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.excepcion.SaldoInsuficienteException;

/** Cada transacción sabe deshacer su propio movimiento de dinero, así el servicio puede revertir cualquiera sin preguntar de qué tipo es (sin switch ni instanceof). */
public interface Reversible {
    boolean puedeRevertirse();
    public void revertir() throws MontoInvalidoException, SaldoInsuficienteException;

}
