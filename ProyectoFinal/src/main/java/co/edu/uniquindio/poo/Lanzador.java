package co.edu.uniquindio.poo;

/**
 * Para ejecutar desde el IDE (botón Run). Como el proyecto no tiene module-info, si se ejecuta App
 * directamente Java responde "faltan los componentes de JavaFX"; arrancando desde una clase que NO
 * extiende Application el problema desaparece. Con Maven (mvn javafx:run) no hace falta.
 */
public class Lanzador {

    public static void main(String[] args) {
        App.main(args);
    }
}
