package co.edu.uniquindio.uqdelivery.exception;

/** Regla de negocio de la solicitud incumplida (p. ej. peso o distancia fuera de límites). */
public class SolicitudInvalidaException extends RuntimeException {
    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
