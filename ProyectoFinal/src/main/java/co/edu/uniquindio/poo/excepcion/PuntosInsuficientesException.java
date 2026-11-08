package co.edu.uniquindio.poo.excepcion;

/** Regla: no se pueden canjear (ni revertir) más puntos de los disponibles. */
public class PuntosInsuficientesException extends MonederoException {

    private final int puntosDisponibles;
    private final int puntosRequeridos;

    public PuntosInsuficientesException(int puntosDisponibles, int puntosRequeridos) {
        super("Puntos insuficientes: disponibles " + puntosDisponibles + ", requeridos " + puntosRequeridos);
        this.puntosDisponibles = puntosDisponibles;
        this.puntosRequeridos = puntosRequeridos;
    }

    public int getPuntosDisponibles() {
        return puntosDisponibles;
    }

    public int getPuntosRequeridos() {
        return puntosRequeridos;
    }
}
