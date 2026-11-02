package co.edu.uniquindio.uqdelivery.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EntregaFila(
        Long repartidorId,
        String repartidorNombre,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaEntrega,
        BigDecimal valorTotal) {
}
