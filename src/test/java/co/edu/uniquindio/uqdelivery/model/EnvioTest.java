package co.edu.uniquindio.uqdelivery.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Test
    void unEnvioNuevoNoTieneFechasNiValor() {
        Envio envio = new Envio();

        assertNull(envio.getFechaCreacion());
        assertNull(envio.getFechaEntrega());
        assertNull(envio.getValorTotal());
    }

    @Test
    void alPersistirAsignaLaFechaDeCreacionSiNoTiene() {
        Envio envio = new Envio();

        LocalDateTime antes = LocalDateTime.now();
        envio.antesDePersistir();

        assertNotNull(envio.getFechaCreacion());
        assertFalse(envio.getFechaCreacion().isBefore(antes));
    }

    @Test
    void alPersistirConservaLaFechaDeCreacionExistente() {
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 1, 8, 30);
        Envio envio = new Envio();
        envio.setFechaCreacion(fecha);

        envio.antesDePersistir();

        assertEquals(fecha, envio.getFechaCreacion());
    }

    @Test
    void conservaFechaDeEntregaYValorTotal() {
        LocalDateTime entrega = LocalDateTime.of(2026, 10, 2, 15, 0);
        Envio envio = new Envio();

        envio.setFechaEntrega(entrega);
        envio.setValorTotal(new BigDecimal("25000.00"));

        assertEquals(entrega, envio.getFechaEntrega());
        assertEquals(new BigDecimal("25000.00"), envio.getValorTotal());
    }
}
