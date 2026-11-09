package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.dto.ShipmentHistoryFilterDto;
import co.edu.uniquindio.uqdelivery.dto.ShipmentHistoryResponseDto;
import co.edu.uniquindio.uqdelivery.exception.ConflictoException;
import co.edu.uniquindio.uqdelivery.exception.ParametroInvalidoException;
import co.edu.uniquindio.uqdelivery.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.EnvioRepository;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class ShipmentService {

    private final EnvioRepository envioRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificationService notificationService;

    public ShipmentService(EnvioRepository envioRepository,
                           UsuarioRepository usuarioRepository,
                           NotificationService notificationService) {
        this.envioRepository = envioRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public Page<ShipmentHistoryResponseDto> getShipmentHistory(ShipmentHistoryFilterDto filter,
                                                                Authentication authentication) {
        validateHistoryFilter(filter);
        Usuario user = findAuthenticatedUser(authentication);
        Rol role = user.getRol();
        if (role != Rol.ADMINISTRADOR && role != Rol.CLIENTE_EMISOR && role != Rol.REPARTIDOR) {
            throw new AccessDeniedException("No tiene permiso para consultar el historial de envíos");
        }

        Long userId = role == Rol.ADMINISTRADOR ? null : user.getId();
        LocalDateTime start = filter.startDate() == null ? null : filter.startDate().atStartOfDay();
        LocalDateTime endExclusive = filter.endDate() == null
                ? null
                : filter.endDate().plusDays(1).atStartOfDay();
        PageRequest pageRequest = PageRequest.of(
                filter.page(),
                filter.size(),
                Sort.by(Sort.Direction.DESC, "fechaCreacion").and(Sort.by(Sort.Direction.DESC, "id")));

        return envioRepository.buscarHistorial(userId, role, filter.status(), start, endExclusive, filter.query(), pageRequest)
                .map(ShipmentHistoryResponseDto::from);
    }

    @Transactional
    public ShipmentHistoryResponseDto cancelShipment(Long shipmentId, String reason, Authentication authentication) {
        Usuario actor = findAuthenticatedUser(authentication);
        Envio shipment = envioRepository.findById(shipmentId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Envío con id " + shipmentId + " no encontrado"));

        boolean owner = shipment.getCliente() != null
                && shipment.getCliente().getId().equals(actor.getId());
        boolean administrator = actor.getRol() == Rol.ADMINISTRADOR;
        if (!owner && !administrator) {
            throw new AccessDeniedException("Solo el cliente propietario o un administrador puede cancelar el envío");
        }
        if (shipment.getEstado() != EstadoEnvio.PENDIENTE && shipment.getEstado() != EstadoEnvio.ASIGNADO) {
            throw new ConflictoException("Solo se puede cancelar un envío antes de iniciar la ruta");
        }
        if (reason == null || reason.isBlank()) {
            throw new ConflictoException("El motivo de cancelación es obligatorio");
        }

        shipment.setEstado(EstadoEnvio.CANCELADO);
        notificationService.createCancellationAlert(shipment, reason);
        return ShipmentHistoryResponseDto.from(shipment);
    }

    private void validateHistoryFilter(ShipmentHistoryFilterDto filter) {
        if (filter == null) {
            throw new ParametroInvalidoException("Los filtros del historial son obligatorios");
        }
        if (filter.page() < 0) {
            throw new ParametroInvalidoException("La página no puede ser negativa");
        }
        if (filter.size() < 1 || filter.size() > 100) {
            throw new ParametroInvalidoException("El tamaño de página debe estar entre 1 y 100");
        }
        if (filter.query() != null && filter.query().length() > 100) {
            throw new ParametroInvalidoException("La búsqueda no puede superar 100 caracteres");
        }
        if (!filter.isDateRangeValid()) {
            throw new ParametroInvalidoException("La fecha inicial no puede ser posterior a la fecha final");
        }
        if (filter.endDate() != null && filter.endDate().equals(LocalDate.MAX)) {
            throw new ParametroInvalidoException("La fecha final no es válida");
        }
    }

    private Usuario findAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AccessDeniedException("Se requiere autenticación");
        }
        return usuarioRepository.findByCorreoIgnoreCase(authentication.getName().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado"));
    }
}
