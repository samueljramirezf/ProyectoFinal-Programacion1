package co.edu.uniquindio.poo.viewController;

import co.edu.uniquindio.poo.App;
import co.edu.uniquindio.poo.controller.SesionController;
import co.edu.uniquindio.poo.excepcion.MonederoException;
import co.edu.uniquindio.poo.modelo.Cliente;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegistroViewController {

    private SesionController sesionController;
    private App app;

    @FXML
    private TextField txtIdentificacion;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtCorreo;

    @FXML
    private TextField txtTelefono;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private PasswordField txtConfirmacion;

    /**
     * Aquí solo se revisa que no falte nada y que las contraseñas coincidan (cosas de la pantalla).
     * Si el correo tiene mal formato o la identificación ya existe, lo detecta el modelo/servicio y
     * llega como DatoInvalidoException.
     */
    @FXML
    void onRegistrar() {
        String identificacion = txtIdentificacion.getText().trim();
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String contrasena = txtContrasena.getText();
        if (identificacion.isEmpty() || nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty()
                || contrasena.isEmpty()) {
            Dialogos.mostrarError("Todos los campos son obligatorios.");
            return;
        }
        if (!contrasena.equals(txtConfirmacion.getText())) {
            Dialogos.mostrarError("Las contraseñas no coinciden.");
            return;
        }
        try {
            Cliente cliente = sesionController.registrarCliente(identificacion, nombre, correo, telefono, contrasena);
            Dialogos.mostrarInfo("Bienvenido(a), " + cliente.getNombre()
                    + ". Crea tu primer monedero desde el panel principal.");
            App.contexto.iniciarSesion(cliente);
            app.openPanel();
        } catch (MonederoException e) {
            Dialogos.mostrarError(e.getMessage());
        }
    }

    @FXML
    void onVolver() {
        app.openLogin();
    }

    @FXML
    void initialize() {
        sesionController = new SesionController(App.contexto.getMonederoServicio());
    }

    public void setApp(App app) {
        this.app = app;
    }
}
