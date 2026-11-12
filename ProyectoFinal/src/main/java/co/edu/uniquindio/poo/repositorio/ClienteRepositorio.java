package co.edu.uniquindio.poo.repositorio;

import co.edu.uniquindio.poo.modelo.Cliente;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Almacén en memoria de clientes, indexados por identificación.
 *
 * Solo guarda y entrega datos; no valida reglas de negocio (eso lo hacen los servicios).
 * Mantener los métodos genéricos (guardar, buscarPorId, listar) facilita convertirlo en la
 * interfaz Repositorio<T, ID> en la Fase 5.
 * LinkedHashMap: búsqueda por id en O(1) y listado en el orden en que se registraron.
 */
public class ClienteRepositorio {

    private final Map<String, Cliente> clientes = new LinkedHashMap<>();

    public void guardar(Cliente cliente) {
        clientes.put(cliente.getIdentificacion(), cliente);
    }

    /** Optional en vez de null: obliga a quien llama a decidir qué hacer si no existe. */
    public Optional<Cliente> buscarPorId(String identificacion) {
        return Optional.ofNullable(clientes.get(identificacion));
    }

    /** Copia inmutable: desde fuera no se puede alterar el almacén. */
    public List<Cliente> listar() {
        return List.copyOf(clientes.values());
    }

    public boolean existe(String identificacion) {
        return clientes.containsKey(identificacion);
    }
}
