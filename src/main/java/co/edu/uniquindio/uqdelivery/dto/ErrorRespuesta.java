package co.edu.uniquindio.uqdelivery.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorRespuesta(
        int estado,
        String mensaje,
        Map<String, String> errores,
        LocalDateTime fecha) {

    public static ErrorRespuesta de(int estado, String mensaje) {
        return new ErrorRespuesta(estado, mensaje, Map.of(), LocalDateTime.now());
    }

    public static ErrorRespuesta de(int estado, String mensaje, Map<String, String> errores) {
        return new ErrorRespuesta(estado, mensaje, errores, LocalDateTime.now());
    }
}
