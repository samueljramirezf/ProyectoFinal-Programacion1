package co.edu.uniquindio.poo.controller;

import co.edu.uniquindio.poo.excepcion.ClienteNoEncontradoException;
import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.modelo.Categoria;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Frecuencia;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.TransaccionProgramada;
import co.edu.uniquindio.poo.servicio.MonederoServicio;
import co.edu.uniquindio.poo.servicio.ProgramadasServicio;
import co.edu.uniquindio.poo.servicio.ProgramadasServicio.ResultadoEjecucion;
import co.edu.uniquindio.poo.util.Reloj;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Intermediario de la pestaña de programadas. No usa JavaFX. */
public class ProgramadasController {

    private final ProgramadasServicio programadasServicio;
    private final MonederoServicio monederoServicio;
    private final Reloj reloj;

    public ProgramadasController(ProgramadasServicio programadasServicio, MonederoServicio monederoServicio,
                                 Reloj reloj) {
        this.programadasServicio = programadasServicio;
        this.monederoServicio = monederoServicio;
        this.reloj = reloj;
    }

    public List<Monedero> monederosDe(String idCliente) throws ClienteNoEncontradoException {
        return monederoServicio.buscarCliente(idCliente).getMonederos();
    }

    public TransaccionProgramada programarDeposito(String idMonedero, BigDecimal monto, Frecuencia frecuencia,
                                                   LocalDate primeraEjecucion, String descripcion)
            throws DatoInvalidoException, MontoInvalidoException {
        return programadasServicio.programarDeposito(idMonedero, monto, frecuencia, primeraEjecucion, descripcion);
    }

    public TransaccionProgramada programarTransferencia(String idOrigen, String idDestino, BigDecimal monto,
                                                        Categoria categoria, Frecuencia frecuencia,
                                                        LocalDate primeraEjecucion, String descripcion)
            throws DatoInvalidoException, MontoInvalidoException {
        return programadasServicio.programarTransferencia(idOrigen, idDestino, monto, categoria, frecuencia,
                primeraEjecucion, descripcion);
    }

    /** Ya vienen ordenadas por la próxima fecha (lo hace el servicio). */
    public List<TransaccionProgramada> listarProgramadas(String idCliente) throws ClienteNoEncontradoException {
        return programadasServicio.listarProgramadas(idCliente);
    }

    public LocalDate fechaActual() {
        return reloj.hoy();
    }

    /**
     * Mueve el reloj y ejecuta lo que se venció, igual que la demo de consola.
     * El reloj es uno solo para toda la aplicación, así que se ejecutan las programadas de TODOS los
     * clientes; a la pantalla solo se le devuelven las del cliente que inició sesión, para no mostrarle
     * movimientos ajenos.
     */
    public List<ResultadoEjecucion> avanzarDias(int dias, Cliente cliente) throws DatoInvalidoException {
        reloj.avanzarDias(dias);
        return programadasServicio.ejecutarPendientes().stream()
                .filter(resultado -> resultado.programada().getOrigen().getPropietario().equals(cliente))
                .toList();
    }
}
