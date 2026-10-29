package co.edu.uniquindio.uqdelivery.exception;

/** Violación de una regla de negocio (correo duplicado, último administrador, etc.). */
public class ConflictoException extends RuntimeException {
  public ConflictoException(String mensaje) {
    super(mensaje);
  }
}
