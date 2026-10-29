package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.dto.UsuarioActualizarRequest;
import co.edu.uniquindio.uqdelivery.dto.UsuarioCrearRequest;
import co.edu.uniquindio.uqdelivery.dto.UsuarioResponse;
import co.edu.uniquindio.uqdelivery.exception.ConflictoException;
import co.edu.uniquindio.uqdelivery.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;
    @Mock
    private PasswordEncoder encoder;
    @InjectMocks
    private UsuarioService service;

    private Usuario usuario(Long id, String correo, Rol rol, boolean activo) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombre("Nombre " + id);
        u.setCorreo(correo);
        u.setTelefono("3001234567");
        u.setPassword("HASH");
        u.setRol(rol);
        u.setActivo(activo);
        return u;
    }

    // ---------- crear ----------

    @Test
    void creaUsuarioCifrandoPasswordYNormalizandoCorreo() {
        when(repository.existsByCorreoIgnoreCase("ana@mail.com")).thenReturn(false);
        when(encoder.encode("Clave1234")).thenReturn("HASH");
        when(repository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioResponse r = service.crear(new UsuarioCrearRequest(
                " Ana Pérez ", " ANA@Mail.com ", "3001234567", "Clave1234", Rol.CLIENTE_EMISOR));

        assertEquals("ana@mail.com", r.correo());
        assertEquals("Ana Pérez", r.nombre());
        assertTrue(r.activo());
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).save(captor.capture());
        assertEquals("HASH", captor.getValue().getPassword());
        assertNotEquals("Clave1234", captor.getValue().getPassword());
    }

    @Test
    void rechazaCrearConCorreoDuplicado() {
        when(repository.existsByCorreoIgnoreCase("ana@mail.com")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> service.crear(new UsuarioCrearRequest(
                "Ana", "ana@mail.com", "3001234567", "Clave1234", Rol.REPARTIDOR)));
        verify(repository, never()).save(any());
    }

    // ---------- consultar ----------

    @Test
    void obtieneUsuarioPorId() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuario(1L, "a@a.com", Rol.REPARTIDOR, true)));
        assertEquals("a@a.com", service.obtener(1L).correo());
    }

    @Test
    void lanzaNoEncontradoSiNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(99L));
    }

    @Test
    void obtienePorCorreoIgnorandoMayusculas() {
        when(repository.findByCorreoIgnoreCase("a@a.com"))
                .thenReturn(Optional.of(usuario(1L, "a@a.com", Rol.REPARTIDOR, true)));
        assertEquals(Rol.REPARTIDOR, service.obtenerPorCorreo(" A@A.com ").rol());
    }

    @Test
    void obtenerPorCorreoInexistenteLanzaNoEncontrado() {
        when(repository.findByCorreoIgnoreCase("x@x.com")).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorCorreo("x@x.com"));
    }

    @Test
    void listaConFiltros() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Usuario> pagina = new PageImpl<>(List.of(usuario(1L, "a@a.com", Rol.REPARTIDOR, true)));
        when(repository.buscar(Rol.REPARTIDOR, true, pageable)).thenReturn(pagina);

        Page<UsuarioResponse> r = service.listar(Rol.REPARTIDOR, true, pageable);

        assertEquals(1, r.getTotalElements());
        assertEquals("a@a.com", r.getContent().get(0).correo());
    }

    // ---------- actualizar / rol ----------

    @Test
    void actualizaDatosYRol() {
        Usuario u = usuario(1L, "a@a.com", Rol.REPARTIDOR, true);
        when(repository.findById(1L)).thenReturn(Optional.of(u));
        when(repository.existsByCorreoIgnoreCaseAndIdNot("nuevo@a.com", 1L)).thenReturn(false);

        UsuarioResponse r = service.actualizar(1L, new UsuarioActualizarRequest(
                " Nuevo Nombre ", "NUEVO@a.com", "3109998888", Rol.CLIENTE_EMISOR));

        assertEquals("Nuevo Nombre", r.nombre());
        assertEquals("nuevo@a.com", u.getCorreo());
        assertEquals("3109998888", u.getTelefono());
        assertEquals(Rol.CLIENTE_EMISOR, u.getRol());
    }

    @Test
    void rechazaActualizarConCorreoDeOtroUsuario() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuario(1L, "a@a.com", Rol.REPARTIDOR, true)));
        when(repository.existsByCorreoIgnoreCaseAndIdNot("otro@a.com", 1L)).thenReturn(true);

        assertThrows(ConflictoException.class, () -> service.actualizar(1L, new UsuarioActualizarRequest(
                "Nombre", "otro@a.com", "3001234567", Rol.REPARTIDOR)));
    }

    @Test
    void cambiaRolDeUsuario() {
        Usuario u = usuario(1L, "a@a.com", Rol.REPARTIDOR, true);
        when(repository.findById(1L)).thenReturn(Optional.of(u));

        assertEquals(Rol.CLIENTE_EMISOR, service.cambiarRol(1L, Rol.CLIENTE_EMISOR).rol());
    }

    @Test
    void noPermiteQuitarRolAlUltimoAdministradorActivo() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuario(1L, "admin@a.com", Rol.ADMINISTRADOR, true)));
        when(repository.countByRolAndActivoTrue(Rol.ADMINISTRADOR)).thenReturn(1L);

        assertThrows(ConflictoException.class, () -> service.cambiarRol(1L, Rol.REPARTIDOR));
    }

    @Test
    void permiteQuitarRolAdministradorSiHayOtros() {
        Usuario u = usuario(1L, "admin@a.com", Rol.ADMINISTRADOR, true);
        when(repository.findById(1L)).thenReturn(Optional.of(u));
        when(repository.countByRolAndActivoTrue(Rol.ADMINISTRADOR)).thenReturn(2L);

        assertEquals(Rol.REPARTIDOR, service.cambiarRol(1L, Rol.REPARTIDOR).rol());
    }

    // ---------- desactivar / activar ----------

    @Test
    void desactivaUsuario() {
        Usuario u = usuario(2L, "rep@a.com", Rol.REPARTIDOR, true);
        when(repository.findById(2L)).thenReturn(Optional.of(u));

        UsuarioResponse r = service.desactivar(2L, "admin@a.com");

        assertFalse(r.activo());
        assertFalse(u.isActivo());
    }

    @Test
    void desactivarUsuarioYaInactivoEsIdempotente() {
        Usuario u = usuario(2L, "rep@a.com", Rol.REPARTIDOR, false);
        when(repository.findById(2L)).thenReturn(Optional.of(u));

        assertFalse(service.desactivar(2L, "admin@a.com").activo());
    }

    @Test
    void noPermiteDesactivarLaPropiaCuenta() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuario(1L, "admin@a.com", Rol.ADMINISTRADOR, true)));

        assertThrows(ConflictoException.class, () -> service.desactivar(1L, "ADMIN@a.com"));
    }

    @Test
    void noPermiteDesactivarAlUltimoAdministrador() {
        when(repository.findById(3L)).thenReturn(Optional.of(usuario(3L, "otro@a.com", Rol.ADMINISTRADOR, true)));
        when(repository.countByRolAndActivoTrue(Rol.ADMINISTRADOR)).thenReturn(1L);

        assertThrows(ConflictoException.class, () -> service.desactivar(3L, "admin@a.com"));
    }

    @Test
    void desactivaAdministradorSiHayOtrosActivos() {
        Usuario u = usuario(3L, "otro@a.com", Rol.ADMINISTRADOR, true);
        when(repository.findById(3L)).thenReturn(Optional.of(u));
        when(repository.countByRolAndActivoTrue(Rol.ADMINISTRADOR)).thenReturn(2L);

        assertFalse(service.desactivar(3L, "admin@a.com").activo());
    }

    @Test
    void reactivaUsuario() {
        Usuario u = usuario(2L, "rep@a.com", Rol.REPARTIDOR, false);
        when(repository.findById(2L)).thenReturn(Optional.of(u));

        assertTrue(service.activar(2L).activo());
    }
}
