package co.edu.uniquindio.uqdelivery.dto;

import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ShipmentHistoryFilterDto(
        @Positive(message = "El id de usuario debe ser mayor a cero")
        Long userId,

        EstadoEnvio status,

        LocalDateTime startDate,

        LocalDateTime endDate,

        @Size(max = 20, message = "El código de seguimiento no puede superar 20 caracteres")
        String trackingNumber,

        @Min(value = 0, message = "La página no puede ser negativa")
        int page,

        @Min(value = 1, message = "El tamaño de página debe ser mayor a cero")
        @Max(value = 100, message = "El tamaño de página no puede superar 100")
        int size) {

    @AssertTrue(message = "La fecha inicial no puede ser posterior a la fecha final")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !startDate.isAfter(endDate);
    }
}
