package co.edu.uniquindio.poo.viewController;

import co.edu.uniquindio.poo.App;
import co.edu.uniquindio.poo.controller.HistorialController;
import co.edu.uniquindio.poo.excepcion.MonederoException;
import co.edu.uniquindio.poo.modelo.Categoria;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.EstadoTransaccion;
import co.edu.uniquindio.poo.modelo.TipoTransaccion;
import co.edu.uniquindio.poo.modelo.Transaccion;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

public class HistorialViewController {

    private HistorialController historialController;
    private Cliente cliente;
    private final ObservableList<Transaccion> listTransacciones = FXCollections.observableArrayList();
    private App app;

    @FXML
    private ComboBox<TipoTransaccion> cmbFiltroTipo;

    @FXML
    private ComboBox<Categoria> cmbFiltroCategoria;

    @FXML
    private DatePicker dpDesde;

    @FXML
    private DatePicker dpHasta;

    @FXML
    private Label lblResultados;

    @FXML
    private TableView<Transaccion> tblTransacciones;

    @FXML
    private TableColumn<Transaccion, String> tbcId;

    @FXML
    private TableColumn<Transaccion, String> tbcFecha;

    @FXML
    private TableColumn<Transaccion, String> tbcTipo;

    @FXML
    private TableColumn<Transaccion, String> tbcMonedero;

    @FXML
    private TableColumn<Transaccion, String> tbcMonto;

    @FXML
    private TableColumn<Transaccion, String> tbcComision;

    @FXML
    private TableColumn<Transaccion, String> tbcCategoria;

    @FXML
    private TableColumn<Transaccion, String> tbcEstado;

    @FXML
    private TableColumn<Transaccion, String> tbcDescripcion;

    @FXML
    void onFiltrar() {
        obtenerTransacciones();
    }

    @FXML
    void onLimpiarFiltros() {
        cmbFiltroTipo.setValue(null);
        cmbFiltroCategoria.setValue(null);
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        obtenerTransacciones();
    }

    /** El servicio valida si se puede revertir; aquí solo se pide confirmación y se muestra el resultado. */
    @FXML
    void onRevertir() {
        Transaccion seleccionada = tblTransacciones.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            Dialogos.mostrarError("Selecciona en la tabla la transacción que quieres revertir.");
            return;
        }
        String pregunta = "Se va a revertir " + seleccionada.getId() + " (" + seleccionada.getTipo() + " de "
                + Formato.dinero(seleccionada.getMonto()) + ").\nSe devuelve el dinero y se descuentan sus puntos."
                + "\n\n¿Continuar?";
        if (!Dialogos.confirmar(pregunta)) {
            return;
        }
        try {
            historialController.revertir(seleccionada.getId());
            Dialogos.mostrarInfo("La transacción " + seleccionada.getId() + " quedó REVERTIDA.");
            obtenerTransacciones();
        } catch (MonederoException e) {
            Dialogos.mostrarError(e.getMessage());
        }
    }

    @FXML
    void onVolver() {
        app.openPanel();
    }

    @FXML
    void initialize() {
        historialController = new HistorialController(App.contexto.getMonederoServicio());
        cliente = App.contexto.getClienteActual();
        cmbFiltroTipo.getItems().setAll(TipoTransaccion.values());
        cmbFiltroCategoria.getItems().setAll(Categoria.values());
        initDataBinding();
        resaltarFilas();
        tblTransacciones.setItems(listTransacciones);
        obtenerTransacciones();
    }

    private void initDataBinding() {
        tbcId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        tbcFecha.setCellValueFactory(cellData -> new SimpleStringProperty(Formato.fechaHora(cellData.getValue().getFecha())));
        tbcTipo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipo().toString()));
        tbcMonedero.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrigen().getId()));
        tbcMonto.setCellValueFactory(cellData -> new SimpleStringProperty(Formato.dinero(cellData.getValue().getMonto())));
        tbcComision.setCellValueFactory(cellData -> new SimpleStringProperty(Formato.dinero(cellData.getValue().getComision())));
        // Optional: los depósitos no tienen categoría (polimorfismo, sin instanceof).
        tbcCategoria.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCategoriaGasto().map(Categoria::toString).orElse("—")));
        tbcEstado.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstado().toString()));
        tbcDescripcion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescripcion()));
    }

    /**
     * Cada fila recibe una clase CSS según la transacción: sospechosa (roja) o revertida (gris).
     * La regla de qué es sospechoso la decide el servicio de análisis; aquí solo se pinta.
     */
    private void resaltarFilas() {
        tblTransacciones.setRowFactory(tabla -> new TableRow<>() {
            @Override
            protected void updateItem(Transaccion transaccion, boolean vacia) {
                super.updateItem(transaccion, vacia);
                getStyleClass().removeAll("fila-sospechosa", "fila-revertida");
                if (vacia || transaccion == null) {
                    return;
                }
                if (transaccion.isSospechosa()) {
                    getStyleClass().add("fila-sospechosa");
                }
                if (transaccion.getEstado() == EstadoTransaccion.REVERTIDA) {
                    getStyleClass().add("fila-revertida");
                }
            }
        });
    }

    private void obtenerTransacciones() {
        try {
            listTransacciones.setAll(historialController.obtenerHistorial(cliente.getIdentificacion(),
                    cmbFiltroTipo.getValue(), cmbFiltroCategoria.getValue(), dpDesde.getValue(), dpHasta.getValue()));
            lblResultados.setText(listTransacciones.size() + " transacciones");
            // Las filas no se redibujan solas cuando cambia el estado de un objeto que ya estaba en la lista.
            tblTransacciones.refresh();
        } catch (MonederoException e) {
            Dialogos.mostrarError(e.getMessage());
        }
    }

    public void setApp(App app) {
        this.app = app;
    }
}
