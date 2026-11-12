package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.util.Validaciones;

import java.time.LocalDate;

/**
 * Beneficio que el cliente obtuvo al canjear puntos.
 * Las reglas de cada tipo (costo, días de vigencia, uso único) viven en el enum TipoBeneficio;
 * esta clase solo guarda cuándo se canjeó, hasta cuándo vale y si ya se usó.
 */
public class Beneficio {

    private static int contadorIds = 1;

    private final String id;
    private final TipoBeneficio tipo;
    private final LocalDate fechaCanje;
    private LocalDate fechaVencimiento; // null = no vence por tiempo; solo cambia con extenderVigencia()
    private boolean usado;

    public Beneficio(TipoBeneficio tipo, LocalDate fechaCanje) throws DatoInvalidoException {
        this.tipo = Validaciones.noNulo(tipo, "tipo de beneficio");
        this.fechaCanje = Validaciones.noNulo(fechaCanje, "fecha de canje");
        this.fechaVencimiento = tipo.getDiasVigencia() > 0
                ? fechaCanje.plusDays(tipo.getDiasVigencia())
                : null;
        this.id = "B-" + String.format("%04d", contadorIds++);
    }

    /** Vigente = no se ha usado y no ha vencido en la fecha dada. */
    public boolean estaVigente(LocalDate hoy) {
        boolean vencido = fechaVencimiento != null && hoy.isAfter(fechaVencimiento);
        return !usado && !vencido;
    }

    /** Consume un beneficio de uso único. Los beneficios por tiempo no se "gastan". */
    public void marcarUsado() {
        if (!tipo.isUsoUnico()) {
            throw new IllegalStateException("El beneficio " + tipo + " es por tiempo, no se consume");
        }
        if (usado) {
            throw new IllegalStateException("El beneficio " + id + " ya fue usado");
        }
        usado = true;
    }

    /**
     * Regla: si el cliente vuelve a canjear un beneficio por tiempo mientras sigue vigente,
     * los días se suman al vencimiento actual en lugar de crear un beneficio paralelo.
     */
    public void extenderVigencia(int dias) {
        if (fechaVencimiento == null) {
            throw new IllegalStateException("El beneficio " + tipo + " no vence por tiempo");
        }
        if (dias <= 0) {
            throw new IllegalArgumentException("Los días a extender deben ser positivos: " + dias);
        }
        fechaVencimiento = fechaVencimiento.plusDays(dias);
    }

    public String getId() {
        return id;
    }

    public TipoBeneficio getTipo() {
        return tipo;
    }

    public LocalDate getFechaCanje() {
        return fechaCanje;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public boolean isUsado() {
        return usado;
    }

    @Override
    public String toString() {
        return id + " " + tipo.getDescripcion()
                + (fechaVencimiento != null ? " (vence " + fechaVencimiento + ")" : "")
                + (usado ? " [USADO]" : "");
    }
}
