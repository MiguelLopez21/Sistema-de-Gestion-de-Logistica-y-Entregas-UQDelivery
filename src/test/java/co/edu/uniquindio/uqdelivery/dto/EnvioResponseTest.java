package co.edu.uniquindio.uqdelivery.dto;

import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnvioResponseTest {

    @Test
    void envioSinRepartidorNoExponeDatosDeRepartidor() {
        Envio envio = new Envio();
        envio.setId(1L);

        EnvioResponse r = EnvioResponse.desde(envio);

        assertEquals(1L, r.id());
        assertEquals(EstadoEnvio.PENDIENTE, r.estado());
        assertNull(r.repartidorId());
        assertNull(r.repartidorNombre());
    }

    @Test
    void envioAsignadoExponeIdYNombreDelRepartidor() {
        Usuario repartidor = new Usuario();
        repartidor.setId(5L);
        repartidor.setNombre("Carlos Ruiz");
        Envio envio = new Envio();
        envio.setId(1L);
        envio.setEstado(EstadoEnvio.ASIGNADO);
        envio.setRepartidor(repartidor);

        EnvioResponse r = EnvioResponse.desde(envio);

        assertEquals(EstadoEnvio.ASIGNADO, r.estado());
        assertEquals(5L, r.repartidorId());
        assertEquals("Carlos Ruiz", r.repartidorNombre());
    }
}
