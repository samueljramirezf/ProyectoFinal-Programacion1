package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.util.Config;
import co.edu.uniquindio.poo.util.Validaciones;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Envío de dinero de un monedero a otro. Agrega el monedero destino y la categoría.
 * Si ambos monederos son del mismo cliente, es un movimiento interno: sin comisión y sin puntos.
 */
public class Transferencia extends Transaccion {

    private final Monedero destino;
    private final Categoria categoria;

    public Transferencia(BigDecimal monto, Monedero origen, Monedero destino, Categoria categoria,
                         LocalDateTime fecha, String descripcion)
            throws MontoInvalidoException, DatoInvalidoException {
        super(monto, origen, fecha, descripcion);
        this.destino = Validaciones.noNulo(destino, "monedero destino");
        if (origen.equals(destino)) {
            throw new DatoInvalidoException("monedero destino", "no puede ser el mismo monedero de origen");
        }
        this.categoria = Validaciones.noNulo(categoria, "categoría");
    }

    @Override
    public TipoTransaccion getTipo() {
        return TipoTransaccion.TRANSFERENCIA;
    }

    /** 3 puntos por cada 100 unidades, solo para el emisor. Entre monederos propios no da puntos. */
    @Override
    public int calcularPuntos() {
        if (esEntreMonederosPropios()) {
            return 0;
        }
        return bloquesParaPuntos() * Config.PUNTOS_POR_BLOQUE_TRANSFERENCIA;
    }

    /** Entre monederos propios no se cobra comisión. */
    @Override
    public BigDecimal getTasaComisionBase() {
        return esEntreMonederosPropios() ? BigDecimal.ZERO : Config.COMISION_TRANSFERENCIA;
    }

    /** Sobrescribe el comportamiento base: una transferencia también involucra al destino. */
    @Override
    public boolean involucraA(Monedero monedero) {
        return super.involucraA(monedero) || destino.equals(monedero);
    }

    public boolean esEntreMonederosPropios() {
        return getOrigen().getPropietario().equals(destino.getPropietario());
    }

    public Monedero getDestino() {
        return destino;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    @Override
    public String toString() {
        return super.toString() + " -> " + destino.getId() + " categoría " + categoria;
    }
}
