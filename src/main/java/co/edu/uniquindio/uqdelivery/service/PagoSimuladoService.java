package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.exception.PagoRechazadoException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Pasarela de pago SIMULADA. Reglas determinísticas por los últimos 4 dígitos de la tarjeta:
 * 0000 → tarjeta rechazada por el emisor; 1111 → fondos insuficientes; cualquier otra → aprobada.
 * No guarda ni registra el número de tarjeta.
 */
@Service
public class PagoSimuladoService {

    /** @return referencia de la transacción aprobada. @throws PagoRechazadoException con el motivo. */
    public String cobrar(String numeroTarjeta, BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new PagoRechazadoException("Monto inválido para el pago");
        }
        if (numeroTarjeta == null || !numeroTarjeta.matches("[0-9]{16}")) {
            throw new PagoRechazadoException("Pago rechazado: número de tarjeta inválido");
        }
        String ultimos = numeroTarjeta.substring(numeroTarjeta.length() - 4);
        switch (ultimos) {
            case "0000" -> throw new PagoRechazadoException("Pago rechazado: tarjeta rechazada por el emisor");
            case "1111" -> throw new PagoRechazadoException("Pago rechazado: fondos insuficientes");
            default -> { }
        }
        return "PAG-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    /**
     * Anula (reembolsa) un cobro aprobado cuando la solicitud no pudo confirmarse.
     * En la simulación no hay pasarela real: es el punto de extensión para el reembolso.
     */
    public void anular(String referencia) {
        // Simulación: sin efecto externo.
    }
}
