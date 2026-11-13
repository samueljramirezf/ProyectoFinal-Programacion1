package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.excepcion.TransaccionNoReversibleException;
import co.edu.uniquindio.poo.util.Config;
import co.edu.uniquindio.poo.util.Validaciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Movimiento de dinero sobre un monedero.
 *
 * ABSTRACCIÓN: es abstracta porque en el negocio no existe una "transacción genérica";
 * siempre es un Deposito, un Retiro o una Transferencia. Esta clase define lo que todas
 * comparten (id, monto, monedero origen, fecha, comisión, estado) y deja abiertos los
 * comportamientos que cambian según el tipo.
 *
 * HERENCIA: las subclases reutilizan atributos, validaciones y el ciclo de estados;
 * solo agregan lo propio (categoría, monedero destino).
 *
 * POLIMORFISMO: calcularPuntos(), getTasaComisionBase(), getTipo() e involucraA() se
 * sobrescriben en cada subclase. Los servicios llaman t.calcularPuntos() sin saber el tipo
 * concreto y sin usar instanceof ni switch.
 *
 * ENCAPSULAMIENTO: monto, origen y fecha son final y sin setter (una transacción hecha no
 * cambia su monto). El estado solo cambia con marcarCompletada() / marcarRevertida(), que
 * validan las transiciones permitidas.
 */
public abstract class Transaccion {

    private static int contadorIds = 1;

    /**
     * Se asigna al completarse (no al crearse): así una operación que falla (por ejemplo, saldo
     * insuficiente) o una simple cotización de comisión no consumen un id y los ids quedan seguidos.
     * Mientras está PENDIENTE vale null.
     */
    private String id;
    private final BigDecimal monto;
    private final Monedero origen;
    private final LocalDateTime fecha;
    private final String descripcion;
    private BigDecimal comision;
    private EstadoTransaccion estado;
    private boolean sospechosa;

    /** Protegido: solo las subclases pueden construir una transacción. */
    protected Transaccion(BigDecimal monto, Monedero origen, LocalDateTime fecha, String descripcion)
            throws MontoInvalidoException, DatoInvalidoException {
        this.monto = Validaciones.montoPositivo(monto);
        this.origen = Validaciones.noNulo(origen, "monedero origen");
        this.fecha = Validaciones.noNulo(fecha, "fecha");
        this.descripcion = descripcion == null ? "" : descripcion.trim();
        this.comision = Config.redondear(BigDecimal.ZERO);
        this.estado = EstadoTransaccion.PENDIENTE;
    }

    // ---------------- Métodos abstractos (polimorfismo) ----------------

    public abstract TipoTransaccion getTipo();

    /** Puntos que genera esta transacción para el emisor. Cada tipo usa su propio factor. */
    public abstract int calcularPuntos();

    /** Porcentaje de comisión antes de descuentos (por rango o beneficios). */
    public abstract BigDecimal getTasaComisionBase();

    /**
     * Categoría de gasto, si la transacción la tiene. Por defecto ninguna (un depósito no es gasto);
     * Retiro y Transferencia lo sobrescriben. Así el historial se filtra por categoría sin instanceof.
     */
    public Optional<Categoria> getCategoriaGasto() {
        return Optional.empty();
    }

    /**
     * ¿Cuenta como gasto en el análisis de patrones? Por defecto, si tiene categoría de gasto.
     * Transferencia lo sobrescribe: mover dinero entre monederos propios no es gastar.
     */
    public boolean esGasto() {
        return getCategoriaGasto().isPresent();
    }

    // ---------------- Comportamiento común ----------------

    /**
     * Bloques completos de 100 unidades del monto, redondeando hacia abajo.
     * Ej.: 250 -> 2. Método protegido de apoyo para que las subclases calculen sus puntos
     * sin repetir la división (reutilización por herencia).
     */
    protected int bloquesParaPuntos() {
        return monto.divide(Config.UNIDADES_POR_BLOQUE_PUNTOS, 0, RoundingMode.FLOOR).intValue();
    }

    /**
     * Calcula y guarda la comisión: monto × tasaBase × factorDescuento.
     * El factor lo decide el servicio (1 = sin descuento, 0 = sin cargo; los descuentos de rango
     * y beneficio se multiplican entre sí). La comisión se cobra ENCIMA del monto.
     */
    public BigDecimal aplicarComision(BigDecimal factorDescuento) throws DatoInvalidoException {
        Validaciones.noNulo(factorDescuento, "factor de descuento");
        if (factorDescuento.signum() < 0 || factorDescuento.compareTo(BigDecimal.ONE) > 0) {
            throw new DatoInvalidoException("factor de descuento", "debe estar entre 0 y 1");
        }
        if (estado != EstadoTransaccion.PENDIENTE) {
            throw new IllegalStateException("La comisión solo se fija antes de completar la transacción " + id);
        }
        comision = Config.redondear(monto.multiply(getTasaComisionBase()).multiply(factorDescuento));
        return comision;
    }

    /** Lo que sale del monedero origen: monto + comisión. */
    public BigDecimal getMontoTotal() {
        return monto.add(comision);
    }

    /**
     * ¿Esta transacción afecta al monedero dado? Por defecto solo al origen;
     * Transferencia lo sobrescribe para incluir también al destino.
     */
    public boolean involucraA(Monedero monedero) {
        return origen.equals(monedero);
    }

    /** PENDIENTE -> COMPLETADA. Llamarlo en otro estado es un error de programación. */
    public void marcarCompletada() {
        if (estado != EstadoTransaccion.PENDIENTE) {
            throw new IllegalStateException("La transacción " + id + " ya está " + estado);
        }
        estado = EstadoTransaccion.COMPLETADA;
        id = "T-" + String.format("%05d", contadorIds++);
    }

    /** COMPLETADA -> REVERTIDA. Regla de negocio: solo una vez y solo si está completada. */
    public void marcarRevertida() throws TransaccionNoReversibleException {
        if (estado != EstadoTransaccion.COMPLETADA) {
            throw new TransaccionNoReversibleException(
                    "La transacción " + id + " no se puede revertir porque está " + estado);
        }
        estado = EstadoTransaccion.REVERTIDA;
    }

    /** La marca el detector de atípicas (Fase 4). */
    public void marcarComoSospechosa() {
        sospechosa = true;
    }

    // ---------------- Getters ----------------

    /** null mientras la transacción está PENDIENTE. */
    public String getId() {
        return id;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public Monedero getOrigen() {
        return origen;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getComision() {
        return comision;
    }

    public EstadoTransaccion getEstado() {
        return estado;
    }

    public boolean isSospechosa() {
        return sospechosa;
    }

    // equals/hashCode NO se sobrescriben: cada transacción es un objeto único (nunca se crean
    // copias de la misma), así que basta la identidad. Además el id cambia de null a su valor al
    // completarse, y un hashCode basado en un campo que cambia rompería HashMap/HashSet.

    /** Las subclases lo amplían con super.toString() + sus datos propios. */
    @Override
    public String toString() {
        return (id != null ? id : "(pendiente)") + " " + getTipo() + " " + monto.toPlainString()
                + " (comisión " + comision.toPlainString() + ") " + estado
                + (sospechosa ? " [SOSPECHOSA]" : "");
    }
}
