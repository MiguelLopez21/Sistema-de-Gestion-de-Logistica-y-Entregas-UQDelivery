package co.edu.uniquindio.uqdelivery.dto;

import co.edu.uniquindio.uqdelivery.model.ZonaEntrega;
import co.edu.uniquindio.uqdelivery.service.CotizacionService.TipoServicio;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record CotizacionRequest(
        @NotNull(message = "La zona de origen es obligatoria")
        ZonaEntrega zonaOrigen,

        @NotNull(message = "La zona de destino es obligatoria")
        ZonaEntrega zonaDestino,

        @NotNull(message = "El peso es obligatorio")
        @Positive(message = "El peso debe ser mayor a 0")
        @DecimalMax(value = "30", message = "El peso excede el máximo de 30 kg")
        Double pesoKg,

        @NotNull(message = "El largo es obligatorio")
        @Positive(message = "El largo debe ser mayor a 0")
        @DecimalMax(value = "200", message = "El largo excede 200 cm")
        Double largoCm,

        @NotNull(message = "El ancho es obligatorio")
        @Positive(message = "El ancho debe ser mayor a 0")
        @DecimalMax(value = "200", message = "El ancho excede 200 cm")
        Double anchoCm,

        @NotNull(message = "El alto es obligatorio")
        @Positive(message = "El alto debe ser mayor a 0")
        @DecimalMax(value = "200", message = "El alto excede 200 cm")
        Double altoCm,

        @NotNull(message = "El tipo de servicio es obligatorio")
        TipoServicio tipoServicio) {
}
