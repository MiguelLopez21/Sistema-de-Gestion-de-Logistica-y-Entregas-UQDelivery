package co.edu.uniquindio.uqdelivery.controller;

import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.EnvioRepository;
import co.edu.uniquindio.uqdelivery.repository.NotificationRepository;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sin @Transactional: la petición hace commit/rollback reales, que es lo que se quiere comprobar
 * (envío NO persistido tras pago rechazado; un único envío ante peticiones concurrentes con la misma clave).
 */
@SpringBootTest(properties = {
        "uqdelivery.admin.correo=admin@uqdelivery.com",
        "uqdelivery.admin.password=Admin12345"})
@AutoConfigureMockMvc
class SolicitudEnvioPagoRechazadoIntegrationTest {

    private static final String URL = "/api/cliente/envios";
    private static final String CORREO = "cliente.real@uq.com";
    private static final String CLAVE = "Clave1234";

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

    @BeforeEach
    void crearCliente() {
        limpiar();
        Usuario u = new Usuario();
        u.setNombre("Cliente Real");
        u.setCorreo(CORREO);
        u.setTelefono("3001234567");
        u.setPassword(passwordEncoder.encode(CLAVE));
        u.setRol(Rol.CLIENTE_EMISOR);
        usuarioRepository.save(u);
    }

    @AfterEach
    void limpiar() {
        usuarioRepository.findByCorreoIgnoreCase(CORREO).ifPresent(u -> {
            notificationRepository.deleteAll();
            envioRepository.findAll().stream()
                    .filter(e -> e.getCliente() != null && e.getCliente().getId().equals(u.getId()))
                    .forEach(envioRepository::delete);
            usuarioRepository.delete(u);
        });
    }

    private static String json(String tarjeta) {
        return """
                {"zonaOrigen":"ARMENIA_CENTRO","direccionOrigen":"Calle 10 # 5-20","zonaDestino":"CALARCA",
                 "direccionDestino":"Carrera 15 # 8-30","destinatarioNombre":"Ana Pérez",
                 "destinatarioTelefono":"3001234567","pesoKg":2,"largoCm":30,"anchoCm":20,"altoCm":10,
                 "descripcion":"Libros","tipoServicio":"ESTANDAR",
                 "pago":{"numeroTarjeta":"%s","titular":"Juan Cliente"}}
                """.formatted(tarjeta);
    }

    private long enviosDelCliente() {
        return envioRepository.countByClienteCorreoIgnoreCase(CORREO);
    }

    @Test
    void pagoRechazadoNoDejaNingunEnvioPersistido() throws Exception {
        mockMvc.perform(post(URL).with(httpBasic(CORREO, CLAVE)).contentType(MediaType.APPLICATION_JSON)
                        .content(json("4000000000000000")))
                .andExpect(status().isPaymentRequired());
        mockMvc.perform(post(URL).with(httpBasic(CORREO, CLAVE)).contentType(MediaType.APPLICATION_JSON)
                        .content(json("4000000000001111")))
                .andExpect(status().isPaymentRequired());

        assertEquals(0, enviosDelCliente());
    }

    @Test
    void peticionesConcurrentesConLaMismaClaveCreanUnSoloEnvio() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            Callable<Integer> llamada = () -> mockMvc.perform(post(URL).with(httpBasic(CORREO, CLAVE))
                            .header("Idempotency-Key", "clave-concurrente-1")
                            .contentType(MediaType.APPLICATION_JSON).content(json("4242424242424242")))
                    .andReturn().getResponse().getStatus();
            List<Future<Integer>> futuros = pool.invokeAll(List.of(llamada, llamada, llamada, llamada));
            int creados = 0;
            for (Future<Integer> f : futuros) {
                int estado = f.get();
                assertTrue(estado == 201 || estado == 200 || estado == 409, "estado inesperado " + estado);
                if (estado == 201) creados++;
            }
            assertEquals(1, creados);
            assertEquals(1, enviosDelCliente());
        } finally {
            pool.shutdownNow();
        }
    }
}
