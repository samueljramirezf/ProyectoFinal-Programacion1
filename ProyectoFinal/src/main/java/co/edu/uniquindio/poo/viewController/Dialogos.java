package co.edu.uniquindio.poo.viewController;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Los Alert que usan todas las pantallas (en el tercer parcial cada viewController tenía su propio
 * mostrarMensaje). Aquí llegan los mensajes de las excepciones personalizadas: nunca un stack trace.
 */
public final class Dialogos {

    private Dialogos() {
    }

    public static void mostrarError(String mensaje) {
        mostrar(Alert.AlertType.WARNING, "No se pudo completar", mensaje);
    }

    public static void mostrarInfo(String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, "Monedero Virtual", mensaje);
    }

    /** Devuelve true solo si el usuario presiona Aceptar. */
    public static boolean confirmar(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        return alert.showAndWait().filter(boton -> boton == ButtonType.OK).isPresent();
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
