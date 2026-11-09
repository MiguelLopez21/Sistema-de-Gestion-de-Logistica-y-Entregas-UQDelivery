package co.edu.uniquindio.uqdelivery.controller;

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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "uqdelivery.admin.correo=admin@uqdelivery.com",
        "uqdelivery.admin.password=Admin12345"})
@AutoConfigureMockMvc
@Transactional
class CotizacionControllerIntegrationTest {

    private static final String URL = "/api/cliente/cotizaciones";
    private static final String CLAVE = "Clave1234";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EnvioRepository envioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void crearUsuarios() {
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

    private static String cuerpo(String origen, String destino, double peso,
                                 int largo, int ancho, int alto, String servicio) {
        return """
                {"zonaOrigen":"%s","zonaDestino":"%s","pesoKg":%s,
                 "largoCm":%d,"anchoCm":%d,"altoCm":%d,"tipoServicio":"%s"}
                """.formatted(origen, destino, peso, largo, ancho, alto, servicio);
    }

    private ResultActions cotizar(String usuario, String cuerpo) throws Exception {
        var peticion = post(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        return mockMvc.perform(usuario == null ? peticion : peticion.with(httpBasic(usuario, CLAVE)));
    }

    @Test
    void cotizaEnvioEstandarEnLaMismaZona() throws Exception {
        // 4000 + 1 km*1200 + 2 kg*800 = 6800; IVA 1292; total 8092
        cotizar("cliente@uq.com", cuerpo("ARMENIA_CENTRO", "ARMENIA_CENTRO", 2, 30, 20, 10, "ESTANDAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanciaKm").value(1.0))
                .andExpect(jsonPath("$.costoBase").value(4000))
                .andExpect(jsonPath("$.costoDistancia").value(1200))
                .andExpect(jsonPath("$.costoPeso").value(1600))
                .andExpect(jsonPath("$.recargoServicio").value(0))
                .andExpect(jsonPath("$.subtotal").value(6800))
                .andExpect(jsonPath("$.iva").value(1292))
                .andExpect(jsonPath("$.total").value(8092));
    }

    @Test
    void servicioExpressAplicaRecargoDel60Porciento() throws Exception {
        cotizar("cliente@uq.com", cuerpo("ARMENIA_CENTRO", "ARMENIA_CENTRO", 2, 30, 20, 10, "EXPRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recargoServicio").value(4080))
                .andExpect(jsonPath("$.subtotal").value(10880))
                .andExpect(jsonPath("$.iva").value(2067))
                .andExpect(jsonPath("$.total").value(12947));
    }

    @Test
    void usaPesoVolumetricoCuandoEsMayorAlReal() throws Exception {
        // 50*40*30 / 5000 = 12 kg -> 4000 + 1200 + 9600 = 14800; IVA 2812; total 17612
        cotizar("cliente@uq.com", cuerpo("ARMENIA_CENTRO", "ARMENIA_CENTRO", 2, 50, 40, 30, "ESTANDAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pesoRealKg").value(2.0))
                .andExpect(jsonPath("$.pesoFacturableKg").value(12.0))
                .andExpect(jsonPath("$.total").value(17612));
    }

    @Test
    void cotizarNoCreaEnvioNiCobra() throws Exception {
        long antes = envioRepository.count();
        cotizar("cliente@uq.com", cuerpo("ARMENIA_CENTRO", "CALARCA", 2, 30, 20, 10, "ESTANDAR"))
                .andExpect(status().isOk());
        assertEquals(antes, envioRepository.count());
    }

    @Test
    void sinCredencialesDevuelve401() throws Exception {
        cotizar(null, cuerpo("ARMENIA_CENTRO", "CALARCA", 2, 30, 20, 10, "ESTANDAR"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void repartidorNoPuedeCotizar() throws Exception {
        cotizar("rep@uq.com", cuerpo("ARMENIA_CENTRO", "CALARCA", 2, 30, 20, 10, "ESTANDAR"))
                .andExpect(status().isForbidden());
    }

    @Test
    void pesoMayorAlMaximoDevuelve400() throws Exception {
        cotizar("cliente@uq.com", cuerpo("ARMENIA_CENTRO", "CALARCA", 31, 30, 20, 10, "ESTANDAR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.pesoKg").exists());
    }

    @Test
    void pesoFacturableMayorAlMaximoDevuelve400() throws Exception {
        cotizar("cliente@uq.com", cuerpo("ARMENIA_CENTRO", "CALARCA", 2, 60, 60, 60, "ESTANDAR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("peso facturable")));
    }

    @Test
    void zonaInexistenteDevuelve400() throws Exception {
        cotizar("cliente@uq.com", cuerpo("NO_EXISTE", "CALARCA", 2, 30, 20, 10, "ESTANDAR"))
                .andExpect(status().isBadRequest());
    }
}