package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.dto.DetalleEnvioResponse;
import co.edu.uniquindio.uqdelivery.dto.TiposEvidencia;
import co.edu.uniquindio.uqdelivery.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.EnvioRepository;
import co.edu.uniquindio.uqdelivery.repository.EvidenciaEntregaRepository;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaEnvioServiceTest {

    @Mock
    private EnvioRepository envioRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EvidenciaEntregaRepository evidenciaRepository;
    @InjectMocks
    private ConsultaEnvioService service;

    private Usuario repartidor;

    @BeforeEach
    void prepararRepartidor() {
        repartidor = usuario(2L, "rep@a.com", Rol.REPARTIDOR);
        when(usuarioRepository.findByCorreoIgnoreCase("rep@a.com")).thenReturn(Optional.of(repartidor));
    }

    private Usuario usuario(Long id, String correo, Rol rol) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombre("Nombre " + id);
        u.setCorreo(correo);
        u.setRol(rol);
        return u;
    }

    private Envio envio(Usuario asignado) {
        Envio e = new Envio();
        e.setId(10L);
        e.setEstado(EstadoEnvio.EN_RUTA);
        e.setCodigoSeguimiento("UQD-ABC");
        e.setRepartidor(asignado);
        when(envioRepository.findById(10L)).thenReturn(Optional.of(e));
        return e;
    }

    @Test
    void devuelveElDetalleDeUnEnvioDelRepartidor() {
        envio(repartidor);
        when(evidenciaRepository.tiposDelEnvio(10L)).thenReturn(Optional.of(new TiposEvidencia("image/png", null)));

        DetalleEnvioResponse r = service.detalleDelRepartidor(10L, "rep@a.com");

        assertEquals(10L, r.id());
        assertEquals("UQD-ABC", r.codigoSeguimiento());
        assertEquals(2L, r.repartidorId());
        assertTrue(r.tieneFoto());
        assertFalse(r.tieneFirma());
    }

    @Test
    void sinEvidenciaElDetalleIndicaQueNoHayFotoNiFirma() {
        envio(repartidor);

        DetalleEnvioResponse r = service.detalleDelRepartidor(10L, "REP@a.com ");

        assertFalse(r.tieneFoto());
        assertFalse(r.tieneFirma());
    }

    @Test
    void rechazaUnEnvioAsignadoAOtroRepartidor() {
        envio(usuario(3L, "otro@a.com", Rol.REPARTIDOR));

        assertThrows(AccessDeniedException.class, () -> service.detalleDelRepartidor(10L, "rep@a.com"));
        verifyNoInteractions(evidenciaRepository);
    }

    @Test
    void rechazaUnEnvioSinRepartidor() {
        envio(null);

        assertThrows(AccessDeniedException.class, () -> service.detalleDelRepartidor(10L, "rep@a.com"));
    }

    @Test
    void falla404SiElEnvioNoExiste() {
        when(envioRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.detalleDelRepartidor(10L, "rep@a.com"));
    }
}
