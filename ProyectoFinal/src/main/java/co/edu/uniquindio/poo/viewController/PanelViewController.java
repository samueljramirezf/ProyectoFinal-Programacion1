package co.edu.uniquindio.poo.viewController;

import co.edu.uniquindio.poo.App;
import co.edu.uniquindio.poo.controller.MonederoController;
import co.edu.uniquindio.poo.excepcion.MonederoException;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.TipoMonedero;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class PanelViewController {

    private MonederoController monederoController;
    private Cliente cliente;
    private final ObservableList<Monedero> listMonederos = FXCollections.observableArrayList();
    private App app;

    @FXML
    private Label lblBienvenida;

    @FXML
    private Label lblFecha;

    @FXML
    private Label lblSaldoTotal;

    @FXML
    private Label lblPuntos;

    @FXML
    private Label lblRango;

    @FXML
    private TableView<Monedero> tblMonederos;

    @FXML
    private TableColumn<Monedero, String> tbcId;

    @FXML
    private TableColumn<Monedero, String> tbcNombre;

    @FXML
    private TableColumn<Monedero, String> tbcTipo;

    @FXML
    private TableColumn<Monedero, String> tbcSaldo;

    @FXML
    private TextField txtNombreMonedero;

    @FXML
    private ComboBox<TipoMonedero> cmbTipoMonedero;

    @FXML
    void onCrearMonedero() {
        String nombre = txtNombreMonedero.getText().trim();
        TipoMonedero tipo = cmbTipoMonedero.getValue();
        if (nombre.isEmpty() || tipo == null) {
            Dialogos.mostrarError("Escribe un nombre y elige el tipo del monedero.");
            return;
        }
        try {
            Monedero monedero = monederoController.crearMonedero(cliente.getIdentificacion(), nombre, tipo);
            txtNombreMonedero.clear();
            cmbTipoMonedero.setValue(null);
            actualizarDatos();
            Dialogos.mostrarInfo("Monedero " + monedero.getId() + " creado.");
        } catch (MonederoException e) {
            Dialogos.mostrarError(e.getMessage());
        }
    }

    @FXML
    void onIrOperaciones() {
        app.openOperaciones();
    }

    @FXML
    void onIrHistorial() {
        app.openHistorial();
    }

    @FXML
    void onIrPuntos() {
        app.openPuntos();
    }

    @FXML
    void onIrAvanzado() {
        app.openAvanzado();
    }

    @FXML
    void onCerrarSesion() {
        App.contexto.cerrarSesion();
        app.openLogin();
    }

    @FXML
    void initialize() {
        monederoController = new MonederoController(App.contexto.getMonederoServicio(), App.contexto.getReloj());
        cliente = App.contexto.getClienteActual();
        cmbTipoMonedero.getItems().setAll(TipoMonedero.values());
        initDataBinding();
        tblMonederos.setItems(listMonederos);
        actualizarDatos();
    }

    private void initDataBinding() {
        tbcId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        tbcNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        tbcTipo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipo().toString()));
        tbcSaldo.setCellValueFactory(cellData -> new SimpleStringProperty(Formato.dinero(cellData.getValue().getSaldo())));
    }

    /** Los puntos y el rango los calcula el modelo; aquí solo se muestran. */
    private void actualizarDatos() {
        lblBienvenida.setText("Hola, " + cliente.getNombre());
        lblFecha.setText("Hoy es " + Formato.fecha(monederoController.fechaActual()));
        lblPuntos.setText(cliente.getPuntosDisponibles() + " disponibles · " + cliente.getPuntosAcumulados() + " acumulados");
        lblRango.setText(cliente.getRango().toString());
        try {
            listMonederos.setAll(monederoController.obtenerMonederos(cliente.getIdentificacion()));
            lblSaldoTotal.setText(Formato.dinero(monederoController.saldoTotal(cliente.getIdentificacion())));
        } catch (MonederoException e) {
            Dialogos.mostrarError(e.getMessage());
        }
    }

    public void setApp(App app) {
        this.app = app;
    }
}
