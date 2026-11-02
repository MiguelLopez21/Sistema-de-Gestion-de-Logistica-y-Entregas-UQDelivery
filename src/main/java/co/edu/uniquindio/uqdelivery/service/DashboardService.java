package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.dto.AgrupacionPeriodo;
import co.edu.uniquindio.uqdelivery.dto.ConteoEstadoFila;
import co.edu.uniquindio.uqdelivery.dto.DashboardResponse;
import co.edu.uniquindio.uqdelivery.dto.DashboardResponse.DesempenoRepartidor;
import co.edu.uniquindio.uqdelivery.dto.DashboardResponse.EstadoTotal;
import co.edu.uniquindio.uqdelivery.dto.DashboardResponse.PeriodoEntregas;
import co.edu.uniquindio.uqdelivery.dto.EntregaFila;
import co.edu.uniquindio.uqdelivery.exception.ConflictoException;
import co.edu.uniquindio.uqdelivery.exception.ParametroInvalidoException;
import co.edu.uniquindio.uqdelivery.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.EnvioRepository;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final int DIAS_POR_DEFECTO = 30;
    private static final long DIAS_MAXIMOS = 366;
    private static final double SEGUNDOS_POR_HORA = 3600.0;

    private final EnvioRepository envioRepository;
    private final UsuarioRepository usuarioRepository;

    public DashboardService(EnvioRepository envioRepository, UsuarioRepository usuarioRepository) {
        this.envioRepository = envioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse obtener(LocalDate desde, LocalDate hasta, Long repartidorId, AgrupacionPeriodo agrupacion) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.minusDays(DIAS_POR_DEFECTO - 1);
        validarRango(inicio, fin);
        validarRepartidor(repartidorId);

        LocalDateTime limiteInferior = inicio.atStartOfDay();
        LocalDateTime limiteSuperior = fin.plusDays(1).atStartOfDay();

        List<ConteoEstadoFila> conteos = envioRepository.contarPorEstado(limiteInferior, limiteSuperior, repartidorId);
        List<EntregaFila> entregas = envioRepository.listarEntregas(
                EstadoEnvio.ENTREGADO, limiteInferior, limiteSuperior, repartidorId);

        return new DashboardResponse(
                inicio,
                fin,
                repartidorId,
                agrupacion,
                enviosPorEstado(conteos),
                entregasPorPeriodo(entregas, inicio, fin, agrupacion),
                (long) entregas.size(),
                promedioHoras(entregas),
                desempenoPorRepartidor(entregas),
                sumarValores(entregas));
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            throw new ParametroInvalidoException("La fecha inicial no puede ser posterior a la fecha final");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) + 1 > DIAS_MAXIMOS) {
            throw new ParametroInvalidoException("El rango de fechas no puede superar " + DIAS_MAXIMOS + " días");
        }
    }

    private void validarRepartidor(Long repartidorId) {
        if (repartidorId == null) {
            return;
        }
        Usuario repartidor = usuarioRepository.findById(repartidorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Repartidor con id " + repartidorId + " no encontrado"));
        if (repartidor.getRol() != Rol.REPARTIDOR) {
            throw new ConflictoException("El usuario seleccionado no tiene rol de repartidor");
        }
    }

    private List<EstadoTotal> enviosPorEstado(List<ConteoEstadoFila> conteos) {
        Map<EstadoEnvio, Long> totales = conteos.stream()
                .collect(Collectors.toMap(ConteoEstadoFila::estado, ConteoEstadoFila::total, Long::sum));
        List<EstadoTotal> resultado = new ArrayList<>();
        for (EstadoEnvio estado : EstadoEnvio.values()) {
            resultado.add(new EstadoTotal(estado, totales.getOrDefault(estado, 0L)));
        }
        return resultado;
    }

    private List<PeriodoEntregas> entregasPorPeriodo(List<EntregaFila> entregas, LocalDate desde, LocalDate hasta,
                                                     AgrupacionPeriodo agrupacion) {
        Map<LocalDate, List<EntregaFila>> porPeriodo = entregas.stream()
                .collect(Collectors.groupingBy(f -> agrupacion.inicio(f.fechaEntrega().toLocalDate())));
        List<PeriodoEntregas> serie = new ArrayList<>();
        for (LocalDate periodo = agrupacion.inicio(desde); !periodo.isAfter(hasta); periodo = agrupacion.siguiente(periodo)) {
            List<EntregaFila> grupo = porPeriodo.getOrDefault(periodo, List.of());
            serie.add(new PeriodoEntregas(periodo, (long) grupo.size(), sumarValores(grupo)));
        }
        return serie;
    }

    private List<DesempenoRepartidor> desempenoPorRepartidor(List<EntregaFila> entregas) {
        Map<Long, List<EntregaFila>> porRepartidor = entregas.stream()
                .filter(f -> f.repartidorId() != null)
                .collect(Collectors.groupingBy(EntregaFila::repartidorId));
        return porRepartidor.values().stream()
                .map(grupo -> new DesempenoRepartidor(
                        grupo.get(0).repartidorId(),
                        grupo.get(0).repartidorNombre(),
                        (long) grupo.size(),
                        promedioHoras(grupo),
                        sumarValores(grupo)))
                .sorted(Comparator.comparing(DesempenoRepartidor::entregas).reversed()
                        .thenComparing(DesempenoRepartidor::repartidor))
                .toList();
    }

    private Double promedioHoras(List<EntregaFila> entregas) {
        OptionalDouble promedio = entregas.stream()
                .mapToDouble(f -> Duration.between(f.fechaCreacion(), f.fechaEntrega()).toSeconds() / SEGUNDOS_POR_HORA)
                .average();
        return promedio.isPresent() ? Math.round(promedio.getAsDouble() * 100.0) / 100.0 : null;
    }

    private BigDecimal sumarValores(List<EntregaFila> entregas) {
        return entregas.stream()
                .map(EntregaFila::valorTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
