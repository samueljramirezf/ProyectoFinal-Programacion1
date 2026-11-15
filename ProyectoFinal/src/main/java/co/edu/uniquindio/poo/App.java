package co.edu.uniquindio.poo;

import co.edu.uniquindio.poo.excepcion.MonederoException;
import co.edu.uniquindio.poo.viewController.AvanzadoViewController;
import co.edu.uniquindio.poo.viewController.HistorialViewController;
import co.edu.uniquindio.poo.viewController.LoginViewController;
import co.edu.uniquindio.poo.viewController.OperacionesViewController;
import co.edu.uniquindio.poo.viewController.PanelViewController;
import co.edu.uniquindio.poo.viewController.PuntosViewController;
import co.edu.uniquindio.poo.viewController.RegistroViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Aplicación JavaFX. Misma estructura del tercer parcial:
 * - un método openXxx() por pantalla, que carga su FXML y le pasa "this" al viewController con setApp;
 * - un atributo static (contexto) con los servicios, que los viewController leen en initialize()
 *   (en initialize() todavía no se ha llamado setApp, por eso no puede llegar por ahí).
 */
public class App extends Application {

    public static ContextoApp contexto = new ContextoApp();

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws MonederoException {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Monedero Virtual");
        // Una sola vez: si se llamara al volver al login, los clientes quedarían duplicados.
        contexto.precargarDatos();
        openLogin();
    }

    public void openLogin() {
        FXMLLoader loader = cargarVista("login.fxml");
        LoginViewController loginViewController = loader.getController();
        loginViewController.setApp(this);
    }

    public void openRegistro() {
        FXMLLoader loader = cargarVista("registro.fxml");
        RegistroViewController registroViewController = loader.getController();
        registroViewController.setApp(this);
    }

    public void openPanel() {
        FXMLLoader loader = cargarVista("panel.fxml");
        PanelViewController panelViewController = loader.getController();
        panelViewController.setApp(this);
    }

    public void openOperaciones() {
        FXMLLoader loader = cargarVista("operaciones.fxml");
        OperacionesViewController operacionesViewController = loader.getController();
        operacionesViewController.setApp(this);
    }

    public void openHistorial() {
        FXMLLoader loader = cargarVista("historial.fxml");
        HistorialViewController historialViewController = loader.getController();
        historialViewController.setApp(this);
    }

    public void openPuntos() {
        FXMLLoader loader = cargarVista("puntos.fxml");
        PuntosViewController puntosViewController = loader.getController();
        puntosViewController.setApp(this);
    }

    public void openAvanzado() {
        FXMLLoader loader = cargarVista("avanzado.fxml");
        AvanzadoViewController avanzadoViewController = loader.getController();
        avanzadoViewController.setApp(this);
    }

    /**
     * Lo que en el tercer parcial se repetía en cada openXxx(): cargar el FXML, aplicarle la hoja
     * de estilos y mostrarlo en la ventana. Devuelve el loader para que cada método tome su controlador.
     */
    private FXMLLoader cargarVista(String archivoFxml) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(App.class.getResource(archivoFxml));
            Parent rootLayout = loader.load();

            Scene scene = new Scene(rootLayout);
            scene.getStylesheets().add(App.class.getResource("estilos.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
            return loader;
        } catch (IOException e) {
            // Solo pasa si falta o está mal escrito un FXML: es un error de programación, no del usuario.
            throw new IllegalStateException("No se pudo cargar la vista " + archivoFxml, e);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
