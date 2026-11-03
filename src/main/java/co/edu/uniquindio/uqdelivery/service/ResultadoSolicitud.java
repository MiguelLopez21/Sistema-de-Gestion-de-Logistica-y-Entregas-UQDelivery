package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.dto.SolicitudEnvioResponse;

/** {@code creado=false} indica que se devolvió el envío de una solicitud previa con la misma Idempotency-Key. */
public record ResultadoSolicitud(SolicitudEnvioResponse envio, boolean creado) {
}
