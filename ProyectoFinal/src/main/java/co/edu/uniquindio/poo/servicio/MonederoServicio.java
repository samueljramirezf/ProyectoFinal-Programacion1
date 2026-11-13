package co.edu.uniquindio.poo.servicio;

import co.edu.uniquindio.poo.excepcion.ClienteNoEncontradoException;
import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.excepcion.PuntosInsuficientesException;
import co.edu.uniquindio.poo.excepcion.SaldoInsuficienteException;
import co.edu.uniquindio.poo.excepcion.TransaccionNoReversibleException;
import co.edu.uniquindio.poo.modelo.Beneficio;
import co.edu.uniquindio.poo.modelo.Categoria;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Deposito;
import co.edu.uniquindio.poo.modelo.EstadoTransaccion;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.Retiro;
import co.edu.uniquindio.poo.modelo.TipoMonedero;
import co.edu.uniquindio.poo.modelo.TipoTransaccion;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.Transferencia;
import co.edu.uniquindio.poo.repositorio.ClienteRepositorio;
import co.edu.uniquindio.poo.repositorio.TransaccionRepositorio;
import co.edu.uniquindio.poo.util.Config;
import co.edu.uniquindio.poo.util.Reloj;
import co.edu.uniquindio.poo.util.Validaciones;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Lógica de negocio del dinero: clientes, monederos, depósitos, retiros, transferencias,
 * consultas y reversión.
 *
 * Todas las operaciones siguen el mismo esqueleto:
 *   crear la transacción (PENDIENTE) -> fijar comisión -> mover el dinero -> completar
 *   -> registrar en monederos y repositorio -> otorgar puntos.
 * El débito es el único paso que puede fallar por saldo, y Monedero.debitar() valida ANTES de
 * modificar; por eso, si algo falla, no queda ningún cambio a medias ni se guarda la transacción.
 *
 * La REVERSIÓN vive aquí (y no en PuntosServicio ni en un servicio aparte) porque es la operación
 * inversa de depositar/retirar/transferir: usa los mismos monederos y el mismo repositorio, y solo
 * delega en PuntosServicio la parte de los puntos.
 */
public class MonederoServicio {

    /** Historial: lo más reciente primero; a igual fecha, el id mayor (creado después) primero. */
    private static final Comparator<Transaccion> MAS_RECIENTE_PRIMERO =
            Comparator.comparing(Transaccion::getFecha)
                    .thenComparing(Transaccion::getId)
                    .reversed();

    private final ClienteRepositorio clienteRepositorio;
    private final TransaccionRepositorio transaccionRepositorio;
    private final PuntosServicio puntosServicio;
    private final AnalisisServicio analisisServicio;
    private final AlertasServicio alertasServicio;
    private final Reloj reloj;

    public MonederoServicio(ClienteRepositorio clienteRepositorio, TransaccionRepositorio transaccionRepositorio,
                            PuntosServicio puntosServicio, AnalisisServicio analisisServicio,
                            AlertasServicio alertasServicio, Reloj reloj) {
        this.clienteRepositorio = Objects.requireNonNull(clienteRepositorio);
        this.transaccionRepositorio = Objects.requireNonNull(transaccionRepositorio);
        this.puntosServicio = Objects.requireNonNull(puntosServicio);
        this.analisisServicio = Objects.requireNonNull(analisisServicio);
        this.alertasServicio = Objects.requireNonNull(alertasServicio);
        this.reloj = Objects.requireNonNull(reloj);
    }

    // ================= Clientes y monederos =================

    /** Registra un cliente nuevo (sin monederos: se crean con crearMonedero). */
    public Cliente registrarCliente(String identificacion, String nombre, String correo, String telefono,
                                    String contrasena) throws DatoInvalidoException {
        Cliente cliente = new Cliente(identificacion, nombre, correo, telefono, contrasena); // valida los datos
        if (clienteRepositorio.existe(cliente.getIdentificacion())) {
            throw new DatoInvalidoException("identificación",
                    "ya existe un cliente con identificación " + cliente.getIdentificacion());
        }
        clienteRepositorio.guardar(cliente);
        return cliente;
    }

    public Cliente buscarCliente(String identificacion) throws ClienteNoEncontradoException {
        return clienteRepositorio.buscarPorId(identificacion)
                .orElseThrow(() -> new ClienteNoEncontradoException(identificacion));
    }

    /** Multimonedero: un cliente puede tener varios monederos, cada uno con su saldo. */
    public Monedero crearMonedero(String idCliente, String nombre, TipoMonedero tipo)
            throws ClienteNoEncontradoException, DatoInvalidoException {
        return buscarCliente(idCliente).abrirMonedero(nombre, tipo);
    }

    /** Busca el monedero entre los de todos los clientes (flatMap aplana la lista de listas). */
    public Monedero buscarMonedero(String idMonedero) throws DatoInvalidoException {
        return clienteRepositorio.listar().stream()
                .flatMap(cliente -> cliente.getMonederos().stream())
                .filter(monedero -> monedero.getId().equals(idMonedero))
                .findFirst()
                .orElseThrow(() -> new DatoInvalidoException("monedero", "no existe el monedero " + idMonedero));
    }

    // ================= Operaciones =================

    public Deposito depositar(String idMonedero, BigDecimal monto, String descripcion)
            throws DatoInvalidoException, MontoInvalidoException {
        Monedero monedero = buscarMonedero(idMonedero);
        Deposito deposito = new Deposito(monto, monedero, reloj.ahora(), descripcion);
        deposito.aplicarComision(BigDecimal.ONE); // tasa de depósito = 0 % (Config), queda en 0.00
        monedero.acreditar(deposito.getMonto());
        completarYRegistrar(deposito, monedero);
        return deposito;
    }

    public Retiro retirar(String idMonedero, BigDecimal monto, Categoria categoria, String descripcion)
            throws DatoInvalidoException, MontoInvalidoException, SaldoInsuficienteException {
        Monedero origen = buscarMonedero(idMonedero);
        Retiro retiro = new Retiro(monto, origen, categoria, reloj.ahora(), descripcion);
        retiro.aplicarComision(factorRetiro(origen.getPropietario()));
        origen.debitar(retiro.getMontoTotal()); // monto + comisión; falla sin modificar si no alcanza
        completarYRegistrar(retiro, origen);
        alertasServicio.revisarSaldoBajo(origen, retiro.getMontoTotal());
        return retiro;
    }

    /**
     * Transfiere a cualquier monedero: de otro cliente o del mismo (en ese caso sin comisión ni puntos,
     * lo decide Transferencia polimórficamente). La comisión la paga el emisor; el destino recibe el monto.
     */
    public Transferencia transferir(String idOrigen, String idDestino, BigDecimal monto, Categoria categoria,
                                    String descripcion)
            throws DatoInvalidoException, MontoInvalidoException, SaldoInsuficienteException {
        Monedero origen = buscarMonedero(idOrigen);
        Monedero destino = buscarMonedero(idDestino);
        Transferencia transferencia = new Transferencia(monto, origen, destino, categoria, reloj.ahora(), descripcion);

        Optional<Beneficio> beneficio = beneficioAplicable(transferencia);
        transferencia.aplicarComision(factorTransferencia(origen.getPropietario(), beneficio.isPresent()));

        origen.debitar(transferencia.getMontoTotal());
        destino.acreditar(transferencia.getMonto());
        completarYRegistrar(transferencia, origen, destino);
        alertasServicio.revisarSaldoBajo(origen, transferencia.getMontoTotal());

        // Al final: si la operación hubiera fallado, el beneficio no se habría gastado.
        beneficio.ifPresent(Beneficio::marcarUsado);
        return transferencia;
    }

    /**
     * Comisión que se cobraría por una operación, sin ejecutarla (para mostrarla antes de confirmar).
     * Usa los mismos objetos y métodos que la operación real, así ambas nunca pueden dar valores distintos.
     * No consume ids porque la transacción de prueba nunca se completa.
     *
     * @param idDestino solo se usa en transferencias.
     */
    public BigDecimal cotizarComision(TipoTransaccion tipo, String idOrigen, String idDestino, BigDecimal monto)
            throws DatoInvalidoException, MontoInvalidoException {
        Validaciones.noNulo(tipo, "tipo de transacción");
        Monedero origen = buscarMonedero(idOrigen);
        Cliente cliente = origen.getPropietario();
        return switch (tipo) {
            case DEPOSITO -> new Deposito(monto, origen, reloj.ahora(), "")
                    .aplicarComision(BigDecimal.ONE);
            case RETIRO -> new Retiro(monto, origen, Categoria.OTROS, reloj.ahora(), "")
                    .aplicarComision(factorRetiro(cliente));
            case TRANSFERENCIA -> {
                Transferencia prueba = new Transferencia(monto, origen, buscarMonedero(idDestino),
                        Categoria.OTROS, reloj.ahora(), "");
                yield prueba.aplicarComision(factorTransferencia(cliente, beneficioAplicable(prueba).isPresent()));
            }
        };
    }

    // ================= Reversión =================

    /**
     * Revierte una transacción COMPLETADA (una sola vez): devuelve el dinero (incluida la comisión),
     * descuenta los puntos que generó y la deja en estado REVERTIDA (no se borra del historial).
     *
     * Orden pensado para no quedar a medias:
     *  1. Validar estado y puntos (no modifican nada).
     *  2. Hacer el único débito posible (el que puede fallar por saldo); si falla, nada cambió.
     *  3. Acreditar, descontar puntos y cambiar el estado: ya no pueden fallar.
     *
     * El switch por tipo es temporal: en la Fase 5A esta parte pasa a la interfaz Reversible,
     * para que cada transacción sepa deshacerse a sí misma.
     */
    public Transaccion revertir(String idTransaccion)
            throws DatoInvalidoException, TransaccionNoReversibleException, SaldoInsuficienteException,
            PuntosInsuficientesException, MontoInvalidoException {
        Transaccion transaccion = transaccionRepositorio.buscarPorId(idTransaccion)
                .orElseThrow(() -> new DatoInvalidoException("transacción", "no existe la transacción " + idTransaccion));

        if (transaccion.getEstado() != EstadoTransaccion.COMPLETADA) {
            throw new TransaccionNoReversibleException("La transacción " + idTransaccion
                    + " no se puede revertir porque está " + transaccion.getEstado());
        }
        puntosServicio.validarReversionPuntos(transaccion);

        switch (transaccion.getTipo()) {
            // El dinero depositado sale de nuevo; si ya se gastó: SaldoInsuficienteException.
            case DEPOSITO -> transaccion.getOrigen().debitar(transaccion.getMonto());
            // Se devuelve todo lo que salió: monto + comisión.
            case RETIRO -> transaccion.getOrigen().acreditar(transaccion.getMontoTotal());
            case TRANSFERENCIA -> {
                Transferencia transferencia = (Transferencia) transaccion;
                // Primero el débito al destinatario: si ya no tiene el dinero, falla sin tocar el origen.
                transferencia.getDestino().debitar(transferencia.getMonto());
                transferencia.getOrigen().acreditar(transferencia.getMontoTotal());
            }
        }

        puntosServicio.revertirPuntos(transaccion);
        transaccion.marcarRevertida();
        return transaccion;
    }

    // ================= Consultas =================

    public BigDecimal consultarSaldo(String idMonedero) throws DatoInvalidoException {
        return buscarMonedero(idMonedero).getSaldo();
    }

    /** Saldo total del cliente: suma declarativa de los saldos de todos sus monederos. */
    public BigDecimal saldoTotal(String idCliente) throws ClienteNoEncontradoException {
        return buscarCliente(idCliente).getMonederos().stream()
                .map(Monedero::getSaldo)
                .reduce(Config.redondear(BigDecimal.ZERO), BigDecimal::add);
    }

    /** Todas las transacciones del cliente, lo más reciente primero. */
    public List<Transaccion> historial(String idCliente) throws ClienteNoEncontradoException, DatoInvalidoException {
        return historialFiltrado(idCliente, null, null, null, null);
    }

    /**
     * Historial con filtros opcionales: un parámetro null significa "no filtrar por eso".
     * Las fechas son inclusivas. Los filtros se combinan como un único Predicate con and().
     *
     * flatMap junta los historiales de todos los monederos; distinct() quita la copia de las
     * transferencias entre monederos propios (que están en el historial de ambos).
     */
    public List<Transaccion> historialFiltrado(String idCliente, TipoTransaccion tipo, Categoria categoria,
                                               LocalDate desde, LocalDate hasta)
            throws ClienteNoEncontradoException, DatoInvalidoException {
        Cliente cliente = buscarCliente(idCliente);
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new DatoInvalidoException("rango de fechas", "la fecha inicial es posterior a la final");
        }

        Predicate<Transaccion> filtro = t -> true;
        if (tipo != null) {
            filtro = filtro.and(t -> t.getTipo() == tipo);
        }
        if (categoria != null) {
            filtro = filtro.and(t -> t.getCategoriaGasto().filter(c -> c == categoria).isPresent());
        }
        if (desde != null) {
            filtro = filtro.and(t -> !t.getFecha().toLocalDate().isBefore(desde));
        }
        if (hasta != null) {
            filtro = filtro.and(t -> !t.getFecha().toLocalDate().isAfter(hasta));
        }

        return cliente.getMonederos().stream()
                .flatMap(monedero -> monedero.getHistorial().stream())
                .distinct()
                .filter(filtro)
                .sorted(MAS_RECIENTE_PRIMERO)
                .toList();
    }

    // ================= Apoyo =================

    /**
     * Pasos finales comunes a toda operación exitosa. Aquí se integra la detección de atípicas:
     * como todas las operaciones pasan por este método, ninguna se queda sin evaluar.
     */
    private void completarYRegistrar(Transaccion transaccion, Monedero... monederos) throws DatoInvalidoException {
        transaccion.marcarCompletada(); // aquí recibe su id
        for (Monedero monedero : monederos) {
            monedero.registrarTransaccion(transaccion);
        }
        transaccionRepositorio.guardar(transaccion);
        puntosServicio.otorgarPuntos(transaccion);
        analisisServicio.evaluarAtipica(transaccion);
    }

    /** Retiros: sin cargo si el cliente tiene vigente el beneficio de 500 puntos. El rango no rebaja retiros. */
    private BigDecimal factorRetiro(Cliente cliente) {
        return puntosServicio.tieneRetirosSinComision(cliente) ? BigDecimal.ZERO : BigDecimal.ONE;
    }

    /**
     * Transferencias: factor = (1 - descuento del rango) x (1 - 10 % si usa el beneficio de 100 puntos).
     * Los descuentos se multiplican (no se suman), así nunca pueden superar el 100 %.
     * El rango se toma ANTES de la operación: los puntos que esta gane no rebajan su propia comisión.
     */
    private BigDecimal factorTransferencia(Cliente cliente, boolean usaBeneficio) {
        BigDecimal factor = BigDecimal.ONE.subtract(cliente.getRango().getDescuentoTransferencia());
        if (usaBeneficio) {
            factor = factor.multiply(BigDecimal.ONE.subtract(Config.DESCUENTO_BENEFICIO_TRANSFERENCIA));
        }
        return factor;
    }

    /** Entre monederos propios no hay comisión, así que no se gasta el beneficio de 100 puntos. */
    private Optional<Beneficio> beneficioAplicable(Transferencia transferencia) {
        if (transferencia.esEntreMonederosPropios()) {
            return Optional.empty();
        }
        return puntosServicio.buscarDescuentoTransferencia(transferencia.getOrigen().getPropietario());
    }
}
