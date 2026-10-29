package co.edu.uniquindio.uqdelivery.dto;

import co.edu.uniquindio.uqdelivery.model.Rol;
import jakarta.validation.constraints.NotNull;

public record CambioRolRequest(
        @NotNull(message = "El rol es obligatorio")
        Rol rol) {
}