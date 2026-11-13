package co.edu.uniquindio.poo.servicio;

import co.edu.uniquindio.poo.excepcion.ClienteNoEncontradoException;
import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MonederoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.excepcion.SaldoInsuficienteException;
import co.edu.uniquindio.poo.modelo.Categoria;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Frecuencia;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.TipoTransaccion;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.TransaccionProgramada;
import co.edu.uniquindio.poo.repositorio.ClienteRepositorio;
import co.edu.uniquindio.poo.util.Reloj;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * Transacciones programadas: depósitos automáticos y transferencias periódicas.
 *
 * No mueve dinero por su cuenta: cuando llega la fecha de una programada, llama a MonederoServicio
 * (depositar o transferir), así cada ejecución cobra comisión, da puntos y se evalúa como atípica
 * exactamente igual que una operación hecha a mano.
 */
public class ProgramadasServicio {

    /** Orden de ejecución: primero la fecha más próxima; a igual fecha, la que se creó antes. */
    private static final Comparator<TransaccionProgramada> POR_FECHA_DE_EJECUCION =
            Comparator.comparing(TransaccionProgramada::getProximaEjecucion)
                    .thenComparing(TransaccionProgramada::getId);

    private final ClienteRepositorio clienteRepositorio;
    private final MonederoServicio monederoServicio;
    private final AlertasServicio alertasServicio;
    private final Reloj reloj;

    public ProgramadasServicio(ClienteRepositorio clienteRepositorio, MonederoServicio monederoServicio,
                               AlertasServicio alertasServicio, Reloj reloj) {
        this.clienteRepositorio = Objects.requireNonNull(clienteRepositorio);
        this.monederoServicio = Objects.requireNonNull(monederoServicio);
        this.alertasServicio = Objects.requireNonNull(alertasServicio);
        this.reloj = Objects.requireNonNull(reloj);
    }

    /**
     * Resultado de una ejecución, para mostrar qué pasó.
     * Si salió bien, trae la transacción creada; si falló, el motivo (y la transacción es null).
     */
    public record ResultadoEjecucion(TransaccionProgramada programada, LocalDate fechaProgramada,
                                     Transaccion transaccion, String error) {
        public boolean exitosa() {
            return error == null;
        }

        @Override
        public String toString() {
            return fechaProgramada + " " + programada.getId() + " " + programada.getTipo() + " "
                    + programada.getMonto().toPlainString() + " -> "
                    + (exitosa() ? "ejecutada como " + transaccion.getId() : "FALLÓ: " + error);
        }
    }

    // ================= Crear y consultar =================

    /** Depósito automático al monedero indicado. */
    public TransaccionProgramada programarDeposito(String idMonedero, BigDecimal monto, Frecuencia frecuencia,
                                                   LocalDate primeraEjecucion, String descripcion)
            throws DatoInvalidoException, MontoInvalidoException {
        Monedero monedero = monederoServicio.buscarMonedero(idMonedero);
        validarFecha(primeraEjecucion);
        TransaccionProgramada programada = new TransaccionProgramada(TipoTransaccion.DEPOSITO, monto, monedero,
                null, null, frecuencia, primeraEjecucion, descripcion);
        monedero.getPropietario().agregarProgramada(programada);
        return programada;
    }

    /** Transferencia periódica desde un monedero del cliente hacia otro monedero. */
    public TransaccionProgramada programarTransferencia(String idOrigen, String idDestino, BigDecimal monto,
                                                        Categoria categoria, Frecuencia frecuencia,
                                                        LocalDate primeraEjecucion, String descripcion)
            throws DatoInvalidoException, MontoInvalidoException {
        Monedero origen = monederoServicio.buscarMonedero(idOrigen);
        Monedero destino = monederoServicio.buscarMonedero(idDestino);
        validarFecha(primeraEjecucion);
        TransaccionProgramada programada = new TransaccionProgramada(TipoTransaccion.TRANSFERENCIA, monto, origen,
                destino, categoria, frecuencia, primeraEjecucion, descripcion);
        origen.getPropietario().agregarProgramada(programada);
        return programada;
    }

    /** Programadas del cliente, ordenadas por su próxima fecha de ejecución. */
    public List<TransaccionProgramada> listarProgramadas(String idCliente) throws ClienteNoEncontradoException {
        Cliente cliente = clienteRepositorio.buscarPorId(idCliente)
                .orElseThrow(() -> new ClienteNoEncontradoException(idCliente));
        return cliente.getProgramadas().stream()
                .sorted(POR_FECHA_DE_EJECUCION)
                .toList();
    }

    // ================= Ejecución ordenada =================

    /**
     * Ejecuta, en orden de fecha, todas las programadas cuya fecha ya llegó según el Reloj.
     *
     * Por qué ordenar: las ejecuciones cambian saldos, así que el orden importa. Si una transferencia
     * del día 3 y otra del día 5 salen del mismo monedero, debe cobrarse primero la del día 3, como
     * habría pasado en la vida real. Procesarlas en el orden en que se crearon daría otro resultado.
     *
     * Por qué una PriorityQueue (cola de prioridad): siempre entrega la programada con la fecha más
     * próxima en O(log n), y permite volver a meter una programada después de reprogramarla. Eso
     * resuelve el caso de avanzar varios días de golpe: una programada DIARIA vencida hace 3 días sale,
     * se ejecuta, se reprograma al día siguiente y vuelve a la cola, quedando bien intercalada con las
     * demás. Con una lista ordenada una sola vez habría que reordenar después de cada reprogramación.
     *
     * Si una ejecución falla por saldo insuficiente, no se detiene el proceso: se genera una alerta,
     * esa fecha se pierde (no se reintenta) y se sigue con las demás.
     */
    public List<ResultadoEjecucion> ejecutarPendientes() throws DatoInvalidoException {
        LocalDate hoy = reloj.hoy();
        PriorityQueue<TransaccionProgramada> cola = new PriorityQueue<>(POR_FECHA_DE_EJECUCION);
        clienteRepositorio.listar().stream()
                .flatMap(cliente -> cliente.getProgramadas().stream())
                .filter(programada -> programada.estaPendienteEn(hoy))
                .forEach(cola::add);

        List<ResultadoEjecucion> resultados = new ArrayList<>();
        while (!cola.isEmpty()) {
            TransaccionProgramada programada = cola.poll();
            resultados.add(ejecutar(programada));
            programada.avanzarProximaEjecucion();
            if (programada.estaPendienteEn(hoy)) {
                cola.add(programada); // todavía le quedan fechas vencidas
            }
        }
        return resultados;
    }

    /** Ejecuta una programada llamando a la operación real de MonederoServicio. */
    private ResultadoEjecucion ejecutar(TransaccionProgramada programada) throws DatoInvalidoException {
        LocalDate fecha = programada.getProximaEjecucion();
        String descripcion = "Programada " + programada.getId()
                + (programada.getDescripcion().isEmpty() ? "" : ": " + programada.getDescripcion());
        try {
            Transaccion transaccion = switch (programada.getTipo()) {
                case DEPOSITO -> monederoServicio.depositar(programada.getOrigen().getId(),
                        programada.getMonto(), descripcion);
                case TRANSFERENCIA -> monederoServicio.transferir(programada.getOrigen().getId(),
                        programada.getDestino().getId(), programada.getMonto(), programada.getCategoria(),
                        descripcion);
                case RETIRO -> throw new DatoInvalidoException("tipo", "no se permiten retiros programados");
            };
            return new ResultadoEjecucion(programada, fecha, transaccion, null);
        } catch (SaldoInsuficienteException e) {
            alertasServicio.alertarFalloProgramada(programada, e.getMessage());
            return new ResultadoEjecucion(programada, fecha, null, e.getMessage());
        } catch (MonederoException e) {
            return new ResultadoEjecucion(programada, fecha, null, e.getMessage());
        }
    }

    /** No se puede programar algo para una fecha que ya pasó. */
    private void validarFecha(LocalDate primeraEjecucion) throws DatoInvalidoException {
        if (primeraEjecucion != null && primeraEjecucion.isBefore(reloj.hoy())) {
            throw new DatoInvalidoException("fecha de primera ejecución",
                    "no puede ser anterior a hoy (" + reloj.hoy() + ")");
        }
    }
}
