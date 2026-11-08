package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.util.Config;

/**
 * Beneficios canjeables con puntos. Cada constante sabe cuánto cuesta, cuántos días dura
 * (0 = no vence por tiempo) y si se consume al usarse una vez.
 */
public enum TipoBeneficio {
    DESCUENTO_TRANSFERENCIA(Config.COSTO_DESCUENTO_TRANSFERENCIA,
            "10 % menos de comisión en una transferencia", 0, true),
    RETIROS_SIN_COMISION(Config.COSTO_RETIROS_SIN_COMISION,
            "Un mes sin cargos por retiros", Config.DIAS_RETIROS_SIN_COMISION, false),
    BONO_SALDO(Config.COSTO_BONO_SALDO,
            "Bono de 50 unidades en el monedero", 0, true);

    private final int costoPuntos;
    private final String descripcion;
    private final int diasVigencia;
    private final boolean usoUnico;

    TipoBeneficio(int costoPuntos, String descripcion, int diasVigencia, boolean usoUnico) {
        this.costoPuntos = costoPuntos;
        this.descripcion = descripcion;
        this.diasVigencia = diasVigencia;
        this.usoUnico = usoUnico;
    }

    public int getCostoPuntos() {
        return costoPuntos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getDiasVigencia() {
        return diasVigencia;
    }

    public boolean isUsoUnico() {
        return usoUnico;
    }
}
