package co.edu.uniquindio.poo.servicio;

import co.edu.uniquindio.poo.excepcion.ClienteNoEncontradoException;
import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.excepcion.PuntosInsuficientesException;
import co.edu.uniquindio.poo.modelo.Beneficio;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.MovimientoPuntos;
import co.edu.uniquindio.poo.modelo.Rango;
import co.edu.uniquindio.poo.modelo.TipoBeneficio;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.repositorio.ClienteRepositorio;
import co.edu.uniquindio.poo.util.Config;
import co.edu.uniquindio.poo.util.Reloj;
import co.edu.uniquindio.poo.util.Validaciones;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Lógica del sistema de puntos: otorgar, revertir, rangos y canje de beneficios.
 *
 * Separado de MonederoServicio porque es otra responsabilidad (gamificación) que cambia por
 * razones distintas a mover dinero. MonederoServicio lo usa para dar puntos y para saber qué
 * descuentos aplicar; este servicio no conoce cómo se mueve el dinero de una transacción.
 */
public class PuntosServicio {

    private final ClienteRepositorio clienteRepositorio;
    private final Reloj reloj;

    /** Dependencias por constructor: el servicio no crea sus propios repositorios ni su reloj. */
    public PuntosServicio(ClienteRepositorio clienteRepositorio, Reloj reloj) {
        this.clienteRepositorio = Objects.requireNonNull(clienteRepositorio);
        this.reloj = Objects.requireNonNull(reloj);
    }

    // ---------------- Otorgar y revertir ----------------

    /**
     * Da al emisor (dueño del monedero origen) los puntos de una transacción completada.
     * POLIMORFISMO: t.calcularPuntos() ejecuta la versión de Deposito, Retiro o Transferencia
     * según el objeto real; aquí no hay if/switch por tipo.
     */
    public int otorgarPuntos(Transaccion transaccion) throws DatoInvalidoException {
        Validaciones.noNulo(transaccion, "transacción");
        int puntos = transaccion.calcularPuntos();
        emisorDe(transaccion).ganarPuntos(puntos, transaccion, reloj.ahora());
        return puntos;
    }

    /**
     * Comprueba, SIN modificar nada, que el emisor todavía tiene los puntos que le dio la transacción.
     * La reversión lo llama antes de mover dinero para no quedar a medias.
     * calcularPuntos() es determinista (solo depende del monto y los monederos), así que devuelve
     * exactamente los mismos puntos que se otorgaron.
     */
    public void validarReversionPuntos(Transaccion transaccion) throws PuntosInsuficientesException {
        Cliente emisor = emisorDe(transaccion);
        int puntos = transaccion.calcularPuntos();
        if (puntos > emisor.getPuntosDisponibles()) {
            throw new PuntosInsuficientesException(emisor.getPuntosDisponibles(), puntos);
        }
    }

    /** Descuenta los puntos de una transacción revertida y deja el movimiento REVERTIDO en el historial. */
    public void revertirPuntos(Transaccion transaccion) throws PuntosInsuficientesException, DatoInvalidoException {
        emisorDe(transaccion).revertirPuntos(transaccion.calcularPuntos(), transaccion, reloj.ahora());
    }

    // ---------------- Consultas ----------------

    /** El rango se calcula siempre con Rango.desdePuntos(puntosAcumulados) (ver Cliente.getRango()). */
    public Rango consultarRango(String idCliente) throws ClienteNoEncontradoException {
        return buscarCliente(idCliente).getRango();
    }

    /** Historial de puntos en orden cronológico (sort estable: los empates conservan el orden de registro). */
    public List<MovimientoPuntos> historialPuntos(String idCliente) throws ClienteNoEncontradoException {
        return buscarCliente(idCliente).getMovimientosPuntos().stream()
                .sorted(Comparator.comparing(MovimientoPuntos::getFecha))
                .toList();
    }

    public List<Beneficio> beneficiosVigentes(String idCliente) throws ClienteNoEncontradoException {
        return buscarCliente(idCliente).getBeneficios().stream()
                .filter(b -> b.estaVigente(reloj.hoy()))
                .toList();
    }

    // ---------------- Canje ----------------

    /**
     * Canjea puntos disponibles por un beneficio.
     *  - Si no alcanzan los puntos: PuntosInsuficientesException y no cambia nada.
     *  - BONO_SALDO: acredita Config.MONTO_BONO_SALDO al monedero elegido directamente (no crea
     *    un Deposito: un bono no debe poder "revertirse" ni generar puntos). El rastro queda en el
     *    historial de puntos. El beneficio nace ya usado porque se aplica al instante.
     *  - Beneficio por tiempo (RETIROS_SIN_COMISION) ya vigente: se le suman los días en vez de
     *    crear otro.
     *
     * @param idMonederoBono solo se usa para BONO_SALDO; debe ser un monedero del propio cliente.
     * @return el beneficio creado o el que se extendió.
     */
    public Beneficio canjear(String idCliente, TipoBeneficio tipo, String idMonederoBono)
            throws ClienteNoEncontradoException, PuntosInsuficientesException,
            DatoInvalidoException, MontoInvalidoException {
        Cliente cliente = buscarCliente(idCliente);
        Validaciones.noNulo(tipo, "tipo de beneficio");

        // 1. Todas las validaciones primero, para que un fallo no deje el canje a medias.
        Monedero monederoBono = null;
        if (tipo == TipoBeneficio.BONO_SALDO) {
            monederoBono = cliente.buscarMonedero(idMonederoBono)
                    .orElseThrow(() -> new DatoInvalidoException("monedero",
                            "el cliente no tiene el monedero " + idMonederoBono));
        }
        Optional<Beneficio> vigenteParaExtender = tipo.isUsoUnico()
                ? Optional.empty()
                : buscarVigente(cliente, tipo);

        // 2. Cobrar los puntos (lanza PuntosInsuficientesException si no alcanzan).
        String descripcion = "Canje: " + tipo.getDescripcion()
                + (monederoBono != null ? " -> " + monederoBono.getId() : "");
        cliente.canjearPuntos(tipo.getCostoPuntos(), descripcion, reloj.ahora());

        // 3. Entregar el beneficio.
        if (vigenteParaExtender.isPresent()) {
            Beneficio existente = vigenteParaExtender.get();
            existente.extenderVigencia(tipo.getDiasVigencia());
            return existente;
        }
        Beneficio nuevo = new Beneficio(tipo, reloj.hoy());
        cliente.agregarBeneficio(nuevo);
        if (monederoBono != null) {
            monederoBono.acreditar(Config.MONTO_BONO_SALDO);
            nuevo.marcarUsado();
        }
        return nuevo;
    }

    // ---------------- Consultas que usa MonederoServicio para las comisiones ----------------

    /** ¿Tiene vigente el beneficio de 500 puntos (retiros sin comisión) según la fecha del Reloj? */
    public boolean tieneRetirosSinComision(Cliente cliente) {
        return buscarVigente(cliente, TipoBeneficio.RETIROS_SIN_COMISION).isPresent();
    }

    /**
     * Beneficio de 100 puntos sin usar. Si tiene varios, se gasta primero el más antiguo
     * (la lista de beneficios está en orden de canje, así que basta findFirst()).
     */
    public Optional<Beneficio> buscarDescuentoTransferencia(Cliente cliente) {
        return buscarVigente(cliente, TipoBeneficio.DESCUENTO_TRANSFERENCIA);
    }

    // ---------------- Apoyo ----------------

    private Optional<Beneficio> buscarVigente(Cliente cliente, TipoBeneficio tipo) {
        return cliente.getBeneficios().stream()
                .filter(b -> b.getTipo() == tipo)
                .filter(b -> b.estaVigente(reloj.hoy()))
                .findFirst();
    }

    /** En una transferencia solo gana (o pierde) puntos el emisor: el dueño del monedero origen. */
    private Cliente emisorDe(Transaccion transaccion) {
        return transaccion.getOrigen().getPropietario();
    }

    private Cliente buscarCliente(String idCliente) throws ClienteNoEncontradoException {
        return clienteRepositorio.buscarPorId(idCliente)
                .orElseThrow(() -> new ClienteNoEncontradoException(idCliente));
    }
}
