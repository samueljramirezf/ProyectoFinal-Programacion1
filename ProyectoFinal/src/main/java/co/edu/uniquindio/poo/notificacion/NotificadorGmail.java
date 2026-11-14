package co.edu.uniquindio.poo.notificacion;
import co.edu.uniquindio.poo.modelo.Alerta;
import co.edu.uniquindio.poo.modelo.Cliente;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
/** Envía las alertas como correos reales a través del servidor SMTP de Gmail. */
public class NotificadorGmail implements Notificador {

    private final Session sesion;
    private final String remitente;

    public NotificadorGmail(String remitente, String claveAplicacion) {
        this.remitente = remitente;
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        this.sesion = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, claveAplicacion);
            }
        });
    }
         @Override
    public void enviar(Cliente cliente, Alerta alerta) {
        try {
            Message correo = new MimeMessage(sesion);
            correo.setFrom(new InternetAddress(remitente));
            correo.setRecipients(Message.RecipientType.TO, InternetAddress.parse(cliente.getCorreo()));
            correo.setSubject("Monedero - Alerta " + alerta.getTipo());
            correo.setText("Hola " + cliente.getNombre() + ",\n\n"
                    + alerta.getMensaje() + "\n\nFecha: " + alerta.getFecha());
            Transport.send(correo);
            System.out.println("Correo enviado a " + cliente.getCorreo());
        } catch (MessagingException e) {
            // Si falla el envío (sin internet, clave mala...), la operación del monedero NO se cancela.
            System.err.println("No se pudo enviar el correo a " + cliente.getCorreo() + ": " + e.getMessage());
        }
    }
}




