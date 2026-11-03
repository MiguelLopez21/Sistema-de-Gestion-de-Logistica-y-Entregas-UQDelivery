package co.edu.uniquindio.uqdelivery.exception;

/** El pago simulado fue rechazado; el mensaje es el motivo que se muestra al cliente. */
public class PagoRechazadoException extends RuntimeException {
    public PagoRechazadoException(String motivo) {
        super(motivo);
    }
}
