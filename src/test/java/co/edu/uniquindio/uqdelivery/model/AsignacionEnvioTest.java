package co.edu.uniquindio.uqdelivery.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AsignacionEnvioTest {

    @Test
    void alPersistirRegistraLaFechaDelCambio() {
        AsignacionEnvio asignacion = new AsignacionEnvio();
        assertNull(asignacion.getFecha());

        LocalDateTime antes = LocalDateTime.now();
        asignacion.antesDePersistir();

        assertNotNull(asignacion.getFecha());
        assertFalse(asignacion.getFecha().isBefore(antes));
    }

    @Test
    void conservaLosParticipantesYElMotivo() {
        Usuario anterior = new Usuario();
        Usuario nuevo = new Usuario();
        Usuario administrador = new Usuario();
        Envio envio = new Envio();
        AsignacionEnvio asignacion = new AsignacionEnvio();

        asignacion.setEnvio(envio);
        asignacion.setRepartidorAnterior(anterior);
        asignacion.setRepartidorNuevo(nuevo);
        asignacion.setAdministrador(administrador);
        asignacion.setMotivo("Cambio de zona");

        assertSame(envio, asignacion.getEnvio());
        assertSame(anterior, asignacion.getRepartidorAnterior());
        assertSame(nuevo, asignacion.getRepartidorNuevo());
        assertSame(administrador, asignacion.getAdministrador());
        assertEquals("Cambio de zona", asignacion.getMotivo());
    }
}
