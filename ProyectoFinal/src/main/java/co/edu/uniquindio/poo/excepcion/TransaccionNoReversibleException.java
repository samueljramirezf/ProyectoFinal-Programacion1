package co.edu.uniquindio.poo.excepcion;

/** Regla: solo se revierte una transacción COMPLETADA, y una sola vez. */
public class TransaccionNoReversibleException extends MonederoException {

    public TransaccionNoReversibleException(String mensaje) {
        super(mensaje);
    }
}
