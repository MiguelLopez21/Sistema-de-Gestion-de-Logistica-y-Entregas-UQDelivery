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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "uqdelivery.admin.correo=admin@uqdelivery.com",
        "uqdelivery.admin.password=Admin12345"})
@AutoConfigureMockMvc
@Transactional
class SolicitudEnvioControllerIntegrationTest {

    private static final String URL = "/api/cliente/envios";
    private static final String CLAVE = "Clave1234";
    private static final String TARJETA_OK = "4242424242424242";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EnvioRepository envioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void crearClienteYRepartidor() {
        crearUsuario("cliente@uq.com", Rol.CLIENTE_EMISOR);
        crearUsuario("rep@uq.com", Rol.REPARTIDOR);
    }

    private void crearUsuario(String correo, Rol rol) {
        Usuario u = new Usuario();
        u.setNombre("Usuario Prueba");
        u.setCorreo(correo);
        u.setTelefono("3001234567");
        u.setPassword(passwordEncoder.encode(CLAVE));
        u.setRol(rol);
        usuarioRepository.save(u);
    }

    private static String json(String origen, String descripcion, String tarjeta, double peso) {
        return """
                {"zonaOrigen":"ARMENIA_CENTRO","direccionOrigen":"%s","zonaDestino":"ARMENIA_CENTRO","direccionDestino":"Carrera 15 # 8-30",
                 "destinatarioNombre":"Ana Pérez","destinatarioTelefono":"3001234567",
                 "pesoKg":%s,"largoCm":30,"anchoCm":20,"altoCm":10,"descripcion":"%s",
                 "tipoServicio":"ESTANDAR","pago":{"numeroTarjeta":"%s","titular":"Juan Cliente"}}
                """.formatted(origen, peso, descripcion, tarjeta);
    }

    private org.springframework.test.web.servlet.ResultActions enviar(String cuerpo) throws Exception {
        return mockMvc.perform(post(URL).with(httpBasic("cliente@uq.com", CLAVE))
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    @Test
    void pagoAprobadoCreaEnvioPendienteConCodigoYValor() throws Exception {
        long antes = envioRepository.count();

        enviar(json("Calle 10 # 5-20", "Libros de texto", TARJETA_OK, 2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.valorCotizado").value(8092))
                .andExpect(jsonPath("$.codigoSeguimiento").value(org.hamcrest.Matchers.matchesPattern("UQD-[A-Z2-9]{10}")));

        assertEquals(antes + 1, envioRepository.count());
        Envio e = envioRepository.findAll().stream()
                .filter(x -> x.getCodigoSeguimiento() != null).findFirst().orElseThrow();
        assertEquals(EstadoEnvio.PENDIENTE, e.getEstado());
        assertEquals("cliente@uq.com", e.getCliente().getCorreo());
    }

    @Test
    void codigosDeSeguimientoSonUnicos() throws Exception {
        Set<String> codigos = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            MvcResult r = enviar(json("Calle 10 # 5-20", "Libros", TARJETA_OK, 2)).andExpect(status().isCreated()).andReturn();
            Matcher m = Pattern.compile("\"codigoSeguimiento\":\"(UQD-[A-Z2-9]{10})\"")
                    .matcher(r.getResponse().getContentAsString());
            assertTrue(m.find());
            assertTrue(codigos.add(m.group(1)));
        }
    }

    @Test
    void pagoRechazadoDevuelve402ConMotivo() throws Exception {
        // Que no quede envío persistido se verifica con commit real en SolicitudEnvioPagoRechazadoIntegrationTest.
        enviar(json("Calle 10 # 5-20", "Libros", "4000000000000000", 2))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.mensaje").value("Pago rechazado: tarjeta rechazada por el emisor"));
    }

    @Test
    void contenidoMaliciosoSeRechazaConDetalleYNoCreaEnvio() throws Exception {
        long antes = envioRepository.count();

        enviar(json("<script>alert(1)</script>", "Libros", TARJETA_OK, 2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.direccionOrigen").exists());
        enviar(json("Calle 1", "x'; DROP TABLE envios;--", TARJETA_OK, 2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.descripcion").exists());

        assertEquals(antes, envioRepository.count());
    }

    @Test
    void pesoFueraDeLimiteDevuelve400() throws Exception {
        enviar(json("Calle 1", "Libros", TARJETA_OK, 99))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.pesoKg").exists());
    }

    @Test
    void camposFaltantesDevuelve400() throws Exception {
        enviar("{}").andExpect(status().isBadRequest());
    }

    @Test
    void sinAutenticacionDevuelve401() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(json("Calle 1", "Libros", TARJETA_OK, 2)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void repartidorYAdministradorNoPuedenSolicitarEnvios() throws Exception {
        String cuerpo = json("Calle 1", "Libros", TARJETA_OK, 2);
        mockMvc.perform(post(URL).with(httpBasic("rep@uq.com", CLAVE))
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo)).andExpect(status().isForbidden());
        mockMvc.perform(post(URL).with(httpBasic("admin@uqdelivery.com", "Admin12345"))
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo)).andExpect(status().isForbidden());
    }

    @Test
    void tipoServicioInexistenteDevuelve400() throws Exception {
        enviar(json("Calle 1", "Libros", TARJETA_OK, 2).replace("ESTANDAR", "VOLANDO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void laRespuestaNoExponeElNumeroDeTarjeta() throws Exception {
        MvcResult r = enviar(json("Calle 1", "Libros", TARJETA_OK, 2)).andExpect(status().isCreated()).andReturn();
        assertFalse(r.getResponse().getContentAsString().contains(TARJETA_OK));
        MvcResult rechazo = enviar(json("Calle 1", "Libros", "4000000000000000", 2)).andReturn();
        assertFalse(rechazo.getResponse().getContentAsString().contains("4000000000000000"));
    }

    @Test
    void mismaIdempotencyKeyNoDuplicaEnvioNiCobro() throws Exception {
        String cuerpo = json("Calle 10 # 5-20", "Libros", TARJETA_OK, 2);
        long antes = envioRepository.count();

        MvcResult primera = mockMvc.perform(post(URL).with(httpBasic("cliente@uq.com", CLAVE))
                        .header("Idempotency-Key", "clave-abc-12345")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated()).andReturn();
        MvcResult segunda = mockMvc.perform(post(URL).with(httpBasic("cliente@uq.com", CLAVE))
                        .header("Idempotency-Key", "clave-abc-12345")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk()).andReturn();

        assertEquals(antes + 1, envioRepository.count());
        Matcher a = Pattern.compile("\"codigoSeguimiento\":\"([^\"]+)\"").matcher(primera.getResponse().getContentAsString());
        Matcher b = Pattern.compile("\"codigoSeguimiento\":\"([^\"]+)\"").matcher(segunda.getResponse().getContentAsString());
        assertTrue(a.find() && b.find());
        assertEquals(a.group(1), b.group(1));
    }

    @Test
    void idempotencyKeyMalFormadaDevuelve400() throws Exception {
        mockMvc.perform(post(URL).with(httpBasic("cliente@uq.com", CLAVE)).header("Idempotency-Key", "x")
                        .contentType(MediaType.APPLICATION_JSON).content(json("Calle 1", "Libros", TARJETA_OK, 2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void zonaInexistenteDevuelve400() throws Exception {
        enviar(json("Calle 1", "Libros", TARJETA_OK, 2).replace("\"zonaDestino\":\"ARMENIA_CENTRO\"", "\"zonaDestino\":\"LUNA\""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void elClienteNoPuedeImponerLaDistancia() throws Exception {
        // distanciaKm ya no forma parte del contrato: un valor enviado se ignora y el precio sale de las zonas.
        enviar(json("Calle 1", "Libros", TARJETA_OK, 2).replace("{\"zonaOrigen\"", "{\"distanciaKm\":0.01,\"zonaOrigen\""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorCotizado").value(8092));
    }
}
