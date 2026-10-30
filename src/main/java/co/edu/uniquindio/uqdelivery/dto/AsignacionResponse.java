package co.edu.uniquindio.uqdelivery.dto;

import co.edu.uniquindio.uqdelivery.model.AsignacionEnvio;

import java.time.LocalDateTime;

public record AsignacionResponse(
        Long id,
        Long envioId,
        Long repartidorAnteriorId,
        Long repartidorNuevoId,
        String administrador,
        LocalDateTime fecha,
        String motivo) {

    public static AsignacionResponse desde(AsignacionEnvio a) {
        return new AsignacionResponse(
                a.getId(),
                a.getEnvio().getId(),
                a.getRepartidorAnterior() != null ? a.getRepartidorAnterior().getId() : null,
                a.getRepartidorNuevo().getId(),
                a.getAdministrador().getCorreo(),
                a.getFecha(),
                a.getMotivo());
    }
}
