package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.Notification;
import co.edu.uniquindio.uqdelivery.model.NotificationType;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.NotificationRepository;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private NotificationService service;

    @Test
    void cambioDeEstadoNotificaAlClienteYAlRepartidor() {
        Usuario client = usuario(1L);
        Usuario courier = usuario(2L);
        Envio shipment = envio(client, courier);

        service.notifyStatusChange(shipment, EstadoEnvio.PENDIENTE, EstadoEnvio.ASIGNADO);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertEquals(2, captor.getAllValues().size());
        assertTrue(captor.getAllValues().stream()
                .allMatch(notification -> notification.getType() == NotificationType.INFO));
        assertTrue(captor.getAllValues().stream()
                .allMatch(notification -> notification.getShipment() == shipment));
    }

    @Test
    void cancelacionIncluyeElMotivoEnLaAlerta() {
        Usuario client = usuario(1L);
        Envio shipment = envio(client, null);

        service.createCancellationAlert(shipment, "  Cambio de planes  ");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(NotificationType.CANCELLED, captor.getValue().getType());
        assertTrue(captor.getValue().getMessage().contains("Motivo: Cambio de planes"));
    }

    @Test
    void noDuplicaNotificacionSiElEstadoNoCambio() {
        service.notifyStatusChange(envio(usuario(1L), null), EstadoEnvio.PENDIENTE, EstadoEnvio.PENDIENTE);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void marcaComoLeidaSoloLaNotificacionDelUsuarioAutenticado() {
        Usuario user = usuario(1L);
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(NotificationType.INFO);
        notification.setTitle("Estado actualizado");
        notification.setMessage("Mensaje");
        when(usuarioRepository.findByCorreoIgnoreCase("user@uqdelivery.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(notification));

        service.markAsRead(7L, "user@uqdelivery.com");

        assertTrue(notification.isRead());
    }

    private Usuario usuario(Long id) {
        Usuario user = new Usuario();
        user.setId(id);
        user.setCorreo("user@uqdelivery.com");
        return user;
    }

    private Envio envio(Usuario client, Usuario courier) {
        Envio shipment = new Envio();
        shipment.setCodigoSeguimiento("UQD-TEST");
        shipment.setCliente(client);
        shipment.setRepartidor(courier);
        return shipment;
    }
}
