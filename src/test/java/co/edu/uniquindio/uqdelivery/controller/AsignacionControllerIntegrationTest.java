package co.edu.uniquindio.uqdelivery.controller;

import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.EnvioRepository;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "uqdelivery.admin.correo=admin@uqdelivery.com",
        "uqdelivery.admin.password=Admin12345"})
@AutoConfigureMockMvc
@Transactional
class AsignacionControllerIntegrationTest {

    private static final String URL = "/api/admin/envios";
    private static final String ENTREGAS = "/api/repartidor/entregas";
    private static final String CLAVE = "Clave1234";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EnvioRepository envioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario crearUsuario(String correo, Rol rol, boolean activo) {
        Usuario u = new Usuario();
        u.setNombre("Usuario " + correo);
        u.setCorreo(correo);
        u.setTelefono("3001234567");
        u.setPassword(passwordEncoder.encode(CLAVE));
        u.setRol(rol);
        u.setActivo(activo);
        return usuarioRepository.save(u);
    }

    private Envio crearEnvio(EstadoEnvio estado, Usuario repartidor) {
        Envio e = new Envio();
        e.setEstado(estado);
        e.setRepartidor(repartidor);
        return envioRepository.save(e);
    }

    private static String cuerpo(Long repartidorId, String motivo) {
        String m = motivo == null ? "null" : "\"" + motivo + "\"";
        return "{\"repartidorId\":" + repartidorId + ",\"motivo\":" + m + "}";
    }

    @Test
    void asignarEnvioPendienteLoPasaAAsignadoYApareceEnLaListaDelRepartidor() throws Exception {
        Usuario repartidor = crearUsuario("rep1@uqdelivery.com", Rol.REPARTIDOR, true);
        Envio envio = crearEnvio(EstadoEnvio.PENDIENTE, null);

        mockMvc.perform(post(URL + "/" + envio.getId() + "/asignacion")
                        .with(httpBasic("admin@uqdelivery.com", "Admin12345"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(repartidor.getId(), null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ASIGNADO"))
                .andExpect(jsonPath("$.repartidorId").value(repartidor.getId()));

        mockMvc.perform(get(ENTREGAS).with(httpBasic("rep1@uqdelivery.com", CLAVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(envio.getId()))
                .andExpect(jsonPath("$[0].estado").value("ASIGNADO"));
    }

    @Test
    void reasignarRegistraElCambioYRetiraElEnvioDelRepartidorAnterior() throws Exception {
        Usuario anterior = crearUsuario("rep1@uqdelivery.com", Rol.REPARTIDOR, true);
        Usuario nuevo = crearUsuario("rep2@uqdelivery.com", Rol.REPARTIDOR, true);
        Envio envio = crearEnvio(EstadoEnvio.ASIGNADO, anterior);

        mockMvc.perform(put(URL + "/" + envio.getId() + "/asignacion")
                        .with(httpBasic("admin@uqdelivery.com", "Admin12345"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(nuevo.getId(), "Moto averiada")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repartidorId").value(nuevo.getId()));

        mockMvc.perform(get(ENTREGAS).with(httpBasic("rep1@uqdelivery.com", CLAVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get(ENTREGAS).with(httpBasic("rep2@uqdelivery.com", CLAVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get(URL + "/" + envio.getId() + "/asignaciones")
                        .with(httpBasic("admin@uqdelivery.com", "Admin12345")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].administrador").value("admin@uqdelivery.com"))
                .andExpect(jsonPath("$[0].repartidorAnteriorId").value(anterior.getId()))
                .andExpect(jsonPath("$[0].repartidorNuevoId").value(nuevo.getId()))
                .andExpect(jsonPath("$[0].motivo").value("Moto averiada"))
                .andExpect(jsonPath("$[0].fecha").exists());
    }

    @Test
    void noPermiteReasignarUnEnvioEntregado() throws Exception {
        Usuario anterior = crearUsuario("rep1@uqdelivery.com", Rol.REPARTIDOR, true);
        Usuario nuevo = crearUsuario("rep2@uqdelivery.com", Rol.REPARTIDOR, true);
        Envio envio = crearEnvio(EstadoEnvio.ENTREGADO, anterior);

        mockMvc.perform(put(URL + "/" + envio.getId() + "/asignacion")
                        .with(httpBasic("admin@uqdelivery.com", "Admin12345"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(nuevo.getId(), null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("No se puede reasignar un envío ya entregado"));
    }

    @Test
    void rechazaAsignarRepartidorDesactivado() throws Exception {
        Usuario inactivo = crearUsuario("rep1@uqdelivery.com", Rol.REPARTIDOR, false);
        Envio envio = crearEnvio(EstadoEnvio.PENDIENTE, null);

        mockMvc.perform(post(URL + "/" + envio.getId() + "/asignacion")
                        .with(httpBasic("admin@uqdelivery.com", "Admin12345"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(inactivo.getId(), null)))
                .andExpect(status().isConflict());
    }

    @Test
    void rechazaAsignarUsuarioQueNoEsRepartidor() throws Exception {
        Usuario cliente = crearUsuario("cli@uqdelivery.com", Rol.CLIENTE_EMISOR, true);
        Envio envio = crearEnvio(EstadoEnvio.PENDIENTE, null);

        mockMvc.perform(post(URL + "/" + envio.getId() + "/asignacion")
                        .with(httpBasic("admin@uqdelivery.com", "Admin12345"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(cliente.getId(), null)))
                .andExpect(status().isConflict());
    }

    @Test
    void envioInexistenteDevuelve404() throws Exception {
        Usuario repartidor = crearUsuario("rep1@uqdelivery.com", Rol.REPARTIDOR, true);

        mockMvc.perform(post(URL + "/999999/asignacion")
                        .with(httpBasic("admin@uqdelivery.com", "Admin12345"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(repartidor.getId(), null)))
                .andExpect(status().isNotFound());
    }

    @Test
    void cuerpoSinRepartidorDevuelve400() throws Exception {
        Envio envio = crearEnvio(EstadoEnvio.PENDIENTE, null);

        mockMvc.perform(post(URL + "/" + envio.getId() + "/asignacion")
                        .with(httpBasic("admin@uqdelivery.com", "Admin12345"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400));
    }

    @Test
    void motivoConCaracteresNoPermitidosDevuelve400() throws Exception {
        Usuario repartidor = crearUsuario("rep1@uqdelivery.com", Rol.REPARTIDOR, true);
        Envio envio = crearEnvio(EstadoEnvio.PENDIENTE, null);

        mockMvc.perform(post(URL + "/" + envio.getId() + "/asignacion")
                        .with(httpBasic("admin@uqdelivery.com", "Admin12345"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(repartidor.getId(), "<script>")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void administradorListaEnviosPendientes() throws Exception {
        crearEnvio(EstadoEnvio.PENDIENTE, null);
        crearEnvio(EstadoEnvio.ENTREGADO, null);

        mockMvc.perform(get(URL).param("estado", "PENDIENTE")
                        .with(httpBasic("admin@uqdelivery.com", "Admin12345")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].estado").value("PENDIENTE"));
    }

    @Test
    @WithMockUser(roles = "REPARTIDOR")
    void repartidorRecibe403EnGestionDeEnvios() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isForbidden());
        mockMvc.perform(post(URL + "/1/asignacion")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo(1L, null)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(URL + "/1/asignacion")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo(1L, null)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE_EMISOR")
    void clienteEmisorRecibe403EnGestionDeEnviosYEnMisEntregas() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isForbidden());
        mockMvc.perform(get(ENTREGAS)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void administradorRecibe403EnMisEntregas() throws Exception {
        mockMvc.perform(get(ENTREGAS)).andExpect(status().isForbidden());
    }

    @Test
    void sinAutenticarRecibe401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
        mockMvc.perform(get(ENTREGAS)).andExpect(status().isUnauthorized());
    }
}
