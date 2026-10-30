package co.edu.uniquindio.uqdelivery.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnvioTest {

    @Test
    void unEnvioNuevoIniciaPendienteYSinRepartidor() {
        Envio envio = new Envio();

        assertEquals(EstadoEnvio.PENDIENTE, envio.getEstado());
        assertNull(envio.getRepartidor());
        assertNull(envio.getId());
    }

    @Test
    void permiteAsociarRepartidorYCambiarDeEstado() {
        Usuario repartidor = new Usuario();
        Envio envio = new Envio();

        envio.setRepartidor(repartidor);
        envio.setEstado(EstadoEnvio.ASIGNADO);

        assertSame(repartidor, envio.getRepartidor());
        assertEquals(EstadoEnvio.ASIGNADO, envio.getEstado());
    }
}
