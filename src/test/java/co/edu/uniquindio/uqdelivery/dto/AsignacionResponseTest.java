package co.edu.uniquindio.uqdelivery.dto;

import co.edu.uniquindio.uqdelivery.model.AsignacionEnvio;
import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AsignacionResponseTest {

    private Usuario usuario(Long id, String correo) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setCorreo(correo);
        return u;
    }

    private AsignacionEnvio asignacion(Usuario anterior) {
        Envio envio = new Envio();
        envio.setId(10L);
        AsignacionEnvio a = new AsignacionEnvio();
        a.setId(7L);
        a.setEnvio(envio);
        a.setRepartidorAnterior(anterior);
        a.setRepartidorNuevo(usuario(3L, "rep2@a.com"));
        a.setAdministrador(usuario(1L, "admin@a.com"));
        a.setMotivo("Moto averiada");
        return a;
    }

    @Test
    void mapeaTodosLosCamposDeLaReasignacion() {
        AsignacionResponse r = AsignacionResponse.desde(asignacion(usuario(2L, "rep1@a.com")));

        assertEquals(7L, r.id());
        assertEquals(10L, r.envioId());
        assertEquals(2L, r.repartidorAnteriorId());
        assertEquals(3L, r.repartidorNuevoId());
        assertEquals("admin@a.com", r.administrador());
        assertEquals("Moto averiada", r.motivo());
    }

    @Test
    void primeraAsignacionNoTieneRepartidorAnterior() {
        AsignacionResponse r = AsignacionResponse.desde(asignacion(null));

        assertNull(r.repartidorAnteriorId());
        assertEquals(3L, r.repartidorNuevoId());
    }
}
