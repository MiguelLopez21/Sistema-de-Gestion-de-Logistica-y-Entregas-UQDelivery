package co.edu.uniquindio.uqdelivery.exception;

import java.util.Map;

public class DatosInvalidosException extends RuntimeException {

    private final Map<String, String> errores;

    public DatosInvalidosException(Map<String, String> errores) {
        super("Datos de entrada inválidos");
        this.errores = Map.copyOf(errores);
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}
