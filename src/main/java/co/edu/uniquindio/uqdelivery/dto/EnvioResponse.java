package co.edu.uniquindio.uqdelivery.dto;

import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;

public record EnvioResponse(
        Long id,
        EstadoEnvio estado,
        Long repartidorId,
        String repartidorNombre) {

    public static EnvioResponse desde(Envio e) {
        boolean asignado = e.getRepartidor() != null;
        return new EnvioResponse(
                e.getId(),
                e.getEstado(),
                asignado ? e.getRepartidor().getId() : null,
                asignado ? e.getRepartidor().getNombre() : null);
    }
}
