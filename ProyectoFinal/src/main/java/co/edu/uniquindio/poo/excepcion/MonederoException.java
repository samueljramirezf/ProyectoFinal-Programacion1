package co.edu.uniquindio.poo.excepcion;

/**
 * Base de todas las excepciones de negocio del monedero.
 *
 * - Es CHECKED (extiende Exception): los fallos de negocio (saldo insuficiente, puntos
 *   insuficientes, transacción no reversible...) son situaciones esperables y recuperables.
 *   El compilador obliga a que el servicio o la interfaz las capture y le muestre un
 *   mensaje al usuario en lugar de que el programa se caiga.
 * - Es ABSTRACTA: nunca se lanza un "error de monedero genérico"; siempre una causa concreta.
 *   A la vez, permite un único catch (MonederoException e) en la interfaz (herencia + polimorfismo).
 */
public abstract class MonederoException extends Exception {

    protected MonederoException(String mensaje) {
        super(mensaje);
    }
}
