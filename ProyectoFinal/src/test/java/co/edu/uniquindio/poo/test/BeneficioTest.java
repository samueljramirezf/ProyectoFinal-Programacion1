package co.edu.uniquindio.poo.test;
import co.edu.uniquindio.poo.modelo.Beneficio;
import co.edu.uniquindio.poo.modelo.TipoBeneficio;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BeneficioTest {

    @Test
    void testToStringSinFechaYNoUsado() throws Exception {
        Beneficio beneficio = new Beneficio(
                TipoBeneficio.DESCUENTO_TRANSFERENCIA,
                LocalDate.of(2026, 11, 12)
        );

        String esperado = beneficio.getId()
                + " 10 % menos de comisión en una transferencia";

        assertEquals(esperado, beneficio.toString());
    }

    @Test
    void testToStringConFechaYNoUsado() throws Exception {
        Beneficio beneficio = new Beneficio(
                TipoBeneficio.RETIROS_SIN_COMISION,
                LocalDate.of(2026, 11, 12)
        );

        String esperado = beneficio.getId()
                + " Un mes sin cargos por retiros"
                + " (vence " + beneficio.getFechaVencimiento() + ")";

        assertEquals(esperado, beneficio.toString());
    }

    @Test
    void testToStringUsado() throws Exception {
        Beneficio beneficio = new Beneficio(
                TipoBeneficio.DESCUENTO_TRANSFERENCIA,
                LocalDate.of(2026, 11, 12)
        );

        beneficio.marcarUsado();

        String esperado = beneficio.getId()
                + " 10 % menos de comisión en una transferencia"
                + " [USADO]";

        assertEquals(esperado, beneficio.toString());
    }

    @Test
    void testToStringConFechaYUsado() throws Exception {
        Beneficio beneficio = new Beneficio(
                TipoBeneficio.RETIROS_SIN_COMISION,
                LocalDate.of(2026, 11, 12)
        );

        beneficio.marcarUsado();

        String esperado = beneficio.getId()
                + " Un mes sin cargos por retiros"
                + " (vence " + beneficio.getFechaVencimiento() + ")"
                + " [USADO]";

        assertEquals(esperado, beneficio.toString());
    }
}