package co.edu.uniquindio.poo.viewController;

import co.edu.uniquindio.poo.App;
import co.edu.uniquindio.poo.controller.SesionController;
import co.edu.uniquindio.poo.excepcion.MonederoException;
import co.edu.uniquindio.poo.modelo.Cliente;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginViewController {

    private SesionController sesionController;
    private App app;

    @FXML
    private TextField txtIdentificacion;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    void onIngresar() {
        String identificacion = txtIdentificacion.getText().trim();
        String contrasena = txtContrasena.getText();
        if (identificacion.isEmpty() || contrasena.isEmpty()) {
            Dialogos.mostrarError("Escribe tu identificación y tu contraseña.");
            return;
        }
        try {
            Cliente cliente = sesionController.iniciarSesion(identificacion, contrasena);
            App.contexto.iniciarSesion(cliente);
            app.openPanel();
        } catch (MonederoException e) {
            Dialogos.mostrarError(e.getMessage());
            txtContrasena.clear();
        }
    }

    @FXML
    void onIrRegistro() {
        app.openRegistro();
    }

    @FXML
    void initialize() {
        // contexto es static, por eso se lee con App.contexto (app aún es null aquí)
        sesionController = new SesionController(App.contexto.getMonederoServicio());
    }

    public void setApp(App app) {
        this.app = app;
    }
}
