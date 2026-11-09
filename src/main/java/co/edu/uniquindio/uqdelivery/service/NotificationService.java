package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.dto.NotificationResponseDto;
import co.edu.uniquindio.uqdelivery.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.exception.SolicitudInvalidaException;
import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.Notification;
import co.edu.uniquindio.uqdelivery.model.NotificationType;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.NotificationRepository;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UsuarioRepository usuarioRepository) {
        this.notificationRepository = notificationRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void notifyStatusChange(Envio shipment, EstadoEnvio previousStatus, EstadoEnvio newStatus) {
        if (newStatus == null || newStatus == previousStatus) {
            return;
        }

        NotificationType type = typeFor(newStatus);
        String title = "Actualización de envío " + shipment.getCodigoSeguimiento();
        String message = "El estado de su envío cambió a " + newStatus + ".";
        createForRecipients(shipment, type, title, message);
    }

    @Transactional
    public void createCancellationAlert(Envio shipment, String reason) {
        String normalizedReason = reason == null ? "" : reason.trim();
        if (normalizedReason.isBlank() || normalizedReason.length() > 255) {
            throw new SolicitudInvalidaException("El motivo de cancelación debe contener entre 1 y 255 caracteres");
        }

        String title = "Envío cancelado " + shipment.getCodigoSeguimiento();
        String message = "El envío fue cancelado. Motivo: " + normalizedReason;
        createForRecipients(shipment, NotificationType.CANCELLED, title, message);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotifications(String username) {
        Usuario user = findUser(username);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(NotificationResponseDto::from)
                .toList();
    }

    @Transactional
    public NotificationResponseDto markAsRead(Long id, String username) {
        Usuario user = findUser(username);
        Notification notification = notificationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación no encontrada"));

        notification.setRead(true);
        return NotificationResponseDto.from(notification);
    }

    private void createForRecipients(Envio shipment, NotificationType type, String title, String message) {
        List<Usuario> recipients = new ArrayList<>(2);
        if (shipment.getCliente() != null) {
            recipients.add(shipment.getCliente());
        }
        Usuario courier = shipment.getRepartidor();
        if (courier != null && recipients.stream().noneMatch(user -> user.getId().equals(courier.getId()))) {
            recipients.add(courier);
        }

        for (Usuario recipient : recipients) {
            Notification notification = new Notification();
            notification.setUser(recipient);
            notification.setShipment(shipment);
            notification.setType(type);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setRead(false);
            notificationRepository.save(notification);
        }
    }

    private NotificationType typeFor(EstadoEnvio status) {
        return switch (status) {
            case CANCELADO -> NotificationType.CANCELLED;
            case ENTREGADO -> NotificationType.DELIVERED;
            case EN_RUTA -> NotificationType.WARNING;
            case PENDIENTE, ASIGNADO -> NotificationType.INFO;
        };
    }

    private Usuario findUser(String username) {
        return usuarioRepository.findByCorreoIgnoreCase(username.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado"));
    }
}
