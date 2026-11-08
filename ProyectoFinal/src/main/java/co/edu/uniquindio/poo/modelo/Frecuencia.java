package co.edu.uniquindio.poo.modelo;

import java.time.LocalDate;

/**
 * Frecuencia de las transacciones programadas.
 * Polimorfismo dentro de un enum: cada constante sobrescribe siguienteFecha() con su propio cálculo,
 * así quien la usa no necesita un switch.
 */
public enum Frecuencia {
    DIARIA {
        @Override
        public LocalDate siguienteFecha(LocalDate desde) {
            return desde.plusDays(1);
        }
    },
    SEMANAL {
        @Override
        public LocalDate siguienteFecha(LocalDate desde) {
            return desde.plusWeeks(1);
        }
    },
    MENSUAL {
        @Override
        public LocalDate siguienteFecha(LocalDate desde) {
            return desde.plusMonths(1);
        }
    };

    public abstract LocalDate siguienteFecha(LocalDate desde);
}
