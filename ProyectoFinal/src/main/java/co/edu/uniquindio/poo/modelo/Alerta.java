package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.util.Validaciones;

import java.time.LocalDateTime;

/** Aviso para el cliente (saldo bajo, recordatorio o transacción atípica). Solo se puede marcar como leída. */
public class Alerta {

    private static int contadorIds = 1;

    private final String id;
    private final TipoAlerta tipo;
    private final String mensaje;
    private final LocalDateTime fecha;
    private boolean leida;

    public Alerta(TipoAlerta tipo, String mensaje, LocalDateTime fecha) throws DatoInvalidoException {
        this.tipo = Validaciones.noNulo(tipo, "tipo de alerta");
        this.mensaje = Validaciones.textoObligatorio(mensaje, "mensaje");
        this.fecha = Validaciones.noNulo(fecha, "fecha");
        this.id = "A-" + String.format("%04d", contadorIds++);
    }

    public void marcarLeida() {
        leida = true;
    }

    public String getId() {
        return id;
    }

    public TipoAlerta getTipo() {
        return tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public boolean isLeida() {
        return leida;
    }

    @Override
    public String toString() {
        return (leida ? "" : "* ") + fecha.toLocalDate() + " [" + tipo + "] " + mensaje;
    }
}
