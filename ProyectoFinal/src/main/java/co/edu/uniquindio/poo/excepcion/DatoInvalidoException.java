package co.edu.uniquindio.poo.excepcion;

/**
 * Regla: los datos ingresados (nombres, correos, referencias obligatorias...) deben ser válidos.
 * Cubre lo que no es un monto; los montos usan MontoInvalidoException.
 */
public class DatoInvalidoException extends MonederoException {

    public DatoInvalidoException(String campo, String motivo) {
        super("Dato inválido en '" + campo + "': " + motivo);
    }
}
