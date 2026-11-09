package co.edu.uniquindio.uqdelivery.dto;

import co.edu.uniquindio.uqdelivery.service.CotizacionService.Cotizacion;
import co.edu.uniquindio.uqdelivery.service.CotizacionService.TipoServicio;

import java.math.BigDecimal;

/** HU-05: desglose de la cotización devuelto al cliente. */
public record CotizacionResponse(
        double distanciaKm,
        double pesoRealKg,
        double pesoFacturableKg,
        TipoServicio tipoServicio,
        BigDecimal costoBase,
        BigDecimal costoDistancia,
        BigDecimal costoPeso,
        BigDecimal recargoServicio,
        BigDecimal subtotal,
        BigDecimal iva,
        BigDecimal total) {

    public static CotizacionResponse desde(CotizacionRequest r, double distanciaKm, Cotizacion c) {
        return new CotizacionResponse(
                distanciaKm,
                r.pesoKg(),
                c.pesoFacturableKg(),
                r.tipoServicio(),
                c.costoBase(),
                c.costoDistancia(),
                c.costoPeso(),
                c.recargoServicio(),
                c.subtotal(),
                c.iva(),
                c.total());
    }
}
