package co.edu.uniquindio.uqdelivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DatosPagoRequest(
        @NotBlank(message = "El número de tarjeta es obligatorio")
        @Pattern(regexp = "^[0-9]{16}$", message = "La tarjeta debe tener 16 dígitos")
        String numeroTarjeta,

        @NotBlank(message = "El titular es obligatorio")
        @Size(max = 100, message = "El titular no puede superar 100 caracteres")
        @Pattern(regexp = ReglasValidacion.NOMBRE_PERSONA, message = ReglasValidacion.NOMBRE_PERSONA_MSG)
        String titular) {

    /** Evita que el número completo de tarjeta aparezca en logs. */
    @Override
    public String toString() {
        return "DatosPagoRequest[titular=" + titular + ", numeroTarjeta=****]";
    }
}
