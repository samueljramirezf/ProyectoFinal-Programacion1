package co.edu.uniquindio.poo.excepcion;

/** Regla: todo monto de dinero debe existir y ser mayor que cero. */
public class MontoInvalidoException extends MonederoException {

    public MontoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
