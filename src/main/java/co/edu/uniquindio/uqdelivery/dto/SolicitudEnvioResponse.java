package co.edu.uniquindio.uqdelivery.dto;

import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;

import java.math.BigDecimal;

public record SolicitudEnvioResponse(
        Long id,
        String codigoSeguimiento,
        EstadoEnvio estado,
        BigDecimal valorCotizado,
        String referenciaPago) {

    public static SolicitudEnvioResponse desde(Envio e) {
        return new SolicitudEnvioResponse(
                e.getId(), e.getCodigoSeguimiento(), e.getEstado(), e.getValorCotizado(), e.getReferenciaPago());
    }
}
