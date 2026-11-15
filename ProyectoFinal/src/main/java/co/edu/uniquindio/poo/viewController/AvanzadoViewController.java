package co.edu.uniquindio.poo.viewController;

import co.edu.uniquindio.poo.App;
import co.edu.uniquindio.poo.controller.AlertasController;
import co.edu.uniquindio.poo.controller.AnalisisController;
import co.edu.uniquindio.poo.controller.ProgramadasController;
import co.edu.uniquindio.poo.excepcion.MonederoException;
import co.edu.uniquindio.poo.modelo.Alerta;
import co.edu.uniquindio.poo.modelo.Categoria;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Frecuencia;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.TipoAlerta;
import co.edu.uniquindio.poo.modelo.TipoTransaccion;
import co.edu.uniquindio.poo.modelo.TransaccionProgramada;
import co.edu.uniquindio.poo.servicio.ProgramadasServicio.ResultadoEjecucion;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Pantalla "Avanzado" con tres pestañas: programadas, análisis de gastos y alertas.
 * Es un solo FXML con un solo viewController (igual que las demás pantallas); el código está
 * separado por pestaña con comentarios.
 */
public class AvanzadoViewController {

    private ProgramadasController programadasController;
    private AnalisisController analisisController;
    private AlertasController alertasController;
    private Cliente cliente;
    private final ObservableList<TransaccionProgramada> listProgramadas = FXCollections.observableArrayList();
    private final ObservableList<ResultadoEjecucion> listResultados = FXCollections.observableArrayList();
    private final ObservableList<Alerta> listAlertas = FXCollections.observableArrayList();
    private App app;

    // ---- Pestaña Programadas: formulario ----
    @FXML
    private ComboBox<TipoTransaccion> cmbProgTipo;

    @FXML
    private ComboBox<Monedero> cmbProgMonedero;

    @FXML
    private Label lblProgMonedero;

    @FXML
    private VBox boxProgDestino;

    @FXML
    private RadioButton rbProgOtroCliente;

    @FXML
    private HBox boxProgOtroCliente;

    @FXML
    private TextField txtProgIdOtroCliente;

    @FXML
    private ComboBox<Monedero> cmbProgDestino;

    @FXML
    private HBox boxProgCategoria;

    @FXML
    private ComboBox<Categoria> cmbProgCategoria;

    @FXML
    private TextField txtProgMonto;

    @FXML
    private ComboBox<Frecuencia> cmbProgFrecuencia;

    @FXML
    private DatePicker dpProgFecha;

    @FXML
    private TextField txtProgDescripcion;

    // ---- Pestaña Programadas: lista y reloj ----
    @FXML
    private TableView<TransaccionProgramada> tblProgramadas;

    @FXML
    private TableColumn<TransaccionProgramada, String> tbcProgId;

    @FXML
    private TableColumn<TransaccionProgramada, String> tbcProgTipo;

    @FXML
    private TableColumn<TransaccionProgramada, String> tbcProgMonto;

    @FXML
    private TableColumn<TransaccionProgramada, String> tbcProgOrigen;

    @FXML
    private TableColumn<TransaccionProgramada, String> tbcProgDestino;

    @FXML
    private TableColumn<TransaccionProgramada, String> tbcProgFrecuencia;

    @FXML
    private TableColumn<TransaccionProgramada, String> tbcProgProxima;

    @FXML
    private Label lblHoy;

    @FXML
    private Spinner<Integer> spDias;

    @FXML
    private ListView<ResultadoEjecucion> lstResultados;

    // ---- Pestaña Análisis ----
    @FXML
    private PieChart pieGastos;

    @FXML
    private TextArea txtReporte;

    // ---- Pestaña Alertas ----
    @FXML
    private Label lblCorreo;

    @FXML
    private TableView<Alerta> tblAlertas;

    @FXML
    private TableColumn<Alerta, String> tbcAlertaFecha;

    @FXML
    private TableColumn<Alerta, String> tbcAlertaTipo;

    @FXML
    private TableColumn<Alerta, String> tbcAlertaMensaje;

    @FXML
    void onVolver() {
        app.openPanel();
    }

    @FXML
    void initialize() {
        programadasController = new ProgramadasController(App.contexto.getProgramadasServicio(),
                App.contexto.getMonederoServicio(), App.contexto.getReloj());
        analisisController = new AnalisisController(App.contexto.getAnalisisServicio());
        alertasController = new AlertasController(App.contexto.getAlertasServicio());
        cliente = App.contexto.getClienteActual();

        initProgramadas();
        initAlertas();
        actualizarTodo();
    }

    /** Avanzar la fecha cambia saldos, gastos y alertas: por eso se refrescan las tres pestañas. */
    private void actualizarTodo() {
        actualizarProgramadas();
        actualizarAnalisis();
        actualizarAlertas();
    }

    // =====================================================================
    // Pestaña 1: Programadas
    // =====================================================================

    private void initProgramadas() {
        // Solo hay depósito automático y transferencia periódica (decisión de la Fase 4).
        cmbProgTipo.getItems().setAll(TipoTransaccion.DEPOSITO, TipoTransaccion.TRANSFERENCIA);
        cmbProgCategoria.getItems().setAll(Categoria.values());
        cmbProgFrecuencia.getItems().setAll(Frecuencia.values());
        cmbProgMonedero.setConverter(Formato.monederoConSaldo());
        cmbProgDestino.setConverter(Formato.monederoConPropietario());
        mostrar(boxProgDestino, false);
        mostrar(boxProgCategoria, false);

        tbcProgId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        tbcProgTipo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipo().toString()));
        tbcProgMonto.setCellValueFactory(cellData -> new SimpleStringProperty(Formato.dinero(cellData.getValue().getMonto())));
        tbcProgOrigen.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrigen().getId()));
        tbcProgDestino.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDestino() == null ? "—" : cellData.getValue().getDestino().getId()));
        tbcProgFrecuencia.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFrecuencia().toString()));
        tbcProgProxima.setCellValueFactory(cellData -> new SimpleStringProperty(Formato.fecha(cellData.getValue().getProximaEjecucion())));
        tblProgramadas.setItems(listProgramadas);

        // Cada resultado se muestra como texto; los que fallaron, en rojo (clase CSS).
        lstResultados.setItems(listResultados);
        lstResultados.setCellFactory(lista -> new ListCell<>() {
            @Override
            protected void updateItem(ResultadoEjecucion resultado, boolean vacia) {
                super.updateItem(resultado, vacia);
                getStyleClass().remove("resultado-fallo");
                if (vacia || resultado == null) {
                    setText(null);
                    return;
                }
                String texto = Formato.fecha(resultado.fechaProgramada()) + "  " + resultado.programada().getId()
                        + " " + resultado.programada().getTipo() + " " + Formato.dinero(resultado.programada().getMonto());
                if (resultado.exitosa()) {
                    setText(texto + "  →  ejecutada como " + resultado.transaccion().getId());
                } else {
                    setText(texto + "  →  FALLÓ: " + resultado.error());
                    getStyleClass().add("resultado-fallo");
                }
            }
        });
    }

    @FXML
    void onProgTipoCambiado() {
        TipoTransaccion tipo = cmbProgTipo.getValue();
        boolean esTransferencia = tipo == TipoTransaccion.TRANSFERENCIA;
        mostrar(boxProgDestino, esTransferencia);
        mostrar(boxProgCategoria, esTransferencia);
        lblProgMonedero.setText(esTransferencia ? "Monedero de origen" : "Monedero que recibe");
        if (esTransferencia) {
            onProgDestinoCambiado();
        }
    }

    @FXML
    void onProgDestinoCambiado() {
        boolean otroCliente = rbProgOtroCliente.isSelected();
        mostrar(boxProgOtroCliente, otroCliente);
        cmbProgDestino.getItems().clear();
        if (!otroCliente) {
            cargarMonederos(cliente.getIdentificacion(), cmbProgDestino);
        }
    }

    @FXML
    void onProgBuscarCliente() {
        String idOtro = txtProgIdOtroCliente.getText().trim();
        if (idOtro.isEmpty()) {
            Dialogos.mostrarError("Escribe la identificación del cliente que recibe.");
            return;
        }
        cargarMonederos(idOtro, cmbProgDestino);
        if (cmbProgDestino.getItems().isEmpty()) {
            Dialogos.mostrarInfo("Ese cliente no tiene monederos.");
        }
    }

    /** La fecha anterior a hoy la rechaza el servicio (DatoInvalidoException); aquí solo que no falte. */
    @FXML
    void onProgramar() {
        TipoTransaccion tipo = cmbProgTipo.getValue();
        if (tipo == null || cmbProgMonedero.getValue() == null || cmbProgFrecuencia.getValue() == null
                || dpProgFecha.getValue() == null) {
            Dialogos.mostrarError("Elige el tipo, el monedero, la frecuencia y la primera fecha.");
            return;
        }
        if (tipo == TipoTransaccion.TRANSFERENCIA
                && (cmbProgDestino.getValue() == null || cmbProgCategoria.getValue() == null)) {
            Dialogos.mostrarError("Elige el monedero destino y la categoría.");
            return;
        }
        BigDecimal monto = leerMonto(txtProgMonto);
        if (monto == null) {
            return;
        }
        String idMonedero = cmbProgMonedero.getValue().getId();
        String descripcion = txtProgDescripcion.getText().trim();
        try {
            TransaccionProgramada programada = tipo == TipoTransaccion.DEPOSITO
                    ? programadasController.programarDeposito(idMonedero, monto, cmbProgFrecuencia.getValue(),
                    dpProgFecha.getValue(), descripcion)
                    : programadasController.programarTransferencia(idMonedero, cmbProgDestino.getValue().getId(),
                    monto, cmbProgCategoria.getValue(), cmbProgFrecuencia.getValue(), dpProgFecha.getValue(),
                    descripcion);
            Dialogos.mostrarInfo("Programada " + programada.getId() + " creada. Primera ejecución: "
                    + Formato.fecha(programada.getProximaEjecucion()) + ".");
            txtProgMonto.clear();
            txtProgDescripcion.clear();
            actualizarProgramadas();
        } catch (MonederoException e) {
            Dialogos.mostrarError(e.getMessage());
        }
    }

    @FXML
    void onAvanzarFecha() {
        int dias = spDias.getValue();
        try {
            List<ResultadoEjecucion> resultados = programadasController.avanzarDias(dias, cliente);
            listResultados.setAll(resultados);
            actualizarTodo();
            if (resultados.isEmpty()) {
                Dialogos.mostrarInfo("Avanzaste " + dias + " día(s). No había programadas tuyas pendientes.");
            }
        } catch (MonederoException e) {
            Dialogos.mostrarError(e.getMessage());
        }
    }

    private void actualizarProgramadas() {
        lblHoy.setText("Hoy es " + Formato.fecha(programadasController.fechaActual()));
        try {
            listProgramadas.setAll(programadasController.listarProgramadas(cliente.getIdentificacion()));
            // Recarga los saldos del selector (cambian al ejecutarse programadas).
            Monedero seleccionado = cmbProgMonedero.getValue();
            cargarMonederos(cliente.getIdentificacion(), cmbProgMonedero);
            if (seleccionado != null) {
                cmbProgMonedero.setValue(seleccionado);
            }
        } catch (MonederoException e) {
            Dialogos.mostrarError(e.getMessage());
        }
    }

    // =====================================================================
    // Pestaña 2: Análisis de gastos
    // =====================================================================

    /** Los totales y porcentajes vienen calculados del servicio; aquí solo se arman los trozos del gráfico. */
    private void actualizarAnalisis() {
        try {
            Map<Categoria, BigDecimal> gastos = analisisController.gastoPorCategoria(cliente.getIdentificacion());
            Map<Categoria, BigDecimal> porcentajes = analisisController.porcentajePorCategoria(cliente.getIdentificacion());
            ObservableList<PieChart.Data> datos = FXCollections.observableArrayList();
            gastos.forEach((categoria, monto) -> datos.add(new PieChart.Data(
                    categoria + " (" + porcentajes.get(categoria).toPlainString() + " %)", monto.doubleValue())));
            pieGastos.setData(datos);
            pieGastos.setTitle(gastos.isEmpty() ? "Aún no tienes gastos" : "Gastos por categoría");
            txtReporte.setText(analisisController.generarReporte(cliente.getIdentificacion()));
        } catch (MonederoException e) {
            Dialogos.mostrarError(e.getMessage());
        }
    }

    // =====================================================================
    // Pestaña 3: Alertas
    // =====================================================================

    private void initAlertas() {
        lblCorreo.setText(App.contexto.isCorreoActivo()
                ? "Envío por Gmail ACTIVO: cada alerta también llega al correo del cliente."
                : "Envío por Gmail DESACTIVADO (faltan las variables de entorno): las alertas solo se guardan aquí.");
        tbcAlertaFecha.setCellValueFactory(cellData -> new SimpleStringProperty(Formato.fechaHora(cellData.getValue().getFecha())));
        tbcAlertaTipo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipo().toString()));
        tbcAlertaMensaje.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMensaje()));
        tblAlertas.setItems(listAlertas);
        // Las atípicas se pintan igual que las transacciones sospechosas del historial.
        tblAlertas.setRowFactory(tabla -> new TableRow<>() {
            @Override
            protected void updateItem(Alerta alerta, boolean vacia) {
                super.updateItem(alerta, vacia);
                getStyleClass().remove("fila-sospechosa");
                if (!vacia && alerta != null && alerta.getTipo() == TipoAlerta.ATIPICA) {
                    getStyleClass().add("fila-sospechosa");
                }
            }
        });
    }

    private void actualizarAlertas() {
        try {
            listAlertas.setAll(alertasController.alertasDe(cliente.getIdentificacion()));
        } catch (MonederoException e) {
            Dialogos.mostrarError(e.getMessage());
        }
    }

    // =====================================================================
    // Apoyo
    // =====================================================================

    private void cargarMonederos(String idCliente, ComboBox<Monedero> combo) {
        try {
            combo.getItems().setAll(programadasController.monederosDe(idCliente));
        } catch (MonederoException e) {
            combo.getItems().clear();
            Dialogos.mostrarError(e.getMessage());
        }
    }

    /** Devuelve el monto escrito, o null si falta o no es un número (y ya mostró el mensaje). */
    private BigDecimal leerMonto(TextField campo) {
        String texto = campo.getText().trim().replace(',', '.');
        if (texto.isEmpty()) {
            Dialogos.mostrarError("Escribe el monto.");
            return null;
        }
        try {
            return new BigDecimal(texto);
        } catch (NumberFormatException e) {
            Dialogos.mostrarError("El monto \"" + campo.getText().trim() + "\" no es un número válido.");
            return null;
        }
    }

    private void mostrar(Node nodo, boolean visible) {
        nodo.setVisible(visible);
        nodo.setManaged(visible);
    }

    public void setApp(App app) {
        this.app = app;
    }
}
