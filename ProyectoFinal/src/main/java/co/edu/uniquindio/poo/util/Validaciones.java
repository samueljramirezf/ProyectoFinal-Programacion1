package co.edu.uniquindio.poo.util;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Validaciones reutilizadas por los constructores del modelo.
 * Centralizarlas evita repetir los mismos if en cada clase y garantiza mensajes uniformes.
 */
public final class Validaciones {

    /** Formato básico de correo: algo@algo.algo (suficiente para un simulador). */
    private static final Pattern PATRON_CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private Validaciones() {
    }

    /** Verifica que el objeto no sea nulo. */
    public static <T> T noNulo(T valor, String campo) throws DatoInvalidoException {
        if (valor == null) {
            throw new DatoInvalidoException(campo, "no puede ser nulo");
        }
        return valor;
    }

    /** Verifica que el texto no sea nulo ni vacío y lo devuelve sin espacios sobrantes. */
    public static String textoObligatorio(String valor, String campo) throws DatoInvalidoException {
        if (valor == null || valor.isBlank()) {
            throw new DatoInvalidoException(campo, "es obligatorio");
        }
        return valor.trim();
    }

    /** Verifica un correo con formato básico. */
    public static String correo(String valor) throws DatoInvalidoException {
        String limpio = textoObligatorio(valor, "correo");
        if (!PATRON_CORREO.matcher(limpio).matches()) {
            throw new DatoInvalidoException("correo", "no tiene un formato válido");
        }
        return limpio;
    }

    /** Verifica que el monto exista y sea mayor que cero; lo devuelve redondeado a 2 decimales. */
    public static BigDecimal montoPositivo(BigDecimal monto) throws MontoInvalidoException {
        if (monto == null) {
            throw new MontoInvalidoException("El monto no puede ser nulo");
        }
        BigDecimal redondeado = Config.redondear(monto);
        if (redondeado.signum() <= 0) {
            throw new MontoInvalidoException("El monto debe ser mayor que cero (recibido: " + monto.toPlainString() + ")");
        }
        return redondeado;
    }
}
