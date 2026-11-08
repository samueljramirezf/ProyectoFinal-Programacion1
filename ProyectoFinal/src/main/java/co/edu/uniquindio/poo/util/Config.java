package co.edu.uniquindio.poo.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Constantes de negocio del monedero, todas en un solo lugar.
 * Los valores marcados como (supuesto) los definió el equipo porque el enunciado no los da;
 * si cambian, solo se tocan aquí.
 *
 * Clase final con constructor privado: es un contenedor de constantes, no tiene sentido
 * instanciarla ni heredarla.
 */
public final class Config {

    private Config() {
    }

    // ---------- Dinero ----------
    /** Todo el dinero se maneja con 2 decimales. */
    public static final int ESCALA_DINERO = 2;
    /** Redondeo bancario (HALF_EVEN): evita el sesgo acumulado de redondear siempre .5 hacia arriba. */
    public static final RoundingMode REDONDEO_DINERO = RoundingMode.HALF_EVEN;

    // ---------- Puntos (enunciado) ----------
    /** Los puntos se otorgan por cada bloque completo de 100 unidades (redondeo hacia abajo). */
    public static final BigDecimal UNIDADES_POR_BLOQUE_PUNTOS = new BigDecimal("100");
    public static final int PUNTOS_POR_BLOQUE_DEPOSITO = 1;
    public static final int PUNTOS_POR_BLOQUE_RETIRO = 2;
    public static final int PUNTOS_POR_BLOQUE_TRANSFERENCIA = 3;

    // ---------- Comisiones (supuesto) ----------
    // Se cobran ENCIMA del monto: un retiro de 100 al 1 % descuenta 101 del saldo.
    public static final BigDecimal COMISION_DEPOSITO = new BigDecimal("0.00");
    public static final BigDecimal COMISION_RETIRO = new BigDecimal("0.01");
    public static final BigDecimal COMISION_TRANSFERENCIA = new BigDecimal("0.02");

    // ---------- Descuento en comisión de transferencias por rango (supuesto) ----------
    public static final BigDecimal DESCUENTO_RANGO_BRONCE = new BigDecimal("0.00");
    public static final BigDecimal DESCUENTO_RANGO_PLATA = new BigDecimal("0.05");
    public static final BigDecimal DESCUENTO_RANGO_ORO = new BigDecimal("0.10");
    public static final BigDecimal DESCUENTO_RANGO_PLATINO = new BigDecimal("0.20");

    // ---------- Canje de puntos (enunciado) ----------
    public static final int COSTO_DESCUENTO_TRANSFERENCIA = 100;
    public static final int COSTO_RETIROS_SIN_COMISION = 500;
    public static final int COSTO_BONO_SALDO = 1000;

    /** 10 % menos de comisión en UNA transferencia; se combina multiplicativamente con el descuento por rango. */
    public static final BigDecimal DESCUENTO_BENEFICIO_TRANSFERENCIA = new BigDecimal("0.10");
    /** "Un mes sin cargos por retiros" = 30 días del reloj del sistema (supuesto). */
    public static final int DIAS_RETIROS_SIN_COMISION = 30;
    /** Bono de saldo; no genera puntos (supuesto). */
    public static final BigDecimal MONTO_BONO_SALDO = new BigDecimal("50.00");

    // ---------- Alertas (supuesto) ----------
    /** Saldo bajo: saldo <= 500. Es solo un aviso, no bloquea movimientos. */
    public static final BigDecimal UMBRAL_SALDO_BAJO = new BigDecimal("500.00");

    // ---------- Detección de atípicas ----------
    /** Mínimo de transacciones históricas para que el análisis estadístico sea significativo (supuesto). */
    public static final int MINIMO_TRANSACCIONES_ANALISIS = 5;
    /** Atípica si monto > promedio + FACTOR × desviación estándar (enunciado). */
    public static final BigDecimal FACTOR_DESVIACION_ATIPICA = new BigDecimal("2");

    /** Lleva cualquier valor monetario a la escala y redondeo estándar del sistema. */
    public static BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(ESCALA_DINERO, REDONDEO_DINERO);
    }
}
