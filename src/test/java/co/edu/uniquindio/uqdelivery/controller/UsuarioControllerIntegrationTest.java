package co.edu.uniquindio.uqdelivery.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de aceptación de HU-02 (criterios de validación 1 y 2) y de las validaciones de seguridad.
 */
@SpringBootTest(properties = {
        "uqdelivery.admin.correo=admin@uqdelivery.com",
        "uqdelivery.admin.password=Admin12345"})
@AutoConfigureMockMvc
@Transactional
class UsuarioControllerIntegrationTest {

    private static final String URL = "/api/admin/usuarios";

    @Autowired
    private MockMvc mockMvc;

    private static String cuerpo(String nombre, String correo, String telefono, String password, String rol) {
        return """
                {"nombre":"%s","correo":"%s","telefono":"%s","password":"%s","rol":"%s"}
                """.formatted(nombre, correo, telefono, password, rol);
    }

    private static final String REPARTIDOR =
            cuerpo("Carlos Ruiz", "carlos.ruiz@uqdelivery.com", "3001234567", "Clave1234", "REPARTIDOR");

    private long crearComoAdmin(String json) throws Exception {
        MvcResult r = mockMvc.perform(post(URL).with(httpBasic("admin@uqdelivery.com", "Admin12345"))
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(r.getResponse().getContentAsString(), "$.id")).longValue();
    }

    // ---------- Criterio 2: acceso denegado (403) a quien no es administrador ----------

    @Test
    @WithMockUser(roles = "CLIENTE_EMISOR")
    void clienteEmisorRecibe403EnGestionDeUsuarios() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isForbidden());
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(REPARTIDOR))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch(URL + "/1/desactivar")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "REPARTIDOR")
    void repartidorRecibe403EnGestionDeUsuarios() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.estado").value(403));
    }

    @Test
    void sinAutenticarRecibe401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }

    // ---------- Gestión por el administrador ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void administradorCreaUsuarioSinExponerPassword() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(REPARTIDOR))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.correo").value("carlos.ruiz@uqdelivery.com"))
                .andExpect(jsonPath("$.rol").value("REPARTIDOR"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void rechazaCorreoDuplicadoConConflicto() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(REPARTIDOR))
                .andExpect(status().isCreated());
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(REPARTIDOR))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listaConFiltroPorRol() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(REPARTIDOR))
                .andExpect(status().isCreated());

        mockMvc.perform(get(URL).param("rol", "REPARTIDOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].rol").value("REPARTIDOR"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void editaUsuarioYCambiaSuRol() throws Exception {
        long id = crearComoAdmin(REPARTIDOR);

        mockMvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nombre":"Carlos R. Ruiz","correo":"carlos.ruiz@uqdelivery.com","telefono":"3110000000","rol":"REPARTIDOR"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Carlos R. Ruiz"));

        mockMvc.perform(patch(URL + "/" + id + "/rol").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rol\":\"CLIENTE_EMISOR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("CLIENTE_EMISOR"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void usuarioInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get(URL + "/999999")).andExpect(status().isNotFound());
    }

    // ---------- Validación del servidor / XSS / SQL injection ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void rechazaNombreConScriptXss() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(
                        cuerpo("<script>alert(1)</script>", "xss@uqdelivery.com", "3001234567", "Clave1234", "REPARTIDOR")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void rechazaPasswordDebilYTelefonoInvalido() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(
                        cuerpo("Ana Gómez", "ana@uqdelivery.com", "abc", "clave", "REPARTIDOR")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.password").exists())
                .andExpect(jsonPath("$.errores.telefono").exists());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void rechazaCorreoInvalidoYRolInexistente() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(
                        cuerpo("Ana Gómez", "no-es-correo", "3001234567", "Clave1234", "REPARTIDOR")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.correo").exists());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(
                        cuerpo("Ana Gómez", "ana@uqdelivery.com", "3001234567", "Clave1234", "SUPERUSUARIO")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void inyeccionSqlEnLoginNoConcedeAcceso() throws Exception {
        mockMvc.perform(get("/api/perfil").with(httpBasic("' OR '1'='1", "' OR '1'='1")))
                .andExpect(status().isUnauthorized());
    }

    // ---------- Criterio 1: cada rol ve solo lo suyo + desactivación ----------

    @Test
    void repartidorCreadoPorAdminSoloVeOpcionesDeSuRolYLuegoSeDesactiva() throws Exception {
        long id = crearComoAdmin(REPARTIDOR);

        mockMvc.perform(get("/api/perfil").with(httpBasic("carlos.ruiz@uqdelivery.com", "Clave1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.rol").value("REPARTIDOR"))
                .andExpect(jsonPath("$.opciones[0]").value("Mis entregas"))
                .andExpect(jsonPath("$.usuario.password").doesNotExist());

        mockMvc.perform(get(URL).with(httpBasic("carlos.ruiz@uqdelivery.com", "Clave1234")))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch(URL + "/" + id + "/desactivar")
                        .with(httpBasic("admin@uqdelivery.com", "Admin12345")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        mockMvc.perform(get("/api/perfil").with(httpBasic("carlos.ruiz@uqdelivery.com", "Clave1234")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void administradorVeLasOpcionesDeAdministracion() throws Exception {
        mockMvc.perform(get("/api/perfil").with(httpBasic("admin@uqdelivery.com", "Admin12345")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.opciones[0]").value("Gestión de usuarios"));
    }

    @Test
    void administradorNoPuedeDesactivarseASiMismo() throws Exception {
        MvcResult r = mockMvc.perform(get("/api/perfil").with(httpBasic("admin@uqdelivery.com", "Admin12345")))
                .andReturn();
        long id = ((Number) JsonPath.read(r.getResponse().getContentAsString(), "$.usuario.id")).longValue();

        mockMvc.perform(patch(URL + "/" + id + "/desactivar")
                        .with(httpBasic("admin@uqdelivery.com", "Admin12345")))
                .andExpect(status().isConflict());
    }
}
