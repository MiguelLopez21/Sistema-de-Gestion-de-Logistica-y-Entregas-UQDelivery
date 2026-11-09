package co.edu.uniquindio.uqdelivery.controller;

import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.Notification;
import co.edu.uniquindio.uqdelivery.model.NotificationType;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.EnvioRepository;
import co.edu.uniquindio.uqdelivery.repository.NotificationRepository;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "uqdelivery.admin.correo=admin@uqdelivery.com",
        "uqdelivery.admin.password=Admin12345"})
@AutoConfigureMockMvc
@Transactional
class CierreEntregaControllerIntegrationTest {

    private static final String ENTREGAS = "/api/repartidor/entregas";
    private static final String CLIENTE = "/api/cliente/envios";
    private static final String ADMIN = "/api/admin/envios";
    private static final String CLAVE = "Clave1234";
    private static final String REP1 = "rep1@uqdelivery.com";
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3, 4};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 9, 8, 7};

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EnvioRepository envioRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario repartidor;
    private Usuario cliente;

    @BeforeEach
    void crearActores() {
        repartidor = crearUsuario(REP1, Rol.REPARTIDOR);
        cliente = crearUsuario("cliente1@uqdelivery.com", Rol.CLIENTE_EMISOR);
        crearUsuario("rep2@uqdelivery.com", Rol.REPARTIDOR);
        crearUsuario("cliente2@uqdelivery.com", Rol.CLIENTE_EMISOR);
    }

    private Usuario crearUsuario(String correo, Rol rol) {
        Usuario u = new Usuario();
        u.setNombre("Usuario " + correo);
        u.setCorreo(correo);
        u.setTelefono("3001234567");
        u.setPassword(passwordEncoder.encode(CLAVE));
        u.setRol(rol);
        u.setActivo(true);
        return usuarioRepository.save(u);
    }

    private Envio crearEnvio(EstadoEnvio estado) {
        Envio e = new Envio();
        e.setEstado(estado);
        e.setRepartidor(repartidor);
        e.setCliente(cliente);
        e.setCodigoSeguimiento("UQD-TEST000001");
        return envioRepository.save(e);
    }

    private static String urlCierre(Long id) {
        return ENTREGAS + "/" + id + "/cierre";
    }

    private static RequestBuilder cierreCompleto(Long id, String correo) {
        return multipart(urlCierre(id))
                .file(new MockMultipartFile("foto", "foto.png", "image/png", PNG))
                .file(new MockMultipartFile("firma", "firma.jpg", "image/jpeg", JPEG))
                .param("nombreReceptor", "Ana Perez")
                .param("latitud", "4.5339")
                .param("longitud", "-75.6811")
                .with(httpBasic(correo, CLAVE));
    }

    @Test
    void cierraLaEntregaYElClienteYElAdministradorConsultanLaEvidencia() throws Exception {
        Envio envio = crearEnvio(EstadoEnvio.EN_RUTA);

        mockMvc.perform(cierreCompleto(envio.getId(), REP1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENTREGADO"))
                .andExpect(jsonPath("$.nombreReceptor").value("Ana Perez"))
                .andExpect(jsonPath("$.latitudEntrega").value(4.5339))
                .andExpect(jsonPath("$.longitudEntrega").value(-75.6811))
                .andExpect(jsonPath("$.fechaEntrega").exists())
                .andExpect(jsonPath("$.tieneFoto").value(true))
                .andExpect(jsonPath("$.tieneFirma").value(true));

        mockMvc.perform(get(CLIENTE + "/" + envio.getId()).with(httpBasic("cliente1@uqdelivery.com", CLAVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENTREGADO"))
                .andExpect(jsonPath("$.codigoSeguimiento").value("UQD-TEST000001"))
                .andExpect(jsonPath("$.tieneFoto").value(true));
        mockMvc.perform(get(CLIENTE + "/" + envio.getId() + "/evidencia/foto")
                        .with(httpBasic("cliente1@uqdelivery.com", CLAVE)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(PNG));
        mockMvc.perform(get(ADMIN + "/" + envio.getId() + "/evidencia/firma")
                        .with(httpBasic("admin@uqdelivery.com", "Admin12345")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(content().bytes(JPEG));
        mockMvc.perform(get(ADMIN + "/" + envio.getId()).with(httpBasic("admin@uqdelivery.com", "Admin12345")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreReceptor").value("Ana Perez"));
    }

    @Test
    void alCerrarNotificaAlClienteYAlRepartidor() throws Exception {
        Envio envio = crearEnvio(EstadoEnvio.EN_RUTA);

        mockMvc.perform(cierreCompleto(envio.getId(), REP1)).andExpect(status().isOk());

        List<Notification> delCliente = notificationRepository.findByUserIdOrderByCreatedAtDesc(cliente.getId());
        assertEquals(1, delCliente.size());
        assertEquals(NotificationType.DELIVERED, delCliente.get(0).getType());
        assertEquals(1, notificationRepository.findByUserIdOrderByCreatedAtDesc(repartidor.getId()).size());
    }

    @Test
    void permiteCerrarSoloConFirma() throws Exception {
        Envio envio = crearEnvio(EstadoEnvio.EN_RUTA);

        mockMvc.perform(multipart(urlCierre(envio.getId()))
                        .file(new MockMultipartFile("firma", "firma.png", "image/png", PNG))
                        .param("nombreReceptor", "Ana Perez")
                        .param("latitud", "4.5")
                        .param("longitud", "-75.6")
                        .with(httpBasic(REP1, CLAVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tieneFoto").value(false))
                .andExpect(jsonPath("$.tieneFirma").value(true));

        mockMvc.perform(get(CLIENTE + "/" + envio.getId() + "/evidencia/foto")
                        .with(httpBasic("cliente1@uqdelivery.com", CLAVE)))
                .andExpect(status().isNotFound());
    }

    @Test
    void unEnvioEntregadoNoAceptaUnSegundoCierre() throws Exception {
        Envio envio = crearEnvio(EstadoEnvio.EN_RUTA);
        mockMvc.perform(cierreCompleto(envio.getId(), REP1)).andExpect(status().isOk());

        mockMvc.perform(cierreCompleto(envio.getId(), REP1))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El envío ya fue entregado y no puede modificarse"));
    }

    @Test
    void rechazaCerrarUnEnvioQueNoEstaEnRuta() throws Exception {
        Envio envio = crearEnvio(EstadoEnvio.ASIGNADO);

        mockMvc.perform(cierreCompleto(envio.getId(), REP1))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Solo se puede cerrar la entrega de un envío en ruta"));
    }

    @Test
    void rechazaAUnRepartidorQueNoTieneAsignadoElEnvio() throws Exception {
        Envio envio = crearEnvio(EstadoEnvio.EN_RUTA);

        mockMvc.perform(cierreCompleto(envio.getId(), "rep2@uqdelivery.com"))
                .andExpect(status().isForbidden());
        assertEquals(EstadoEnvio.EN_RUTA, envioRepository.findById(envio.getId()).orElseThrow().getEstado());
    }

    @Test
    void soloElRepartidorPuedeCerrarEntregas() throws Exception {
        Envio envio = crearEnvio(EstadoEnvio.EN_RUTA);

        mockMvc.perform(cierreCompleto(envio.getId(), "cliente1@uqdelivery.com"))
                .andExpect(status().isForbidden());
        mockMvc.perform(multipart(urlCierre(envio.getId()))
                        .file(new MockMultipartFile("foto", "foto.png", "image/png", PNG)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exigeAlMenosFotoOFirma() throws Exception {
        Envio envio = crearEnvio(EstadoEnvio.EN_RUTA);

        mockMvc.perform(multipart(urlCierre(envio.getId()))
                        .param("nombreReceptor", "Ana Perez")
                        .param("latitud", "4.5")
                        .param("longitud", "-75.6")
                        .with(httpBasic(REP1, CLAVE)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.evidencia").exists());
    }

    @Test
    void validaReceptorCoordenadasYTipoDeArchivo() throws Exception {
        Envio envio = crearEnvio(EstadoEnvio.EN_RUTA);

        mockMvc.perform(multipart(urlCierre(envio.getId()))
                        .file(new MockMultipartFile("foto", "foto.gif", "image/gif", PNG))
                        .param("latitud", "95")
                        .param("longitud", "-75.6")
                        .with(httpBasic(REP1, CLAVE)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombreReceptor").exists())
                .andExpect(jsonPath("$.errores.latitud").exists())
                .andExpect(jsonPath("$.errores.foto").exists());
        assertEquals(EstadoEnvio.EN_RUTA, envioRepository.findById(envio.getId()).orElseThrow().getEstado());
    }

    @Test
    void unClienteNoVeLosEnviosDeOtroCliente() throws Exception {
        Envio envio = crearEnvio(EstadoEnvio.EN_RUTA);
        mockMvc.perform(cierreCompleto(envio.getId(), REP1)).andExpect(status().isOk());

        mockMvc.perform(get(CLIENTE + "/" + envio.getId()).with(httpBasic("cliente2@uqdelivery.com", CLAVE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(CLIENTE + "/" + envio.getId() + "/evidencia/foto")
                        .with(httpBasic("cliente2@uqdelivery.com", CLAVE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void rechazaUnTipoDeEvidenciaDesconocido() throws Exception {
        Envio envio = crearEnvio(EstadoEnvio.EN_RUTA);
        mockMvc.perform(cierreCompleto(envio.getId(), REP1)).andExpect(status().isOk());

        mockMvc.perform(get(CLIENTE + "/" + envio.getId() + "/evidencia/otra")
                        .with(httpBasic("cliente1@uqdelivery.com", CLAVE)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void devuelve404SiElEnvioNoExiste() throws Exception {
        mockMvc.perform(get(CLIENTE + "/999999").with(httpBasic("cliente1@uqdelivery.com", CLAVE)))
                .andExpect(status().isNotFound());
    }

    @Test
    void elRepartidorConsultaElDetalleDeSuEnvio() throws Exception {
        Envio envio = crearEnvio(EstadoEnvio.EN_RUTA);

        mockMvc.perform(get(ENTREGAS + "/" + envio.getId()).with(httpBasic(REP1, CLAVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(envio.getId()))
                .andExpect(jsonPath("$.estado").value("EN_RUTA"))
                .andExpect(jsonPath("$.codigoSeguimiento").value("UQD-TEST000001"))
                .andExpect(jsonPath("$.tieneFoto").value(false));
    }

    @Test
    void elRepartidorNoConsultaEnviosDeOtroRepartidor() throws Exception {
        Envio envio = crearEnvio(EstadoEnvio.EN_RUTA);

        mockMvc.perform(get(ENTREGAS + "/" + envio.getId()).with(httpBasic("rep2@uqdelivery.com", CLAVE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void elClienteNoUsaElDetalleDelRepartidor() throws Exception {
        Envio envio = crearEnvio(EstadoEnvio.EN_RUTA);

        mockMvc.perform(get(ENTREGAS + "/" + envio.getId()).with(httpBasic("cliente1@uqdelivery.com", CLAVE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void detalleDelRepartidorDevuelve404SiElEnvioNoExiste() throws Exception {
        mockMvc.perform(get(ENTREGAS + "/999999").with(httpBasic(REP1, CLAVE)))
                .andExpect(status().isNotFound());
    }
}
