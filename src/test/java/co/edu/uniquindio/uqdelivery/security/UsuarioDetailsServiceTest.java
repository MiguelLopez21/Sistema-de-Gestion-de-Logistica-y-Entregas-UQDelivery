package co.edu.uniquindio.uqdelivery.security;

import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository repository;
    @InjectMocks
    private UsuarioDetailsService service;

    private Usuario usuario(boolean activo) {
        Usuario u = new Usuario();
        u.setCorreo("rep@a.com");
        u.setPassword("HASH");
        u.setRol(Rol.REPARTIDOR);
        u.setActivo(activo);
        return u;
    }

    @Test
    void cargaUsuarioActivoConSuRol() {
        when(repository.findByCorreoIgnoreCase("rep@a.com")).thenReturn(Optional.of(usuario(true)));

        UserDetails d = service.loadUserByUsername(" rep@a.com ");

        assertTrue(d.isEnabled());
        assertEquals("ROLE_REPARTIDOR", d.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void usuarioDesactivadoNoEstaHabilitado() {
        when(repository.findByCorreoIgnoreCase("rep@a.com")).thenReturn(Optional.of(usuario(false)));

        assertFalse(service.loadUserByUsername("rep@a.com").isEnabled());
    }

    @Test
    void usuarioInexistenteLanzaExcepcionGenerica() {
        when(repository.findByCorreoIgnoreCase("x@x.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("x@x.com"));
    }
}
