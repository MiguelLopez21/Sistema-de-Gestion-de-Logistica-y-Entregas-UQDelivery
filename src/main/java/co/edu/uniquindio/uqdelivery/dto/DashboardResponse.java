package co.edu.uniquindio.uqdelivery.dto;

import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DashboardResponse(
        LocalDate desde,
        LocalDate hasta,
        Long repartidorId,
        AgrupacionPeriodo agrupacion,
        List<EstadoTotal> enviosPorEstado,
        List<PeriodoEntregas> entregasPorPeriodo,
        Long totalEntregas,
        Double tiempoPromedioEntregaHoras,
        List<DesempenoRepartidor> desempenoPorRepartidor,
        BigDecimal ingresosTotales) {

    public record EstadoTotal(EstadoEnvio estado, Long total) {
    }

    public record PeriodoEntregas(LocalDate inicio, Long entregas, BigDecimal ingresos) {
    }

    public record DesempenoRepartidor(
            Long repartidorId,
            String repartidor,
            Long entregas,
            Double tiempoPromedioHoras,
            BigDecimal ingresos) {
    }
}
