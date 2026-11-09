package co.edu.uniquindio.uqdelivery.dto;

import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ShipmentHistoryFilterDto(
        @Size(max = 100, message = "La búsqueda no puede superar 100 caracteres")
        String query,

        EstadoEnvio status,

        LocalDate startDate,

        LocalDate endDate,

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
