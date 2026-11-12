package co.edu.uniquindio.poo.util;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Fecha y hora simulada del sistema. Ninguna clase usa LocalDateTime.now(): todas le piden la
 * hora a este objeto, así se puede "avanzar el tiempo" para demostrar vencimientos de beneficios
 * y transacciones programadas sin esperar días reales.
 *
 * Es un objeto (no una clase estática) que se crea una sola vez y se pasa por constructor a los
 * servicios: así en la Fase 5 se inyecta como cualquier otra dependencia.
 */
public class Reloj {

    private LocalDateTime ahora;

    /** Arranca en la fecha fija definida en Config. */
    public Reloj() {
        this(Config.FECHA_INICIO_RELOJ);
    }

    public Reloj(LocalDateTime inicio) {
        this.ahora = Objects.requireNonNull(inicio, "La fecha inicial del reloj no puede ser nula");
    }

    public LocalDateTime ahora() {
        return ahora;
    }

    public LocalDate hoy() {
        return ahora.toLocalDate();
    }

    /** El tiempo solo avanza: retroceder permitiría, por ejemplo, "revivir" beneficios vencidos. */
    public void avanzarDias(int dias) throws DatoInvalidoException {
        if (dias < 0) {
            throw new DatoInvalidoException("días", "el reloj no puede retroceder");
        }
        ahora = ahora.plusDays(dias);
    }
}
