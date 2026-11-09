package co.edu.uniquindio.uqdelivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelShipmentRequestDto(
        @NotBlank(message = "El motivo de cancelación es obligatorio")
        @Size(max = 255, message = "El motivo de cancelación no puede superar 255 caracteres")
        String reason) {
}
