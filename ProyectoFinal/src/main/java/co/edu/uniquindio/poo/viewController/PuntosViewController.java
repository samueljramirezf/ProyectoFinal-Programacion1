package co.edu.uniquindio.poo.viewController;

import co.edu.uniquindio.poo.App;
import co.edu.uniquindio.poo.controller.PuntosController;
import co.edu.uniquindio.poo.excepcion.MonederoException;
import co.edu.uniquindio.poo.modelo.Beneficio;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.MovimientoPuntos;
import co.edu.uniquindio.poo.modelo.TipoBeneficio;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class PuntosViewController {

    private PuntosController puntosController;
    private Cliente cliente;
    private final ObservableList<Beneficio> listBeneficios = FXCollections.observableArrayList();
    private final ObservableList<MovimientoPuntos> listMovimientos = FXCollections.observableArrayList();
    private App app;

    @FXML
    private Label lblDisponibles;

    @FXML
    private Label lblAcumulados;

    @FXML
    private Label lblRango;

    @FXML
    private Label lblDescuento100;

    @FXML
    private Label lblRetiros500;

    @FXML
    private Label lblBono1000;

    @FXML
    private ComboBox<Monedero> cmbMonederoBono;

    @FXML
    private TableView<Beneficio> tblBeneficios;

    @FXML
    private TableColumn<Beneficio, String> tbcBeneficioId;

    @FXML
    private TableColumn<Beneficio, String> tbcBeneficioDescripcion;

    @FXML
    private TableColumn<Beneficio, String> tbcBeneficioVence;

    @FXML
    private TableView<MovimientoPuntos> tblMovimientos;

    @FXML
    private TableColumn<MovimientoPuntos, String> tbcMovFecha;

    @FXML
    private TableColumn<MovimientoPuntos, String> tbcMovTipo;

    @FXML
    private TableColumn<MovimientoPuntos, String> tbcMovPuntos;

    @FXML
    private TableColumn<MovimientoPuntos, String> tbcMovDescripcion;

    @FXML
    void onCanjearDescuento() {
        canjear(TipoBeneficio.DESCUENTO_TRANSFERENCIA, null);
    }

    @FXML
    void onCanjearRetiros() {
        canjear(TipoBeneficio.RETIROS_SIN_COMISION, null);
    }

    @FXML
    void onCanjearBono() {
        Monedero monedero = cmbMonederoBono.getValue();
        if (monedero == null) {
            Dialogos.mostrarError("Elige el monedero que va a recibir el bono.");
            return;
        }
        canjear(TipoBeneficio.BONO_SALDO, monedero.getId());
    }

    @FXML
    void onVolver() {
        app.openPanel();
    }

    @FXML
    void initialize() {
        puntosController = new PuntosController(App.contexto.getPuntosServicio(), App.contexto.getMonederoServicio());
        cliente = App.contexto.getClienteActual();
        // El costo y la descripción de cada canje salen del enum, así no se repiten aquí.
        lblDescuento100.setText(textoBeneficio(TipoBeneficio.DESCUENTO_TRANSFERENCIA));
        lblRetiros500.setText(textoBeneficio(TipoBeneficio.RETIROS_SIN_COMISION));
        lblBono1000.setText(textoBeneficio(TipoBeneficio.BONO_SALDO));
        cmbMonederoBono.setConverter(Formato.monederoConSaldo());
        initDataBinding();
        tblBeneficios.setItems(listBeneficios);
        tblMovimientos.setItems(listMovimientos);
        actualizarDatos();
    }

    /** El servicio cobra los puntos y entrega el beneficio; aquí solo se confirma y se muestra el resultado. */
    private void canjear(TipoBeneficio tipo, String idMonederoBono) {
        String pregunta = "Vas a canjear " + tipo.getCostoPuntos() + " puntos por:\n" + tipo.getDescripcion()
                + "\n\n¿Continuar?";
        if (!Dialogos.confirmar(pregunta)) {
            return;
        }
        try {
            Beneficio beneficio = puntosController.canjear(cliente.getIdentificacion(), tipo, idMonederoBono);
            Dialogos.mostrarInfo("Canje realizado: " + beneficio);
            actualizarDatos();
        } catch (MonederoException e) {
            Dialogos.mostrarError(e.getMessage());
        }
    }

    private String textoBeneficio(TipoBeneficio tipo) {
        return tipo.getCostoPuntos() + " puntos\n" + tipo.getDescripcion();
    }

    private void initDataBinding() {
        tbcBeneficioId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        tbcBeneficioDescripcion.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTipo().getDescripcion()));
        // null = no vence por tiempo (el de 100 puntos dura hasta que se usa)
        tbcBeneficioVence.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getFechaVencimiento() == null
                        ? "Hasta usarlo"
                        : Formato.fecha(cellData.getValue().getFechaVencimiento())));

        tbcMovFecha.setCellValueFactory(cellData -> new SimpleStringProperty(Formato.fechaHora(cellData.getValue().getFecha())));
        tbcMovTipo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipo().toString()));
        tbcMovPuntos.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getPuntos())));
        tbcMovDescripcion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescripcion()));
    }

    private void actualizarDatos() {
        lblDisponibles.setText(String.valueOf(cliente.getPuntosDisponibles()));
        lblAcumulados.setText(String.valueOf(cliente.getPuntosAcumulados()));
        try {
            lblRango.setText(puntosController.consultarRango(cliente.getIdentificacion()).toString());
            listBeneficios.setAll(puntosController.beneficiosVigentes(cliente.getIdentificacion()));
            listMovimientos.setAll(puntosController.historialPuntos(cliente.getIdentificacion()));
            cmbMonederoBono.getItems().setAll(puntosController.monederosDe(cliente.getIdentificacion()));
        } catch (MonederoException e) {
            Dialogos.mostrarError(e.getMessage());
        }
    }

    public void setApp(App app) {
        this.app = app;
    }
}
