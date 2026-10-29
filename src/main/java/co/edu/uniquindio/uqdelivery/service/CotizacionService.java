package co.edu.uniquindio.uqdelivery.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *  HU-05 Cotizacion automatica de envios
 *
 * */
@Service
public class CotizacionService {

    // Tarifa en pesos colombianos
    private static final BigDecimal TARIFA_BASE = new BigDecimal("4000");
    private static final BigDecimal COSTO_POR_KM = new BigDecimal("1200");
    private static final BigDecimal COSTO_POR_KG = new BigDecimal("800");
    private static final BigDecimal IVA = new BigDecimal("0.19");

    // Divisor del peso estandar en mensajería
    private static final double DIVISOR_VOLUMETRICO = 5000.0;

    // Limitaciones del envio
    private static final double DISTANCIA_MAXIMA_KM = 50.0;
    private static final double PESO_MAXIMO_KG = 30.0;

    // Tipos de servicio y multiplicador
    public enum TipoServicio {
        ESTANDAR(new BigDecimal("1.00")),
        EXPRESS(new BigDecimal("1.60"));

        private final BigDecimal factor;

        TipoServicio(BigDecimal factor) {
            this.factor = factor;
        }

        public BigDecimal getFactor() {
            return factor;
        }
    }

    // Datos que el usuario debe de ingresar
    public record SolicitudCotizacion(
            double distanciaKm,
            double pesoKg,
            double largoCm,
            double anchoCm,
            double altoCm,
            TipoServicio tipoServicio) {
    }

    // Resumen de la cotizacion generada
    public record Cotizacion(
            double pesoFacturableKg,
            BigDecimal costoBase,
            BigDecimal costoDistancia,
            BigDecimal costoPeso,
            BigDecimal recargoServicio,
            BigDecimal subtotal,
            BigDecimal iva,
            BigDecimal total) {
    }

    /**
     * Calcula la cotizacion
     *
     * @throws IllegalArgumentException  Si los datos son invalidos o pasan los limites establecidos
     */
    public Cotizacion cotizar(SolicitudCotizacion solicitud) {
        validar(solicitud);

        double pesoFacturable = calcularPesoFacturable(solicitud);

        BigDecimal costoDistancia = COSTO_POR_KM.multiply(BigDecimal.valueOf(solicitud.distanciaKm()));
        BigDecimal costoPeso = COSTO_POR_KG.multiply(BigDecimal.valueOf(pesoFacturable));
        BigDecimal subtotalEstandar = TARIFA_BASE.add(costoDistancia).add(costoPeso);

        BigDecimal subtotal = subtotalEstandar.multiply(solicitud.tipoServicio().getFactor());
        BigDecimal recargo = subtotal.subtract(subtotalEstandar);
        BigDecimal iva = subtotal.multiply(IVA);
        BigDecimal total = subtotal.add(iva);

        return new Cotizacion(
                pesoFacturable,
                redondear(TARIFA_BASE),
                redondear(costoDistancia),
                redondear(costoPeso),
                redondear(recargo),
                redondear(subtotal),
                redondear(iva),
                redondear(total));
    }

    /** El peso facturable es el mayor entre el peso real y el del volumen. */
    public double calcularPesoFacturable(SolicitudCotizacion s) {
        double volumetrico = (s.largoCm() * s.anchoCm() * s.altoCm()) / DIVISOR_VOLUMETRICO;
        return Math.max(s.pesoKg(), volumetrico);
    }

    private void validar(SolicitudCotizacion s) {
        if (s == null) {
            throw new IllegalArgumentException("La solicitud de cotización no puede ser nula");
        }
        if (s.tipoServicio() == null) {
            throw new IllegalArgumentException("El tipo de servicio es obligatorio");
        }
        if (s.distanciaKm() <= 0) {
            throw new IllegalArgumentException("La distancia debe ser mayor a 0");
        }
        if (s.distanciaKm() > DISTANCIA_MAXIMA_KM) {
            throw new IllegalArgumentException("La distancia excede el máximo de " + DISTANCIA_MAXIMA_KM + " km");
        }
        if (s.pesoKg() <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor a 0");
        }
        if (s.largoCm() <= 0 || s.anchoCm() <= 0 || s.altoCm() <= 0) {
            throw new IllegalArgumentException("Las dimensiones deben ser mayores a 0");
        }
        if (calcularPesoFacturable(s) > PESO_MAXIMO_KG) {
            throw new IllegalArgumentException("El peso facturable excede el máximo de " + PESO_MAXIMO_KG + " kg");
        }
    }

    private BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(0, RoundingMode.HALF_UP);
    }
}