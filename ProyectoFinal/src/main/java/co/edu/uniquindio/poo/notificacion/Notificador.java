package co.edu.uniquindio.poo.notificacion;
import co.edu.uniquindio.poo.modelo.Alerta;
import co.edu.uniquindio.poo.modelo.Cliente;


/** Permite enviar alertas por cualquier canal sin que AlertasServicio sepa cuál es:
 *  agregar o cambiar un canal (Gmail, SMS, WhatsApp...) no obliga a tocar el servicio. */
public interface Notificador {
    public void enviar(Cliente cliente, Alerta alerta);

}
