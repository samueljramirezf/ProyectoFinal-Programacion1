package co.edu.uniquindio.poo.excepcion;

/** Regla: las operaciones solo se hacen sobre clientes registrados. */
public class ClienteNoEncontradoException extends MonederoException {

    public ClienteNoEncontradoException(String identificacion) {
        super("No existe un cliente con identificación " + identificacion);
    }
}
