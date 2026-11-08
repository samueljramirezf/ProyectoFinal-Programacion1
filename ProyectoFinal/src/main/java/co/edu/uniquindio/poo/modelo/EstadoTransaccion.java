package co.edu.uniquindio.poo.modelo;

/**
 * Ciclo de vida de una transacción: PENDIENTE -> COMPLETADA -> REVERTIDA.
 * Una transacción revertida no se borra: queda en el historial con este estado.
 */
public enum EstadoTransaccion {
    PENDIENTE,
    COMPLETADA,
    REVERTIDA
}
