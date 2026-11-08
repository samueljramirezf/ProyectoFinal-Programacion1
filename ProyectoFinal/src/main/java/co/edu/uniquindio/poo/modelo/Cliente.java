package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.PuntosInsuficientesException;
import co.edu.uniquindio.poo.util.Validaciones;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Cliente del monedero. Hereda los datos personales de Usuario y agrega monederos, puntos,
 * beneficios y alertas.
 *
 * Puntos con DOS contadores:
 *  - puntosDisponibles: los que se pueden gastar; bajan al canjear.
 *  - puntosAcumulados: los ganados en total; solo bajan al revertir. El rango se calcula con
 *    estos, para que canjear no castigue al cliente bajándole de rango.
 *
 * Encapsulamiento: los puntos no tienen setter; solo cambian con ganarPuntos / canjearPuntos /
 * revertirPuntos, que además dejan registro en el historial. Las listas se exponen como
 * vistas de solo lectura para que nadie las modifique desde fuera.
 */
public class Cliente extends Usuario {

    private final List<Monedero> monederos = new ArrayList<>();
    private final List<MovimientoPuntos> movimientosPuntos = new ArrayList<>();
    private final List<Beneficio> beneficios = new ArrayList<>();
    private final List<Alerta> alertas = new ArrayList<>();
    private int puntosDisponibles;
    private int puntosAcumulados;

    public Cliente(String identificacion, String nombre, String correo, String telefono, String contrasena)
            throws DatoInvalidoException {
        super(identificacion, nombre, correo, telefono, contrasena);
    }

    @Override
    public String getRol() {
        return "CLIENTE";
    }

    // ---------------- Monederos ----------------

    /**
     * Crea un monedero nuevo para este cliente. Composición: el monedero solo puede nacer
     * desde su dueño (su constructor es de paquete), así nunca queda un monedero huérfano.
     */
    public Monedero abrirMonedero(String nombre, TipoMonedero tipo) throws DatoInvalidoException {
        String nombreLimpio = Validaciones.textoObligatorio(nombre, "nombre del monedero");
        boolean repetido = monederos.stream()
                .anyMatch(m -> m.getNombre().equalsIgnoreCase(nombreLimpio));
        if (repetido) {
            throw new DatoInvalidoException("nombre del monedero", "ya existe un monedero llamado " + nombreLimpio);
        }
        Monedero monedero = new Monedero(this, nombreLimpio, tipo);
        monederos.add(monedero);
        return monedero;
    }

    public Optional<Monedero> buscarMonedero(String idMonedero) {
        return monederos.stream()
                .filter(m -> m.getId().equals(idMonedero))
                .findFirst();
    }

    public List<Monedero> getMonederos() {
        return Collections.unmodifiableList(monederos);
    }

    // ---------------- Puntos ----------------

    /** Suma puntos ganados por una transacción. Si son 0 no se registra movimiento. */
    public void ganarPuntos(int puntos, Transaccion origen, LocalDateTime fecha) throws DatoInvalidoException {
        validarPuntosNoNegativos(puntos);
        Validaciones.noNulo(origen, "transacción");
        if (puntos == 0) {
            return;
        }
        puntosDisponibles += puntos;
        puntosAcumulados += puntos;
        movimientosPuntos.add(new MovimientoPuntos(TipoMovimientoPuntos.GANADO, puntos,
                "Puntos por " + origen.getTipo() + " " + origen.getId(), origen, fecha));
    }

    /** Gasta puntos disponibles en un canje. No afecta los acumulados (no baja el rango). */
    public void canjearPuntos(int puntos, String descripcion, LocalDateTime fecha)
            throws PuntosInsuficientesException, DatoInvalidoException {
        validarPuntosNoNegativos(puntos);
        if (puntos > puntosDisponibles) {
            throw new PuntosInsuficientesException(puntosDisponibles, puntos);
        }
        MovimientoPuntos movimiento = new MovimientoPuntos(TipoMovimientoPuntos.CANJEADO, puntos, descripcion, null, fecha);
        puntosDisponibles -= puntos;
        movimientosPuntos.add(movimiento);
    }

    /**
     * Descuenta los puntos que dio una transacción revertida (de ambos contadores).
     * Si el cliente ya canjeó esos puntos y no le alcanzan, se lanza excepción: así no se puede
     * ganar puntos, canjearlos y luego revertir la transacción para quedarse con el beneficio.
     */
    public void revertirPuntos(int puntos, Transaccion origen, LocalDateTime fecha)
            throws PuntosInsuficientesException, DatoInvalidoException {
        validarPuntosNoNegativos(puntos);
        Validaciones.noNulo(origen, "transacción");
        if (puntos == 0) {
            return;
        }
        if (puntos > puntosDisponibles) {
            throw new PuntosInsuficientesException(puntosDisponibles, puntos);
        }
        puntosDisponibles -= puntos;
        puntosAcumulados -= puntos;
        movimientosPuntos.add(new MovimientoPuntos(TipoMovimientoPuntos.REVERTIDO, puntos,
                "Reverso de " + origen.getTipo() + " " + origen.getId(), origen, fecha));
    }

    private void validarPuntosNoNegativos(int puntos) throws DatoInvalidoException {
        if (puntos < 0) {
            throw new DatoInvalidoException("puntos", "no pueden ser negativos");
        }
    }

    /** El rango no se guarda: se calcula siempre a partir de los puntos acumulados, así nunca queda desactualizado. */
    public Rango getRango() {
        return Rango.desdePuntos(puntosAcumulados);
    }

    public int getPuntosDisponibles() {
        return puntosDisponibles;
    }

    public int getPuntosAcumulados() {
        return puntosAcumulados;
    }

    public List<MovimientoPuntos> getMovimientosPuntos() {
        return Collections.unmodifiableList(movimientosPuntos);
    }

    // ---------------- Beneficios y alertas ----------------

    public void agregarBeneficio(Beneficio beneficio) throws DatoInvalidoException {
        beneficios.add(Validaciones.noNulo(beneficio, "beneficio"));
    }

    public List<Beneficio> getBeneficios() {
        return Collections.unmodifiableList(beneficios);
    }

    public void agregarAlerta(Alerta alerta) throws DatoInvalidoException {
        alertas.add(Validaciones.noNulo(alerta, "alerta"));
    }

    public List<Alerta> getAlertas() {
        return Collections.unmodifiableList(alertas);
    }
}
