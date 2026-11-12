package co.edu.uniquindio.poo.repositorio;

import co.edu.uniquindio.poo.modelo.Transaccion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Almacén en memoria de todas las transacciones completadas del sistema, indexadas por id.
 * Lo usan la reversión (buscar por id) y, más adelante, los reportes globales del administrador.
 * Los filtros (por tipo, fecha, categoría) se hacen en los servicios con streams.
 */
public class TransaccionRepositorio {

    private final Map<String, Transaccion> transacciones = new LinkedHashMap<>();

    public void guardar(Transaccion transaccion) {
        transacciones.put(transaccion.getId(), transaccion);
    }

    public Optional<Transaccion> buscarPorId(String id) {
        return Optional.ofNullable(transacciones.get(id));
    }

    public List<Transaccion> listar() {
        return List.copyOf(transacciones.values());
    }
}
