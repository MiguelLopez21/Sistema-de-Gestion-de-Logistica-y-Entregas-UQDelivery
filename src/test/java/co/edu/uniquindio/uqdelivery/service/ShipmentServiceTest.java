package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.dto.ShipmentHistoryFilterDto;
import co.edu.uniquindio.uqdelivery.exception.ConflictoException;
import co.edu.uniquindio.uqdelivery.exception.ParametroInvalidoException;
import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.EnvioRepository;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ShipmentService service;

    @Test
    void historialLimitaResultadosAlClienteAutenticado() {
        Usuario client = user(3L, Rol.CLIENTE_EMISOR);
        when(usuarioRepository.findByCorreoIgnoreCase("client@uqdelivery.com")).thenReturn(Optional.of(client));
        when(envioRepository.buscarHistorial(eq(3L), eq(Rol.CLIENTE_EMISOR), eq(EstadoEnvio.PENDIENTE),
                any(), any(), eq("UQD"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.getShipmentHistory(
                new ShipmentHistoryFilterDto("UQD", EstadoEnvio.PENDIENTE,
                        LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 8), 0, 10),
                authentication("client@uqdelivery.com"));

        verify(envioRepository).buscarHistorial(eq(3L), eq(Rol.CLIENTE_EMISOR), eq(EstadoEnvio.PENDIENTE),
                eq(LocalDate.of(2026, 11, 1).atStartOfDay()),
                eq(LocalDate.of(2026, 11, 9).atStartOfDay()), eq("UQD"), any(PageRequest.class));
    }

    @Test
    void administradorConsultaHistorialSinFiltrarPorUsuario() {
        Usuario admin = user(1L, Rol.ADMINISTRADOR);
        when(usuarioRepository.findByCorreoIgnoreCase("admin@uqdelivery.com")).thenReturn(Optional.of(admin));
        when(envioRepository.buscarHistorial(eq(null), eq(Rol.ADMINISTRADOR), eq(null),
                eq(null), eq(null), eq(null), any(PageRequest.class)))
                .thenReturn(Page.empty());

        service.getShipmentHistory(
                new ShipmentHistoryFilterDto(null, null, null, null, 0, 20),
                authentication("admin@uqdelivery.com"));

        verify(envioRepository).buscarHistorial(eq(null), eq(Rol.ADMINISTRADOR), eq(null),
                eq(null), eq(null), eq(null), any(PageRequest.class));
    }

    @Test
    void rechazaBusquedaDeHistorialMayorA100Caracteres() {
        ShipmentHistoryFilterDto filter = new ShipmentHistoryFilterDto(
                "x".repeat(101), null, null, null, 0, 20);

        assertThrows(ParametroInvalidoException.class,
                () -> service.getShipmentHistory(filter, authentication("client@uqdelivery.com")));
    }

    @Test
    void clientePropietarioPuedeCancelarAntesDeIniciarRuta() {
        Usuario client = user(3L, Rol.CLIENTE_EMISOR);
        Envio shipment = new Envio();
        shipment.setId(8L);
        shipment.setCliente(client);
        shipment.setEstado(EstadoEnvio.PENDIENTE);
        when(usuarioRepository.findByCorreoIgnoreCase("client@uqdelivery.com")).thenReturn(Optional.of(client));
        when(envioRepository.findById(8L)).thenReturn(Optional.of(shipment));

        service.cancelShipment(8L, "Cambio de planes", authentication("client@uqdelivery.com"));

        assertEquals(EstadoEnvio.CANCELADO, shipment.getEstado());
        verify(notificationService).createCancellationAlert(shipment, "Cambio de planes");
    }

    @Test
    void noPermiteCancelarEnvioQueYaEstaEnRuta() {
        Usuario client = user(3L, Rol.CLIENTE_EMISOR);
        Envio shipment = new Envio();
        shipment.setId(8L);
        shipment.setCliente(client);
        shipment.setEstado(EstadoEnvio.EN_RUTA);
        when(usuarioRepository.findByCorreoIgnoreCase("client@uqdelivery.com")).thenReturn(Optional.of(client));
        when(envioRepository.findById(8L)).thenReturn(Optional.of(shipment));

        assertThrows(ConflictoException.class,
                () -> service.cancelShipment(8L, "Cambio de planes", authentication("client@uqdelivery.com")));
    }

    private Usuario user(Long id, Rol role) {
        Usuario user = new Usuario();
        user.setId(id);
        user.setCorreo(role == Rol.ADMINISTRADOR ? "admin@uqdelivery.com" : "client@uqdelivery.com");
        user.setRol(role);
        return user;
    }

    private Authentication authentication(String username) {
        return new UsernamePasswordAuthenticationToken(username, "password");
    }
}
