package co.edu.uniquindio.poo.modelo;

/** Todo lo que genera puntos sabe calcular cuántos, así PuntosServicio otorga y revierte puntos de cualquier transacción sin preguntar de qué tipo es (cada hija usa su propio factor). */
public interface Puntuable {
    int calcularPuntos();
}
