package co.edu.uniquindio.poo.controller;

import co.edu.uniquindio.poo.excepcion.ClienteNoEncontradoException;
import co.edu.uniquindio.poo.modelo.Categoria;
import co.edu.uniquindio.poo.servicio.AnalisisServicio;

import java.math.BigDecimal;
import java.util.Map;

/** Intermediario de la pestaña de análisis de gastos. No usa JavaFX: los totales los calcula el servicio. */
public class AnalisisController {

    private final AnalisisServicio analisisServicio;

    public AnalisisController(AnalisisServicio analisisServicio) {
        this.analisisServicio = analisisServicio;
    }

    public Map<Categoria, BigDecimal> gastoPorCategoria(String idCliente) throws ClienteNoEncontradoException {
        return analisisServicio.gastoPorCategoria(idCliente);
    }

    public Map<Categoria, BigDecimal> porcentajePorCategoria(String idCliente) throws ClienteNoEncontradoException {
        return analisisServicio.porcentajePorCategoria(idCliente);
    }

    public String generarReporte(String idCliente) throws ClienteNoEncontradoException {
        return analisisServicio.generarReporte(idCliente);
    }
}
