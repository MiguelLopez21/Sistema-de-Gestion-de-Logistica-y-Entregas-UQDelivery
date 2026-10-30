package co.edu.uniquindio.uqdelivery.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AsignacionRequest(

        @NotNull(message = "El repartidor es obligatorio")
        @Positive(message = "El identificador del repartidor no es válido")
        Long repartidorId,

        @Size(max = 255, message = "El motivo no puede superar 255 caracteres")
        @Pattern(regexp = ReglasValidacion.MOTIVO, message = ReglasValidacion.MOTIVO_MSG)
        String motivo) {
}
