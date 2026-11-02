package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.dto.AgrupacionPeriodo;
import co.edu.uniquindio.uqdelivery.dto.ConteoEstadoFila;
import co.edu.uniquindio.uqdelivery.dto.DashboardResponse;
import co.edu.uniquindio.uqdelivery.dto.EntregaFila;
import co.edu.uniquindio.uqdelivery.exception.ConflictoException;
import co.edu.uniquindio.uqdelivery.exception.ParametroInvalidoException;
import co.edu.uniquindio.uqdelivery.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.EnvioRepository;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final LocalDate DESDE = LocalDate.of(2026, 10, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 10, 3);

    @Mock
    private EnvioRepository envioRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @InjectMocks
    private DashboardService service;

    private void datos(List<ConteoEstadoFila> conteos, List<EntregaFila> entregas) {
        when(envioRepository.contarPorEstado(any(), any(), any())).thenReturn(conteos);
        when(envioRepository.listarEntregas(any(), any(), any(), any())).thenReturn(entregas);
    }

    private EntregaFila fila(Long repartidorId, String nombre, LocalDateTime creacion, LocalDateTime entrega, String valor) {
        return new EntregaFila(repartidorId, nombre, creacion, entrega, valor == null ? null : new BigDecimal(valor));
    }

    private EntregaFila entregaEl(LocalDate dia, String valor) {
        LocalDateTime entrega = dia.atTime(12, 0);
        return fila(1L, "Ana", entrega.minusHours(2), entrega, valor);
    }

    private void assertValor(String esperado, BigDecimal actual) {
        assertEquals(0, new BigDecimal(esperado).compareTo(actual));
    }

    private Usuario usuario(Long id, Rol rol, boolean activo) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setRol(rol);
        u.setActivo(activo);
        return u;
    }

    @Test
    void sinFechasUsaLosUltimos30DiasTerminandoHoy() {
        datos(List.of(), List.of());
        LocalDate hoy = LocalDate.now();

        DashboardResponse r = service.obtener(null, null, null, AgrupacionPeriodo.DIA);

        assertEquals(hoy.minusDays(29), r.desde());
        assertEquals(hoy, r.hasta());
        assertEquals(30, r.entregasPorPeriodo().size());
        verify(envioRepository).contarPorEstado(hoy.minusDays(29).atStartOfDay(), hoy.plusDays(1).atStartOfDay(), null);
    }

    @Test
    void soloConDesdeUsaHoyComoFin() {
        datos(List.of(), List.of());

        DashboardResponse r = service.obtener(LocalDate.now().minusDays(2), null, null, AgrupacionPeriodo.DIA);

        assertEquals(LocalDate.now(), r.hasta());
        assertEquals(3, r.entregasPorPeriodo().size());
    }

    @Test
    void consultaElRangoCompletoIncluyendoElUltimoDia() {
        datos(List.of(), List.of());

        service.obtener(DESDE, HASTA, null, AgrupacionPeriodo.DIA);

        verify(envioRepository).contarPorEstado(DESDE.atStartOfDay(), HASTA.plusDays(1).atStartOfDay(), null);
        verify(envioRepository).listarEntregas(
                EstadoEnvio.ENTREGADO, DESDE.atStartOfDay(), HASTA.plusDays(1).atStartOfDay(), null);
    }

    @Test
    void enviosPorEstadoIncluyeTodosLosEstadosConCeroSiNoHayDatos() {
        datos(List.of(new ConteoEstadoFila(EstadoEnvio.PENDIENTE, 4L), new ConteoEstadoFila(EstadoEnvio.ENTREGADO, 2L)),
                List.of());

        DashboardResponse r = service.obtener(DESDE, HASTA, null, AgrupacionPeriodo.DIA);

        assertEquals(EstadoEnvio.values().length, r.enviosPorEstado().size());
        r.enviosPorEstado().forEach(e -> {
            long esperado = switch (e.estado()) {
                case PENDIENTE -> 4L;
                case ENTREGADO -> 2L;
                default -> 0L;
            };
            assertEquals(esperado, e.total());
        });
    }

    @Test
    void entregasPorDiaRellenaLosDiasSinEntregas() {
        datos(List.of(), List.of(
                entregaEl(LocalDate.of(2026, 10, 1), "10000"),
                entregaEl(LocalDate.of(2026, 10, 1), "5000"),
                entregaEl(LocalDate.of(2026, 10, 3), null)));

        DashboardResponse r = service.obtener(DESDE, HASTA, null, AgrupacionPeriodo.DIA);

        assertEquals(3, r.entregasPorPeriodo().size());
        assertEquals(LocalDate.of(2026, 10, 1), r.entregasPorPeriodo().get(0).inicio());
        assertEquals(2L, r.entregasPorPeriodo().get(0).entregas());
        assertValor("15000", r.entregasPorPeriodo().get(0).ingresos());
        assertEquals(0L, r.entregasPorPeriodo().get(1).entregas());
        assertValor("0", r.entregasPorPeriodo().get(1).ingresos());
        assertEquals(1L, r.entregasPorPeriodo().get(2).entregas());
        assertValor("0", r.entregasPorPeriodo().get(2).ingresos());
    }

    @Test
    void entregasPorSemanaAgrupaDesdeElLunes() {
        datos(List.of(), List.of(
                entregaEl(LocalDate.of(2026, 10, 1), "1000"),
                entregaEl(LocalDate.of(2026, 10, 6), "1000"),
                entregaEl(LocalDate.of(2026, 10, 7), "1000"),
                entregaEl(LocalDate.of(2026, 10, 13), "1000")));

        DashboardResponse r = service.obtener(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 14), null, AgrupacionPeriodo.SEMANA);

        assertEquals(3, r.entregasPorPeriodo().size());
        assertEquals(LocalDate.of(2026, 9, 28), r.entregasPorPeriodo().get(0).inicio());
        assertEquals(1L, r.entregasPorPeriodo().get(0).entregas());
        assertEquals(LocalDate.of(2026, 10, 5), r.entregasPorPeriodo().get(1).inicio());
        assertEquals(2L, r.entregasPorPeriodo().get(1).entregas());
        assertEquals(LocalDate.of(2026, 10, 12), r.entregasPorPeriodo().get(2).inicio());
        assertEquals(1L, r.entregasPorPeriodo().get(2).entregas());
    }

    @Test
    void entregasPorMesAgrupaPorMesCalendario() {
        datos(List.of(), List.of(
                entregaEl(LocalDate.of(2026, 9, 20), "1000"),
                entregaEl(LocalDate.of(2026, 10, 10), "1000"),
                entregaEl(LocalDate.of(2026, 10, 11), "1000")));

        DashboardResponse r = service.obtener(
                LocalDate.of(2026, 9, 15), LocalDate.of(2026, 11, 2), null, AgrupacionPeriodo.MES);

        assertEquals(3, r.entregasPorPeriodo().size());
        assertEquals(LocalDate.of(2026, 9, 1), r.entregasPorPeriodo().get(0).inicio());
        assertEquals(1L, r.entregasPorPeriodo().get(0).entregas());
        assertEquals(2L, r.entregasPorPeriodo().get(1).entregas());
        assertEquals(0L, r.entregasPorPeriodo().get(2).entregas());
    }

    @Test
    void tiempoPromedioEsElPromedioEnHorasDeCreacionAEntrega() {
        LocalDateTime base = LocalDateTime.of(2026, 10, 1, 8, 0);
        datos(List.of(), List.of(
                fila(1L, "Ana", base, base.plusHours(2), "1000"),
                fila(1L, "Ana", base, base.plusHours(4), "1000")));

        DashboardResponse r = service.obtener(DESDE, HASTA, null, AgrupacionPeriodo.DIA);

        assertEquals(3.0, r.tiempoPromedioEntregaHoras());
        assertEquals(2L, r.totalEntregas());
    }

    @Test
    void tiempoPromedioSeRedondeaADosDecimales() {
        LocalDateTime base = LocalDateTime.of(2026, 10, 1, 8, 0);
        datos(List.of(), List.of(fila(1L, "Ana", base, base.plusMinutes(100), "1000")));

        DashboardResponse r = service.obtener(DESDE, HASTA, null, AgrupacionPeriodo.DIA);

        assertEquals(1.67, r.tiempoPromedioEntregaHoras());
    }

    @Test
    void sinEntregasNoHayTiempoPromedioNiIngresos() {
        datos(List.of(), List.of());

        DashboardResponse r = service.obtener(DESDE, HASTA, null, AgrupacionPeriodo.DIA);

        assertNull(r.tiempoPromedioEntregaHoras());
        assertEquals(0L, r.totalEntregas());
        assertValor("0", r.ingresosTotales());
        assertTrue(r.desempenoPorRepartidor().isEmpty());
    }

    @Test
    void ingresosTotalesSumaLosValoresIgnorandoNulos() {
        datos(List.of(), List.of(
                entregaEl(LocalDate.of(2026, 10, 1), "12000.50"),
                entregaEl(LocalDate.of(2026, 10, 2), "8000"),
                entregaEl(LocalDate.of(2026, 10, 3), null)));

        DashboardResponse r = service.obtener(DESDE, HASTA, null, AgrupacionPeriodo.DIA);

        assertValor("20000.50", r.ingresosTotales());
    }

    @Test
    void desempenoAgrupaPorRepartidorOrdenadoPorEntregas() {
        LocalDateTime base = LocalDateTime.of(2026, 10, 1, 8, 0);
        datos(List.of(), List.of(
                fila(1L, "Ana", base, base.plusHours(2), "1000"),
                fila(2L, "Beto", base, base.plusHours(1), "2000"),
                fila(2L, "Beto", base, base.plusHours(3), "3000"),
                fila(null, null, base, base.plusHours(5), "500")));

        DashboardResponse r = service.obtener(DESDE, HASTA, null, AgrupacionPeriodo.DIA);

        assertEquals(2, r.desempenoPorRepartidor().size());
        DashboardResponse.DesempenoRepartidor primero = r.desempenoPorRepartidor().get(0);
        assertEquals(2L, primero.repartidorId());
        assertEquals("Beto", primero.repartidor());
        assertEquals(2L, primero.entregas());
        assertEquals(2.0, primero.tiempoPromedioHoras());
        assertValor("5000", primero.ingresos());
        DashboardResponse.DesempenoRepartidor segundo = r.desempenoPorRepartidor().get(1);
        assertEquals("Ana", segundo.repartidor());
        assertEquals(1L, segundo.entregas());
        assertEquals(4L, r.totalEntregas());
    }

    @Test
    void rechazaDesdePosteriorAHasta() {
        assertThrows(ParametroInvalidoException.class,
                () -> service.obtener(HASTA, DESDE, null, AgrupacionPeriodo.DIA));

        verifyNoInteractions(envioRepository);
    }

    @Test
    void rechazaRangosMayoresA366Dias() {
        assertThrows(ParametroInvalidoException.class, () -> service.obtener(
                LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 2), null, AgrupacionPeriodo.DIA));

        verifyNoInteractions(envioRepository);
    }

    @Test
    void aceptaUnRangoDeExactamente366Dias() {
        datos(List.of(), List.of());

        DashboardResponse r = service.obtener(
                LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 1), null, AgrupacionPeriodo.DIA);

        assertEquals(366, r.entregasPorPeriodo().size());
    }

    @Test
    void repartidorInexistenteLanzaNoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.obtener(DESDE, HASTA, 99L, AgrupacionPeriodo.DIA));

        verifyNoInteractions(envioRepository);
    }

    @Test
    void rechazaFiltrarPorUsuarioQueNoEsRepartidor() {
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario(5L, Rol.CLIENTE_EMISOR, true)));

        assertThrows(ConflictoException.class,
                () -> service.obtener(DESDE, HASTA, 5L, AgrupacionPeriodo.DIA));

        verifyNoInteractions(envioRepository);
    }

    @Test
    void elFiltroDeRepartidorSePasaATodasLasConsultas() {
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario(7L, Rol.REPARTIDOR, true)));
        datos(List.of(), List.of());

        DashboardResponse r = service.obtener(DESDE, HASTA, 7L, AgrupacionPeriodo.DIA);

        assertEquals(7L, r.repartidorId());
        verify(envioRepository).contarPorEstado(any(), any(), eq(7L));
        verify(envioRepository).listarEntregas(eq(EstadoEnvio.ENTREGADO), any(), any(), eq(7L));
    }

    @Test
    void permiteFiltrarPorUnRepartidorDesactivado() {
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario(7L, Rol.REPARTIDOR, false)));
        datos(List.of(), List.of());

        DashboardResponse r = service.obtener(DESDE, HASTA, 7L, AgrupacionPeriodo.DIA);

        assertEquals(7L, r.repartidorId());
    }
}
