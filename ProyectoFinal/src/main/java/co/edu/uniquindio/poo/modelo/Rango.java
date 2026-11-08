package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.util.Config;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Rangos de cliente según puntos ACUMULADOS (los canjes no bajan el rango).
 * Cada constante encapsula sus propios límites y su beneficio, así la regla
 * "qué rango corresponde" vive en un solo lugar: desdePuntos(int).
 */
public enum Rango {
    BRONCE(0, 500, Config.DESCUENTO_RANGO_BRONCE),
    PLATA(501, 1000, Config.DESCUENTO_RANGO_PLATA),
    ORO(1001, 5000, Config.DESCUENTO_RANGO_ORO),
    PLATINO(5001, Integer.MAX_VALUE, Config.DESCUENTO_RANGO_PLATINO);

    private final int puntosMinimos;
    private final int puntosMaximos;
    /** Descuento adicional sobre la comisión de transferencias (supuesto). */
    private final BigDecimal descuentoTransferencia;

    Rango(int puntosMinimos, int puntosMaximos, BigDecimal descuentoTransferencia) {
        this.puntosMinimos = puntosMinimos;
        this.puntosMaximos = puntosMaximos;
        this.descuentoTransferencia = descuentoTransferencia;
    }

    /** Devuelve el rango que corresponde a una cantidad de puntos acumulados (búsqueda declarativa con Stream). */
    public static Rango desdePuntos(int puntos) {
        if (puntos < 0) {
            throw new IllegalArgumentException("Los puntos acumulados no pueden ser negativos: " + puntos);
        }
        return Arrays.stream(values())
                .filter(rango -> rango.contiene(puntos))
                .findFirst()
                .orElseThrow(); // imposible: los rangos cubren 0..Integer.MAX_VALUE sin huecos
    }

    public boolean contiene(int puntos) {
        return puntos >= puntosMinimos && puntos <= puntosMaximos;
    }

    public int getPuntosMinimos() {
        return puntosMinimos;
    }

    public int getPuntosMaximos() {
        return puntosMaximos;
    }

    public BigDecimal getDescuentoTransferencia() {
        return descuentoTransferencia;
    }
}
