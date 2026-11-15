package co.edu.uniquindio.poo.viewController;

import co.edu.uniquindio.poo.App;
import co.edu.uniquindio.poo.controller.OperacionController;
import co.edu.uniquindio.poo.excepcion.MonederoException;
import co.edu.uniquindio.poo.modelo.Categoria;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.TipoTransaccion;
import co.edu.uniquindio.poo.modelo.Transaccion;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.List;

public class OperacionesViewController {

    private OperacionController operacionController;
    private Cliente cliente;
    private App app;

    @FXML
    private ComboBox<TipoTransaccion> cmbTipo;

    @FXML
    private Label lblMonedero;

    @FXML
    private ComboBox<Monedero> cmbMonedero;

    @FXML
    private VBox boxTransferencia;

    @FXML
    private RadioButton rbPropio;

    @FXML
    private RadioButton rbOtroCliente;

    @FXML
    private HBox boxOtroCliente;

    @FXML
    private TextField txtIdOtroCliente;

    @FXML
    private ComboBox<Monedero> cmbDestino;

    @FXML
    private HBox boxCategoria;

    @FXML
    private ComboBox<Categoria> cmbCategoria;

    @FXML
    private TextField txtMonto;

    @FXML
    private TextField txtDescripcion;

    @FXML
    private Label lblComision;

    // ---------------- Eventos ----------------

    /** Muestra solo los campos que necesita cada tipo de operación. */
    @FXML
    void onTipoCambiado() {
        TipoTransaccion tipo = cmbTipo.getValue();
        boolean esTransferencia = tipo == TipoTransaccion.TRANSFERENCIA;
        mostrar(boxTransferencia, esTransferencia);
        mostrar(boxCategoria, tipo != null && tipo != TipoTransaccion.DEPOSITO);
        lblMonedero.setText(tipo == TipoTransaccion.DEPOSITO ? "Monedero que recibe" : "Monedero de origen");
        if (esTransferencia) {
            onDestinoCambiado();
        }
        limpiarComision();
    }

    /** Destino propio: se listan mis monederos. Otro cliente: hay que buscarlo por su identificación. */
    @FXML
    void onDestinoCambiado() {
        boolean otroCliente = rbOtroCliente.isSelected();
        mostrar(boxOtroCliente, otroCliente);
        cmbDestino.getItems().clear();
        if (!otroCliente) {
            cargarMonederos(cliente.getIdentificacion(), cmbDestino);
        }
        limpiarComision();
    }

    @FXML
    void onBuscarCliente() {
        String idOtro = txtIdOtroCliente.getText().trim();
        if (idOtro.isEmpty()) {
            Dialogos.mostrarError("Escribe la identificación del cliente al que le vas a transferir.");
            return;
        }
        cargarMonederos(idOtro, cmbDestino);
        if (cmbDestino.getItems().isEmpty()) {
            Dialogos.mostrarInfo("Ese cliente no tiene monederos para recibir transferencias.");
        }
    }

    @FXML
    void onCampoCambiado() {
        limpiarComision();
    }

    @FXML
    void onCalcularComision() {
        BigDecimal monto = validarFormulario();
        if (monto == null) {
            return;
        }
        try {
            lblComision.setText("Comisión: " + Formato.dinero(cotizar(monto)));
        } catch (MonederoException e) {
            Dialogos.mostrarError(e.getMessage());
        }
    }

    /** Pide la comisión al servicio, la muestra para confirmar y solo entonces ejecuta la operación. */
    @FXML
    void onRealizarOperacion() {
        BigDecimal monto = validarFormulario();
        if (monto == null) {
            return;
        }
        TipoTransaccion tipo = cmbTipo.getValue();
        try {
            BigDecimal comision = cotizar(monto);
            lblComision.setText("Comisión: " + Formato.dinero(comision));
            String resumen = tipo + " de " + Formato.dinero(monto)
                    + "\nComisión: " + Formato.dinero(comision)
                    + "\n\n¿Confirmas la operación?";
            if (!Dialogos.confirmar(resumen)) {
                return;
            }
            Transaccion transaccion = ejecutar(tipo, monto);
            String mensaje = "Operación " + transaccion.getId() + " realizada.";
            if (transaccion.isSospechosa()) {
                mensaje += "\nSe marcó como SOSPECHOSA: el monto se sale de lo habitual.";
            }
            Dialogos.mostrarInfo(mensaje);
            limpiarFormulario();
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
        operacionController = new OperacionController(App.contexto.getMonederoServicio());
        cliente = App.contexto.getClienteActual();
        cmbTipo.getItems().setAll(TipoTransaccion.values());
        cmbCategoria.getItems().setAll(Categoria.values());
        cmbMonedero.setConverter(Formato.monederoConSaldo());
        cmbDestino.setConverter(Formato.monederoConPropietario());
        cargarMonederos(cliente.getIdentificacion(), cmbMonedero);
        mostrar(boxTransferencia, false);
        mostrar(boxCategoria, false);
    }

    // ---------------- Apoyo ----------------

    /**
     * Validaciones de pantalla: que no falte nada y que el monto sea un número.
     * Si el monto es 0 o negativo lo rechaza el modelo (MontoInvalidoException), no la vista.
     * Devuelve el monto leído, o null si algo falta (ya se mostró el mensaje).
     */
    private BigDecimal validarFormulario() {
        TipoTransaccion tipo = cmbTipo.getValue();
        if (tipo == null) {
            Dialogos.mostrarError("Elige el tipo de operación.");
            return null;
        }
        if (cmbMonedero.getValue() == null) {
            Dialogos.mostrarError("Elige el monedero.");
            return null;
        }
        if (tipo == TipoTransaccion.TRANSFERENCIA && cmbDestino.getValue() == null) {
            Dialogos.mostrarError("Elige el monedero destino.");
            return null;
        }
        if (tipo != TipoTransaccion.DEPOSITO && cmbCategoria.getValue() == null) {
            Dialogos.mostrarError("Elige la categoría.");
            return null;
        }
        String texto = txtMonto.getText().trim().replace(',', '.');
        if (texto.isEmpty()) {
            Dialogos.mostrarError("Escribe el monto.");
            return null;
        }
        try {
            return new BigDecimal(texto);
        } catch (NumberFormatException e) {
            Dialogos.mostrarError("El monto \"" + txtMonto.getText().trim() + "\" no es un número válido.");
            return null;
        }
    }

    private BigDecimal cotizar(BigDecimal monto) throws MonederoException {
        String idDestino = cmbDestino.getValue() != null ? cmbDestino.getValue().getId() : null;
        return operacionController.cotizarComision(cmbTipo.getValue(), cmbMonedero.getValue().getId(), idDestino, monto);
    }

    private Transaccion ejecutar(TipoTransaccion tipo, BigDecimal monto) throws MonederoException {
        String idOrigen = cmbMonedero.getValue().getId();
        String descripcion = txtDescripcion.getText().trim();
        return switch (tipo) {
            case DEPOSITO -> operacionController.depositar(idOrigen, monto, descripcion);
            case RETIRO -> operacionController.retirar(idOrigen, monto, cmbCategoria.getValue(), descripcion);
            case TRANSFERENCIA -> operacionController.transferir(idOrigen, cmbDestino.getValue().getId(), monto,
                    cmbCategoria.getValue(), descripcion);
        };
    }

    private void cargarMonederos(String idCliente, ComboBox<Monedero> combo) {
        try {
            List<Monedero> monederos = operacionController.monederosDe(idCliente);
            combo.getItems().setAll(monederos);
        } catch (MonederoException e) {
            combo.getItems().clear();
            Dialogos.mostrarError(e.getMessage());
        }
    }

    /** Tras una operación se recargan los monederos para que los saldos del selector estén al día. */
    private void limpiarFormulario() {
        String idSeleccionado = cmbMonedero.getValue().getId();
        cargarMonederos(cliente.getIdentificacion(), cmbMonedero);
        cmbMonedero.getItems().stream()
                .filter(m -> m.getId().equals(idSeleccionado))
                .findFirst()
                .ifPresent(cmbMonedero::setValue);
        txtMonto.clear();
        txtDescripcion.clear();
        limpiarComision();
    }

    private void limpiarComision() {
        lblComision.setText("");
    }

    /** visible + managed: el nodo oculto tampoco ocupa espacio. */
    private void mostrar(Node nodo, boolean visible) {
        nodo.setVisible(visible);
        nodo.setManaged(visible);
    }

    public void setApp(App app) {
        this.app = app;
    }
}
