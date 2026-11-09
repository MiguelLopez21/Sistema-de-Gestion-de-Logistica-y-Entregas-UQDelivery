package co.edu.uniquindio.uqdelivery.dto;

import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.ZonaEntrega;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShipmentHistoryResponseDto(
        Long id,
        String trackingNumber,
        EstadoEnvio status,
        ZonaEntrega origin,
        ZonaEntrega destination,
        LocalDateTime createdAt,
        LocalDateTime deliveredAt,
        BigDecimal totalValue) {

    public static ShipmentHistoryResponseDto from(Envio shipment) {
        return new ShipmentHistoryResponseDto(
                shipment.getId(),
                shipment.getCodigoSeguimiento(),
                shipment.getEstado(),
                shipment.getZonaOrigen(),
                shipment.getZonaDestino(),
                shipment.getFechaCreacion(),
                shipment.getFechaEntrega(),
                shipment.getValorTotal());
    }
}
