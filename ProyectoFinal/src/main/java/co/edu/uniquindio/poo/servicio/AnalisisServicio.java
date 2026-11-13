package co.edu.uniquindio.poo.servicio;

import co.edu.uniquindio.poo.excepcion.ClienteNoEncontradoException;
import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.modelo.Categoria;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.EstadoTransaccion;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.repositorio.ClienteRepositorio;
import co.edu.uniquindio.poo.util.Config;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Análisis del historial de un cliente: patrones de gasto y detección de transacciones atípicas.
 *
 * Ambos están en la misma clase porque hacen lo mismo en el fondo: recorrer el historial del cliente
 * con streams y sacar estadísticas (sumas, agrupaciones, promedio, desviación estándar).
 */
public class AnalisisServicio {

    /** Precisión de los cálculos intermedios (promedio, varianza, raíz) antes de redondear a 2 decimales. */
    private static final MathContext PRECISION = MathContext.DECIMAL64;
    private static final BigDecimal CIEN = new BigDecimal("100");

    private final ClienteRepositorio clienteRepositorio;
    private final AlertasServicio alertasServicio;

    public AnalisisServicio(ClienteRepositorio clienteRepositorio, AlertasServicio alertasServicio) {
        this.clienteRepositorio = Objects.requireNonNull(clienteRepositorio);
        this.alertasServicio = Objects.requireNonNull(alertasServicio);
    }

    // ================= Detección de transacciones atípicas =================

    /**
     * Evalúa si una transacción recién completada es atípica para su cliente y, si lo es, la marca
     * como sospechosa y genera una alerta ATIPICA. MonederoServicio la llama en cada operación.
     *
     * <p><b>Algoritmo.</b> Se toman los montos de las transacciones anteriores hechas por el cliente
     * (él es el origen; no cuentan las que recibió ni las revertidas) y se calcula:
     * <pre>
     *   promedio   = (x1 + ... + xn) / n
     *   desviación = raíz( ((x1 - promedio)² + ... + (xn - promedio)²) / n )   (poblacional)
     *   umbral     = promedio + 2 × desviación
     * </pre>
     * La transacción es atípica si su monto es mayor que el umbral. Si hay menos de
     * {@link Config#MINIMO_TRANSACCIONES_ANALISIS} transacciones previas no se evalúa.
     *
     * <p><b>Justificación.</b> El promedio describe el monto "habitual" del cliente y la desviación
     * estándar cuánto suelen variar sus montos. Un monto más de dos desviaciones por encima del promedio
     * se aleja claramente de su comportamiento normal. Como el umbral depende de la variabilidad de
     * cada cliente, el criterio se adapta a cada uno: quien siempre mueve montos parecidos tiene un
     * umbral cercano a su promedio; quien mueve montos muy variados, uno más alto. Se exige un mínimo
     * de transacciones porque con muy pocos datos el promedio y la desviación no son representativos.
     * Se usa la desviación poblacional porque se analiza todo el historial del cliente, no una muestra.
     *
     * <p><b>Complejidad temporal</b> (n = transacciones del historial del cliente):
     * <ul>
     *   <li>Reunir los montos del historial: O(n).</li>
     *   <li>Promedio: una pasada, O(n).</li>
     *   <li>Desviación estándar: otra pasada, O(n).</li>
     *   <li>Comparar con el umbral y marcar la transacción: O(1).</li>
     * </ul>
     * Total O(n) por transacción evaluada, con memoria adicional O(n) para la lista de montos.
     */
    public boolean evaluarAtipica(Transaccion nueva) throws DatoInvalidoException {
        Cliente cliente = nueva.getOrigen().getPropietario();
        List<BigDecimal> montos = transaccionesHechasPor(cliente).stream()
                .filter(t -> t != nueva)
                .filter(t -> t.getEstado() != EstadoTransaccion.REVERTIDA)
                .map(Transaccion::getMonto)
                .toList();

        if (montos.size() < Config.MINIMO_TRANSACCIONES_ANALISIS) {
            return false;
        }

        BigDecimal n = BigDecimal.valueOf(montos.size());
        BigDecimal promedio = montos.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(n, PRECISION);
        BigDecimal varianza = montos.stream()
                .map(x -> x.subtract(promedio).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(n, PRECISION);
        BigDecimal desviacion = varianza.sqrt(PRECISION);
        BigDecimal umbral = promedio.add(Config.FACTOR_DESVIACION_ATIPICA.multiply(desviacion));

        if (nueva.getMonto().compareTo(umbral) <= 0) {
            return false;
        }
        nueva.marcarComoSospechosa();
        alertasServicio.alertarAtipica(nueva, "monto " + nueva.getMonto().toPlainString()
                + " supera el umbral " + Config.redondear(umbral).toPlainString()
                + " (promedio " + Config.redondear(promedio).toPlainString()
                + " + 2 × desviación " + Config.redondear(desviacion).toPlainString() + ")");
        return true;
    }

    // ================= Patrones de gasto =================

    /**
     * Total gastado por categoría. groupingBy agrupa los gastos por su categoría y reducing suma los
     * montos de cada grupo. EnumMap mantiene las categorías en el orden en que están declaradas.
     */
    public Map<Categoria, BigDecimal> gastoPorCategoria(String idCliente) throws ClienteNoEncontradoException {
        return gastosDe(buscarCliente(idCliente)).stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategoriaGasto().orElseThrow(),
                        () -> new EnumMap<>(Categoria.class),
                        Collectors.reducing(Config.redondear(BigDecimal.ZERO), Transaccion::getMonto, BigDecimal::add)));
    }

    /** Categoría en la que más gastó el cliente (vacío si no ha gastado). */
    public Optional<Categoria> categoriaMayorGasto(String idCliente) throws ClienteNoEncontradoException {
        return gastoPorCategoria(idCliente).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    /** Porcentaje del gasto total que representa cada categoría (2 decimales). */
    public Map<Categoria, BigDecimal> porcentajePorCategoria(String idCliente) throws ClienteNoEncontradoException {
        Map<Categoria, BigDecimal> gastos = gastoPorCategoria(idCliente);
        BigDecimal total = gastos.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<Categoria, BigDecimal> porcentajes = new EnumMap<>(Categoria.class);
        if (total.signum() == 0) {
            return porcentajes;
        }
        gastos.forEach((categoria, monto) ->
                porcentajes.put(categoria, monto.multiply(CIEN).divide(total, 2, RoundingMode.HALF_EVEN)));
        return porcentajes;
    }

    /** Gasto total de cada mes en que hubo gastos, en orden cronológico. */
    public Map<YearMonth, BigDecimal> gastoPorMes(String idCliente) throws ClienteNoEncontradoException {
        return gastosDe(buscarCliente(idCliente)).stream()
                .sorted(Comparator.comparing(Transaccion::getFecha))
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getFecha()),
                        LinkedHashMap::new,
                        Collectors.reducing(Config.redondear(BigDecimal.ZERO), Transaccion::getMonto, BigDecimal::add)));
    }

    /** Promedio de gasto por mes, contando solo los meses en que hubo gastos. */
    public BigDecimal promedioGastoMensual(String idCliente) throws ClienteNoEncontradoException {
        Map<YearMonth, BigDecimal> porMes = gastoPorMes(idCliente);
        if (porMes.isEmpty()) {
            return Config.redondear(BigDecimal.ZERO);
        }
        BigDecimal total = porMes.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(porMes.size()), Config.ESCALA_DINERO, Config.REDONDEO_DINERO);
    }

    /** Reporte de gastos en texto, listo para mostrar en consola o en la interfaz. */
    public String generarReporte(String idCliente) throws ClienteNoEncontradoException {
        Cliente cliente = buscarCliente(idCliente);
        Map<Categoria, BigDecimal> gastos = gastoPorCategoria(idCliente);
        Map<Categoria, BigDecimal> porcentajes = porcentajePorCategoria(idCliente);
        BigDecimal total = gastos.values().stream().reduce(Config.redondear(BigDecimal.ZERO), BigDecimal::add);

        StringBuilder reporte = new StringBuilder();
        reporte.append("Reporte de gastos de ").append(cliente.getNombre()).append('\n');
        if (gastos.isEmpty()) {
            return reporte.append("  Sin gastos registrados.\n").toString();
        }
        gastos.forEach((categoria, monto) -> reporte.append(String.format("  %-16s %10s  (%s %%)%n",
                categoria, monto.toPlainString(), porcentajes.get(categoria).toPlainString())));
        reporte.append(String.format("  %-16s %10s%n", "TOTAL", total.toPlainString()));
        categoriaMayorGasto(idCliente).ifPresent(c ->
                reporte.append("  Categoría de mayor gasto: ").append(c).append('\n'));
        gastoPorMes(idCliente).forEach((mes, monto) ->
                reporte.append("  Gasto en ").append(mes).append(": ").append(monto.toPlainString()).append('\n'));
        reporte.append("  Promedio de gasto mensual: ").append(promedioGastoMensual(idCliente).toPlainString()).append('\n');
        return reporte.toString();
    }

    // ================= Apoyo =================

    /**
     * Transacciones hechas por el cliente (él es el origen), sin repetidas. flatMap junta los
     * historiales de todos sus monederos; distinct() quita la copia de las transferencias entre
     * monederos propios, que aparecen en el historial de ambos.
     */
    private List<Transaccion> transaccionesHechasPor(Cliente cliente) {
        return cliente.getMonederos().stream()
                .flatMap(monedero -> monedero.getHistorial().stream())
                .distinct()
                .filter(t -> t.getOrigen().getPropietario().equals(cliente))
                .toList();
    }

    /** Gastos = transacciones completadas que esGasto() (polimorfismo: cada tipo decide si es gasto). */
    private List<Transaccion> gastosDe(Cliente cliente) {
        return transaccionesHechasPor(cliente).stream()
                .filter(t -> t.getEstado() == EstadoTransaccion.COMPLETADA)
                .filter(Transaccion::esGasto)
                .toList();
    }

    private Cliente buscarCliente(String idCliente) throws ClienteNoEncontradoException {
        return clienteRepositorio.buscarPorId(idCliente)
                .orElseThrow(() -> new ClienteNoEncontradoException(idCliente));
    }
}
