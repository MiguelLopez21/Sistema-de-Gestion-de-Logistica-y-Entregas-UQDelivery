package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.dto.AsignacionRequest;
import co.edu.uniquindio.uqdelivery.dto.AsignacionResponse;
import co.edu.uniquindio.uqdelivery.dto.EnvioResponse;
import co.edu.uniquindio.uqdelivery.exception.ConflictoException;
import co.edu.uniquindio.uqdelivery.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.model.AsignacionEnvio;
import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.AsignacionEnvioRepository;
import co.edu.uniquindio.uqdelivery.repository.EnvioRepository;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignacionServiceTest {

    @Mock
    private EnvioRepository envioRepository;
    @Mock
    private AsignacionEnvioRepository asignacionRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @InjectMocks
    private AsignacionService service;

    private Usuario usuario(Long id, String correo, Rol rol, boolean activo) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombre("Nombre " + id);
        u.setCorreo(correo);
        u.setRol(rol);
        u.setActivo(activo);
        return u;
    }

    private Envio envio(Long id, EstadoEnvio estado, Usuario repartidor) {
        Envio e = new Envio();
        e.setId(id);
        e.setEstado(estado);
        e.setRepartidor(repartidor);
        return e;
    }

    private Usuario admin() {
        return usuario(1L, "admin@a.com", Rol.ADMINISTRADOR, true);
    }

    @Test
    void asignaRepartidorActivoAEnvioPendiente() {
        Usuario repartidor = usuario(2L, "rep@a.com", Rol.REPARTIDOR, true);
        Envio envio = envio(10L, EstadoEnvio.PENDIENTE, null);
        when(envioRepository.findById(10L)).thenReturn(Optional.of(envio));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(repartidor));
        when(usuarioRepository.findByCorreoIgnoreCase("admin@a.com")).thenReturn(Optional.of(admin()));

        EnvioResponse r = service.asignar(10L, new AsignacionRequest(2L, null), "admin@a.com");

        assertEquals(EstadoEnvio.ASIGNADO, r.estado());
        assertEquals(2L, r.repartidorId());
        assertEquals(EstadoEnvio.ASIGNADO, envio.getEstado());
        assertSame(repartidor, envio.getRepartidor());
    }

    @Test
    void asignarRegistraHistorialSinRepartidorAnterior() {
        Usuario repartidor = usuario(2L, "rep@a.com", Rol.REPARTIDOR, true);
        when(envioRepository.findById(10L)).thenReturn(Optional.of(envio(10L, EstadoEnvio.PENDIENTE, null)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(repartidor));
        when(usuarioRepository.findByCorreoIgnoreCase("admin@a.com")).thenReturn(Optional.of(admin()));

        service.asignar(10L, new AsignacionRequest(2L, "  Primera asignación  "), "admin@a.com");

        ArgumentCaptor<AsignacionEnvio> captor = ArgumentCaptor.forClass(AsignacionEnvio.class);
        verify(asignacionRepository).save(captor.capture());
        AsignacionEnvio registro = captor.getValue();
        assertNull(registro.getRepartidorAnterior());
        assertSame(repartidor, registro.getRepartidorNuevo());
        assertEquals("admin@a.com", registro.getAdministrador().getCorreo());
        assertEquals("Primera asignación", registro.getMotivo());
    }

    @Test
    void motivoEnBlancoSeGuardaComoNulo() {
        when(envioRepository.findById(10L)).thenReturn(Optional.of(envio(10L, EstadoEnvio.PENDIENTE, null)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "rep@a.com", Rol.REPARTIDOR, true)));
        when(usuarioRepository.findByCorreoIgnoreCase("admin@a.com")).thenReturn(Optional.of(admin()));

        service.asignar(10L, new AsignacionRequest(2L, "   "), "admin@a.com");

        ArgumentCaptor<AsignacionEnvio> captor = ArgumentCaptor.forClass(AsignacionEnvio.class);
        verify(asignacionRepository).save(captor.capture());
        assertNull(captor.getValue().getMotivo());
    }

    @Test
    void asignarNormalizaElCorreoDelAdministrador() {
        when(envioRepository.findById(10L)).thenReturn(Optional.of(envio(10L, EstadoEnvio.PENDIENTE, null)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "rep@a.com", Rol.REPARTIDOR, true)));
        when(usuarioRepository.findByCorreoIgnoreCase("admin@a.com")).thenReturn(Optional.of(admin()));

        assertDoesNotThrow(() -> service.asignar(10L, new AsignacionRequest(2L, null), " ADMIN@a.com "));
    }

    @Test
    void asignarEnvioInexistenteLanzaNoEncontrado() {
        when(envioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.asignar(99L, new AsignacionRequest(2L, null), "admin@a.com"));
        verify(asignacionRepository, never()).save(any());
    }

    @Test
    void asignarRepartidorInexistenteLanzaNoEncontrado() {
        when(envioRepository.findById(10L)).thenReturn(Optional.of(envio(10L, EstadoEnvio.PENDIENTE, null)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.asignar(10L, new AsignacionRequest(2L, null), "admin@a.com"));
    }

    @Test
    void rechazaAsignarUsuarioQueNoEsRepartidor() {
        Envio envio = envio(10L, EstadoEnvio.PENDIENTE, null);
        when(envioRepository.findById(10L)).thenReturn(Optional.of(envio));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, "cli@a.com", Rol.CLIENTE_EMISOR, true)));

        assertThrows(ConflictoException.class,
                () -> service.asignar(10L, new AsignacionRequest(3L, null), "admin@a.com"));
        assertEquals(EstadoEnvio.PENDIENTE, envio.getEstado());
        verify(asignacionRepository, never()).save(any());
    }

    @Test
    void rechazaAsignarRepartidorDesactivado() {
        Envio envio = envio(10L, EstadoEnvio.PENDIENTE, null);
        when(envioRepository.findById(10L)).thenReturn(Optional.of(envio));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "rep@a.com", Rol.REPARTIDOR, false)));

        assertThrows(ConflictoException.class,
                () -> service.asignar(10L, new AsignacionRequest(2L, null), "admin@a.com"));
        assertNull(envio.getRepartidor());
    }

    @Test
    void rechazaAsignarEnvioQueYaNoEstaPendiente() {
        for (EstadoEnvio estado : List.of(EstadoEnvio.ASIGNADO, EstadoEnvio.EN_RUTA, EstadoEnvio.ENTREGADO)) {
            when(envioRepository.findById(10L)).thenReturn(Optional.of(envio(10L, estado, null)));

            assertThrows(ConflictoException.class,
                    () -> service.asignar(10L, new AsignacionRequest(2L, null), "admin@a.com"));
        }
        verify(asignacionRepository, never()).save(any());
    }

    @Test
    void reasignaEnvioAsignadoYLoRetiraDelRepartidorAnterior() {
        Usuario anterior = usuario(2L, "rep1@a.com", Rol.REPARTIDOR, true);
        Usuario nuevo = usuario(3L, "rep2@a.com", Rol.REPARTIDOR, true);
        Envio envio = envio(10L, EstadoEnvio.ASIGNADO, anterior);
        when(envioRepository.findById(10L)).thenReturn(Optional.of(envio));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(nuevo));
        when(usuarioRepository.findByCorreoIgnoreCase("admin@a.com")).thenReturn(Optional.of(admin()));

        EnvioResponse r = service.reasignar(10L, new AsignacionRequest(3L, "Moto averiada"), "admin@a.com");

        assertEquals(3L, r.repartidorId());
        assertSame(nuevo, envio.getRepartidor());
        assertEquals(EstadoEnvio.ASIGNADO, envio.getEstado());
    }

    @Test
    void reasignarRegistraQuienYElRepartidorAnterior() {
        Usuario anterior = usuario(2L, "rep1@a.com", Rol.REPARTIDOR, true);
        Usuario nuevo = usuario(3L, "rep2@a.com", Rol.REPARTIDOR, true);
        when(envioRepository.findById(10L)).thenReturn(Optional.of(envio(10L, EstadoEnvio.ASIGNADO, anterior)));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(nuevo));
        when(usuarioRepository.findByCorreoIgnoreCase("admin@a.com")).thenReturn(Optional.of(admin()));

        service.reasignar(10L, new AsignacionRequest(3L, "Moto averiada"), "admin@a.com");

        ArgumentCaptor<AsignacionEnvio> captor = ArgumentCaptor.forClass(AsignacionEnvio.class);
        verify(asignacionRepository).save(captor.capture());
        AsignacionEnvio registro = captor.getValue();
        assertSame(anterior, registro.getRepartidorAnterior());
        assertSame(nuevo, registro.getRepartidorNuevo());
        assertEquals("admin@a.com", registro.getAdministrador().getCorreo());
        assertEquals("Moto averiada", registro.getMotivo());
    }

    @Test
    void reasignarEnvioEnRutaLoDevuelveAAsignado() {
        Usuario anterior = usuario(2L, "rep1@a.com", Rol.REPARTIDOR, true);
        Envio envio = envio(10L, EstadoEnvio.EN_RUTA, anterior);
        when(envioRepository.findById(10L)).thenReturn(Optional.of(envio));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, "rep2@a.com", Rol.REPARTIDOR, true)));
        when(usuarioRepository.findByCorreoIgnoreCase("admin@a.com")).thenReturn(Optional.of(admin()));

        service.reasignar(10L, new AsignacionRequest(3L, null), "admin@a.com");

        assertEquals(EstadoEnvio.ASIGNADO, envio.getEstado());
    }

    @Test
    void rechazaReasignarEnvioEntregado() {
        Usuario anterior = usuario(2L, "rep1@a.com", Rol.REPARTIDOR, true);
        Envio envio = envio(10L, EstadoEnvio.ENTREGADO, anterior);
        when(envioRepository.findById(10L)).thenReturn(Optional.of(envio));

        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> service.reasignar(10L, new AsignacionRequest(3L, null), "admin@a.com"));

        assertTrue(ex.getMessage().contains("entregado"));
        assertSame(anterior, envio.getRepartidor());
        verify(asignacionRepository, never()).save(any());
    }

    @Test
    void rechazaReasignarEnvioPendiente() {
        when(envioRepository.findById(10L)).thenReturn(Optional.of(envio(10L, EstadoEnvio.PENDIENTE, null)));

        assertThrows(ConflictoException.class,
                () -> service.reasignar(10L, new AsignacionRequest(3L, null), "admin@a.com"));
    }

    @Test
    void rechazaReasignarAlMismoRepartidor() {
        Usuario actual = usuario(2L, "rep1@a.com", Rol.REPARTIDOR, true);
        when(envioRepository.findById(10L)).thenReturn(Optional.of(envio(10L, EstadoEnvio.ASIGNADO, actual)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(actual));

        assertThrows(ConflictoException.class,
                () -> service.reasignar(10L, new AsignacionRequest(2L, null), "admin@a.com"));
        verify(asignacionRepository, never()).save(any());
    }

    @Test
    void rechazaReasignarARepartidorDesactivado() {
        Usuario actual = usuario(2L, "rep1@a.com", Rol.REPARTIDOR, true);
        when(envioRepository.findById(10L)).thenReturn(Optional.of(envio(10L, EstadoEnvio.ASIGNADO, actual)));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, "rep2@a.com", Rol.REPARTIDOR, false)));

        assertThrows(ConflictoException.class,
                () -> service.reasignar(10L, new AsignacionRequest(3L, null), "admin@a.com"));
    }

    @Test
    void reasignarEnvioInexistenteLanzaNoEncontrado() {
        when(envioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.reasignar(99L, new AsignacionRequest(3L, null), "admin@a.com"));
    }

    @Test
    void listaEnviosPorEstado() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Envio> pagina = new PageImpl<>(List.of(envio(10L, EstadoEnvio.PENDIENTE, null)));
        when(envioRepository.buscar(EstadoEnvio.PENDIENTE, pageable)).thenReturn(pagina);

        Page<EnvioResponse> r = service.listarEnvios(EstadoEnvio.PENDIENTE, pageable);

        assertEquals(1, r.getTotalElements());
        assertEquals(EstadoEnvio.PENDIENTE, r.getContent().get(0).estado());
        assertNull(r.getContent().get(0).repartidorId());
    }

    @Test
    void historialDevuelveLosRegistrosDelEnvio() {
        Usuario repartidor = usuario(2L, "rep@a.com", Rol.REPARTIDOR, true);
        Envio envio = envio(10L, EstadoEnvio.ASIGNADO, repartidor);
        AsignacionEnvio registro = new AsignacionEnvio();
        registro.setEnvio(envio);
        registro.setRepartidorNuevo(repartidor);
        registro.setAdministrador(admin());
        when(envioRepository.findById(10L)).thenReturn(Optional.of(envio));
        when(asignacionRepository.findByEnvioIdOrderByIdAsc(10L)).thenReturn(List.of(registro));

        List<AsignacionResponse> r = service.historial(10L);

        assertEquals(1, r.size());
        assertEquals(10L, r.get(0).envioId());
        assertEquals("admin@a.com", r.get(0).administrador());
    }

    @Test
    void historialDeEnvioInexistenteLanzaNoEncontrado() {
        when(envioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.historial(99L));
    }

    @Test
    void entregasDelRepartidorConsultaSoloEstadosEnEntrega() {
        Usuario repartidor = usuario(2L, "rep@a.com", Rol.REPARTIDOR, true);
        when(envioRepository.findByRepartidorCorreoIgnoreCaseAndEstadoInOrderByIdAsc(
                "rep@a.com", List.of(EstadoEnvio.ASIGNADO, EstadoEnvio.EN_RUTA)))
                .thenReturn(List.of(envio(10L, EstadoEnvio.ASIGNADO, repartidor)));

        List<EnvioResponse> r = service.entregasDelRepartidor(" REP@a.com ");

        assertEquals(1, r.size());
        assertEquals(10L, r.get(0).id());
        assertEquals(2L, r.get(0).repartidorId());
    }
}
