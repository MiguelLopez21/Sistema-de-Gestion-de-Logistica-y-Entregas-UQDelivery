package co.edu.uniquindio.uqdelivery.controller;

import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.EnvioRepository;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "uqdelivery.admin.correo=admin@uqdelivery.com",
        "uqdelivery.admin.password=Admin12345"})
@AutoConfigureMockMvc
@Transactional
class DashboardControllerIntegrationTest {

    private static final String URL = "/api/admin/dashboard";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EnvioRepository envioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private RequestPostProcessor admin;
    private LocalDate hoy;
    private LocalDateTime base;
    private Usuario ana;
    private Usuario beto;

    @BeforeEach
    void preparar() {
        admin = httpBasic("admin@uqdelivery.com", "Admin12345");
        hoy = LocalDate.now();
        base = hoy.atStartOfDay();
        ana = crearUsuario("ana@uqdelivery.com", "Ana", Rol.REPARTIDOR);
        beto = crearUsuario("beto@uqdelivery.com", "Beto", Rol.REPARTIDOR);
        crearEnvio(EstadoEnvio.ENTREGADO, ana, base.plusHours(1), base.plusHours(3), "10000.00");
        crearEnvio(EstadoEnvio.ENTREGADO, ana, base.plusHours(2), base.plusHours(6), "20000.00");
        crearEnvio(EstadoEnvio.ENTREGADO, beto, base.plusHours(1), base.plusHours(6), "7000.00");
        crearEnvio(EstadoEnvio.PENDIENTE, null, base.plusHours(1), null, null);
    }

    private Usuario crearUsuario(String correo, String nombre, Rol rol) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setTelefono("3001234567");
        u.setPassword(passwordEncoder.encode("Clave1234"));
        u.setRol(rol);
        u.setActivo(true);
        return usuarioRepository.save(u);
    }

    private void crearEnvio(EstadoEnvio estado, Usuario repartidor, LocalDateTime creacion,
                            LocalDateTime entrega, String valor) {
        Envio e = new Envio();
        e.setEstado(estado);
        e.setRepartidor(repartidor);
        e.setFechaCreacion(creacion);
        e.setFechaEntrega(entrega);
        e.setValorTotal(valor == null ? null : new BigDecimal(valor));
        envioRepository.save(e);
    }

    @Test
    void administradorVeLasMetricasCalculadasConDatosReales() throws Exception {
        mockMvc.perform(get(URL).param("desde", hoy.toString()).param("hasta", hoy.toString()).with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.desde").value(hoy.toString()))
                .andExpect(jsonPath("$.hasta").value(hoy.toString()))
                .andExpect(jsonPath("$.totalEntregas").value(3))
                .andExpect(jsonPath("$.ingresosTotales").value(37000.0))
                .andExpect(jsonPath("$.tiempoPromedioEntregaHoras").value(3.67))
                .andExpect(jsonPath("$.enviosPorEstado.length()").value(EstadoEnvio.values().length))
                .andExpect(jsonPath("$.enviosPorEstado[?(@.estado=='ENTREGADO')].total").value(3))
                .andExpect(jsonPath("$.enviosPorEstado[?(@.estado=='PENDIENTE')].total").value(1))
                .andExpect(jsonPath("$.entregasPorPeriodo.length()").value(1))
                .andExpect(jsonPath("$.entregasPorPeriodo[0].entregas").value(3))
                .andExpect(jsonPath("$.desempenoPorRepartidor.length()").value(2))
                .andExpect(jsonPath("$.desempenoPorRepartidor[0].repartidor").value("Ana"))
                .andExpect(jsonPath("$.desempenoPorRepartidor[0].entregas").value(2))
                .andExpect(jsonPath("$.desempenoPorRepartidor[0].tiempoPromedioHoras").value(3.0))
                .andExpect(jsonPath("$.desempenoPorRepartidor[0].ingresos").value(30000.0));
    }

    @Test
    void sinParametrosUsaLosUltimos30DiasYAgrupaPorDia() throws Exception {
        mockMvc.perform(get(URL).with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasta").value(hoy.toString()))
                .andExpect(jsonPath("$.desde").value(hoy.minusDays(29).toString()))
                .andExpect(jsonPath("$.agrupacion").value("DIA"))
                .andExpect(jsonPath("$.entregasPorPeriodo.length()").value(30))
                .andExpect(jsonPath("$.totalEntregas").value(3));
    }

    @Test
    void cambiarElRangoActualizaLasMetricas() throws Exception {
        mockMvc.perform(get(URL).param("desde", hoy.minusDays(10).toString())
                        .param("hasta", hoy.minusDays(5).toString()).with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEntregas").value(0))
                .andExpect(jsonPath("$.ingresosTotales").value(0))
                .andExpect(jsonPath("$.tiempoPromedioEntregaHoras").isEmpty())
                .andExpect(jsonPath("$.desempenoPorRepartidor.length()").value(0))
                .andExpect(jsonPath("$.enviosPorEstado[?(@.estado=='ENTREGADO')].total").value(0));
    }

    @Test
    void cambiarElRepartidorActualizaLasMetricas() throws Exception {
        mockMvc.perform(get(URL).param("desde", hoy.toString()).param("hasta", hoy.toString())
                        .param("repartidorId", beto.getId().toString()).with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repartidorId").value(beto.getId()))
                .andExpect(jsonPath("$.totalEntregas").value(1))
                .andExpect(jsonPath("$.ingresosTotales").value(7000.0))
                .andExpect(jsonPath("$.tiempoPromedioEntregaHoras").value(5.0))
                .andExpect(jsonPath("$.desempenoPorRepartidor.length()").value(1))
                .andExpect(jsonPath("$.desempenoPorRepartidor[0].repartidor").value("Beto"))
                .andExpect(jsonPath("$.enviosPorEstado[?(@.estado=='PENDIENTE')].total").value(0));
    }

    @Test
    void agrupaLasEntregasPorSemanaYPorMes() throws Exception {
        mockMvc.perform(get(URL).param("agrupacion", "SEMANA").with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agrupacion").value("SEMANA"))
                .andExpect(jsonPath("$.entregasPorPeriodo").isNotEmpty());
        mockMvc.perform(get(URL).param("agrupacion", "MES").with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agrupacion").value("MES"))
                .andExpect(jsonPath("$.entregasPorPeriodo").isNotEmpty());
    }

    @Test
    void rangoInvertidoDevuelve400() throws Exception {
        mockMvc.perform(get(URL).param("desde", hoy.toString()).param("hasta", hoy.minusDays(1).toString()).with(admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400));
    }

    @Test
    void rangoMayorA366DiasDevuelve400() throws Exception {
        mockMvc.perform(get(URL).param("desde", hoy.minusDays(400).toString()).param("hasta", hoy.toString()).with(admin))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fechaOAgrupacionInvalidasDevuelven400() throws Exception {
        mockMvc.perform(get(URL).param("desde", "no-es-fecha").with(admin)).andExpect(status().isBadRequest());
        mockMvc.perform(get(URL).param("agrupacion", "ANIO").with(admin)).andExpect(status().isBadRequest());
    }

    @Test
    void repartidorInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get(URL).param("repartidorId", "999999").with(admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void filtrarPorUsuarioQueNoEsRepartidorDevuelve409() throws Exception {
        Usuario cliente = crearUsuario("cli@uqdelivery.com", "Cliente", Rol.CLIENTE_EMISOR);

        mockMvc.perform(get(URL).param("repartidorId", cliente.getId().toString()).with(admin))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "REPARTIDOR")
    void repartidorRecibe403() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE_EMISOR")
    void clienteEmisorRecibe403() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isForbidden());
    }

    @Test
    void sinAutenticarRecibe401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }
}
